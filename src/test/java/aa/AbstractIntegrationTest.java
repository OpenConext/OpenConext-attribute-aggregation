package aa;


import aa.aggregators.PrePopulatedJsonHttpHeaders;
import io.restassured.RestAssured;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.context.jdbc.SqlConfig.ErrorMode.FAIL_ON_ERROR;
import static org.springframework.test.context.jdbc.SqlConfig.TransactionMode.ISOLATED;

/**
 * Override the @WebIntegrationTest annotation if you don't want to have mock shibboleth headers (e.g. you want to
 * impersonate EB or other identity).
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "attribute_authorities_config_path=classpath:testAttributeAuthorities.yml")
@ActiveProfiles({"test"})
@Transactional
@Sql(scripts = {"classpath:sql/clear.sql", "classpath:sql/seed.sql"},
        config = @SqlConfig(errorMode = FAIL_ON_ERROR, transactionMode = ISOLATED))
public abstract class AbstractIntegrationTest {

    @LocalServerPort
    protected int port;

    protected TestRestTemplate restTemplate;

    protected HttpHeaders headers = new PrePopulatedJsonHttpHeaders();

    @Before
    public void before() throws Exception {
        restTemplate = isBasicAuthenticated() ? new TestRestTemplate("eb", "secret") : new TestRestTemplate();
        RestAssured.port = port;
    }

    protected boolean isBasicAuthenticated() {
        return false;
    }

}
