package flambus.app.dto.member;


import lombok.Data;

@Data
public class JoinRequestDto {
    private String email; //이메일
    private String password; //비밀번호
    private String nickName; //닉네임
    private int platform; //가입 플랫폼 0:flatform 1:kakao 2:naver
}
