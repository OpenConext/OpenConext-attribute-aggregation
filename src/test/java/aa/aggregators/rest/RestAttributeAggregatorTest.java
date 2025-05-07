package aa.aggregators.rest;

import aa.model.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.*;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@SuppressWarnings("unchecked")
public class RestAttributeAggregatorTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private RestAttributeAggregator subject;

    private AttributeAuthorityConfiguration configuration;

    @Mock
    private RestTemplate restTemplate;

    @BeforeEach
    void setUp() {
        configuration = new AttributeAuthorityConfiguration("domain1");
        configuration.setEndpoint("https://domain1.com");
        configuration.setTimeOut(15000);
        configuration.setMappings(List.of(new Mapping("dummy", "dummy", new MappingFilter())));
        configuration.setRequestMethod("GET");
        subject = new RestAttributeAggregator(configuration, objectMapper);
        ReflectionTestUtils.setField(subject, "restTemplate", restTemplate);
    }

    @Test
    void aggregateEmptyMappings() {
        configuration.setMappings(Collections.emptyList());
        List<UserAttribute> input = List.of(
                new UserAttribute("attribute1", Collections.singletonList("value1")),
                new UserAttribute("attribute2", Collections.singletonList("value2"))
        );
        when(restTemplate.exchange(anyString(), any(), any(), any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.ok("response"));

        assertThrows(IllegalArgumentException.class, () -> subject.aggregate(input, Collections.emptyMap()));
    }

    @Test
    void aggregateRequestWithHeaders() {
        configuration.setHeaders(List.of(
                new Header("headerKey1", "headerValue1"),
                new Header("headerKey2", "headerValue2")
        ));
        when(restTemplate.exchange(anyString(), any(), any(), any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.ok("response"));

        HttpHeaders expectedHeaders = new HttpHeaders();
        expectedHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        expectedHeaders.setContentType(MediaType.APPLICATION_JSON);
        expectedHeaders.add("headerKey1", "headerValue1");
        expectedHeaders.add("headerKey2", "headerValue2");

        subject.aggregate(Collections.emptyList(), Collections.emptyMap());

        verify(restTemplate, times(1)).exchange(
                eq("https://domain1.com"),
                eq(HttpMethod.GET),
                eq(new HttpEntity<>(null, expectedHeaders)),
                any(ParameterizedTypeReference.class)
        );
    }

    @Test
    void aggregateRequestWithPathParams() {
        configuration.setPathParams(new ArrayList<>(List.of(
                new PathParam(0, "attribute1"),
                new PathParam(1, "attribute2")
        )));
        configuration.setEndpoint("https://domain1.com/%s/sub/%s");
        List<UserAttribute> input = List.of(
                new UserAttribute("attribute1", Collections.singletonList("value1")),
                new UserAttribute("attribute2", Collections.singletonList("value2"))
        );
        when(restTemplate.exchange(anyString(), any(), any(), any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.ok("response"));

        subject.aggregate(input, Collections.emptyMap());

        verify(restTemplate, times(1)).exchange(
                eq("https://domain1.com/value1/sub/value2"),
                eq(HttpMethod.GET),
                any(),
                any(ParameterizedTypeReference.class)
        );
    }

    @Test
    void aggregateRequestWithRequestParams() {
        configuration.setRequestParams(new ArrayList<>(List.of(
                new RequestParam("param1", "attribute1"),
                new RequestParam("param2", "attribute2")
        )));
        configuration.setEndpoint("https://domain1.com");
        List<UserAttribute> input = List.of(
                new UserAttribute("attribute1", Collections.singletonList("value1")),
                new UserAttribute("attribute2", Collections.singletonList("value2"))
        );
        when(restTemplate.exchange(anyString(), any(), any(), any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.ok("response"));

        subject.aggregate(input, Collections.emptyMap());

        verify(restTemplate, times(1)).exchange(
                eq("https://domain1.com?param1=value1&param2=value2"),
                eq(HttpMethod.GET),
                any(),
                any(ParameterizedTypeReference.class)
        );
    }

    @Test
    void aggregateRequestDefaultToGet() {
        configuration.setRequestMethod(null);
        when(restTemplate.exchange(anyString(), any(), any(), any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.ok("response"));

        subject.aggregate(Collections.emptyList(), Collections.emptyMap());

        verify(restTemplate, times(1)).exchange(
                eq("https://domain1.com"),
                eq(HttpMethod.GET),
                any(),
                any(ParameterizedTypeReference.class)
        );
    }

    @Test
    void aggregate() throws IOException {
        configuration.setRootListName("records");
        configuration.setRequestParams(new ArrayList<>(List.of(
                new RequestParam("param1", "attribute1"),
                new RequestParam("param2", "attribute2")
        )));
        configuration.setMappings(List.of(
                new Mapping("field1", "target1", null),
                new Mapping("field2", "target2", null)
        ));
        List<UserAttribute> input = List.of(
                new UserAttribute("attribute1", Collections.singletonList("value1")),
                new UserAttribute("attribute2", Collections.singletonList("value2"))
        );
        JsonNode apiResponse = objectMapper
                .readValue(new ClassPathResource("rest/result.json").getInputStream(), JsonNode.class);
        String stringResponse = objectMapper.writeValueAsString(apiResponse);
        when(restTemplate.exchange(anyString(), any(), any(), any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.ok(stringResponse));

        List<UserAttribute> result = subject.aggregate(input, Collections.emptyMap());

        verify(restTemplate, times(1)).exchange(
                eq("https://domain1.com?param1=value1&param2=value2"),
                eq(HttpMethod.GET),
                any(),
                any(ParameterizedTypeReference.class)
        );
        assertEquals(2, result.size());
        String value1 = result.stream().filter(userAttribute -> userAttribute.getName().equals("target1")).findFirst().get().getValues().get(0);
        assertEquals("value1", value1);
        String value2 = result.stream().filter(userAttribute -> userAttribute.getName().equals("target2")).findFirst().get().getValues().get(0);
        assertEquals("value2", value2);
    }

    @Test
    void aggregateMultiple() throws IOException {
        configuration.setRootListName("records");
        configuration.setRequestParams(new ArrayList<>(List.of(
                new RequestParam("param1", "attribute1"),
                new RequestParam("param2", "attribute2")
        )));
        configuration.setMappings(List.of(
                new Mapping("field1", "target1", null),
                new Mapping("field2", "target2", null)
        ));
        List<UserAttribute> input = List.of(
                new UserAttribute("attribute1", Collections.singletonList("value1")),
                new UserAttribute("attribute2", Collections.singletonList("value2"))
        );
        JsonNode apiResponse = objectMapper
                .readValue(new ClassPathResource("rest/multiple_result.json").getInputStream(), JsonNode.class);
        String stringResponse = objectMapper.writeValueAsString(apiResponse);
        when(restTemplate.exchange(anyString(), any(), any(), any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.ok(stringResponse));

        List<UserAttribute> result = subject.aggregate(input, Collections.emptyMap());

        verify(restTemplate, times(1)).exchange(
                eq("https://domain1.com?param1=value1&param2=value2"),
                eq(HttpMethod.GET),
                any(),
                any(ParameterizedTypeReference.class)
        );
        assertEquals(2, result.size());
        List<String> values = result.stream().filter(userAttribute -> userAttribute.getName().equals("target1")).findFirst().get().getValues();
        assertEquals(2, values.size());
        List<String> values2 = result.stream().filter(userAttribute -> userAttribute.getName().equals("target2")).findFirst().get().getValues();
        assertEquals(2, values2.size());
    }

    @Test
    void aggregateMultipleWithFilterMapping() throws IOException {
        configuration.setRootListName("records");
        configuration.setRequestParams(new ArrayList<>(List.of(
                new RequestParam("param1", "attribute1"),
                new RequestParam("param2", "attribute2")
        )));
        configuration.setMappings(List.of(
                new Mapping("field1", "target1", new MappingFilter("field2", "value4"))
        ));
        List<UserAttribute> input = List.of(
                new UserAttribute("attribute1", Collections.singletonList("value1")),
                new UserAttribute("attribute2", Collections.singletonList("value2"))
        );
        JsonNode apiResponse = objectMapper
                .readValue(new ClassPathResource("rest/multiple_result.json").getInputStream(), JsonNode.class);
        String stringResponse = objectMapper.writeValueAsString(apiResponse);
        when(restTemplate.exchange(anyString(), any(), any(), any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.ok(stringResponse));

        List<UserAttribute> result = subject.aggregate(input, Collections.emptyMap());

        verify(restTemplate, times(1)).exchange(
                eq("https://domain1.com?param1=value1&param2=value2"),
                eq(HttpMethod.GET),
                any(),
                any(ParameterizedTypeReference.class)
        );
        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getValues().size());
        assertEquals("value3", result.get(0).getValues().get(0));
    }

    @Test
    void aggregateDoNotApplyEmptyFilter() throws IOException {
        MappingFilter filter = new MappingFilter();
        filter.setKey("");
        filter.setValue("value");

        configuration.setRootListName("records");
        configuration.setRequestParams(new ArrayList<>(List.of(
                new RequestParam("param1", "attribute1"),
                new RequestParam("param2", "attribute2")
        )));
        configuration.setMappings(List.of(
                new Mapping("field1", "target1", filter)
        ));
        List<UserAttribute> input = List.of(
                new UserAttribute("attribute1", Collections.singletonList("value1")),
                new UserAttribute("attribute2", Collections.singletonList("value2"))
        );
        JsonNode apiResponse = objectMapper
                .readValue(new ClassPathResource("rest/multiple_result.json").getInputStream(), JsonNode.class);
        String stringResponse = objectMapper.writeValueAsString(apiResponse);
        when(restTemplate.exchange(anyString(), any(), any(), any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.ok(stringResponse));

        List<UserAttribute> result = subject.aggregate(input, Collections.emptyMap());

        verify(restTemplate, times(1)).exchange(
                eq("https://domain1.com?param1=value1&param2=value2"),
                eq(HttpMethod.GET),
                any(),
                any(ParameterizedTypeReference.class)
        );
        assertEquals(1, result.size());
        assertEquals(2, result.get(0).getValues().size());
        assertEquals("value1", result.get(0).getValues().get(0));
        assertEquals("value3", result.get(0).getValues().get(1));
    }

    @Test
    void aggregateWithNestedRoot() throws IOException {
        configuration.setRootListName("records[0].field2");
        configuration.setRequestParams(new ArrayList<>(List.of(
                new RequestParam("param1", "attribute1"),
                new RequestParam("param2", "attribute2")
        )));
        configuration.setMappings(List.of(
                new Mapping("subField1", "target1", null),
                new Mapping("subField2", "target2", null)
        ));
        List<UserAttribute> input = List.of(
                new UserAttribute("attribute1", Collections.singletonList("value1")),
                new UserAttribute("attribute2", Collections.singletonList("value2"))
        );
        JsonNode apiResponse = objectMapper
                .readValue(new ClassPathResource("rest/nested_result.json").getInputStream(), JsonNode.class);
        String stringResponse = objectMapper.writeValueAsString(apiResponse);
        when(restTemplate.exchange(anyString(), any(), any(), any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.ok(stringResponse));

        List<UserAttribute> result = subject.aggregate(input, Collections.emptyMap());

        verify(restTemplate, times(1)).exchange(
                eq("https://domain1.com?param1=value1&param2=value2"),
                eq(HttpMethod.GET),
                any(),
                any(ParameterizedTypeReference.class)
        );
        assertEquals(2, result.size());
        String value1 = result.stream().filter(userAttribute -> userAttribute.getName().equals("target1")).findFirst().get().getValues().get(0);
        assertEquals("subValue1", value1);
        String value2 = result.stream().filter(userAttribute -> userAttribute.getName().equals("target2")).findFirst().get().getValues().get(0);
        assertEquals("subValue2", value2);
    }

    @Test
    void aggregateRequestNoMappings() throws IOException {
        configuration.setRequestParams(new ArrayList<>(List.of(
                new RequestParam("param1", "attribute1"),
                new RequestParam("param2", "attribute2")
        )));
        List<UserAttribute> input = List.of(
                new UserAttribute("attribute1", Collections.singletonList("value1")),
                new UserAttribute("attribute2", Collections.singletonList("value2"))
        );
        JsonNode apiResponse = objectMapper
                .readValue(new ClassPathResource("rest/result.json").getInputStream(), JsonNode.class);
        String stringResponse = objectMapper.writeValueAsString(apiResponse);
        when(restTemplate.exchange(anyString(), any(), any(), any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.ok(stringResponse));

        List<UserAttribute> result = subject.aggregate(input, Collections.emptyMap());

        verify(restTemplate, times(1)).exchange(
                eq("https://domain1.com?param1=value1&param2=value2"),
                eq(HttpMethod.GET),
                any(),
                any(ParameterizedTypeReference.class)
        );
        assertTrue(result.isEmpty());
    }

    @Test
    void aggregateRequestInvalidApiResponse() {
        configuration.setRequestParams(new ArrayList<>(List.of(
                new RequestParam("param1", "attribute1"),
                new RequestParam("param2", "attribute2")
        )));
        configuration.setMappings(List.of(
                new Mapping("field1", "target1", null),
                new Mapping("field2", "target2", null)
        ));
        List<UserAttribute> input = List.of(
                new UserAttribute("attribute1", Collections.singletonList("value1")),
                new UserAttribute("attribute2", Collections.singletonList("value2"))
        );
        when(restTemplate.exchange(anyString(), any(), any(), any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.ok("invalid"));

        List<UserAttribute> result = subject.aggregate(input, Collections.emptyMap());

        verify(restTemplate, times(1)).exchange(
                eq("https://domain1.com?param1=value1&param2=value2"),
                eq(HttpMethod.GET),
                any(),
                any(ParameterizedTypeReference.class)
        );
        assertTrue(result.isEmpty());
    }

    @Test
    void aggregateApiErrorWithBody() throws IOException {
        configuration.setRootListName("records");
        configuration.setRequestParams(new ArrayList<>(List.of(
                new RequestParam("param1", "attribute1"),
                new RequestParam("param2", "attribute2")
        )));
        configuration.setMappings(List.of(
                new Mapping("field1", "target1", null),
                new Mapping("field2", "target2", null)
        ));
        List<UserAttribute> input = List.of(
                new UserAttribute("attribute1", Collections.singletonList("value1")),
                new UserAttribute("attribute2", Collections.singletonList("value2"))
        );
        JsonNode apiResponse = objectMapper
                .readValue(new ClassPathResource("rest/result.json").getInputStream(), JsonNode.class);
        String stringResponse = objectMapper.writeValueAsString(apiResponse);
        when(restTemplate.exchange(anyString(), any(), any(), any(ParameterizedTypeReference.class)))
                .thenThrow(new HttpStatusCodeException(HttpStatus.NOT_FOUND, "", stringResponse.getBytes(), StandardCharsets.UTF_8) {
                    @Override
                    public HttpStatusCode getStatusCode() {
                        return super.getStatusCode();
                    }
                });

        List<UserAttribute> result = subject.aggregate(input, Collections.emptyMap());

        verify(restTemplate, times(1)).exchange(
                eq("https://domain1.com?param1=value1&param2=value2"),
                eq(HttpMethod.GET),
                any(),
                any(ParameterizedTypeReference.class)
        );
        assertEquals(2, result.size());
        String value1 = result.stream().filter(userAttribute -> userAttribute.getName().equals("target1")).findFirst().get().getValues().get(0);
        assertEquals("value1", value1);
        String value2 = result.stream().filter(userAttribute -> userAttribute.getName().equals("target2")).findFirst().get().getValues().get(0);
        assertEquals("value2", value2);
    }

    @Test
    void aggregateApiErrorWithEmptyBody() {
        configuration.setRootListName("records");
        configuration.setRequestParams(new ArrayList<>(List.of(
                new RequestParam("param1", "attribute1"),
                new RequestParam("param2", "attribute2")
        )));
        configuration.setMappings(List.of(
                new Mapping("field1", "target1", null),
                new Mapping("field2", "target2", null)
        ));
        List<UserAttribute> input = List.of(
                new UserAttribute("attribute1", Collections.singletonList("value1")),
                new UserAttribute("attribute2", Collections.singletonList("value2"))
        );
        when(restTemplate.exchange(anyString(), any(), any(), any(ParameterizedTypeReference.class)))
                .thenThrow(new HttpStatusCodeException(HttpStatus.NOT_FOUND, "", "".getBytes(), StandardCharsets.UTF_8) {
                    @Override
                    public HttpStatusCode getStatusCode() {
                        return super.getStatusCode();
                    }
                });

        assertThrows(HttpStatusCodeException.class, () -> subject.aggregate(input, Collections.emptyMap()));
    }

    @Test
    void aggregateApiErrorWithEmptyBodyAsEmptyAttributes() {
        configuration.setRootListName("records");
        configuration.setRequestParams(new ArrayList<>(List.of(
                new RequestParam("param1", "attribute1"),
                new RequestParam("param2", "attribute2")
        )));
        configuration.setMappings(List.of(
                new Mapping("field1", "target1", null),
                new Mapping("field2", "target2", null)
        ));
        configuration.setHandleResponseErrorAsEmpty(true);
        List<UserAttribute> input = List.of(
                new UserAttribute("attribute1", Collections.singletonList("value1")),
                new UserAttribute("attribute2", Collections.singletonList("value2"))
        );
        when(restTemplate.exchange(anyString(), any(), any(), any(ParameterizedTypeReference.class)))
                .thenThrow(new HttpStatusCodeException(HttpStatus.NOT_FOUND, "", "".getBytes(), StandardCharsets.UTF_8) {
                    @Override
                    public HttpStatusCode getStatusCode() {
                        return super.getStatusCode();
                    }
                });

        List<UserAttribute> result = subject.aggregate(input, Collections.emptyMap());

        verify(restTemplate, times(1)).exchange(
                eq("https://domain1.com?param1=value1&param2=value2"),
                eq(HttpMethod.GET),
                any(),
                any(ParameterizedTypeReference.class)
        );
        assertEquals(2, result.size());
        assertEquals(1, result.get(0).getValues().size());
        assertEquals("", result.get(0).getValues().get(0));
        assertEquals(1, result.get(1).getValues().size());
        assertEquals("", result.get(1).getValues().get(0));
    }

    @Test
    void aggregateRequestApiErrorAsEmptyAttributes() {
        configuration.setRequestParams(new ArrayList<>(List.of(
                new RequestParam("param1", "attribute1"),
                new RequestParam("param2", "attribute2")
        )));
        configuration.setMappings(List.of(
                new Mapping("field1", "target1", null),
                new Mapping("field2", "target2", null)
        ));
        configuration.setHandleResponseErrorAsEmpty(true);
        List<UserAttribute> input = List.of(
                new UserAttribute("attribute1", Collections.singletonList("value1")),
                new UserAttribute("attribute2", Collections.singletonList("value2"))
        );
        when(restTemplate.exchange(anyString(), any(), any(), any(ParameterizedTypeReference.class)))
                .thenThrow(new RestClientException("error"));

        List<UserAttribute> result = subject.aggregate(input, Collections.emptyMap());

        verify(restTemplate, times(1)).exchange(
                eq("https://domain1.com?param1=value1&param2=value2"),
                eq(HttpMethod.GET),
                any(),
                any(ParameterizedTypeReference.class)
        );
        assertEquals(2, result.size());
        assertEquals(1, result.get(0).getValues().size());
        assertEquals("", result.get(0).getValues().get(0));
        assertEquals(1, result.get(1).getValues().size());
        assertEquals("", result.get(1).getValues().get(0));
    }

    @Test
    void aggregateRequestEmptyApiResponseAsEmptyAttributes() {
        configuration.setRequestParams(new ArrayList<>(List.of(
                new RequestParam("param1", "attribute1"),
                new RequestParam("param2", "attribute2")
        )));
        configuration.setMappings(List.of(
                new Mapping("field1", "target1", null),
                new Mapping("field2", "target2", null)
        ));
        configuration.setHandleResponseErrorAsEmpty(true);
        List<UserAttribute> input = List.of(
                new UserAttribute("attribute1", Collections.singletonList("value1")),
                new UserAttribute("attribute2", Collections.singletonList("value2"))
        );
        when(restTemplate.exchange(anyString(), any(), any(), any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.ok("[]"));

        List<UserAttribute> result = subject.aggregate(input, Collections.emptyMap());

        verify(restTemplate, times(1)).exchange(
                eq("https://domain1.com?param1=value1&param2=value2"),
                eq(HttpMethod.GET),
                any(),
                any(ParameterizedTypeReference.class)
        );
        assertEquals(2, result.size());
        assertEquals(1, result.get(0).getValues().size());
        assertEquals("", result.get(0).getValues().get(0));
        assertEquals(1, result.get(1).getValues().size());
        assertEquals("", result.get(1).getValues().get(0));
    }

    @Test
    void aggregateUseCache() throws IOException {
        configuration.setRootListName("records");
        configuration.setRequestParams(new ArrayList<>(List.of(
                new RequestParam("param1", "attribute1"),
                new RequestParam("param2", "attribute2")
        )));
        configuration.setMappings(List.of(
                new Mapping("field1", "target1", null),
                new Mapping("field2", "target2", null)
        ));
        configuration.setCache(new Cache(
                true,
                "https://cache.com",
                "",
                "GET",
                null,
                "records.findAll{record->record.%s == \"%s\"}[0]",
                Collections.singletonList(new CacheFilter(0, "field1", "attribute1"))
        ));
        List<UserAttribute> input = List.of(
                new UserAttribute("attribute1", Collections.singletonList("value1")),
                new UserAttribute("attribute2", Collections.singletonList("value2"))
        );
        JsonNode apiResponse = objectMapper
                .readValue(new ClassPathResource("rest/multiple_result.json").getInputStream(), JsonNode.class);
        String stringResponse = objectMapper.writeValueAsString(apiResponse);
        when(restTemplate.exchange(eq("https://domain1.com?param1=value1&param2=value2"), any(), any(), any(ParameterizedTypeReference.class)))
                .thenThrow(new RestClientException("error"));
        when(restTemplate.exchange(eq("https://cache.com"), any(), any(), any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.ok(stringResponse));

        subject.run();
        List<UserAttribute> result = subject.aggregate(input, Collections.emptyMap());

        verify(restTemplate, times(1)).exchange(
                eq("https://domain1.com?param1=value1&param2=value2"),
                eq(HttpMethod.GET),
                any(),
                any(ParameterizedTypeReference.class)
        );
        verify(restTemplate, times(1)).exchange(
                eq("https://cache.com"),
                eq(HttpMethod.GET),
                any(),
                any(ParameterizedTypeReference.class)
        );
        assertEquals(2, result.size());
        List<String> values = result.stream().filter(userAttribute -> userAttribute.getName().equals("target1")).findFirst().get().getValues();
        assertEquals(1, values.size());
        assertEquals("value1", values.get(0));
        List<String> values2 = result.stream().filter(userAttribute -> userAttribute.getName().equals("target2")).findFirst().get().getValues();
        assertEquals(1, values2.size());
        assertEquals("value2", values2.get(0));
    }

    @Test
    void aggregateEmptyResultOnFetchErrorAndEmptyCache() throws IOException {
        configuration.setRootListName("records");
        configuration.setRequestParams(new ArrayList<>(List.of(
                new RequestParam("param1", "attribute1"),
                new RequestParam("param2", "attribute2")
        )));
        configuration.setMappings(List.of(
                new Mapping("field1", "target1", null),
                new Mapping("field2", "target2", null)
        ));
        configuration.setCache(new Cache(
                true,
                "https://cache.com",
                "",
                "GET",
                null,
                "records",
                Collections.singletonList(new CacheFilter(0, "field1", "attribute1"))
        ));
        List<UserAttribute> input = List.of(
                new UserAttribute("attribute1", Collections.singletonList("value1")),
                new UserAttribute("attribute2", Collections.singletonList("value2"))
        );
        JsonNode apiResponse = objectMapper
                .readValue(new ClassPathResource("rest/multiple_result.json").getInputStream(), JsonNode.class);
        String stringResponse = objectMapper.writeValueAsString(apiResponse);
        when(restTemplate.exchange(eq("https://domain1.com?param1=value1&param2=value2"), any(), any(), any(ParameterizedTypeReference.class)))
                .thenThrow(new RestClientException("error"));
        when(restTemplate.exchange(eq("https://cache.com"), any(), any(), any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.ok(stringResponse));

        List<UserAttribute> result = subject.aggregate(input, Collections.emptyMap());

        assertTrue(result.isEmpty());
    }

    @Test
    void aggregateDoNotUseDisabledCache() {
        configuration.setRootListName("records");
        configuration.setRequestParams(new ArrayList<>(List.of(
                new RequestParam("param1", "attribute1"),
                new RequestParam("param2", "attribute2")
        )));
        configuration.setMappings(List.of(
                new Mapping("field1", "target1", null),
                new Mapping("field2", "target2", null)
        ));
        configuration.setCache(new Cache(
                false,
                "https://cache.com",
                "",
                "GET",
                null,
                "records",
                Collections.singletonList(new CacheFilter(0, "field1", "attribute1"))
        ));
        List<UserAttribute> input = List.of(
                new UserAttribute("attribute1", Collections.singletonList("value1")),
                new UserAttribute("attribute2", Collections.singletonList("value2"))
        );
        when(restTemplate.exchange(anyString(), any(), any(), any(ParameterizedTypeReference.class)))
                .thenThrow(new RestClientException("error"));

        subject.run();
        assertThrows(RestClientException.class, () -> subject.aggregate(input, Collections.emptyMap()));
    }

    @Test
    void aggregateFailCacheRefresh() {
        configuration.setRootListName("records");
        configuration.setRequestParams(new ArrayList<>(List.of(
                new RequestParam("param1", "attribute1"),
                new RequestParam("param2", "attribute2")
        )));
        configuration.setMappings(List.of(
                new Mapping("field1", "target1", null),
                new Mapping("field2", "target2", null)
        ));
        configuration.setCache(new Cache(
                true,
                "https://cache.com",
                "",
                "GET",
                null,
                "records",
                Collections.singletonList(new CacheFilter(0, "field1", "attribute1"))
        ));
        when(restTemplate.exchange(anyString(), any(), any(), any(ParameterizedTypeReference.class)))
                .thenThrow(new RestClientException("error"));

        subject.run();

        verify(restTemplate, times(1)).exchange(
                eq("https://cache.com"),
                eq(HttpMethod.GET),
                any(),
                any(ParameterizedTypeReference.class)
        );
    }

}
