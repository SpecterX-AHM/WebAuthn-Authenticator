package app.webauthn.data;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.yubico.webauthn.attestation.Attestation;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.PublicKeyCredentialDescriptor;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class U2fRegistrationResponse
{
    @NonNull PublicKeyCredentialDescriptor keyId;

    boolean attestationTrusted;

    @NonNull ByteArray publicKeyCose;

    @NonNull @Builder.Default List<String> warnings = Collections.emptyList();

    @NonNull @Builder.Default
    Optional<Attestation> attestationMetadata = Optional.empty();
}
