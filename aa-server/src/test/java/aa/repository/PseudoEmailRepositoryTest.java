package aa.repository;

import aa.AbstractIntegrationTest;
import aa.model.PseudoEmail;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

import static org.junit.Assert.*;

public class PseudoEmailRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    private PseudoEmailRepository subject;

    @Test
    public void findByEmailIgnoreCase() throws Exception {
        Optional<PseudoEmail> pseudoEmail = subject.findByEmail("john.doe@example.com");
        assertTrue(pseudoEmail.isPresent());
    }

}