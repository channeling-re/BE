package channeling.be.domain.auth.presentation;


public class OauthRes {
    public record ReIssueToken(
        String reIssuedAccessToken
    ) {
    }

}
