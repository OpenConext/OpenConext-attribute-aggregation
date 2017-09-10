package aa.aggregators.pseudo;

import aa.aggregators.AbstractAttributeAggregator;
import aa.model.AttributeAuthorityConfiguration;
import aa.model.PseudoEmail;
import aa.model.UserAttribute;
import aa.repository.PseudoEmailRepository;
import aa.util.StreamUtils;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static java.util.Collections.singletonList;

public class PseudoEmailAggregator extends AbstractAttributeAggregator {

    private String emailPostfix;
    private PseudoEmailRepository pseudoEmailRepository;

    public PseudoEmailAggregator(AttributeAuthorityConfiguration configuration,
                                 PseudoEmailRepository pseudoEmailRepository,
                                 String emailPostfix) {
        super(configuration);
        this.pseudoEmailRepository = pseudoEmailRepository;
        this.emailPostfix = emailPostfix;
    }

    @Override
    public List<UserAttribute> aggregate(List<UserAttribute> input) {
        String email = getUserAttributeSingleValue(input, EMAIL);

        Optional<PseudoEmail> emailOptional = pseudoEmailRepository.findByEmailIgnoreCase(email);

        PseudoEmail pseudoEmail = emailOptional.orElseGet(() ->
            new PseudoEmail(
                email,
                String.format("%s@%s", UUID.randomUUID().toString(), this.emailPostfix)));

        LOG.debug("Retrieved Pseudo email for email: {} and result {}", email, emailOptional);

        if (pseudoEmail.getId() == null) {
            pseudoEmailRepository.save(pseudoEmail);
        }

        return mapValuesToUserAttribute(EMAIL, singletonList(pseudoEmail.getPseudoEmail()));
    }

}
