package channeling.be.domain.member;


import channeling.be.domain.member.domain.Member;
import channeling.be.domain.member.domain.repository.MemberRepository;
import channeling.be.global.annotation.WithMockJwtUser;
import channeling.be.infrastructure.jwt.JwtUtil;
import channeling.be.support.IntegrationTestSupport;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import static channeling.be.domain.member.presentation.MemberReqDTO.*;
        import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
        import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * MemberController 통합 테스트
 *
 * @Transactional을 사용하여 각 테스트가 자동으로 롤백됩니다.
 * - DB 정리 코드 불필요
 * - 빠른 실행 속도
 * - 완벽한 테스트 격리
 * - Spring Boot 공식 권장 방식
 */
@Slf4j
@Transactional  // ⭐ 핵심: 모든 테스트가 자동으로 롤백됨
@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@DisplayName("MemberController 통합 테스트")
public class MemberControllerTest extends IntegrationTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private MemberRepository memberRepository;

    @Nested
    @DisplayName("GET /members - 회원 정보 조회")
    class GetMemberInfo {

        @Test
        @WithMockJwtUser
        @DisplayName("성공: 로그인한 회원의 정보를 정상적으로 조회한다")
        void getMemberInfo_Success(Member member) throws Exception {
            // given
            String accessToken = jwtUtil.createAccessToken(member);

            // when
            ResultActions result = mockMvc.perform(
                    get("/members")
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
            );

            // then
            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.isSuccess").value(true))
                    .andExpect(jsonPath("$.result.memberId").value(member.getId()))
                    .andExpect(jsonPath("$.result.nickname").value(member.getNickname()))
                    .andExpect(jsonPath("$.result.googleEmail").value(member.getGoogleEmail()))
                    .andExpect(jsonPath("$.result.profileImage").hasJsonPath())
                    .andExpect(jsonPath("$.result.instagramLink").hasJsonPath())
                    .andExpect(jsonPath("$.result.tiktokLink").hasJsonPath())
                    .andExpect(jsonPath("$.result.facebookLink").hasJsonPath())
                    .andExpect(jsonPath("$.result.twitterLink").hasJsonPath());
        }

        @Test
        @DisplayName("실패: 인증되지 않은 사용자가 회원 정보 조회를 시도하면 401 에러가 발생한다")
        void getMemberInfo_WithoutAuth_Fail() throws Exception {
            // when
            ResultActions result = mockMvc.perform(
                    get("/members")
                            .contentType(MediaType.APPLICATION_JSON)
            );

            // then
            result.andExpect(status().isUnauthorized())
                    .andDo(print());
        }
    }

    @Nested
    @DisplayName("PATCH /members/update-sns - SNS 링크 수정")
    class UpdateSns {

        @Test
        @WithMockJwtUser
        @DisplayName("성공: 모든 SNS 링크를 정상적으로 수정한다")
        void updateSns_AllLinks_Success(Member member) throws Exception {
            // given
            String accessToken = jwtUtil.createAccessToken(member);

            updateSnsReq request = new updateSnsReq(
                    "https://www.instagram.com/testuser",
                    "https://www.tiktok.com/@testuser",
                    "https://www.facebook.com/testuser",
                    "https://www.twitter.com/testuser"
            );
            String requestBody = objectMapper.writeValueAsString(request);

            // when
            ResultActions result = mockMvc.perform(
                    patch("/members/update-sns")
                            .header("Authorization", "Bearer " + accessToken)
                            .content(requestBody)
                            .contentType(MediaType.APPLICATION_JSON)
            );

            // then
            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.isSuccess").value(true))
                    .andExpect(jsonPath("$.result.instagramLink").value("https://www.instagram.com/testuser"))
                    .andExpect(jsonPath("$.result.tiktokLink").value("https://www.tiktok.com/@testuser"))
                    .andExpect(jsonPath("$.result.facebookLink").value("https://www.facebook.com/testuser"))
                    .andExpect(jsonPath("$.result.twitterLink").value("https://www.twitter.com/testuser"))
                    .andDo(print());

            // DB에서 변경 확인
            Member updatedMember = memberRepository.findById(member.getId())
                    .orElseThrow();
            assertThat(updatedMember.getInstagramLink()).isEqualTo("https://www.instagram.com/testuser");
            assertThat(updatedMember.getTiktokLink()).isEqualTo("https://www.tiktok.com/@testuser");
            assertThat(updatedMember.getFacebookLink()).isEqualTo("https://www.facebook.com/testuser");
            assertThat(updatedMember.getTwitterLink()).isEqualTo("https://www.twitter.com/testuser");
        }

        @Test
        @WithMockJwtUser
        @DisplayName("성공: 일부 SNS 링크만 수정한다")
        void updateSns_PartialLinks_Success(Member member) throws Exception {
            // given
            String accessToken = jwtUtil.createAccessToken(member);

            updateSnsReq request = new updateSnsReq(
                    "https://www.instagram.com/testuser",
                    null,  // TikTok은 업데이트하지 않음
                    "https://www.facebook.com/testuser",
                    null   // Twitter는 업데이트하지 않음
            );
            String requestBody = objectMapper.writeValueAsString(request);

            // when
            ResultActions result = mockMvc.perform(
                    patch("/members/update-sns")
                            .header("Authorization", "Bearer " + accessToken)
                            .content(requestBody)
                            .contentType(MediaType.APPLICATION_JSON)
            );

            // then
            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.isSuccess").value(true))
                    .andExpect(jsonPath("$.result.instagramLink").value("https://www.instagram.com/testuser"))
                    .andExpect(jsonPath("$.result.facebookLink").value("https://www.facebook.com/testuser"))
                    .andDo(print());

            // DB에서 변경 확인
            Member updatedMember = memberRepository.findById(member.getId())
                    .orElseThrow();
            assertThat(updatedMember.getInstagramLink()).isEqualTo("https://www.instagram.com/testuser");
            assertThat(updatedMember.getFacebookLink()).isEqualTo("https://www.facebook.com/testuser");
        }

        @Test
        @WithMockJwtUser
        @DisplayName("성공: 빈 문자열로 SNS 링크를 삭제한다")
        void updateSns_EmptyString_Success(Member member) throws Exception {
            // given
            String accessToken = jwtUtil.createAccessToken(member);

            // 먼저 링크 설정
            member.updateSnsLinks(
                    "https://www.instagram.com/old",
                    "https://www.tiktok.com/@old",
                    null,
                    null
            );
            memberRepository.save(member);

            // 빈 문자열로 삭제 요청
            updateSnsReq request = new updateSnsReq(
                    "",  // 빈 문자열
                    "",  // 빈 문자열
                    null,
                    null
            );
            String requestBody = objectMapper.writeValueAsString(request);

            // when
            ResultActions result = mockMvc.perform(
                    patch("/members/update-sns")
                            .header("Authorization", "Bearer " + accessToken)
                            .content(requestBody)
                            .contentType(MediaType.APPLICATION_JSON)
            );

            // then
            result.andExpect(status().isOk())
                    .andDo(print());

            // DB에서 null로 변경되었는지 확인
            Member updatedMember = memberRepository.findById(member.getId())
                    .orElseThrow();
            assertThat(updatedMember.getInstagramLink()).isNull();
            assertThat(updatedMember.getTiktokLink()).isNull();
        }

        @Test
        @WithMockJwtUser
        @DisplayName("실패: 잘못된 Instagram 링크 형식으로 수정 시도 시 400 에러가 발생한다")
        void updateSns_InvalidInstagramLink_Fail(Member member) throws Exception {
            // given
            String accessToken = jwtUtil.createAccessToken(member);

            updateSnsReq request = new updateSnsReq(
                    "invalid-instagram-link",  // 잘못된 형식
                    null,
                    null,
                    null
            );
            String requestBody = objectMapper.writeValueAsString(request);

            // when
            ResultActions result = mockMvc.perform(
                    patch("/members/update-sns")
                            .header("Authorization", "Bearer " + accessToken)
                            .content(requestBody)
                            .contentType(MediaType.APPLICATION_JSON)
            );

            // then
            result.andExpect(status().isBadRequest())
                    .andDo(print());
        }

        @Test
        @WithMockJwtUser
        @DisplayName("실패: 잘못된 TikTok 링크 형식으로 수정 시도 시 400 에러가 발생한다")
        void updateSns_InvalidTiktokLink_Fail(Member member) throws Exception {
            // given
            String accessToken = jwtUtil.createAccessToken(member);

            updateSnsReq request = new updateSnsReq(
                    null,
                    "https://youtube.com/invalid",  // TikTok이 아닌 다른 도메인
                    null,
                    null
            );
            String requestBody = objectMapper.writeValueAsString(request);

            // when
            ResultActions result = mockMvc.perform(
                    patch("/members/update-sns")
                            .header("Authorization", "Bearer " + accessToken)
                            .content(requestBody)
                            .contentType(MediaType.APPLICATION_JSON)
            );

            // then
            result.andExpect(status().isBadRequest())
                    .andDo(print());
        }

        @Test
        @WithMockJwtUser
        @DisplayName("실패: 잘못된 Facebook 링크 형식으로 수정 시도 시 400 에러가 발생한다")
        void updateSns_InvalidFacebookLink_Fail(Member member) throws Exception {
            // given
            String accessToken = jwtUtil.createAccessToken(member);

            updateSnsReq request = new updateSnsReq(
                    null,
                    null,
                    "not-a-url",  // 잘못된 형식
                    null
            );
            String requestBody = objectMapper.writeValueAsString(request);

            // when
            ResultActions result = mockMvc.perform(
                    patch("/members/update-sns")
                            .header("Authorization", "Bearer " + accessToken)
                            .content(requestBody)
                            .contentType(MediaType.APPLICATION_JSON)
            );

            // then
            result.andExpect(status().isBadRequest())
                    .andDo(print());
        }

        @Test
        @WithMockJwtUser
        @DisplayName("실패: 잘못된 Twitter 링크 형식으로 수정 시도 시 400 에러가 발생한다")
        void updateSns_InvalidTwitterLink_Fail(Member member) throws Exception {
            // given
            String accessToken = jwtUtil.createAccessToken(member);

            updateSnsReq request = new updateSnsReq(
                    null,
                    null,
                    null,
                    "ftp://invalid-protocol.com"  // 잘못된 프로토콜
            );
            String requestBody = objectMapper.writeValueAsString(request);

            // when
            ResultActions result = mockMvc.perform(
                    patch("/members/update-sns")
                            .header("Authorization", "Bearer " + accessToken)
                            .content(requestBody)
                            .contentType(MediaType.APPLICATION_JSON)
            );

            // then
            result.andExpect(status().isBadRequest())
                    .andDo(print());
        }

        @Test
        @DisplayName("실패: 인증되지 않은 사용자가 SNS 링크 수정을 시도하면 401 에러가 발생한다")
        void updateSns_WithoutAuth_Fail() throws Exception {
            // given
            updateSnsReq request = new updateSnsReq(
                    "https://www.instagram.com/testuser",
                    null,
                    null,
                    null
            );
            String requestBody = objectMapper.writeValueAsString(request);

            // when
            ResultActions result = mockMvc.perform(
                    patch("/members/update-sns")
                            .content(requestBody)
                            .contentType(MediaType.APPLICATION_JSON)
            );

            // then
            result.andExpect(status().isUnauthorized())
                    .andDo(print());
        }
    }

    @Nested
    @DisplayName("PATCH /members/profile-images - 프로필 이미지 수정")
    class UpdateProfileImage {

        @Test
        @WithMockJwtUser
        @DisplayName("성공: 프로필 이미지를 정상적으로 수정한다")
        void updateProfileImage_Success(Member member) throws Exception {
            // given
            String accessToken = jwtUtil.createAccessToken(member);

            // MockMultipartFile 생성
            MockMultipartFile imageFile = new MockMultipartFile(
                    "image",                          // 파라미터 이름
                    "profile.jpg",                    // 원본 파일명
                    "image/jpeg",                     // Content-Type
                    "test image content".getBytes()   // 파일 내용
            );

            // when
            ResultActions result = mockMvc.perform(
                    multipart("/members/profile-images")
                            .file(imageFile)
                            .header("Authorization", "Bearer " + accessToken)
                            .with(request -> {
                                request.setMethod("PATCH");  // PATCH 메서드로 변경
                                return request;
                            })
            );

            // then
            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.isSuccess").value(true))
                    .andExpect(jsonPath("$.result.updatedProfileImage").exists())
                    .andDo(print());

            // DB에서 변경 확인 (실제 S3 업로드는 모킹되어야 하므로 존재 여부만 확인)
            Member updatedMember = memberRepository.findById(member.getId())
                    .orElseThrow();
            assertThat(updatedMember.getProfileImage()).isNotNull();
        }

        @Test
        @WithMockJwtUser
        @DisplayName("실패: 이미지 파일 없이 수정 시도 시 400 에러가 발생한다")
        void updateProfileImage_WithoutImage_Fail(Member member) throws Exception {
            // given
            String accessToken = jwtUtil.createAccessToken(member);

            // when
            ResultActions result = mockMvc.perform(
                    multipart("/members/profile-images")
                            .header("Authorization", "Bearer " + accessToken)
                            .with(request -> {
                                request.setMethod("PATCH");
                                return request;
                            })
            );

            // then
            result.andExpect(status().isBadRequest())
                    .andDo(print());
        }

        @Test
        @DisplayName("실패: 인증되지 않은 사용자가 프로필 이미지 수정을 시도하면 401 에러가 발생한다")
        void updateProfileImage_WithoutAuth_Fail() throws Exception {
            // given
            MockMultipartFile imageFile = new MockMultipartFile(
                    "image",
                    "profile.jpg",
                    "image/jpeg",
                    "test image content".getBytes()
            );

            // when
            ResultActions result = mockMvc.perform(
                    multipart("/members/profile-images")
                            .file(imageFile)
                            .with(request -> {
                                request.setMethod("PATCH");
                                return request;
                            })
            );

            // then
            result.andExpect(status().isUnauthorized())
                    .andDo(print());
        }
    }
}