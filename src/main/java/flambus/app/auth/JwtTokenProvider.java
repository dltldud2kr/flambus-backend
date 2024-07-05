package flambus.app.auth;


import flambus.app._enum.CustomExceptionCode;
import flambus.app.dto.member.TokenDto;
import flambus.app.entity.Member;
import flambus.app.exception.CustomException;
import flambus.app.repository.MemberRepository;
import io.jsonwebtoken.*;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

/**
 * JwtTokenProvider는 토큰 생성, 토큰 복호화 및 정보 추출, 토큰 유효성 검증의 기능이 구현된 클래스.
 * @author rimsong
 * application.properties에 jwt.secret: 값을 넣어 설정 추가해준 뒤 사용합니다.
 * jwt.secret는 토큰의 암호화, 복호화를 위한 secret key로서 이후 HS256알고리즘을 사용하기 위해, 256비트보다 커야합니다.
 * 알파벳이 한 단어당 8bit니, 32글자 이상이면 됩니다! 너무 짧으면 에러가 뜹니다.
 */

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {
    @Value("${spring.jwt.secret}")
    private String secretKey;

    private final MemberRepository memberRepository;


    // 유저 정보를 가지고 AccessToken, RefreshToken 을 생성하는 메서드
    public TokenDto generateToken(Authentication authentication) {
        // 권한 가져오기
        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        long now = (new Date()).getTime();
        // Access Token 생성
        // 2시간만 유지합니다.
//        Date accessTokenExpiresIn = new Date(now + (5 * 60 * 1000)); //5분
        Date accessTokenExpiresIn = new Date(now + (2 * 60 * 60 * 1000));
        String accessToken = Jwts.builder()
                .setSubject(authentication.getName())
                .claim("auth", authorities)
                .setExpiration(accessTokenExpiresIn)
                .signWith(SignatureAlgorithm.HS256, Base64.getEncoder().encodeToString(secretKey.getBytes())) // 알고리즘, 시크릿 키
                .compact();

        // Refresh Token 생성 후 해당 사용자의 DB에 저장한다.
        String refreshToken = Jwts.builder()
                //86400000 1일
                .setExpiration(new Date(now + (86400000 * 3))) // 3일간만 유지합니다.
                .signWith(SignatureAlgorithm.HS256, Base64.getEncoder().encodeToString(secretKey.getBytes())) // 알고리즘, 시크릿 키
                .compact();


        //해당 사용자 정보의 리플래쉬 토큰을 업데이트 해줌.
        Optional<Member> byMember = memberRepository.findByEmail(authentication.getName());
        log.info(authentication.getName());
        memberRepository.updateRefreshToken(byMember.get().getEmail(),refreshToken);

        //accesstoken 받았어, -> 이동
        return TokenDto.builder()
                .accessToken(accessToken)
                .build();
    }

    // JWT 토큰을 복호화하여 토큰에 들어있는 정보를 꺼내는 메서드
    public Authentication getAuthentication(String accessToken) {
        // 토큰 복호화
        Claims claims = parseClaims(accessToken);


        if (claims.get("auth") == null) {
            throw new RuntimeException("권한 정보가 없는 토큰입니다.");
        }

        //플램버스는 무조건 유저 디비에서 따로 권한을 관리합니다.
        List<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));

        // UserDetails 객체를 만들어서 Authentication 리턴
        UserDetails principal = new User(claims.getSubject(), "", authorities);
        return new UsernamePasswordAuthenticationToken(principal, "", authorities);
    }

    // 토큰 정보를 검증하는 메서드
    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(secretKey.getBytes()).parseClaimsJws(token);
            return true;
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            log.info("올바른 형식의 토큰이 아님.", e);
//            throw new CustomException(CustomExceptionCode.INVALID_JWT);
        } catch (ExpiredJwtException e) {
            log.info("토큰 만료됨.", e);
//            throw new CustomException(CustomExceptionCode.EXPIRED_JWT);
        } catch (UnsupportedJwtException e) {
            log.info("Unsupported JWT Token", e);
        } catch (IllegalArgumentException e) {
            log.info("JWT claims string is empty.", e);
        }
        return false;
    }


    // Access Token을 파싱하고, 그 안에 포함된 클레임(Claims) 정보를 추출
    private Claims parseClaims(String accessToken) {
        try {
//            return Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(accessToken).getBody();
            return Jwts.parser().setSigningKey(secretKey.getBytes()).parseClaimsJws(accessToken).getBody();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        }
    }
}
