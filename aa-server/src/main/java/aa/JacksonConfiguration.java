package aa;

import com.fasterxml.jackson.datatype.hibernate5.Hibernate5Module;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfiguration {

    @Bean
    public Hibernate5Module hibernate5Module() {
        return new Hibernate5Module();
    }

    @Bean
    public Jdk8Module jdk8Module() {
        return new Jdk8Module();
    }

    @Bean
    public JavaTimeModule javaTimeModule() {
        return new JavaTimeModule();
    }

}

