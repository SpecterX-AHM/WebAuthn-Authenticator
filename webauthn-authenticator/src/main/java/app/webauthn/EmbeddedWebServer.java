package app.webauthn;

import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;

import app.App;

/**
 * Standalone Java application launcher that runs the application with the API but no static resources (i.e., no web
 * GUI)
 */
public class EmbeddedWebServer
{
    public static void main(String[] args) throws Exception
    {
        final int port = WebAuthnConfiguration.getPort();

        App app = new App();

        ResourceConfig config = new ResourceConfig();
        config.registerClasses(app.getClasses());
        config.registerInstances(app.getSingletons());

        SslContextFactory ssl = new SslContextFactory("my-release-key.keystore");
        ssl.setKeyStorePassword("humayun");

        Server server = new Server();
        HttpConfiguration httpConfig = new HttpConfiguration();
        httpConfig.setSecureScheme("https");
        httpConfig.setSecurePort(port);
        HttpConfiguration httpsConfig = new HttpConfiguration(httpConfig);
        httpsConfig.addCustomizer(new SecureRequestCustomizer());

        ServerConnector connector =
            new ServerConnector(
                server,
                new SslConnectionFactory(ssl, HttpVersion.HTTP_1_1.asString()),
                new HttpConnectionFactory(httpsConfig));

        connector.setPort(port);
        connector.setHost("127.0.0.1");

        ServletHolder servlet = new ServletHolder(new ServletContainer(config));
        ServletContextHandler context = new ServletContextHandler(server, "/");
        context.addServlet(DefaultServlet.class, "/");
        context.setResourceBase("src/main/webapp");
        context.addServlet(servlet, "/webauthn/*");

        server.setConnectors(new Connector[] { connector });
        server.start();
    }
}
