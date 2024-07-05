package flambus.app.service;


import flambus.app.dto.email.emailResponseDto;
import flambus.app.dto.member.JoinRequestDto;
import flambus.app.dto.member.MemberDto;
import flambus.app.dto.member.TokenDto;
import flambus.app.entity.Member;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@Service
public interface MemberService {

    Map<String, Object> login(String email, String password);
    TokenDto createToken(Long memberIdx);
    TokenDto join(JoinRequestDto request);

    Member getMember(String email);

    Member getMember(long memberIdx);

    boolean isMember(String email);

    Member isAlreadyEmail(String email);

    List<MemberDto> getAllMembers();

    boolean isAdmin(long memberIdx);

    @Transactional
    long addAcorns(Member member, int count);

    @Transactional
    long removeAcorns(Member member, int count);

    /**
     * 이메일 인증확인 (발송된 인증번호와 동일한지 확인)
     * @param email
     * @param verifCode
     * @return
     */
    boolean emailCheck(String email, String verifCode);




    /**
     * 비밀번호 변경
     */

    boolean changePw(String email, String password);

    /**
     * 카카오 회원가입
     */
    TokenDto join(String email, String kakaoIdx, String nickname);

    /**
     * 카카오 로그인
     */
    TokenDto kakaoLogin(String email, String password);



    /**
     *
     * 카카오 로그인 토큰 값
     */
    String getReturnAccessToken(String code, HttpServletRequest request);

    /**
     * 카카오로그인 파싱 결과
     * @param access_token
     * @return
     */
    public Map<String,Object> getUserInfo(String access_token);

}
