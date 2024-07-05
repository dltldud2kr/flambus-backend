package flambus.app.service.impl;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import flambus.app._enum.CustomExceptionCode;
import flambus.app._enum.EmailAuthStatus;
import flambus.app.auth.JwtTokenProvider;
import flambus.app.dto.member.JoinRequestDto;
import flambus.app.dto.member.MemberDto;
import flambus.app.dto.member.TokenDto;
import flambus.app.entity.EmailAuth;
import flambus.app.entity.Member;
import flambus.app.exception.CustomException;
import flambus.app.mapper.MemberMapper;
import flambus.app.repository.EmailAuthRepository;
import flambus.app.repository.MemberRepository;
import flambus.app.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {
    private final MemberRepository memberRepository;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final JwtTokenProvider jwtTokenProvider;
    private final MemberMapper memberMapper;

    private final EmailAuthRepository emailAuthRepository;

    /**
     * 1. 로그인 요청으로 들어온 ID, PWD 기반으로 Authentication 객체 생성
     * 2. authenticate() 메서드를 통해 요청된 Member에 대한 검증이 진행 => loadUserByUsername 메서드를 실행. 해당 메서드는 검증을 위한 유저 객체를 가져오는 부분으로써, 어떤 객체를 검증할 것인지에 대해 직접 구현
     * 3. 검증이 정상적으로 통과되었다면 인증된 Authentication객체를 기반으로 JWT 토큰을 생성
     */
    @Transactional
    public Map<String, Object> login(String email, String password) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(CustomExceptionCode.NOT_FOUND_EMAIL));
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(email, password);

        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);

        TokenDto tokenDto = jwtTokenProvider.generateToken(authentication);

        // Map을 사용하여 TokenDto와 추가로 전달할 문자열 값을 함께 담아 반환
        Map<String, Object> response = new HashMap<>();
        response.put("tokenDto", tokenDto);
        response.put("memberIdx", member.getIdx());

        return response;
    }

    public TokenDto createToken(Long memberIdx) {
        Member member = memberRepository.findById(memberIdx)
                .orElseThrow(() -> new CustomException(CustomExceptionCode.NOT_FOUND));

        if (jwtTokenProvider.validateToken(member.getRefreshToken())) {
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(member.getEmail(), member.getPassword());
            System.out.println("authenticationToken : "+authenticationToken);
            Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
            TokenDto tokenDto = jwtTokenProvider.generateToken(authentication);
            return tokenDto;
        } else {
            //만료된 리프레쉬 토큰.
            throw new CustomException(CustomExceptionCode.EXPIRED_JWT);
        }
    }


    //회원가입 로직
    @Transactional
    public TokenDto join(JoinRequestDto request) {
        try {
            //해당 이메일이 존재하는지 확인.
            if(this.getMember(request.getEmail()) != null) {
                throw new CustomException(CustomExceptionCode.DUPLICATED);
            }
            //해당 이메일이 디비에 존재하는지 확인.
            Member member = Member.builder()
                    .email(request.getEmail())
                    .password(request.getPassword())
                    .isAdmin(false)
                    .platform(0)
                    .refreshToken(null)
                    .serviceAgree(false)
                    .serviceAgreeDate(LocalDateTime.now())
                    .termsAgree(false)
                    .profileImageUrl("test")                // 추가 (널값허용안받음)
                    .nickname(request.getNickName())        // 추가 (널값허용안받음)
                    .withdrawal(false)                      // 추가 (널값허용안받음)
                    .withdrawalDate(LocalDateTime.now())    //널값허용왜안됨?
                    .subscriptionDate(LocalDateTime.now())  // 추가
                    .termsAgreeDate(LocalDateTime.now())
                    .useGpsAgree(false)
                    .useGpsAgreeDate(LocalDateTime.now())
                    .follower(0)
                    .following(0)
                    .build();
            memberRepository.save(member);
            //사용자 인증 정보를 담은 토큰을 생성함. (이메일, 비밀번호 )
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword());

            //authenticationManagerBuilder를 사용하여 authenticationToken을 이용한 사용자의 인증을 시도합니다.
            // 여기서 실제로 로그인 발생  ( 성공: Authentication 반환 //   실패 : Exception 발생
            Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
            // 인증이 된 경우 JWT 토큰을 발급  (요청에 대한 인증처리)
            TokenDto tokenDto = jwtTokenProvider.generateToken(authentication);
            log.info(tokenDto.getAccessToken());
            if (tokenDto.getAccessToken().isEmpty()){
                log.info(tokenDto.getAccessToken());
                log.info("token empty");
            }
            return tokenDto;
        } catch (DataAccessException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    // 카카오 회원가입
    @Transactional
    @Override
    public TokenDto join(String email, String kakaoIdx, String nickname) {
        try {
            //해당 이메일이 존재하는지 확인.
            Optional<Member> optionalMember =  memberRepository.findByEmail(email);
            if(optionalMember.isPresent()) {
                throw new CustomException(CustomExceptionCode.DUPLICATED);
            }
            //해당 이메일이 디비에 존재하는지 확인.
            Member member = Member.builder()
                    .email(email)
                    .password(kakaoIdx)
                    .isAdmin(false)
                    .platform(1)
                    .refreshToken(null)
                    .serviceAgree(false)
                    .serviceAgreeDate(LocalDateTime.now())
                    .termsAgree(false)
                    .profileImageUrl("test")                // 추가 (널값허용안받음)
                    .nickname(nickname)        // 추가 (널값허용안받음)
                    .withdrawal(false)                      // 추가 (널값허용안받음)
                    .withdrawalDate(LocalDateTime.now())    //널값허용왜안됨?
                    .subscriptionDate(LocalDateTime.now())  // 추가
                    .termsAgreeDate(LocalDateTime.now())
                    .useGpsAgree(false)
                    .useGpsAgreeDate(LocalDateTime.now())
                    .follower(0)
                    .following(0)
                    .build();
            memberRepository.save(member);

            //사용자 인증 정보를 담은 토큰을 생성함. (이메일, 카카오고유번호(비밀번호 역할) )
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(email, kakaoIdx);

            //authenticationManagerBuilder를 사용하여 authenticationToken을 이용한 사용자의 인증을 시도합니다.
            // 여기서 실제로 로그인 발생  ( 성공: Authentication 반환 //   실패 : Exception 발생
            Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
            // 인증이 된 경우 JWT 토큰을 발급  (요청에 대한 인증처리)
            TokenDto tokenDto = jwtTokenProvider.generateToken(authentication);
            log.info(tokenDto.getAccessToken());
            if (tokenDto.getAccessToken().isEmpty()){
                log.info(tokenDto.getAccessToken());
                log.info("token empty");
            }
            return tokenDto;



        } catch (DataAccessException e) {
            throw new RuntimeException(e.getMessage());
        }
    }


    // 카카오 로그인
    @Transactional
    public TokenDto kakaoLogin(String email, String kakaoIdx) {
        Optional<Member> optionalMember =  memberRepository.findByEmail(email);

        memberRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(CustomExceptionCode.NOT_FOUND_EMAIL));

        //사용자 인증 정보를 담은 토큰을 생성함. (이메일, 멤버 인덱스 정보 포함 )
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(email, kakaoIdx);

        //authenticationManagerBuilder를 사용하여 authenticationToken을 이용한 사용자의 인증을 시도합니다.
        // 여기서 실제로 로그인 발생  ( 성공: Authentication 반환 //   실패 : Exception 발생
        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);

        // 인증이 된 경우 JWT 토큰을 발급  (요청에 대한 인증처리)
        TokenDto tokenDto = jwtTokenProvider.generateToken(authentication);

        if (tokenDto.getAccessToken().isEmpty()){
            log.info(tokenDto.getAccessToken());
        }

        return tokenDto;
    }


    //이메일 -> 사용자 정보를 찾아고  pk
    public Member getMember(String email) {
        Optional<Member> byEmail = memberRepository.findByEmail(email);
        // 비어있는 경우 예외 처리 또는 기본값을 반환하는 로직 추가

        return byEmail.orElse(null);
    }

    public Member getMember(long memberIdx) {
        Optional<Member> member = memberRepository.findById(memberIdx);
        // 비어있는 경우 예외 처리 또는 기본값을 반환하는 로직 추가
        return member.orElse(null);
    }

    public boolean isMember(String email) {
        Optional<Member> byEmail = memberRepository.findByEmail(email);
        if (byEmail.isPresent()){
            return true;
        }
        return false;
    }

    @Override
    public Member isAlreadyEmail(String email) {
        return getMember(email);
    }

    //가입된 사용자 정보를 조회
    //탈퇴된 사용자는 제외하고 조회합니다.
    public List<MemberDto> getAllMembers() {
        //Optional<Member> member = memberRepository.findAll(memberIdx);
        //return member.orElse(null);
        MemberDto parameter = new MemberDto();
        List<MemberDto> list = memberMapper.selectList(parameter);
        return list;
    }

    @Override
    public boolean isAdmin(long memberIdx) {
        return memberRepository.findById(memberIdx).get().isAdmin();
    }

    /**
     * @title 도토리 지급을 위한 함수입니다.
     * @param member 지급할 사용자
     * @param count 지급할 도토리 개수
     * @return
     */
    @Override
    public long addAcorns(Member member, int count) {
        Long acornsCount = member.getAcornsCount() + count;
        member.setAcornsCount(acornsCount);
        memberRepository.save(member);
        return acornsCount;
    }

    /**
     * @title 사용자 보유 도토리를 감소시킵니다.
     * @param member 감소시킬 사용자
     * @param count 수소시킬 도토리 개수
     * @return
     */
    @Override
    public long removeAcorns(Member member, int count) {
        if(member.getAcornsCount() == 0) {
            return 0;
        }
        Long acornsCount = member.getAcornsCount() - count;
        member.setAcornsCount(acornsCount);
        memberRepository.save(member);
        return acornsCount;
    }


    /**
     * @title 이메일 인증 로직
     * @param email 회원 이메일x`
     * @return
     */

    //1.  member 테이블에 이미 있는 회원인지 확인 .   있으면 duplicated_member 예외처리
    //2.  이미 인증된 상태 (VERIFIED) 인지 확인. VERIFIED상태라면 VERIFIED_MEMBER 예외처리
    //3. UNVERIFIED 상태를 찾음. 이 때 유효시간 3분이 지났으면 EmailAuthStatus를 EXPIRED 로 변경 후 EXPIRED_AUTH 예외처리.
    // 30분이 안 지났으면 EmailAuthStatus를 VERIFIED 상태로 변경해줌.

    /*
    1. EXPIRED
    2. VERIFIED
    3. UNVERIFIED
    4. INVALID
     */

    // CustomException 예외 발생시  이 예외가 emailCheck 메서드 내에서 발생하면
    // 트랜잭션을 롤백시키기 때문에 DB에 변경 사항이 반영되지 않음.
    // @Transactional(noRollbackFor = CustomException.class)를 사용하여
    // CustomException 이 발생해도 트랜잭션을 롤백하지 않도록 설정

    @Transactional(noRollbackFor = CustomException.class)
    @Override
    public boolean emailCheck(String email, String verifCode){




        // UNVERIFIED  있을 시 만료됐는지 확인 후 인증처리
        Optional<EmailAuth> optionalEmailAuth = emailAuthRepository.findByEmailAndEmailAuthStatus(email,EmailAuthStatus.UNVERIFIED);
        if (optionalEmailAuth.isPresent()){

            EmailAuth emailAuth = optionalEmailAuth.get();
            LocalDateTime creationTime = emailAuth.getCreated();
            LocalDateTime expirationTime = creationTime.plusMinutes(3); // 3분 유효시간
            String receiveVerifCode = emailAuth.getVerifCode();
            LocalDateTime now = LocalDateTime.now();
            // 인증번호 체크
            if (!verifCode.equals(receiveVerifCode)){
                throw new CustomException(CustomExceptionCode.INVALID_CODE);
            }

            if (now.isAfter(expirationTime)) {
                emailAuth.setEmailAuthStatus(EmailAuthStatus.EXPIRED); // 만료
                updateEmailAuthStatus(emailAuth);
                throw new CustomException(CustomExceptionCode.EXPIRED_AUTH);
            } else {

                emailAuth.setEmailAuthStatus(EmailAuthStatus.VERIFIED);
                updateEmailAuthStatus(emailAuth);

            }

        }
        // UNVERIFIED 없을 시
        else {

                throw new CustomException(CustomExceptionCode.INVALID_AUTH);
        }

        return true;
    }



    @Transactional
    public void updateEmailAuthStatus(EmailAuth emailAuth) {
        emailAuthRepository.save(emailAuth);
    }

    @Transactional
    @Override
    public boolean changePw(String email, String password) {

        Member member = memberRepository.findByEmail(email)
                .orElseThrow(()-> new CustomException(CustomExceptionCode.NOT_FOUND_USER));

        member.setPassword(password);
        System.out.println("변경 비번 " + member.getPassword());

        memberRepository.save(member);


        return true;
    }

    @Override
    public String getReturnAccessToken(String code, HttpServletRequest request) {
        String access_token = "";
        String refresh_token = "";
        String reqURL = "https://kauth.kakao.com/oauth/token";

        try {
            URL url = new URL(reqURL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            //HttpURLConnection 설정 값 셋팅
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);

            // buffer 스트림 객체 값 셋팅 후 요청
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream()));
            StringBuilder sb = new StringBuilder();
            sb.append("grant_type=authorization_code");
//           sb.append("&client_id=b22a0873d0ccefbc5f331106fa7b9287");  // REST API 키

            String origin = request.getHeader("Origin"); //요청이 들어온 Origin을 가져옵니다.
            sb.append("&client_id=ccf25614050bf5afb0bf4c82541cebb8");  // REST API 키

            sb.append("&redirect_uri=http://localhost:8080/auth/kakao/callback");

            // 테스트 서버, 퍼블리싱 서버 구분
            /*
            if("http://localhost:8080".equals(origin)){

                sb.append("&redirect_uri=http://localhost:8080/auth/kakao/callback"); // 앱 CALLBACK 경로
            } else {
                sb.append("&redirect_uri=https://app.lunaweb.dev/auth/kakao/callback"); // 다른 경로
            }

             */
            sb.append("&code=" + code);
            bw.write(sb.toString());
            bw.flush();

            //  RETURN 값 result 변수에 저장
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String br_line = "";
            String result = "";

            while ((br_line = br.readLine()) != null) {
                result += br_line;
            }

            JsonParser parser = new JsonParser();
            JsonElement element = parser.parse(result);


            // 토큰 값 저장 및 리턴
            access_token = element.getAsJsonObject().get("access_token").getAsString();
            refresh_token = element.getAsJsonObject().get("refresh_token").getAsString();

            System.out.println("access_token: " + access_token);
            System.out.println("refresh_token: " + refresh_token);

            br.close();
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return access_token;
    }

    @Override
    public Map<String, Object> getUserInfo(String access_token) {
        Map<String,Object> resultMap =new HashMap<>();
        String reqURL = "https://kapi.kakao.com/v2/user/me";
        try {
            URL url = new URL(reqURL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            //요청에 필요한 Header에 포함될 내용
            conn.setRequestProperty("Authorization", "Bearer " + access_token);

            int responseCode = conn.getResponseCode();
            log.info("responseCode : " + responseCode);

            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));

            String br_line = "";
            String result = "";

            while ((br_line = br.readLine()) != null) {
                result += br_line;
            }
            log.info("response:" + result);


            JsonParser parser = new JsonParser();
            JsonElement element = parser.parse(result);
            log.warn("element:: " + element);
            JsonObject properties = element.getAsJsonObject().get("properties").getAsJsonObject();
            JsonObject kakao_account = element.getAsJsonObject().get("kakao_account").getAsJsonObject();
            log.warn("id:: "+element.getAsJsonObject().get("id").getAsString());
            String id = element.getAsJsonObject().get("id").getAsString();
            String nickname = properties.getAsJsonObject().get("nickname").getAsString();
            String email = kakao_account.getAsJsonObject().get("email").getAsString();
            // 프로필 이미지 정보 반환
            String profileImage = properties.getAsJsonObject().get("profile_image").getAsString();

            log.warn("email:: " + email);
            resultMap.put("nickname", nickname);
            resultMap.put("id", id);
            resultMap.put("email", email);
            // Map에 프로필 이미지 정보를 추가합니다.
            resultMap.put("profile_image", profileImage);

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return resultMap;
    }




}