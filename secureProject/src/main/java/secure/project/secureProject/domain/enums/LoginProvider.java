package secure.project.secureProject.domain.enums;

public enum LoginProvider {
    KAKAO("KAKAO"),
    NAVER("NAVER"),
    GOOGLE("GOOGLE");

    private final String loginProvider;

    LoginProvider(String loginProvider) {
        this.loginProvider = loginProvider;
    }

    public String getLoginProvider() {
        return loginProvider;
    }
}

