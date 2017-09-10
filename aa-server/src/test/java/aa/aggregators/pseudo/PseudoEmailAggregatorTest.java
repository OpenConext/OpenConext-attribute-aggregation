package aa.aggregators.pseudo;

import aa.model.AttributeAuthorityConfiguration;
import aa.model.PseudoEmail;
import aa.model.RequiredInputAttribute;
import aa.model.UserAttribute;
import aa.repository.PseudoEmailRepository;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static aa.aggregators.AttributeAggregator.EMAIL;
import static aa.aggregators.AttributeAggregator.NAME_ID;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PseudoEmailAggregatorTest {

    private PseudoEmailAggregator subject;

    private PseudoEmailRepository pseudoEmailRepository;

    private PseudoEmail pseudoEmail = new PseudoEmail(
        "jdoe@example.org",
        "6799299b-66ba-32f0-82ad-71e159a8fd40@openconext.org");

    @Before
    public void before() {
        AttributeAuthorityConfiguration configuration = new AttributeAuthorityConfiguration("pseudo_email");
        configuration.setRequiredInputAttributes(singletonList(new RequiredInputAttribute(EMAIL)));
        this.pseudoEmailRepository = mock(PseudoEmailRepository.class);
        subject = new PseudoEmailAggregator(configuration, pseudoEmailRepository, "openconext.org");
        pseudoEmail.setId(1L);
    }

    @Test
    public void testGetExistingPseudoMailFlow() throws Exception {
        UserAttribute userAttribute = doGetPseudoEmail(pseudoEmail);
        assertEquals(Collections.singletonList(this.pseudoEmail.getPseudoEmail()), userAttribute.getValues());

        verify(pseudoEmailRepository, never()).save(any(PseudoEmail.class));
    }

    @Test
    public void testNewPseudoMailFlow() throws Exception {
        UserAttribute userAttribute = doGetPseudoEmail(null);
        assertNotNull(userAttribute.getValues().get(0));


        verify(pseudoEmailRepository, times(1)).save(any(PseudoEmail.class));
    }

    private UserAttribute doGetPseudoEmail(PseudoEmail pseudoEmail) {
        when(pseudoEmailRepository.findByEmail(this.pseudoEmail.getEmail()))
            .thenReturn(Optional.ofNullable(pseudoEmail));

        List<UserAttribute> userAttributes = subject.aggregate(inputUserAttributes(NAME_ID));

        assertEquals(1, userAttributes.size());

        UserAttribute userAttribute = userAttributes.get(0);
        assertEquals(EMAIL, userAttribute.getName());
        assertEquals("pseudo_email", userAttribute.getSource());

        return userAttribute;
    }

    private List<UserAttribute> inputUserAttributes(String nameIdType) {
        return Arrays.asList(
            new UserAttribute(nameIdType, singletonList("saml2_user.com")),
            new UserAttribute(EMAIL, singletonList("jdoe@example.org")));
    }

}