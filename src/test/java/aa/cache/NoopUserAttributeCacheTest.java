package aa.cache;

import aa.model.UserAttribute;
import org.junit.Test;

import java.util.Collections;
import java.util.Optional;

import static java.util.Collections.singletonList;
import static org.junit.Assert.*;

public class NoopUserAttributeCacheTest {

    private NoopUserAttributeCache subject = new NoopUserAttributeCache();

    @Test
    public void get() throws Exception {
        Optional<String> key = Optional.of("key");
        subject.put(key, singletonList(new UserAttribute("name", singletonList("value"))));
        assertFalse(subject.get(key).isPresent());
    }


}