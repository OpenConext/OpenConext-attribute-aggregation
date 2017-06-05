package aa;


import aa.aggregators.PrePopulatedJsonHttpHeaders;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.http.HttpHeaders.ACCEPT;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.context.jdbc.SqlConfig.ErrorMode.FAIL_ON_ERROR;
import static org.springframework.test.context.jdbc.SqlConfig.TransactionMode.ISOLATED;

/**
 * Override the @WebIntegrationTest annotation if you don't want to have mock shibboleth headers (e.g. you want to
 * impersonate EB or other identity).
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = "attribute.authorities.config.path=classpath:testAttributeAuthorities.yml")
@ActiveProfiles("dev,aa-test")
//@Transactional
//@Sql(scripts = {"classpath:sql/clear.sql", "classpath:sql/seed.sql"},
//    config = @SqlConfig(errorMode = FAIL_ON_ERROR, transactionMode = ISOLATED))
public abstract class AbstractIntegrationTest {

    protected static final String spEntityID = "http://mock-sp";

    @Value("${local.server.port}")
    protected int port;

    protected TestRestTemplate restTemplate;

    protected HttpHeaders headers = new PrePopulatedJsonHttpHeaders();

    @Before
    public void before() throws Exception {
        restTemplate = isBasicAuthenticated() ? new TestRestTemplate("eb", "secret") : new TestRestTemplate();
    }

    protected boolean isBasicAuthenticated() {
        return false;
    }

}
