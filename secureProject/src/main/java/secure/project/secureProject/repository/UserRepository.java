package secure.project.secureProject.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import secure.project.secureProject.domain.User;
import secure.project.secureProject.domain.enums.LoginProvider;
import secure.project.secureProject.security.oauth.UserLoginForm;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByNickname(String nickname);
    Optional<User> findByLoginId(String loginId);

    User findBySocialIdAndLoginProvider(String socialId, LoginProvider loginProvider);

    @Query("SELECT c.id AS id, c.userRole AS role FROM User c WHERE c.id = :customerId AND c.isLogin = true AND c.refreshToken is not null")
    Optional<UserLoginForm> findByIdAndRefreshToken(@Param("customerId") Long customerId);

    @Query("SELECT c.id AS id, c.userRole AS role FROM User c WHERE c.id = :customerId AND c.isLogin = true AND c.refreshToken = :refreshToken")
    Optional<UserLoginForm> findByIdAndRefreshToken(@Param("customerId") Long customerId, @Param("refreshToken") String refreshToken);

    User findByIdAndRefreshTokenIsNotNullAndIsLoginIsTrue(Long customerId);
}
