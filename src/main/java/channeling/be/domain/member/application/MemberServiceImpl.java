package channeling.be.domain.member.application;

import channeling.be.domain.member.domain.Member;
import channeling.be.domain.member.domain.repository.MemberRepository;
import channeling.be.domain.member.presentation.MemberConverter;
import channeling.be.domain.member.presentation.MemberResDTO;
import channeling.be.domain.memberAgree.domain.MemberAgree;
import channeling.be.domain.memberAgree.domain.repository.MemberAgreeRepository;
import channeling.be.global.auth.application.MemberOauth2UserService;
import channeling.be.global.auth.application.MemberOauth2UserService.MemberResult;
import channeling.be.infrastructure.aws.S3Service;
import channeling.be.response.code.status.ErrorStatus;
import channeling.be.response.exception.handler.MemberHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static channeling.be.domain.member.presentation.MemberReqDTO.*;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class MemberServiceImpl implements MemberService {
	private final MemberRepository memberRepository;
	private final S3Service s3Service;
	private final MemberAgreeRepository memberAgreeRepository;
	@Transactional
	@Override
	public MemberResDTO.updateSnsRes updateSns(Member loginMember,updateSnsReq updateSnsReq) {
		Member member = memberRepository.findById(loginMember.getId())
			.orElseThrow(() -> new MemberHandler(ErrorStatus._MEMBER_NOT_FOUND));

		member.updateSnsLinks(
			updateSnsReq.instagramLink(),
			updateSnsReq.tiktokLink(),
			updateSnsReq.facebookLink(),
			updateSnsReq.twitterLink()
		);

		return MemberConverter.toUpdatedSns(
			member.getInstagramLink(),
			member.getTiktokLink(),
			member.getFacebookLink(),
			member.getTwitterLink()
		);
	}

	@Transactional
	@Override
	public MemberResDTO.updateProfileImageRes updateProfileImage(Member loginMember, ProfileImageUpdateReq updateProfileImageReq) {
		// 멤버 조회 -> 실제 DB 조회로 영속화
		Member member = memberRepository.findByGoogleId(loginMember.getGoogleId()).orElseThrow(() -> new MemberHandler(ErrorStatus._MEMBER_NOT_FOUND));
		// 새로운 사진 s3에 업로드 후, url 얻기
		String uploadedUrl = s3Service.uploadImage(updateProfileImageReq.image());
		// 엔티티 업데이트 -> 더티 체킹
		member.profileImage(uploadedUrl);
		return MemberConverter.toUpdatedProfileImage(uploadedUrl);
	}

	@Override
	@Transactional
    public MemberResult findOrCreateMember(String googleId, String email, String nickname, String profileImage) {
		return memberRepository.findByGoogleId(googleId)
				.map(member -> new MemberResult(member, false)) // 기존 회원
				.orElseGet(() -> {
					Member newMember = memberRepository.save(
							Member.builder()
									.googleId(googleId)
									.googleEmail(email)
									.nickname(nickname)
									.profileImage(profileImage)
									.build()
					);
					return  new MemberResult(newMember, true); // 신규 회원
				});
	}


	@Override
	public MemberResDTO.getMemberInfo getMemberInfo(Member loginMember) {
		Member member = memberRepository.findById(loginMember.getId())
				.orElseThrow(() -> new MemberHandler(ErrorStatus._MEMBER_NOT_FOUND));
		Optional<MemberAgree> byMemberId = memberAgreeRepository.findByMemberId(loginMember.getId());
		if (byMemberId.isPresent()) {
			MemberAgree memberAgree = byMemberId.get();
			return MemberConverter.toGetMemberInfo(member, memberAgree.getMarketingEmailAgree(), memberAgree.getDayContentEmailAgree());
		} else {
			return MemberConverter.toGetMemberInfo(member, false,false);
		}
	}

}
