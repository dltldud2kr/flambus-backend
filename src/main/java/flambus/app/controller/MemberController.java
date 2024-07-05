package flambus.app.controller;


import flambus.app._enum.ApiResponseCode;
import flambus.app._enum.CustomExceptionCode;
import flambus.app.dto.ResultDTO;
import flambus.app.dto.email.EmailCheckDto;
import flambus.app.dto.member.*;
import flambus.app.repository.MemberRepository;
import flambus.app.service.EmailService;
import flambus.app.entity.Member;
import flambus.app.exception.CustomException;
import flambus.app.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Optional;


@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
@Tag(name = "회원 관련 API", description = "회원 도메인 관련 API 입니다.")
@CrossOrigin(origins = "*", exposedHeaders = {"Content-Disposition"}, allowedHeaders = "*")
public class MemberController {
    private final MemberService memberService;
    private final EmailService emailService;
    private final MemberRepository memberRepository;



    @Operation(summary = "로그인 요청", description = "" +
            "회원 로그인을 요청하고 토큰을 발급합니다." +
            "\n### HTTP STATUS 에 따른 요청 결과" +
            "\n- 200: 서버요청 정상 성공" +
            "\n- 403: 회원정보 인증 실패" +
            "\n- 500: 서버에서 요청 처리중 문제가 발생했습니다." +
            "\n### Result Code 에 따른 요청 결과" +
            "\n- SUCCESS: 로그인 성공 및 정상 토큰 발급" +
            "\n- NOT_FOUND_EMAIL: 요청한 이메일 가입자가 존재하지 않음")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그인 성공"),
    })
    @PostMapping("/auth/login")
    public ResultDTO<Map<String, Object>> login(@RequestBody LoginRequestDto loginRequest) {
        try {
            // .
            String email = loginRequest.getEmail();
            String password = loginRequest.getPassword();

            Map<String, Object> response = memberService.login(email, password);



            return ResultDTO.of(true, ApiResponseCode.SUCCESS.getCode(), "로그인 성공", response);
        } catch (CustomException e) {
            return ResultDTO.of(false, e.getCustomErrorCode().getStatusCode(), e.getDetailMessage(), null);
        }
    }

    /**
     * 카카오 로그인
     * id = email
     * pw = 카카오 고유번호
     * @param code
     * @param request
     * @return
     */
    @GetMapping("/auth/kakao/callback")
    public @ResponseBody KakaoJoinRequestDto<Object> kakaoCallback(String code, HttpServletRequest request) {
        System.out.println("code: " + code);

        // 접속토큰 get
        String kakaoToken = memberService.getReturnAccessToken(code, request);

        // 접속자 정보 get
        // id, connected_at , prop
        Map<String, Object> result = memberService.getUserInfo(kakaoToken);
        log.info("result: " + result);
        String kakaoIdx = (String) result.get("id");
        String nickname = (String) result.get("nickname");
        String email = (String) result.get("email");
        String profileImage = (String) result.get("profileImage");


        // 이메일값으로 멤버가 존재하는지 확인.
        Optional<Member> member = memberRepository.findByEmail(email);
        if (member.isPresent()) {
            try {
                TokenDto tokenDto = memberService.kakaoLogin(email, kakaoIdx);

                KakaoJoinRequestDto<Object> response = KakaoJoinRequestDto.of(true, "기존회원",
                        ApiResponseCode.SUCCESS.getCode(), "로그인 성공했음.", tokenDto);
                response.setUserInfo(result);  // 사용자 정보를 설정합니다.
                return response;
            } catch (CustomException e) {
                KakaoJoinRequestDto<Object> response = KakaoJoinRequestDto.of(false, "에러",
                        e.getCustomErrorCode().getStatusCode(), e.getDetailMessage(), null);
                response.setUserInfo(result);  // 사용자 정보를 설정합니다.
                return response;
            }
        } else {
            TokenDto tokenDto = memberService.join(email, kakaoIdx, nickname);
            KakaoJoinRequestDto<Object> response = KakaoJoinRequestDto.of(true, "신규회원",
                    ApiResponseCode.CREATED.getCode(), "회원가입이 완료되었습니다.", tokenDto);
            response.setUserInfo(result);  // 사용자 정보를 설정합니다.
            return response;
        }
    }

    @Operation(summary = "Access Token 발급 요청", description = "" +
            "RefreshToken으로 Access Token 발급을 요청합니다." +
            "\n### 토큰 별 유효기간" +
            "\n- AccessToken: 2시간" +
            "\n- RefreshToken: 3일" +
            "\n### HTTP STATUS 에 따른 조회 결과" +
            "\n- 200: 서버요청 정상 성공" +
            "\n- 401: 만료된 토큰이거나, 잘못된 토큰" +
            "\n- 500: 서버에서 요청 처리중 문제가 발생"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "토큰 발급 성공"),
    })
    @PostMapping("/auth/token")
    public ResultDTO<TokenDto> getAccessToken(@RequestBody long memberIdx) {
        try {
            return ResultDTO.of(true, ApiResponseCode.SUCCESS.getCode(), "토큰이 갱신 되었습니다.", memberService.createToken(memberIdx));
        } catch (CustomException e) {
            memberService.createToken(memberIdx);
            return ResultDTO.of(false, e.getCustomErrorCode().getStatusCode(), e.getDetailMessage(), null);
        }
    }
    @Operation(summary = "가입된 이메일 여부 확인", description = "" +
            "가입된 이메일 여부를 확인합니다." +
            "\n### HTTP STATUS 에 따른 조회 결과" +
            "\n- 200: 조회 성공 " +
            "\n- 500: 서버에서 요청 처리중 문제가 발생" +
            "\n### Result Code 에 따른 요청 결과" +
            "\n- DUPLICATED: 동일한 이메일이 존재합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
    })
    @GetMapping("/auth/checkEmail")
    public ResultDTO isAleadyEmail(@RequestParam("email") String email) {
        try {
            Member member = memberService.getMember(email);
            return ResultDTO.of(true, ApiResponseCode.SUCCESS.getCode(), "가입여부 조회 성공 (data : false(가입))", member == null);
        } catch (CustomException e) {
            return ResultDTO.of(false, e.getCustomErrorCode().getStatusCode(), e.getDetailMessage(), null);
        }
    }

    @Operation(summary = "회원가입 요청", description = "" +
            "임시 회원가입을 요청합니다." +
            "회원가입 화면 디자인 완료시 예외처리 및 추가 개발 예정입니다." +
            "테스트 용도로 확인만 해주세요." +
            "\n### HTTP STATUS 에 따른 조회 결과" +
            "\n- 201: 회원가입 성공 " +
            "\n- 500: 서버에서 요청 처리중 문제가 발생" +
            "\n### Result Code 에 따른 요청 결과" +
            "\n- DUPLICATED: 동일한 이메일이 존재합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "회원가입 성공"),
    })
    @PostMapping("/auth/join")
    public ResultDTO join(@RequestBody JoinRequestDto joinRequestDto) {
        try {
            TokenDto result = memberService.join(joinRequestDto);
            return ResultDTO.of(true, ApiResponseCode.CREATED.getCode(), "회원가입이 완료되었습니다.", result);
        } catch (CustomException e) {
            return ResultDTO.of(false, e.getCustomErrorCode().getStatusCode(), e.getDetailMessage(), null);
        }
    }

    @Operation(summary = "사용자 정보 조회", description = "" +
            "사용자 정보를 요청합니다." +
            "\n### HTTP STATUS 에 따른 조회 결과" +
            "\n- 200: 서버요청 정상 성공 " +
            "\n- 500: 서버에서 요청 처리중 문제가 발생" +
            "\n### Result Code 에 따른 요청 결과" +
            "\n- ")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "서버 요청 성공"),
    })

    @GetMapping("/member")
    public ResultDTO<MemberDto> member(@RequestParam("idx") long memberIdx) {
        Member member = memberService.getMember(memberIdx);

        MemberDto memberDto = MemberDto.builder()
                .idx(member.getIdx())
                .acornsCount(member.getAcornsCount())
                .canLimitCount(member.getCanLimitCount())
                .email(member.getEmail())
                .isAdmin(member.isAdmin())
                .follower(member.getFollower())
                .following(member.getFollowing())
                .introduce(member.getIntroduce())
                .platform(member.getPlatform())
                .subscriptionDate(member.getSubscriptionDate())
                .profileImageUrl(member.getProfileImageUrl())
                .serviceAgree(member.isServiceAgree())
                .serviceAgreeDate(member.getServiceAgreeDate())
                .termsAgree(member.isTermsAgree())
                .termsAgreeDate(member.getTermsAgreeDate())
                .useGpsAgree(member.isUseGpsAgree())
                .useGpsAgreeDate(member.getUseGpsAgreeDate())
                .build();

        return ResultDTO.of(member != null, ApiResponseCode.SUCCESS.getCode(), member != null ? "성공" : "해당 사용자를 찾을 수 없습니다.", memberDto);
    }


    @Operation(summary = "가입된 모든 사용자 정보 조회", description = "" +
            "사용자 정보를 요청합니다." +
            "\n### HTTP STATUS 에 따른 조회 결과" +
            "\n- 200: 서버요청 정상 성공 " +
            "\n- 500: 서버에서 요청 처리중 문제가 발생" +
            "\n### Result Code 에 따른 요청 결과" +
            "\n- ")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "서버 요청 성공"),
    })

    @GetMapping("/member/all")
    public List<MemberDto> allMember() {
        List<MemberDto> members = memberService.getAllMembers();
        System.out.println(members);
        return members;
    }


    @Operation(summary = "사용자 정보 수정", description = "" +
            "사용자 정보를 수정합니다." +
            "\n### HTTP STATUS 에 따른 조회 결과" +
            "\n- 200: 서버요청 정상 성공 " +
            "\n- 500: 서버에서 요청 처리중 문제가 발생" +
            "\n### Result Code 에 따른 요청 결과" +
            "\n- ")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "서버 요청 성공"),
    })
    @PatchMapping("/member")
    public ResultDTO modify() {
        return null;
    }


    @Operation(summary = "회원 탈퇴 요청", description = "" +
            "사용자 회원 탈퇴 요청." +
            "\n### HTTP STATUS 에 따른 조회 결과" +
            "\n- 200: 서버요청 정상 성공 " +
            "\n- 500: 서버에서 요청 처리중 문제가 발생" +
            "\n### Result Code 에 따른 요청 결과" +
            "\n- ")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "서버 요청 성공"),
    })

    @DeleteMapping("/member")
    public ResultDTO delete() {
        return null;
    }


    @Operation(summary = "이메일 인증번호 전송 ", description = "" +
            "이메일 인증번호 전송" +
            "\n### HTTP STATUS 에 따른 조회 결과" +
            "\n- 200: 서버요청 정상 성공 " +
            "\n- 500: 서버에서 요청 처리중 문제가 발생" +
            "\n### Result Code 에 따른 요청 결과" +
            "\n- ")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "서버 요청 성공"),
    })

    @PostMapping("/emailSend")
    public ResultDTO<Object> emailSend(@RequestBody EmailDto dto) throws Exception {
        try {
            boolean result = memberService.isMember(dto.getEmail());

            //회원이 있을 시 예외처리
            if (result){
                throw new CustomException(CustomExceptionCode.DUPLICATED_MEMBER);
            }
            emailService.sendEmailVerification(dto.getEmail());
            return ResultDTO.of(true, ApiResponseCode.SUCCESS.getCode(), "인증번호가 정상적으로 발송되었습니다.", null);
        } catch (CustomException e) {
            return ResultDTO.of(false, e.getCustomErrorCode().getStatusCode(), "메일 발송 중 문제가 발생했습니다.", null);
        }
    }


    @Operation(summary = "회원 가입 시 이메일 인증 처리", description = "" +
            "회원 가입 시 이메일 인증 처리." +
            "\n### HTTP STATUS 에 따른 조회 결과" +
            "\n- 200: 서버요청 정상 성공 " +
            "\n- 500: 서버에서 요청 처리중 문제가 발생" +
            "\n### Result Code 에 따른 요청 결과" +
            "\n- ")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "서버 요청 성공"),
    })

    // 인증한 이메일의 이메일인증여부를 변경
    @PostMapping("/email/auth")
    public ResultDTO<Object> emailCheck(@RequestBody EmailCheckDto dto) {
        try {
            boolean result = memberService.isMember(dto.getEmail());
            if (result == true){
                throw new CustomException(CustomExceptionCode.DUPLICATED_MEMBER);
            }
            memberService.emailCheck(dto.getEmail(),dto.getVerifcode());
            return ResultDTO.of(true, ApiResponseCode.SUCCESS.getCode(), "이메일 인증이 정상적으로 되었습니다.", dto.getEmail());
        } catch (CustomException e) {
            return ResultDTO.of(false, e.getCustomErrorCode().getStatusCode(), e.getDetailMessage(), null);
        }
    }

    @Operation(summary = "비밀번호 찾기 시 이메일 인증 처리", description = "" +
            "비밀번호 찾기 시 이메일 인증 처리." +
            "\n### HTTP STATUS 에 따른 조회 결과" +
            "\n- 200: 서버요청 정상 성공 " +
            "\n- 500: 서버에서 요청 처리중 문제가 발생" +
            "\n### Result Code 에 따른 요청 결과" +
            "\n- ")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "서버 요청 성공"),
    })
    @PostMapping("/pwFind/email/auth")
    public ResultDTO<Object> findPwEmailCheck(@RequestBody EmailCheckDto dto){

        try {
            boolean result = memberService.isMember(dto.getEmail());
            if (result == false){
                throw new CustomException(CustomExceptionCode.NOT_FOUND_USER);
            }
            memberService.emailCheck(dto.getEmail(),dto.getVerifcode());
            return ResultDTO.of(true, ApiResponseCode.SUCCESS.getCode(), "이메일 인증이 정상적으로 되었습니다.", null);
        } catch (CustomException e) {
            return ResultDTO.of(false, e.getCustomErrorCode().getStatusCode(), e.getDetailMessage(), null);
        }
    }
    @Operation(summary = "비밀번호 변경 ", description = "" +
            " 비밀번호 변경." +
            "\n### HTTP STATUS 에 따른 조회 결과" +
            "\n- 200: 서버요청 정상 성공 " +
            "\n- 500: 서버에서 요청 처리중 문제가 발생" +
            "\n### Result Code 에 따른 요청 결과" +
            "\n- ")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "서버 요청 성공"),
    })
    @PostMapping("/pwChange")
    public ResultDTO<Object> pwChange(@RequestBody PwChangeDto dto){

        try {
             memberService.changePw(dto.getEmail(),dto.getPassword());
            return ResultDTO.of(true, ApiResponseCode.SUCCESS.getCode(), "이메일 인증이 정상적으로 되었습니다.", null);
        } catch (CustomException e) {
            return ResultDTO.of(false, e.getCustomErrorCode().getStatusCode(), e.getDetailMessage(), null);
        }
    }

    @Operation(summary = "비밀번호 찾기 시 이메일 인증번호 전송", description = "" +
            "비밀번호 찾기 시 이메일 인증번호 전송." +
            "\n### HTTP STATUS 에 따른 조회 결과" +
            "\n- 200: 서버요청 정상 성공 " +
            "\n- 500: 서버에서 요청 처리중 문제가 발생" +
            "\n### Result Code 에 따른 요청 결과" +
            "\n- ")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "서버 요청 성공"),
    })
    @PostMapping("/pwFind/emailSend")
    public ResultDTO<Object> pwFindEmailSend(@RequestBody PasswordFoundDto dto) throws Exception{

        try {
            boolean result = memberService.isMember(dto.getEmail());

            // 회원이 없을 시 예외처리
            if (!result){
                throw new CustomException(CustomExceptionCode.NOT_FOUND_USER);
            }
            emailService.sendEmailVerification(dto.getEmail());
            return ResultDTO.of(true, ApiResponseCode.SUCCESS.getCode(), "인증번호가 메일로 발송되었습니다.", null);
        } catch (CustomException e) {
            return ResultDTO.of(false, e.getCustomErrorCode().getStatusCode(), "메일 발송 중 문제가 발생했습니다.", null);
        }
    }

    @Operation(summary = "개인정보 수집 마케팅 광고 활용여부 동의", description = "" +
            "개인정보 수집 마케팅 광고 활용여부 동의" +
            "\n### HTTP STATUS 에 따른 조회 결과" +
            "\n- 200: 서버요청 정상 성공 " +
            "\n- 500: 서버에서 요청 처리중 문제가 발생" +
            "\n### Result Code 에 따른 요청 결과" +
            "\n- ")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "서버 요청 성공"),
    })

    @PostMapping("/agreement/privacy")
    public ResultDTO<Object> marketingAgreement(@RequestParam String memberIdx) throws Exception {
        try {
//            emailService.sendEmailVerification(email);
            return ResultDTO.of(true, ApiResponseCode.SUCCESS.getCode(), "개인정보 수집, 마케팅 광고 동의가 완료되었습니다.", null);
        } catch (CustomException e) {
            return ResultDTO.of(false, e.getCustomErrorCode().getStatusCode(), "오류 발생", null);
        }
    }


    @Operation(summary = "GPS 수집 약관 동의", description = "" +
            "GPS 수집 약관 동의" +
            "\n### HTTP STATUS 에 따른 조회 결과" +
            "\n- 200: 서버요청 정상 성공 " +
            "\n- 500: 서버에서 요청 처리중 문제가 발생" +
            "\n### Result Code 에 따른 요청 결과" +
            "\n- ")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "서버 요청 성공"),
    })

    @PostMapping("/agreement/gps")
    public ResultDTO<Object> gpsAgreement(@RequestParam String memberIdx) throws Exception {
        try {
//            emailService.sendEmailVerification(email);
            return ResultDTO.of(true, ApiResponseCode.SUCCESS.getCode(), "GPS 수집 및 사용관련 동의가 완료되었습니다.", null);
        } catch (CustomException e) {
            return ResultDTO.of(false, e.getCustomErrorCode().getStatusCode(), "오류 발생", null);
        }
    }



}



//        try{
//            memberService.emailCheck(email);
//            return ResultDTO.of(true, ApiResponseCode.SUCCESS.getCode(), "이메일 인증이 정상적으로 되었습니다.",null);
//        } catch (CustomException e) {
//            return ResultDTO.of(false,e.getCustomErrorCode().getStatusCode(),e.getDetailMessage(),null);
//
//
//}