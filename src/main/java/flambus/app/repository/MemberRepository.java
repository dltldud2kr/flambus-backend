package flambus.app.repository;//package flambus.flambus_v10.repository;


import flambus.app.entity.Member;
        import org.springframework.data.jpa.repository.JpaRepository;
        import org.springframework.data.jpa.repository.Modifying;
        import org.springframework.data.jpa.repository.Query;
        import org.springframework.data.repository.query.Param;
        import org.springframework.stereotype.Repository;
        import org.springframework.transaction.annotation.Transactional;

        import java.util.Optional;



@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByEmail(String email);

    @Modifying
    @Query(value = "UPDATE member SET refresh_token = :refreshToken WHERE email = :email", nativeQuery = true)
    void updateRefreshToken(@Param("email") String email, @Param("refreshToken") String refreshToken);


}