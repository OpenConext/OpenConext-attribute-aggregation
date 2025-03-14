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

@Entity(name = "accounts")
@Getter
@Setter
@NoArgsConstructor
@ToString
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column
    private String urn;

    @Column
    private String schacHome;

    @Column
    @Enumerated(EnumType.STRING)
    @NotNull
    private AccountType accountType;

    @NotNull
    @Column
    private String linkedId;

    @Column
    private Instant created;

    public Account(String urn, String schacHome, AccountType accountType) {
        this.urn = urn;
        this.schacHome = schacHome;
        this.accountType = accountType;
    }

}
