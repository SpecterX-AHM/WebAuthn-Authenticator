package app.webauthn;

import java.util.Collection;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.yubico.internal.util.CollectionUtil;
import com.yubico.webauthn.AssertionResult;
import com.yubico.webauthn.CredentialRepository;
import com.yubico.webauthn.RegisteredCredential;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.PublicKeyCredentialDescriptor;

import app.webauthn.data.RegisterCredential;

/**
 * This class implements the persistent storage of WebAuthn credentials and registration of credentials
 */
public class StoreCredentialsInMemory implements CredentialRepository
{
    private final Cache<String, Set<RegisterCredential>> storage =
        CacheBuilder.newBuilder().maximumSize(1000).expireAfterAccess(1, TimeUnit.DAYS).build();

    private static final Logger logger = LoggerFactory.getLogger(StoreCredentialsInMemory.class);

    ////////////////////////////////////////////////////////////////////////////////
    // The following methods are required by the CredentialRepository interface.
    ////////////////////////////////////////////////////////////////////////////////

    @Override
    public Set<PublicKeyCredentialDescriptor> getCredentialIdsForUsername(String username)
    {
        return getRegistrationsByUsername(username).stream()
            .map(
                registration ->
                    PublicKeyCredentialDescriptor.builder()
                        .id(registration.getCredential().getCredentialId())
                        .transports(registration.getTransports())
                        .build())
            .collect(Collectors.toSet());
    }

    @Override
    public Optional<String> getUsernameForUserHandle(ByteArray userHandle)
    {
        return getRegistrationsByUserHandle(userHandle).stream()
            .findAny()
            .map(RegisterCredential::getUsername);
    }

    @Override
    public Optional<ByteArray> getUserHandleForUsername(String username)
    {
        return getRegistrationsByUsername(username).stream()
            .findAny()
            .map(reg -> reg.getUserIdentity().getId());
    }

    @Override
    public Optional<RegisteredCredential> lookup(ByteArray credentialId, ByteArray userHandle)
    {
        Optional<RegisterCredential> registrationMaybe =
            storage.asMap().values().stream()
                .flatMap(Collection::stream)
                .filter(credReg -> credentialId.equals(credReg.getCredential().getCredentialId()))
                .findAny();

        logger.debug(
            "lookup credential ID: {}, user handle: {}; result: {}",
            credentialId,
            userHandle,
            registrationMaybe);
        return registrationMaybe.map(
            registration ->
                RegisteredCredential.builder()
                    .credentialId(registration.getCredential().getCredentialId())
                    .userHandle(registration.getUserIdentity().getId())
                    .publicKeyCose(registration.getCredential().getPublicKeyCose())
                    .signatureCount(registration.getCredential().getSignatureCount())
                    .build());
    }

    @Override
    public Set<RegisteredCredential> lookupAll(ByteArray credentialId)
    {
        return CollectionUtil.immutableSet(
            storage.asMap().values().stream()
                .flatMap(Collection::stream)
                .filter(reg -> reg.getCredential().getCredentialId().equals(credentialId))
                .map(
                    reg ->
                        RegisteredCredential.builder()
                            .credentialId(reg.getCredential().getCredentialId())
                            .userHandle(reg.getUserIdentity().getId())
                            .publicKeyCose(reg.getCredential().getPublicKeyCose())
                            .signatureCount(reg.getCredential().getSignatureCount())
                            .build())
                .collect(Collectors.toSet()));
    }

    ////////////////////////////////////////////////////////////////////////////////
    // The following methods are specific to the application.
    ////////////////////////////////////////////////////////////////////////////////

    public boolean addRegistrationByUsername(String username, RegisterCredential reg)
    {
        try {
            return storage.get(username, HashSet::new).add(reg);
        } catch (ExecutionException e) {
            logger.error("Failed to add registration", e);
            throw new RuntimeException(e);
        }
    }

    public Collection<RegisterCredential> getRegistrationsByUsername(String username)
    {
        try {
            return storage.get(username, HashSet::new);
        } catch (ExecutionException e) {
            logger.error("Registration lookup failed", e);
            throw new RuntimeException(e);
        }
    }

    public Collection<RegisterCredential> getRegistrationsByUserHandle(ByteArray userHandle)
    {
        return storage.asMap().values().stream()
            .flatMap(Collection::stream)
            .filter(
                registerCredential ->
                    userHandle.equals(registerCredential.getUserIdentity().getId()))
            .collect(Collectors.toList());
    }

    public void updateSignatureCount(AssertionResult result)
    {
        RegisterCredential registration =
            getRegistrationByUsernameAndCredentialId(result.getUsername(), result.getCredentialId())
                .orElseThrow(
                    () ->
                        new NoSuchElementException(
                            String.format(
                                "Credential \"%s\" is not registered to user \"%s\"",
                                result.getCredentialId(), result.getUsername())));

        Set<RegisterCredential> regs = storage.getIfPresent(result.getUsername());
        regs.remove(registration);
        regs.add(
            registration.withCredential(
                registration.getCredential().toBuilder()
                    .signatureCount(result.getSignatureCount())
                    .build()));
    }

    public Optional<RegisterCredential> getRegistrationByUsernameAndCredentialId(
        String username, ByteArray id)
    {
        try {
            return storage.get(username, HashSet::new).stream()
                .filter(credReg -> id.equals(credReg.getCredential().getCredentialId()))
                .findFirst();
        } catch (ExecutionException e) {
            logger.error("Registration lookup failed", e);
            throw new RuntimeException(e);
        }
    }

    public boolean removeRegistrationByUsername(
        String username, RegisterCredential registerCredential)
    {
        try {
            return storage.get(username, HashSet::new).remove(registerCredential);
        } catch (ExecutionException e) {
            logger.error("Failed to remove registration", e);
            throw new RuntimeException(e);
        }
    }

    public boolean removeAllRegistrations(String username)
    {
        storage.invalidate(username);
        return true;
    }

    public boolean userExists(String username)
    {
        return !getRegistrationsByUsername(username).isEmpty();
    }
}
