package aa.repository;

import aa.AbstractIntegrationTest;
import aa.model.Attribute;
import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.*;

public class AttributeRepositoryTest extends AbstractIntegrationTest {

  @Test
  public void testFindByAttributeAuthorityIdAndName() throws Exception {
    Optional<Attribute> attribute = attributeRepository.findByAttributeAuthorityIdAndName("aa1", "urn:mace:dir:attribute-def:eduPersonOrcid");
    assertTrue(attribute.isPresent());
    assertEquals(1L, attribute.get().getId().longValue());
  }
}