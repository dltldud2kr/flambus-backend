package flambus.app.service;

import org.springframework.http.ResponseEntity;

public interface EmailService {
    void sendEmailVerification(String to)throws Exception;
}
