package secure.project.secureProject.security.oauth;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import secure.project.secureProject.exception.ApiException;
import secure.project.secureProject.exception.ErrorDefine;
import secure.project.secureProject.repository.UserRepository;

import java.util.ArrayList;
import java.util.Collection;

@Service
@RequiredArgsConstructor
public class CustomUserDetailService implements UserDetailsService {

    private final UserRepository userRepository;


    @Override
    public UserDetails loadUserByUsername(String username) {
        Collection<SimpleGrantedAuthority> roles = new ArrayList<>();
        roles.add(new SimpleGrantedAuthority("ROLE_USER"));

        UserLoginForm user = userRepository.findByIdAndRefreshToken(Long.parseLong(username))
                .orElseThrow(() -> new ApiException(ErrorDefine.ACCESS_DENIED));

        return CustomUserDetail.create(user);
    }

    public CustomUserDetail loadUserByUsernameAndUserRole(String username, String role) {
        Collection<SimpleGrantedAuthority> roles = new ArrayList<>();
        roles.add(new SimpleGrantedAuthority("ROLE_" + role));

        UserLoginForm user;
        switch (role) {
            case "CUSTOMER":
                user = userRepository.findByIdAndRefreshToken(Long.parseLong(username))
                        .orElseThrow(() -> new ApiException(ErrorDefine.ACCESS_DENIED));
                break;
            default:
                throw new ApiException(ErrorDefine.ACCESS_DENIED);
        }

        return CustomUserDetail.create(user);
    }
}

