package flambus.app.exception;

import flambus.app._enum.CustomExceptionCode;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CustomErrorResponse{
    private CustomExceptionCode status;
    private String statusMessage;
}
