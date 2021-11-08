package app.webauthn.data;

import java.time.Instant;
import java.util.Optional;
import java.util.SortedSet;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.yubico.webauthn.RegisteredCredential;
import com.yubico.webauthn.attestation.Attestation;
import com.yubico.webauthn.data.AuthenticatorTransport;
import com.yubico.webauthn.data.UserIdentity;

import lombok.Builder;
import lombok.Value;
import lombok.With;

@With @Value
@Builder
public class RegisterCredential
{
    UserIdentity userIdentity;

    Optional<String> credentialNickname;

    SortedSet<AuthenticatorTransport> transports;

    @JsonIgnore Instant registrationTime;

    RegisteredCredential credential;

    Optional<Attestation> attestationMetadata;

    public String getUsername()
    {
        return userIdentity.getName();
    }
}
