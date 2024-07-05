package flambus.app._enum;

import org.springframework.http.HttpStatus;

public enum CustomExceptionCode {

    // BAD_REQUEST (400): 클라이언트 요청이 유효하지 않거나 잘못된 데이터로 인해 서버에서 처리할 수 없는 경우.
    //NOT_FOUND (404): 요청한 리소스를 찾을 수 없는 경우.
    //INTERNAL_SERVER_ERROR (500): 서버에서 예상치 못한 오류가 발생한 경우.


    EXPIRED_JWT("EXPIRED_JWT", "만료된 토큰입니다.", HttpStatus.BAD_REQUEST),
    INVALID_JWT("INVALID_JWT", "올바른 형식의 토큰이 아닙니다.", HttpStatus.BAD_REQUEST),
    NOT_FOUND_EMAIL("NOT_FOUND_EMAIL", "이메일을 찾을 수 없습니다.", HttpStatus.BAD_REQUEST),
    NOT_FOUND("NOT_FOUND", "해당 정보를 찾을 수 없습니다.", HttpStatus.BAD_REQUEST),
    NOT_FOUND_USER("NOT_FOUND_USER", "존재하지 않는 회원입니다.", HttpStatus.BAD_REQUEST),
    DUPLICATED("DUPLICATED", "중복된 정보가 존재합니다.", HttpStatus.BAD_REQUEST),
    SERVER_ERROR("SERVER_ERROR", "요청중 서버 문제가 발생했습니다.", HttpStatus.BAD_REQUEST),
    ACCESS_DENIED("ACCESS_DENIED", "요청이 거부 되었습니다.", HttpStatus.BAD_REQUEST),

    //메일 인증
    DUPLICATED_MEMBER("DUPLICATED_MEMBER", "이미 존재하는 회원", HttpStatus.BAD_REQUEST),
    VERIFIED_MEMBER("VERIFIED_MEMBER", "이미 인증이 완료된 회원", HttpStatus.BAD_REQUEST),
    INVALID_AUTH("INVALID_AUTH", "유효하지 않은 인증", HttpStatus.BAD_REQUEST),
    EXPIRED_AUTH("EXPIRED_AUTH", "만료된 인증", HttpStatus.BAD_REQUEST),
    INVALID_CODE("INVALID_CODE", "인증번호가 틀렸습니다.", HttpStatus.BAD_REQUEST),
    STORE_CREATION_FAILED("STORE_CREATION_FAILED", "가게 등록 실패.", HttpStatus.BAD_REQUEST);




    // 추후에 추가될 다른 업로드 타입들

    private final String statusCode;
    private final String statusMessage;

    // 추가: HttpStatus 열거 값을 저장할 필드
    private final HttpStatus httpStatus;

    CustomExceptionCode(String statusCode, String statusMessage, HttpStatus httpStatus) {
        this.statusCode = statusCode;
        this.statusMessage = statusMessage;
        this.httpStatus = httpStatus; // HttpStatus를 생성자로 받아서 저장
    }
    public String getStatusCode() {
        return statusCode;
    }
    public String getStatusMessage() {
        return statusMessage;
    }
    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

}
