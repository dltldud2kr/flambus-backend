package flambus.app._enum;

public enum EmailAuthStatus {
    UNVERIFIED, // 0: 미인증
    VERIFIED,   // 1: 인증
    COMPLETED,  // 2: 가입완료
    EXPIRED,    // 3: 만료됨
    INVALID     // 4. 유효하지않음  -> 여러 번 인증메일을 보낼 경우 젤 최근의 인증메일 1건만 유효함
}