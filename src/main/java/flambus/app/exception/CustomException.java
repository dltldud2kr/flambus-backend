package flambus.app.exception;


import flambus.app._enum.CustomExceptionCode;
import lombok.*;
import org.springframework.http.HttpStatus;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
public class CustomException extends RuntimeException {
    private CustomExceptionCode customErrorCode;
    private String detailMessage;

    // 추가: getHttpStatus() 메서드 정의
    public HttpStatus getHttpStatus() {
        return customErrorCode == null ? HttpStatus.INTERNAL_SERVER_ERROR : HttpStatus.BAD_REQUEST;
    }

    // 추가: getMessage() 메서드 정의
    @Override
    public String getMessage() {
        return detailMessage;
    }


    public CustomException(CustomExceptionCode customExceptionCode) {
        super(customExceptionCode.getStatusMessage());
        this.customErrorCode = customExceptionCode;
        this.detailMessage = customExceptionCode.getStatusMessage();
    }

}
