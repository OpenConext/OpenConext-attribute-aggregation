package aa.lifecycle;

import aa.AbstractIntegrationTest;
import org.junit.Test;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static java.util.stream.Collectors.toMap;
import static org.junit.Assert.assertEquals;

public class UserLifeCycleControllerTest extends AbstractIntegrationTest {

    private final String personUrn = "saml2_user.com";

    @Test
    public void preview() {
        LifeCycleResult result = given()
                .auth()
                .preemptive()
                .basic("life", "secret")
                .when()
                .get("aa/api/deprovision/{user}", personUrn)
                .as(LifeCycleResult.class);
        assertLifeCycleResult(result);
    }

    @Test
    public void previewUnauthenticated() {
        given()
                .auth()
                .preemptive()
                .basic("life", "nope")
                .when()
                .get("aa/api/deprovision/{user}", personUrn)
                .then()
                .statusCode(403);
    }

    @Test
    public void unsupportedContentNegotion() {
        LifeCycleResult result = given()
                .auth()
                .preemptive()
                .basic("life", "secret")
                .when()
                .delete("aa/api/deprovision/nope.me")
                .as(LifeCycleResult.class);
        assertEquals(0, result.getData().size());
    }

    @Test
    public void dryRun() {
        LifeCycleResult result = doDeprovision(true);
        assertLifeCycleResult(result);
    }

    @Test
    public void deprovision() {
        LifeCycleResult result = doDeprovision(false);
        assertLifeCycleResult(result);

        result = doDeprovision(false);
        assertEquals(0, result.getData().size());
    }

    private LifeCycleResult doDeprovision(boolean dryRun) {
        return given()
                .auth()
                .preemptive()
                .basic("life", "secret")
                .when()
                .delete("aa/api/deprovision/{user}" + (dryRun ? "/dry-run" : ""), this.personUrn)
                .as(LifeCycleResult.class);
    }

    private void assertLifeCycleResult(LifeCycleResult result) {
        Map<String, String> map = result.getData().stream().collect(toMap(attr -> attr.getName(), attr -> attr.getValue()));
        assertEquals(2, map.size());
        assertEquals(map.get("ORCID"), "http://orcid.org/0000-0002-4926-2859");
        assertEquals(map.get("urn"), personUrn);
    }
}