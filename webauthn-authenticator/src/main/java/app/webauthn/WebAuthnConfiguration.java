package app.webauthn;

import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yubico.internal.util.CollectionUtil;
import com.yubico.webauthn.data.RelyingPartyIdentity;
import com.yubico.webauthn.extension.appid.AppId;
import com.yubico.webauthn.extension.appid.InvalidAppIdException;

/**
 * Various WebAuthn server-based configurations
 */
public class WebAuthnConfiguration
{
    private static final Logger logger = LoggerFactory.getLogger(WebAuthnConfiguration.class);

    private static final String DEFAULT_ORIGIN = "https://localhost:8888";

    private static final int DEFAULT_PORT = 8888;

    private static final RelyingPartyIdentity DEFAULT_RP_ID =
        RelyingPartyIdentity.builder().id("localhost").name("WebAuthn test").build();

    private final Set<String> origins;

    private final int port;

    private final RelyingPartyIdentity rpIdentity;

    private final Optional<AppId> appId;

    private WebAuthnConfiguration(
        Set<String> origins, int port, RelyingPartyIdentity rpIdentity, Optional<AppId> appId)
    {
        this.origins = CollectionUtil.immutableSet(origins);
        this.port = port;
        this.rpIdentity = rpIdentity;
        this.appId = appId;
    }

    private static WebAuthnConfiguration instance;

    private static WebAuthnConfiguration getInstance()
    {
        if (instance == null) {
            try {
                instance = new WebAuthnConfiguration(computeOrigins(), computePort(), computeRpIdentity(), computeAppId());
            } catch (MalformedURLException | InvalidAppIdException e) {
                throw new RuntimeException(e);
            }
        }
        return instance;
    }

    public static Set<String> getOrigins()
    {
        return getInstance().origins;
    }

    public static int getPort()
    {
        return getInstance().port;
    }

    public static RelyingPartyIdentity getRpIdentity()
    {
        return getInstance().rpIdentity;
    }

    public static Optional<AppId> getAppId()
    {
        return getInstance().appId;
    }

    private static Set<String> computeOrigins()
    {
        final String origins = System.getenv("YUBICO_WEBAUTHN_ALLOWED_ORIGINS");

        logger.debug("YUBICO_WEBAUTHN_ALLOWED_ORIGINS: {}", origins);

        final Set<String> result;

        if (origins == null) {
            result = Collections.singleton(DEFAULT_ORIGIN);
        } else {
            result = new HashSet<>(Arrays.asList(origins.split(",")));
        }

        logger.info("Origins: {}", result);

        return result;
    }

    private static int computePort()
    {
        final String port = System.getenv("YUBICO_WEBAUTHN_PORT");

        if (port == null) {
            return DEFAULT_PORT;
        } else {
            return Integer.parseInt(port);
        }
    }

    private static RelyingPartyIdentity computeRpIdentity() throws MalformedURLException
    {
        final String name = System.getenv("YUBICO_WEBAUTHN_RP_NAME");
        final String id = System.getenv("YUBICO_WEBAUTHN_RP_ID");

        logger.debug("RP name: {}", name);
        logger.debug("RP ID: {}", id);

        RelyingPartyIdentity.RelyingPartyIdentityBuilder resultBuilder = DEFAULT_RP_ID.toBuilder();

        if (name == null) {
            logger.debug("RP name not given - using default.");
        } else {
            resultBuilder.name(name);
        }

        if (id == null) {
            logger.debug("RP ID not given - using default.");
        } else {
            resultBuilder.id(id);
        }

        final RelyingPartyIdentity result = resultBuilder.build();
        logger.info("RP identity: {}", result);
        return result;
    }

    private static Optional<AppId> computeAppId() throws InvalidAppIdException
    {
        final String appId = System.getenv("YUBICO_WEBAUTHN_U2F_APPID");
        logger.debug("YUBICO_WEBAUTHN_U2F_APPID: {}", appId);

        AppId result = appId == null ? new AppId("https://localhost:8888") : new AppId(appId);

        logger.debug("U2F AppId: {}", result.getId());
        return Optional.of(result);
    }
}
