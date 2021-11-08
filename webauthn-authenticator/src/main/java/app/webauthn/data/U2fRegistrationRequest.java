package app.webauthn.data;

import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.yubico.webauthn.data.ByteArray;

import lombok.NonNull;
import lombok.Value;

@Value
public class U2fRegistrationRequest
{
    ByteArray requestId;

    U2fCredential credential;

    Optional<ByteArray> sessionToken;

    @JsonCreator
    public U2fRegistrationRequest(
        @NonNull @JsonProperty("requestId") ByteArray requestId,
        @NonNull @JsonProperty("credential") U2fCredential credential,
        @NonNull @JsonProperty("sessionToken") Optional<ByteArray> sessionToken)
    {
        this.requestId = requestId;
        this.credential = credential;
        this.sessionToken = sessionToken;
    }
}
