package aa.web;

import org.apache.hc.core5.http.HttpHost;
import org.junit.After;
import org.junit.Test;

import java.net.URI;
import java.net.URL;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class HttpHostProviderTest {

    @After
    public void after() {
        System.clearProperty("http.proxyHost");
        System.clearProperty("http.proxyPort");
        System.clearProperty("http.nonProxyHosts");
    }

    @Test
    public void noProxyConfigured() throws Exception {
        assertFalse(HttpHostProvider.resolveHttpHost(url("https://example.com")).isPresent());
    }

    @Test
    public void proxyConfiguredWithDefaultPort() throws Exception {
        System.setProperty("http.proxyHost", "proxy.example.com");

        Optional<HttpHost> httpHost = HttpHostProvider.resolveHttpHost(url("https://example.com"));

        assertEquals("proxy.example.com", httpHost.get().getHostName());
        assertEquals(80, httpHost.get().getPort());
    }

    @Test
    public void proxyConfiguredWithExplicitPort() throws Exception {
        System.setProperty("http.proxyHost", "proxy.example.com");
        System.setProperty("http.proxyPort", "8080");

        Optional<HttpHost> httpHost = HttpHostProvider.resolveHttpHost(url("https://example.com"));

        assertEquals("proxy.example.com", httpHost.get().getHostName());
        assertEquals(8080, httpHost.get().getPort());
    }

    @Test
    public void proxyConfiguredButHostIsNonProxied() throws Exception {
        System.setProperty("http.proxyHost", "proxy.example.com");
        System.setProperty("http.nonProxyHosts", "localhost|*.internal.example.com");

        assertFalse(HttpHostProvider.resolveHttpHost(url("https://foo.internal.example.com")).isPresent());
    }

    @Test
    public void proxyConfiguredAndHostDoesNotMatchNonProxyHosts() throws Exception {
        System.setProperty("http.proxyHost", "proxy.example.com");
        System.setProperty("http.nonProxyHosts", "localhost|*.internal.example.com");

        assertEquals("proxy.example.com",
                HttpHostProvider.resolveHttpHost(url("https://example.com")).get().getHostName());
    }

    private URL url(String uri) throws Exception {
        return URI.create(uri).toURL();
    }
}
