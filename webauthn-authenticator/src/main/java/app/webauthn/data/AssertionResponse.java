package app.webauthn.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.yubico.webauthn.data.AuthenticatorAssertionResponse;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.ClientAssertionExtensionOutputs;
import com.yubico.webauthn.data.PublicKeyCredential;

import lombok.Value;

@Value
@JsonIgnoreProperties({ "sessionToken" })
public class AssertionResponse
{
    ByteArray requestId;

    PublicKeyCredential<AuthenticatorAssertionResponse, ClientAssertionExtensionOutputs>
        credential;

    public AssertionResponse(
        @JsonProperty("requestId") ByteArray requestId,
        @JsonProperty("credential")
            PublicKeyCredential<AuthenticatorAssertionResponse, ClientAssertionExtensionOutputs>
            credential)
    {
        this.requestId = requestId;
        this.credential = credential;
    }
}
