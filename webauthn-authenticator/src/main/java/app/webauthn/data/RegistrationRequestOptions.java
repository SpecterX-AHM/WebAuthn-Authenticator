package app.webauthn.data;

import java.util.Optional;

import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.PublicKeyCredentialCreationOptions;

import lombok.EqualsAndHashCode;
import lombok.Value;

@Value
@EqualsAndHashCode(callSuper = false)
public class RegistrationRequestOptions
{
    String username;

    Optional<String> credentialNickname;

    ByteArray requestId;

    PublicKeyCredentialCreationOptions publicKeyCredentialCreationOptions;

    Optional<ByteArray> sessionToken;
}
