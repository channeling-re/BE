package channeling.be.domain.member.application;

import channeling.be.global.auth.application.MemberOauth2UserService;
import channeling.be.domain.member.domain.Member;
import channeling.be.domain.member.presentation.MemberResDTO;
import static channeling.be.domain.member.presentation.MemberReqDTO.*;

public interface MemberService {

    MemberOauth2UserService.MemberResult findOrCreateMember(String googleId, String email, String nickname, String profileImage);
    MemberResDTO.updateSnsRes updateSns(Member loginMember, updateSnsReq updateSnsReq);
    MemberResDTO.updateProfileImageRes updateProfileImage(Member loginMember, ProfileImageUpdateReq updateProfileImageReq);
    MemberResDTO.getMemberInfo getMemberInfo(Member loginMember);

}
