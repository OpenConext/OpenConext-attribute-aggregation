package aa.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;

@Entity(name = "pseudo_emails")
@Getter
@Setter
@NoArgsConstructor
public class PseudoEmail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column
    private String email;

    @Column
    @NotNull
    private String pseudoEmail;

    @NotNull
    @Column
    private String spEntityId;

    @Column
    private Instant created = Instant.now();

    @Column
    private Instant updated;

    public PseudoEmail(String email, String pseudoEmail, String spEntityId) {
        this.email = email;
        this.pseudoEmail = pseudoEmail;
        this.spEntityId = spEntityId;
    }
}
