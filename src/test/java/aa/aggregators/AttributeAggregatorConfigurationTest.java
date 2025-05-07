package aa.aggregators;

import aa.cache.NoopUserAttributeCache;
import aa.config.AuthorityResolver;
import aa.repository.AccountRepository;
import aa.repository.PseudoEmailRepository;
import aa.service.AttributeAggregatorService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.scheduling.TaskScheduler;

import java.io.IOException;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.springframework.test.util.ReflectionTestUtils.getField;

public class AttributeAggregatorConfigurationTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private AttributeAggregatorConfiguration subject;

    @Before
    public void before() throws Exception {
        this.doBefore("classpath:/attributeAuthoritiesProductionTemplate.yml");
    }

    private void doBefore(String configFileLocation) throws IOException {
        subject = new AttributeAggregatorConfiguration(
            "http://localhost:8889/oauth/token",
            "surfconext.nl",
            new ClassPathResource("/serviceProviderConfig.json"),
            new AuthorityResolver(new DefaultResourceLoader(), configFileLocation),
            new NoopUserAttributeCache(),
            Mockito.mock(AccountRepository.class),
            Mockito.mock(PseudoEmailRepository.class),
            Mockito.mock(TaskScheduler.class),
                objectMapper

        );
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testAttributeAggregatorService() throws Exception {
        AttributeAggregatorService attributeAggregatorService = subject.attributeAggregatorService();
        Map<String, AttributeAggregator> aggregators = (Map<String, AttributeAggregator>) getField(attributeAggregatorService, "aggregators");

        assertEquals(5, aggregators.size());
        asList("pseudo_email", "orcid", "voot", "idin", "test:mock")
                .forEach(authorityId -> assertEquals(authorityId, aggregators.get(authorityId).getAttributeAuthorityId()));
    }

    @Test(expected = IllegalArgumentException.class)
    @SuppressWarnings("unchecked")
    public void testAttributeAggregatorServiceIllegalAuthorityId() throws Exception {
        this.doBefore("classpath:/testAttributeAuthorities.yml");
        subject.attributeAggregatorService();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testRestAttributeAggregatorService() throws Exception {
        this.doBefore("classpath:/testRestAttributeAuthority.yml");
        subject.attributeAggregatorService();
    }

}