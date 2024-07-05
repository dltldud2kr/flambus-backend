package flambus.app.repository;//package flambus.flambus_v10.repository;

import flambus.app._enum.EmailAuthStatus;
import flambus.app.entity.EmailAuth;
import flambus.app.entity.Review;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.swing.text.html.Option;
import javax.validation.constraints.Email;
import java.util.List;
import java.util.Optional;


@Repository
public interface EmailAuthRepository extends JpaRepository<EmailAuth, Long> {

    Optional<EmailAuth> findByEmailAndEmailAuthStatus(String email, EmailAuthStatus emailAuthStatus);

    List<EmailAuth> findListByEmailAndEmailAuthStatus(String email, EmailAuthStatus emailAuthStatus);

    List<EmailAuth> findAllByEmail(String email);

    EmailAuth findByEmail(String email);



}
