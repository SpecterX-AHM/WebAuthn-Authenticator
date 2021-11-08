package app;

import java.security.cert.CertificateException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.core.Application;

import com.yubico.webauthn.extension.appid.InvalidAppIdException;

import app.webauthn.WebAuthnManager;

public class App extends Application
{
    @Override
    public Set<Class<?>> getClasses()
    {
        return new HashSet<>(List.of(CorsFilter.class));
    }

    @Override
    public Set<Object> getSingletons()
    {
        try {
            return new HashSet<>(List.of(new WebAuthnManager()));
        } catch (InvalidAppIdException | CertificateException e) {
            throw new RuntimeException(e);
        }
    }
}
