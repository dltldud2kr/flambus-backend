package flambus.app.entity;

import flambus.app._enum.EmailAuthStatus;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name= "email_auth")
@Data
public class EmailAuth {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDateTime created;
    private String email;
    private String verifCode;

    @Enumerated(EnumType.STRING)
    private EmailAuthStatus emailAuthStatus;

}
