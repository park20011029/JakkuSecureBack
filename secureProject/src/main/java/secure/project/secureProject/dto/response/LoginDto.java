package secure.project.secureProject.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoginDto {
    private String username;
    private String access_token;
    private String refresh_token;
}
