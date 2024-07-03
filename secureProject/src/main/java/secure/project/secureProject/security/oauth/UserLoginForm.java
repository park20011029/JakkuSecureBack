package secure.project.secureProject.security.oauth;

import secure.project.secureProject.domain.enums.UserRole;

public interface UserLoginForm {
    public Long getId();
    public UserRole getRole();
}
