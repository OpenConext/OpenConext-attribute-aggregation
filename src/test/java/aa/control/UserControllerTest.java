package aa.control;

import aa.AbstractIntegrationTest;
import aa.shibboleth.mock.MockShibbolethFilter;
import org.junit.Test;

import java.util.Collections;

import static io.restassured.RestAssured.given;
import static org.apache.http.HttpStatus.SC_OK;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.equalTo;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;

public class UserControllerTest extends AbstractIntegrationTest {

    @Test
    public void currentUser() {
        given()
                .when()
                .get("aa/api/client/users/me")
                .then()
                .statusCode(SC_OK)
                .body("username", equalTo(MockShibbolethFilter.SAML2_USER));
    }

    @Test
    public void logout() {
        given()
                .when()
                .delete("aa/api/client/users/logout")
                .then()
                .body(is(emptyString()))
                .statusCode(SC_OK);
    }

    @Test
    public void error() {
        given()
                .header(CONTENT_TYPE, "application/json")
                .when()
                .body(Collections.singletonMap("error", "message"))
                .post("aa/api/client/error")
                .then()
                .statusCode(SC_OK);
    }
}