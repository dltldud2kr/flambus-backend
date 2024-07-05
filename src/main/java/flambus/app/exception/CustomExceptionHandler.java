package flambus.app.exception;

import flambus.app._enum.ApiResponseCode;
import flambus.app.dto.ResultDTO;
import flambus.app.exception.CustomException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class CustomExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResultDTO handleCustomException(CustomException e) {
        return ResultDTO.of(false,e.getCustomErrorCode().getStatusCode(), e.getDetailMessage(), null);
    }
}
