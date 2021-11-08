package app.webauthn;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.cert.CertificateException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.yubico.internal.util.JacksonCodecs;
import com.yubico.util.Either;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.exception.Base64UrlException;
import com.yubico.webauthn.extension.appid.InvalidAppIdException;
import com.yubico.webauthn.meta.VersionInfo;

import app.webauthn.data.AssertionRequestOptions;
import app.webauthn.data.RegistrationRequestOptions;
import lombok.NonNull;

/**
 * This class serves as a REST Resource for WebAuthn based request URLs
 */
@Path("/v1")
@Produces(MediaType.APPLICATION_JSON)
public class WebAuthnManager
{
    private static final Logger logger = LoggerFactory.getLogger(WebAuthnManager.class);

    private final WebAuthnServerManager server;

    private final ObjectMapper jsonMapper = JacksonCodecs.json();

    private final JsonNodeFactory jsonFactory = JsonNodeFactory.instance;

    @Context private UriInfo uriInfo;

    public WebAuthnManager() throws InvalidAppIdException, CertificateException
    {
        this(new WebAuthnServerManager());
    }

    public WebAuthnManager(WebAuthnServerManager server)
    {
        this.server = server;
    }

    @GET
    public Response index() throws IOException
    {
        return Response.ok(writeJson(new IndexResponse())).build();
    }

    @GET
    @Path("version")
    public Response version() throws JsonProcessingException
    {
        return Response.ok(writeJson(new VersionResponse())).build();
    }

    @Consumes("application/x-www-form-urlencoded")
    @Path("register")
    @POST
    public Response startRegistration(
        @NonNull @FormParam("username") String username,
        @NonNull @FormParam("displayName") String displayName,
        @FormParam("credentialNickname") String credentialNickname,
        @FormParam("requireResidentKey") @DefaultValue("false") boolean requireResidentKey,
        @FormParam("sessionToken") String sessionTokenBase64)
        throws MalformedURLException, ExecutionException
    {
        logger.trace(
            "startRegistration username: {}, displayName: {}, credentialNickname: {}, requireResidentKey: {}",
            username,
            displayName,
            credentialNickname,
            requireResidentKey);
        Either<String, RegistrationRequestOptions> result =
            server.startRegistration(
                username,
                displayName,
                Optional.ofNullable(credentialNickname),
                requireResidentKey,
                Optional.ofNullable(sessionTokenBase64)
                    .map(
                        base64 -> {
                            try {
                                return ByteArray.fromBase64Url(base64);
                            } catch (Base64UrlException e) {
                                throw new RuntimeException(e);
                            }
                        }));

        if (result.isRight()) {
            return startResponse(
                "startRegistration", new StartRegistrationResponse(result.right().get()));
        } else {
            return messagesJson(Response.status(Status.BAD_REQUEST), result.left().get());
        }
    }

    @Path("register/finish")
    @POST
    public Response finishRegistration(@NonNull String responseJson)
    {
        logger.trace("finishRegistration responseJson: {}", responseJson);
        Either<List<String>, WebAuthnServerManager.SuccessfulRegistrationResult> result =
            server.finishRegistration(responseJson);
        return finishResponse(
            result,
            "Attestation verification failed; further error message(s) were unfortunately lost to an internal server error.",
            "finishRegistration",
            responseJson);
    }

    @Path("register/finish-u2f")
    @POST
    public Response finishU2fRegistration(@NonNull String responseJson) throws ExecutionException
    {
        logger.trace("finishRegistration responseJson: {}", responseJson);
        Either<List<String>, WebAuthnServerManager.SuccessfulU2fRegistrationResult> result =
            server.finishU2fRegistration(responseJson);
        return finishResponse(
            result,
            "U2F registration failed; further error message(s) were unfortunately lost to an internal server error.",
            "finishU2fRegistration",
            responseJson);
    }

    @Consumes("application/x-www-form-urlencoded")
    @Path("authenticate")
    @POST
    public Response startAuthentication(@FormParam("username") String username)
        throws MalformedURLException
    {
        logger.trace("startAuthentication username: {}", username);
        Either<List<String>, AssertionRequestOptions> request =
            server.startAuthentication(Optional.ofNullable(username));
        if (request.isRight()) {
            return startResponse(
                "startAuthentication", new StartAuthenticationResponse(request.right().get()));
        } else {
            return messagesJson(Response.status(Status.BAD_REQUEST), request.left().get());
        }
    }

    @Path("authenticate/finish")
    @POST
    public Response finishAuthentication(@NonNull String responseJson)
    {
        logger.trace("finishAuthentication responseJson: {}", responseJson);

        Either<List<String>, WebAuthnServerManager.SuccessfulAuthenticationResult> result =
            server.finishAuthentication(responseJson);

        return finishResponse(
            result,
            "Authentication verification failed; further error message(s) were unfortunately lost to an internal server error.",
            "finishAuthentication",
            responseJson);
    }

    @Path("action/deregister")
    @POST
    public Response deregisterCredential(
        @NonNull @FormParam("sessionToken") String sessionTokenBase64,
        @NonNull @FormParam("credentialId") String credentialIdBase64)
        throws MalformedURLException, Base64UrlException
    {
        logger.trace(
            "deregisterCredential sesion: {}, credentialId: {}",
            sessionTokenBase64,
            credentialIdBase64);

        final ByteArray credentialId;
        try {
            credentialId = ByteArray.fromBase64Url(credentialIdBase64);
        } catch (Base64UrlException e) {
            return messagesJson(
                Response.status(Status.BAD_REQUEST),
                "Credential ID is not valid Base64Url data: " + credentialIdBase64);
        }

        Either<List<String>, WebAuthnServerManager.DeregisterCredentialResult> result =
            server.deregisterCredential(ByteArray.fromBase64Url(sessionTokenBase64), credentialId);

        if (result.isRight()) {
            return finishResponse(
                result,
                "Failed to deregister credential; further error message(s) were unfortunately lost to an internal server error.",
                "deregisterCredential",
                "");
        } else {
            return messagesJson(Response.status(Status.BAD_REQUEST), result.left().get());
        }
    }

    @Path("delete-account")
    @DELETE
    public Response deleteAccount(@NonNull @FormParam("username") String username)
    {
        logger.trace("deleteAccount username: {}", username);

        Either<List<String>, JsonNode> result =
            server.deleteAccount(
                username,
                () ->
                    ((ObjectNode)
                        jsonFactory.objectNode().set("success", jsonFactory.booleanNode(true)))
                        .set("deletedAccount", jsonFactory.textNode(username)));

        if (result.isRight()) {
            return Response.ok(result.right().get().toString()).build();
        } else {
            return messagesJson(Response.status(Status.BAD_REQUEST), result.left().get());
        }
    }

    private Response startResponse(String operationName, Object request)
    {
        try {
            String json = writeJson(request);
            logger.debug("{} JSON response: {}", operationName, json);
            return Response.ok(json).build();
        } catch (IOException e) {
            logger.error("Failed to encode response as JSON: {}", request, e);
            return jsonFail();
        }
    }

    private Response finishResponse(
        Either<List<String>, ?> result,
        String jsonFailMessage,
        String methodName,
        String responseJson)
    {
        if (result.isRight()) {
            try {
                return Response.ok(writeJson(result.right().get())).build();
            } catch (JsonProcessingException e) {
                logger.error("Failed to encode response as JSON: {}", result.right().get(), e);
                return messagesJson(Response.ok(), jsonFailMessage);
            }
        } else {
            logger.debug("fail {} responseJson: {}", methodName, responseJson);
            return messagesJson(Response.status(Status.BAD_REQUEST), result.left().get());
        }
    }

    private Response jsonFail()
    {
        return Response.status(Status.INTERNAL_SERVER_ERROR)
            .entity("{\"messages\":[\"Failed to encode response as JSON\"]}")
            .build();
    }

    private Response messagesJson(ResponseBuilder response, String message)
    {
        return messagesJson(response, List.of(message));
    }

    private Response messagesJson(ResponseBuilder response, List<String> messages)
    {
        logger.debug("Encoding messages as JSON: {}", messages);
        try {
            return response
                .entity(
                    writeJson(
                        jsonFactory
                            .objectNode()
                            .set(
                                "messages",
                                jsonFactory
                                    .arrayNode()
                                    .addAll(
                                        messages.stream()
                                            .map(jsonFactory::textNode)
                                            .collect(Collectors.toList())))))
                .build();
        } catch (JsonProcessingException e) {
            logger.error("Failed to encode messages as JSON: {}", messages, e);
            return jsonFail();
        }
    }

    private String writeJson(Object o) throws JsonProcessingException
    {
        if (uriInfo.getQueryParameters().containsKey("pretty")) {
            return jsonMapper.writerWithDefaultPrettyPrinter().writeValueAsString(o);
        } else {
            return jsonMapper.writeValueAsString(o);
        }
    }

    private static final class VersionResponse
    {
        public final VersionInfo version = VersionInfo.getInstance();
    }

    private final class IndexResponse
    {
        public final Index actions = new Index();

        public final Info info = new Info();

        private IndexResponse() throws MalformedURLException
        {
        }
    }

    private final class Index
    {
        public final URL register;

        public final URL authenticate;

        public final URL deleteAccount;

        public final URL deregister;

        public Index() throws MalformedURLException
        {
            register = uriInfo.getAbsolutePathBuilder().path("register").build().toURL();
            authenticate = uriInfo.getAbsolutePathBuilder().path("authenticate").build().toURL();
            deleteAccount = uriInfo.getAbsolutePathBuilder().path("delete-account").build().toURL();
            deregister =
                uriInfo.getAbsolutePathBuilder().path("action").path("deregister").build().toURL();
        }
    }

    private final class Info
    {
        public final URL version;

        public Info() throws MalformedURLException
        {
            version = uriInfo.getAbsolutePathBuilder().path("version").build().toURL();
        }
    }

    private final class StartRegistrationResponse
    {
        public final boolean success = true;

        public final RegistrationRequestOptions request;

        public final StartRegistrationActions actions = new StartRegistrationActions();

        private StartRegistrationResponse(RegistrationRequestOptions request) throws MalformedURLException
        {
            this.request = request;
        }
    }

    private final class StartRegistrationActions
    {
        public final URL finish = uriInfo.getAbsolutePathBuilder().path("finish").build().toURL();

        public final URL finishU2f =
            uriInfo.getAbsolutePathBuilder().path("finish-u2f").build().toURL();

        private StartRegistrationActions() throws MalformedURLException
        {
        }
    }

    private final class StartAuthenticationResponse
    {
        public final boolean success = true;

        public final AssertionRequestOptions request;

        public final StartAuthenticationActions actions = new StartAuthenticationActions();

        private StartAuthenticationResponse(AssertionRequestOptions request)
            throws MalformedURLException
        {
            this.request = request;
        }
    }

    private final class StartAuthenticationActions
    {
        public final URL finish = uriInfo.getAbsolutePathBuilder().path("finish").build().toURL();

        private StartAuthenticationActions() throws MalformedURLException
        {
        }
    }
}
