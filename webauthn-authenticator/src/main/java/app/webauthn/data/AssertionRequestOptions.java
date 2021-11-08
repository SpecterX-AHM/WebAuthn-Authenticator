package app.webauthn.data;

import java.util.Optional;

import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.PublicKeyCredentialRequestOptions;

import lombok.NonNull;
import lombok.Value;

@Value
public class AssertionRequestOptions
{
    @NonNull ByteArray requestId;

    @NonNull PublicKeyCredentialRequestOptions publicKeyCredentialRequestOptions;

    @NonNull Optional<String> username;

    @NonNull transient com.yubico.webauthn.AssertionRequest request;

    public AssertionRequestOptions(
        @NonNull ByteArray requestId, @NonNull com.yubico.webauthn.AssertionRequest request)
    {
        this.requestId = requestId;
        this.publicKeyCredentialRequestOptions = request.getPublicKeyCredentialRequestOptions();
        this.username = request.getUsername();
        this.request = request;
    }
}
