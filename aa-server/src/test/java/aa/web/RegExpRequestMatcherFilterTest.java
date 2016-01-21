package aa.web;

import org.junit.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RegExpRequestMatcherFilterTest {

  @Test
  public void testDoFilter() throws Exception {
    doTestFilter("/internal/doSomething", true);
    doTestFilter("/INTERNAL/doSomething/else", true);
    doTestFilter("/css/some.css", true);
    doTestFilter("/images/some.png", true);

    doTestFilter("/attribute/aggregate", false);
    doTestFilter("/health", false);
    doTestFilter("/v2/ServiceProviderConfig", false);
    doTestFilter("/v2/ResourceType", false);
    doTestFilter("/v2/Me", false);
    doTestFilter("/v2/Schema", false);
  }

  private void doTestFilter(String path, boolean doFilter) throws IOException, ServletException {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setServletPath(path);

    HttpServletResponse response = new MockHttpServletResponse();
    FilterChain chain = new MockFilterChain() {
      @Override
      public void doFilter(ServletRequest request, ServletResponse response) throws IOException, ServletException {
        assertFalse(doFilter);
      }
    };

    RegExpRequestMatcherFilter subject = new RegExpRequestMatcherFilter(new TestFilter(doFilter), WebSecurityConfigurer.NON_SHIBBOLETH_PROTECTED_METHODS);
    subject.doFilter(request, response, chain);
  }

  private static class TestFilter extends GenericFilterBean {
    private boolean doFilter;

    public TestFilter(boolean doFilter) {
      this.doFilter = doFilter;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
      assertTrue(doFilter);
    }
  }

}