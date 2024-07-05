package flambus.app.entity;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.*;

import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name= "member")
@Data
public class Member implements UserDetails {

    @Id
    @GeneratedValue
    private Long idx;
    @Column(nullable = false)
    private String email;
    @Column(nullable = false)
    private String nickname;
    @Column(nullable = false)
    private String password;
    @Column(nullable = false)
    private boolean isAdmin; //0:사용자 1:관리자
    @Column(nullable = false)
    private int platform; //연동 플랫폼 네이버,카카오...등등,flambus
    private String introduce; //소개
    @Column
    private String refreshToken; //리프레쉬 토큰
    @Column(nullable = false)
    private boolean termsAgree; //개인 약관 동의 여부 0:no 1:ye
    @Column(nullable = false)
    private LocalDateTime termsAgreeDate; //약관 동의날짜
    @Column(nullable = false)
    private boolean serviceAgree; //서비스 이용 약관 동의 여부 0:no 1:yes
    @Column(nullable = false)
    private LocalDateTime serviceAgreeDate; //약관 동의날짜
    @Column(nullable = false)
    private boolean useGpsAgree; //GPS 이용 약관
    @Column(nullable = false)
    private LocalDateTime useGpsAgreeDate; //약관 동의날짜
    @Column(nullable = false)
    private long follower; //팔로워
    @Column(nullable = false)
    private long following; //팔로잉

    @Column(nullable = false)
    private long acornsCount; //도토리 개수
    @Column(nullable = false)
    private long canLimitCount; //리뷰 작성 가능 수(탐험일지 작성수) 자정마다 2로
    @Column(nullable = false)
    private String profileImageUrl; ///프로필 링크
    @Column(nullable = false)
    private LocalDateTime subscriptionDate; //가입일
    @Column(nullable = false)
    private boolean withdrawal; //탈퇴여부 0 , 1

    private LocalDateTime withdrawalDate; //탈퇴일자
    private boolean emailAuth;


    @ElementCollection(fetch = FetchType.EAGER)
    @Builder.Default
    private List<String> roles = new ArrayList<>();
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.roles.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }
    @Override
    public String getUsername() {
        return email;
    }

    public String getEmail() {return email;}


    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
