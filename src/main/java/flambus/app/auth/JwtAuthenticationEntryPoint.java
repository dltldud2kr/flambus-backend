package flambus.app.auth;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        // 여기서 만료된 토큰 처리를 수행하고 클라이언트에게 응답을 보낼 수 있습니다. 401
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "토큰이 만료되었습니다.");

    }
}
