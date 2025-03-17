package aa.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
