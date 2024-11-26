package aa.aggregators.rest;

import aa.model.*;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static aa.aggregators.AttributeAggregator.*;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class InviteAttributeAggregatorTest {

    private RestAttributeAggregator subject;

    private final List<UserAttribute> input = List.of(
            new UserAttribute(NAME_ID, List.of("urn")),
            new UserAttribute(SP_ENTITY_ID, List.of("http://localhost.sp"))
    );

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(8889);

    @Before
    public void before() {
        AttributeAuthorityConfiguration configuration = new AttributeAuthorityConfiguration("access");
        configuration.setType(AggregatorType.rest);
        configuration.setUser("user");
        configuration.setPassword("password");
        configuration.setEndpoint("http://localhost:8889/api/aa/%s");
        configuration.setRequiredInputAttributes(List.of(new RequiredInputAttribute(NAME_ID), new RequiredInputAttribute(SP_ENTITY_ID)));
        configuration.setPathParams(Arrays.asList(new PathParam(1, NAME_ID)));
        configuration.setRequestParams(List.of(new RequestParam("SPentityID","SPentityID")));
        configuration.setMappings(List.of(new Mapping("id", IS_MEMBER_OF, null)));
        subject = new RestAttributeAggregator(configuration);
    }

    @Test
    public void testGetRoles() throws Exception {
        String response = read("access/roles.json");
        stubForAccess(response, 200);
        List<UserAttribute> userAttributes = subject.aggregate(input, Collections.emptyMap());
        assertEquals(1, userAttributes.size());
        UserAttribute userAttribute = userAttributes.get(0);
        assertEquals(IS_MEMBER_OF, userAttribute.getName());

        List<String> values = userAttribute.getValues();
        assertEquals(2, values.size());
        assertEquals(List.of("aa", "bb"), values.stream().map(s -> s.substring(s.lastIndexOf(":") + 1)).toList());
        values.forEach(value -> assertTrue(value.startsWith("urn:mace:surf.nl:test.surfaccess.nl")));
    }

    @Test
    public void testGetRolesEmpty() throws Exception {
        String response = read("access/no_roles.json");
        stubForAccess(response, 200);
        List<UserAttribute> userAttributes = subject.aggregate(input, Collections.emptyMap());
        assertEquals(0, userAttributes.size());
    }

    private void stubForAccess(String response, int status) {
        stubFor(get(urlEqualTo("/api/aa/urn?SPentityID=http://localhost.sp"))
            .willReturn(aResponse()
                    .withStatus(status)
                    .withBody(response)
                    .withHeader("Content-Type", "application/json"))
        );
    }

    private String read(String path) throws IOException {
        return IOUtils.toString(new ClassPathResource(path).getInputStream(), Charset.defaultCharset());
    }

}