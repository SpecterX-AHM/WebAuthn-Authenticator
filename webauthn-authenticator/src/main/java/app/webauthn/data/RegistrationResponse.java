package app.webauthn.data;

import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.yubico.webauthn.data.AuthenticatorAttestationResponse;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.ClientRegistrationExtensionOutputs;
import com.yubico.webauthn.data.PublicKeyCredential;

import lombok.Value;

@Value
public class RegistrationResponse
{
    ByteArray requestId;

    PublicKeyCredential<
        AuthenticatorAttestationResponse, ClientRegistrationExtensionOutputs>
        credential;

    Optional<ByteArray> sessionToken;

    @JsonCreator
    public RegistrationResponse(
        @JsonProperty("requestId") ByteArray requestId,
        @JsonProperty("credential")
            PublicKeyCredential<AuthenticatorAttestationResponse, ClientRegistrationExtensionOutputs>
            credential,
        @JsonProperty("sessionToken") Optional<ByteArray> sessionToken)
    {
        this.requestId = requestId;
        this.credential = credential;
        this.sessionToken = sessionToken;
    }
}
