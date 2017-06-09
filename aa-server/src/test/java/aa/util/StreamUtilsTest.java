package aa.util;

import org.junit.Test;

import java.util.ArrayList;

import static aa.util.StreamUtils.iteratorToList;
import static aa.util.StreamUtils.singletonOptionalCollector;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class StreamUtilsTest {

    @Test
    public void testSingletonOptionalCollector() throws Exception {
        assertEquals("1", asList("1").stream().collect(singletonOptionalCollector()).get());
        assertFalse(new ArrayList<String>().stream().collect(singletonOptionalCollector()).isPresent());
    }

    @Test
    public void testIteratorToList() throws Exception {
        assertEquals("1", iteratorToList(asList("1").iterator()).get(0));
    }

    @Test
    public void testListFromIterable() throws Exception {
        assertEquals(StreamUtils.listFromIterable(asList(1, 2, 3)), asList(1, 2, 3));
    }

}