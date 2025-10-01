package channeling.be.global.auth.presentation;


public class OauthRes {
    public record ReIssueToken(
        String reIssuedAccessToken
    ) {
    }

}
