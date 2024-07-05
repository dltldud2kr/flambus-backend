package flambus.app.dto.member;


import lombok.*;

import javax.persistence.Column;
import java.time.LocalDateTime;

/**
 * @title 사용자 정보를 반환하는 DTO
 */

@Data
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class MemberDto {
    private Long idx;
    private String email;
    private boolean isAdmin;
    private int platform;
    private String introduce;
    private boolean termsAgree;
    private LocalDateTime termsAgreeDate; //약관 동의날짜
    private boolean serviceAgree; //서비스 이용 약관 동의 여부 0:no 1:yes
    private LocalDateTime serviceAgreeDate; //약관 동의날짜
    private boolean useGpsAgree; //GPS 이용 약관
    private LocalDateTime useGpsAgreeDate; //약관 동의날짜
    private long follower; //팔로워
    private boolean emailAuth;
    private long following; //팔로잉
    private long acornsCount; //도토리 개수
    private long canLimitCount; //리뷰 작성 가능 수(탐험일지 작성수) 자정마다 2로
    private String profileImageUrl; ///프로필 링크
    private LocalDateTime subscriptionDate; //가입일
}
