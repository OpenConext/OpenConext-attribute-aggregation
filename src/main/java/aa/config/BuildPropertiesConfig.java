package aa.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import java.io.IOException;
import java.time.Instant;
import java.util.Properties;

@Configuration
public class BuildPropertiesConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(BuildPropertiesConfig.class);

    @Bean
    public BuildProperties buildProperties() {
        try {
            Resource resource = new ClassPathResource("META-INF/build-info.properties");
            if (!resource.exists()) {
                LOGGER.warn("META-INF/build-info.properties not found, using default build properties");
                return new BuildProperties(buildDefaultProperties("test"));
            }

            Properties properties = PropertiesLoaderUtils.loadProperties(resource);
            Properties flattenedProperties = new Properties();
            properties.forEach((key, value) -> {
                String newKey = key.toString().replaceFirst("^build\\.", "");
                flattenedProperties.put(newKey, value);
            });

            return new BuildProperties(flattenedProperties);
        } catch (IOException e) {
            LOGGER.error("Error loading build-info.properties", e);
            return new BuildProperties(buildDefaultProperties("error"));
        }
    }

    private Properties buildDefaultProperties(String tag) {
        Properties defaultProperties = new Properties();
        defaultProperties.put("time", Instant.now().toString());
        defaultProperties.put("version", String.format("0.0.0-%s", tag.toUpperCase()));
        defaultProperties.put("name", String.format("%s-build", tag));
        defaultProperties.put("group", String.format("%s-group", tag));
        defaultProperties.put("artifact", String.format("%s-artifact", tag));
        return defaultProperties;
    }

}
