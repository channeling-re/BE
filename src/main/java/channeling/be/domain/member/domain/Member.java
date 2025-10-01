package channeling.be.domain.member.domain;

import channeling.be.domain.common.BaseEntity;
import channeling.be.response.code.status.ErrorStatus;
import channeling.be.response.exception.handler.MemberHandler;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Member extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 30, nullable = false)
    private String nickname; // 닉네임

    @Column(nullable = false, length = 100, unique = true)
    private String googleEmail; // 구글 이메일

    @Column(length = 255)
    private String profileImage; // 프로필 이미지

    @Column(length = 100)
    private String instagramLink; // 인스타 링크

    @Column(length = 100)
    private String tiktokLink; // 틱톡 링크

    @Column(length = 100)
    private String facebookLink; // 페이스북 링크

    @Column(length = 100)
    private String twitterLink; // 트위터 링크

    @Column(length = 50)
    private String googleId; // 구글 아이디 (로그인 구별을 위한..)


    private enum SnsPatterns {
        INSTAGRAM("^https?://(www\\.)?instagram\\.com/.*$"),
        TIKTOK("^https?://(www\\.)?tiktok\\.com/.*$"),
        FACEBOOK("^https?://(www\\.)?facebook\\.com/.*$"),
        TWITTER("^https?://(www\\.)?twitter\\.com/.*$");

        private final String regex;

        SnsPatterns(String regex) {
            this.regex = regex;
        }

        public String getRegex() {
            return regex;
        }
    }
    public void updateSnsLinks(String instagramLink, String tiktokLink, String facebookLink, String twitterLink) {
        this.instagramLink = validateSnsLink(instagramLink,SnsPatterns.INSTAGRAM.getRegex());
        this.tiktokLink = validateSnsLink(tiktokLink, SnsPatterns.TIKTOK.getRegex());
        this.facebookLink = validateSnsLink(facebookLink, SnsPatterns.FACEBOOK.getRegex());
        this.twitterLink = validateSnsLink(twitterLink, SnsPatterns.TWITTER.getRegex());
    }

    private String validateSnsLink(String link, String regex) {
        if (link == null || link.isBlank()) {
            return null;
        }
        String trimmed = link.trim();
        if (!trimmed.matches(regex)) {
			throw new MemberHandler(ErrorStatus._SNS_LINK_INVALID);
        }
        return trimmed;
    }

    public void profileImage(String profileImage) {
        this.profileImage = profileImage;
    }
}
