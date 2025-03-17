package aa.config;

import aa.model.Attribute;
import aa.model.AttributeAuthorityConfiguration;
import aa.model.RequiredInputAttribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.IOException;

@Service
public class AuthorityResolver {

    private final static Logger LOG = LoggerFactory.getLogger(AuthorityResolver.class);

    private AuthorityConfiguration configuration;

    @Autowired
    public AuthorityResolver(ResourceLoader resourceLoader,
                             @Value("${attribute_authorities_config_path}") String configFileLocation) throws IOException {
        this.parse(resourceLoader, configFileLocation);
        this.references();
        LOG.info("Parsed {} with configuration {}", configFileLocation, this.configuration);
    }

    private void parse(ResourceLoader resourceLoader, String configFileLocation) throws IOException {
        Constructor constructor = new Constructor(AuthorityConfiguration.class, new LoaderOptions());
        TypeDescription authorityConfigurationDescription = new TypeDescription(AuthorityConfiguration.class);
        authorityConfigurationDescription.addPropertyParameters("authorities", AttributeAuthorityConfiguration.class);

        TypeDescription attributeAuthorityDescription = new TypeDescription(AttributeAuthorityConfiguration.class);
        authorityConfigurationDescription.addPropertyParameters("attributes", Attribute.class);
        authorityConfigurationDescription.addPropertyParameters("requiredInputAttributes", RequiredInputAttribute.class);

        constructor.addTypeDescription(authorityConfigurationDescription);
        constructor.addTypeDescription(attributeAuthorityDescription);

        this.configuration = new Yaml(constructor).load(resourceLoader.getResource(configFileLocation).getInputStream());
    }

    private void references() {
        this.configuration.getAuthorities().forEach(this::reference);
    }

    private void reference(AttributeAuthorityConfiguration attributeAuthorityConfiguration) {
        attributeAuthorityConfiguration.getAttributes().forEach(attribute -> attribute.setAttributeAuthorityId(attributeAuthorityConfiguration.getId()));
    }

    public AuthorityConfiguration getConfiguration() {
        return configuration;
    }
}
