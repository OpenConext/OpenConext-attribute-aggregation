package aa.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;
import java.time.Instant;

@Entity(name = "accounts")
@Getter
@Setter
@NoArgsConstructor
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column
    private String urn;

    @NotNull
    @Column
    private String name;

    @Column
    private String email;

    @Column
    @Enumerated(EnumType.STRING)
    @NotNull
    private AccountType accountType;

    @NotNull
    @Column
    private String linkedId;

    @Column
    private Instant created;
}
