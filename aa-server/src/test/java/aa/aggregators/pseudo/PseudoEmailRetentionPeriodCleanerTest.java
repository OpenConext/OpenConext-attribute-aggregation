package aa.aggregators.pseudo;

import aa.AbstractIntegrationTest;
import aa.model.PseudoEmail;
import aa.repository.PseudoEmailRepository;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.ZonedDateTime;

import static org.junit.Assert.assertEquals;

public class PseudoEmailRetentionPeriodCleanerTest extends AbstractIntegrationTest {

    @Autowired
    private PseudoEmailRepository pseudoEmailRepository;

    @Autowired
    private PseudoEmailRetentionPeriodCleaner subject ;

    @Test
    public void testRetentionPeriod() {
        PseudoEmail pseudoEmail = new PseudoEmail("t@t", "p@p", "sp");
        pseudoEmail.setUpdated(ZonedDateTime.now().minusYears(5).toInstant());
        pseudoEmailRepository.save(pseudoEmail);

        int deleted = subject.clean();
        assertEquals(1, deleted);
    }

}