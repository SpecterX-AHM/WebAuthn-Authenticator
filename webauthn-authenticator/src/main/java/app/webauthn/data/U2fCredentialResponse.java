package app.webauthn.data;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.yubico.webauthn.data.ByteArray;

import lombok.NonNull;
import lombok.Value;

@Value
public class U2fCredentialResponse
{
    ByteArray keyHandle;

    ByteArray publicKey;

    ByteArray attestationCertAndSignature;

    ByteArray clientDataJSON;

    @JsonCreator
    public U2fCredentialResponse(
        @NonNull @JsonProperty("keyHandle") ByteArray keyHandle,
        @NonNull @JsonProperty("publicKey") ByteArray publicKey,
        @NonNull @JsonProperty("attestationCertAndSignature") ByteArray attestationCertAndSignature,
        @NonNull @JsonProperty("clientDataJSON") ByteArray clientDataJSON)
    {
        this.keyHandle = keyHandle;
        this.publicKey = publicKey;
        this.attestationCertAndSignature = attestationCertAndSignature;
        this.clientDataJSON = clientDataJSON;
    }
}
