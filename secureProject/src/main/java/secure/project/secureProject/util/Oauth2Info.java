package secure.project.secureProject.util;


import lombok.Getter;

@Getter
public class Oauth2Info {
    private String socialId;
    private String socialName;

    public Oauth2Info(String socialId, String socialName) {
        this.socialId = socialId;
        this.socialName = socialName;
    }

}