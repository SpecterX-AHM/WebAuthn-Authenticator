package com.yubico.webauthn;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import com.fasterxml.jackson.databind.JsonNode;
import com.yubico.internal.util.CertificateParser;
import com.yubico.internal.util.ExceptionUtil;
import com.yubico.internal.util.JacksonCodecs;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.exception.Base64UrlException;
import com.yubico.webauthn.extension.appid.AppId;

import app.webauthn.data.RegistrationRequestOptions;
import app.webauthn.data.U2fRegistrationRequest;

public class U2fVerifier
{
    public static boolean verify(
        AppId appId, RegistrationRequestOptions regRequest, U2fRegistrationRequest u2fRequest)
        throws CertificateException, IOException, Base64UrlException
    {
        final ByteArray appIdHash = Crypto.sha256(appId.getId());
        final ByteArray clientDataHash =
            Crypto.sha256(u2fRequest.getCredential().getU2fResponse().getClientDataJSON());

        final JsonNode clientData =
            JacksonCodecs.json()
                .readTree(u2fRequest.getCredential().getU2fResponse().getClientDataJSON().getBytes());
        final String challengeBase64 = clientData.get("challenge").textValue();

        ExceptionUtil.assure(
            regRequest
                .getPublicKeyCredentialCreationOptions()
                .getChallenge()
                .equals(ByteArray.fromBase64Url(challengeBase64)),
            "Wrong challenge.");

        InputStream attestationCertAndSignatureStream =
            new ByteArrayInputStream(
                u2fRequest.getCredential().getU2fResponse().getAttestationCertAndSignature().getBytes());

        final X509Certificate attestationCert =
            CertificateParser.parseDer(attestationCertAndSignatureStream);

        byte[] signatureBytes = new byte[attestationCertAndSignatureStream.available()];
        attestationCertAndSignatureStream.read(signatureBytes);
        final ByteArray signature = new ByteArray(signatureBytes);

        return new U2fRawRegisterResponse(
            u2fRequest.getCredential().getU2fResponse().getPublicKey(),
            u2fRequest.getCredential().getU2fResponse().getKeyHandle(),
            attestationCert,
            signature)
            .verifySignature(appIdHash, clientDataHash);
    }
}
