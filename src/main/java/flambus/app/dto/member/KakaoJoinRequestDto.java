package flambus.app.dto.member;


import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@RequiredArgsConstructor(staticName = "of")
public class KakaoJoinRequestDto<D> {
    private final boolean success;
    private final String type;
    private final String resultCode;
    private final String message;
    private final D data;

    private Map<String, Object> userInfo;  // 사용자 정보를 담을 필드

    // 사용자 정보를 추가하는 새로운 메서드를 추가합니다.
    public void setUserInfo(Map<String, Object> userInfo) {
        this.userInfo = userInfo;
    }
}

