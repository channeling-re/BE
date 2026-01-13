package channeling.be.global;

import channeling.be.domain.member.domain.Member;
import channeling.be.global.annotation.WithMockJwtUser;
import channeling.be.global.auth.presentation.OauthReq;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * OauthController 통합 테스트
 *
 * @Transactional을 사용하여 각 테스트가 자동으로 롤백됩니다.
 */
@Slf4j
@Transactional
@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@DisplayName("OauthController 통합 테스트")
public class OauthControllerTest extends IntegrationTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Nested
    @DisplayName("POST /oauth/reissue - 액세스 토큰 재발급")
    class ReissueToken {

        @Test
        @WithMockJwtUser
        @DisplayName("성공: 유효한 리프레시 토큰으로 액세스 토큰을 재발급한다")
        void reissueToken_Success(Member member) throws Exception {
            // given
            String refreshToken = jwtUtil.createRefreshToken(member);

            OauthReq.ReIssueToken request = new OauthReq.ReIssueToken();
            request.setRefreshToken(refreshToken);

            String requestBody = objectMapper.writeValueAsString(request);

            // when
            ResultActions result = mockMvc.perform(
                    post("/oauth/reissue")
                            .content(requestBody)
                            .contentType(MediaType.APPLICATION_JSON)
            );

            // then
            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.isSuccess").value(true))
                    .andExpect(jsonPath("$.result.reIssuedAccessToken").exists())
                    .andDo(print());
        }

        @Test
        @DisplayName("실패: refreshToken이 null인 경우 400 에러가 발생한다")
        void reissueToken_NullRefreshToken_Fail() throws Exception {
            // given
            String requestBody = """
                {
                    "refreshToken": null
                }
                """;

            // when
            ResultActions result = mockMvc.perform(
                    post("/oauth/reissue")
                            .content(requestBody)
                            .contentType(MediaType.APPLICATION_JSON)
            );

            // then
            result.andExpect(status().isBadRequest())
                    .andDo(print());
        }

        @Test
        @DisplayName("실패: 유효하지 않은 리프레시 토큰으로 재발급 시도 시 에러가 발생한다")
        void reissueToken_InvalidRefreshToken_Fail() throws Exception {
            // given
            OauthReq.ReIssueToken request = new OauthReq.ReIssueToken();
            request.setRefreshToken("invalid_refresh_token");

            String requestBody = objectMapper.writeValueAsString(request);

            // when
            ResultActions result = mockMvc.perform(
                    post("/oauth/reissue")
                            .content(requestBody)
                            .contentType(MediaType.APPLICATION_JSON)
            );

            // then
            result.andExpect(status().is4xxClientError())
                    .andDo(print());
        }
    }

    @Nested
    @DisplayName("POST /oauth/logout - 로그아웃")
    class Logout {

        @Test
        @WithMockJwtUser
        @DisplayName("성공: 유효한 리프레시 토큰으로 로그아웃한다")
        void logout_Success(Member member) throws Exception {
            // given
            String refreshToken = jwtUtil.createRefreshToken(member);

            OauthReq.Logout request = new OauthReq.Logout();
            request.setRefreshToken(refreshToken);

            String requestBody = objectMapper.writeValueAsString(request);

            // when
            ResultActions result = mockMvc.perform(
                    post("/oauth/logout")
                            .content(requestBody)
                            .contentType(MediaType.APPLICATION_JSON)
            );

            // then
            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.isSuccess").value(true))
                    .andExpect(jsonPath("$.result").exists())
                    .andDo(print());
        }

        @Test
        @DisplayName("실패: refreshToken이 null인 경우 400 에러가 발생한다")
        void logout_NullRefreshToken_Fail() throws Exception {
            // given
            String requestBody = """
                {
                    "refreshToken": null
                }
                """;

            // when
            ResultActions result = mockMvc.perform(
                    post("/oauth/logout")
                            .content(requestBody)
                            .contentType(MediaType.APPLICATION_JSON)
            );

            // then
            result.andExpect(status().isBadRequest())
                    .andDo(print());
        }
    }

    @Nested
    @DisplayName("POST /oauth/withdrawal - 회원 탈퇴")
    class Withdrawal {

        @Test
        @WithMockJwtUser
        @DisplayName("성공: 유효한 토큰으로 회원 탈퇴한다")
        void withdrawal_Success(Member member) throws Exception {
            // given
            String accessToken = jwtUtil.createAccessToken(member);
            String refreshToken = jwtUtil.createRefreshToken(member);

            OauthReq.withdrawal request = new OauthReq.withdrawal();
            request.setAccessToken(accessToken);
            request.setRefreshToken(refreshToken);

            String requestBody = objectMapper.writeValueAsString(request);

            // when
            ResultActions result = mockMvc.perform(
                    post("/oauth/withdrawal")
                            .header("Authorization", "Bearer " + accessToken)
                            .content(requestBody)
                            .contentType(MediaType.APPLICATION_JSON)
            );

            // then
            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.isSuccess").value(true))
                    .andExpect(jsonPath("$.result").exists())
                    .andDo(print());
        }

        @Test
        @DisplayName("실패: 인증되지 않은 사용자가 탈퇴 시도 시 401 에러가 발생한다")
        void withdrawal_WithoutAuth_Fail() throws Exception {
            // given
            OauthReq.withdrawal request = new OauthReq.withdrawal();
            request.setAccessToken("some_access_token");
            request.setRefreshToken("some_refresh_token");

            String requestBody = objectMapper.writeValueAsString(request);

            // when
            ResultActions result = mockMvc.perform(
                    post("/oauth/withdrawal")
                            .content(requestBody)
                            .contentType(MediaType.APPLICATION_JSON)
            );

            // then
            result.andExpect(status().isUnauthorized())
                    .andDo(print());
        }

        @Test
        @WithMockJwtUser
        @DisplayName("실패: accessToken이 null인 경우 400 에러가 발생한다")
        void withdrawal_NullAccessToken_Fail(Member member) throws Exception {
            // given
            String accessToken = jwtUtil.createAccessToken(member);

            String requestBody = """
                {
                    "accessToken": null,
                    "refreshToken": "some_refresh_token"
                }
                """;

            // when
            ResultActions result = mockMvc.perform(
                    post("/oauth/withdrawal")
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
        @DisplayName("실패: refreshToken이 null인 경우 400 에러가 발생한다")
        void withdrawal_NullRefreshToken_Fail(Member member) throws Exception {
            // given
            String accessToken = jwtUtil.createAccessToken(member);

            String requestBody = """
                {
                    "accessToken": "some_access_token",
                    "refreshToken": null
                }
                """;

            // when
            ResultActions result = mockMvc.perform(
                    post("/oauth/withdrawal")
                            .header("Authorization", "Bearer " + accessToken)
                            .content(requestBody)
                            .contentType(MediaType.APPLICATION_JSON)
            );

            // then
            result.andExpect(status().isBadRequest())
                    .andDo(print());
        }
    }

    @Nested
    @DisplayName("POST /oauth/resign_up - 재가입")
    class ResignUp {

        @Test
        @DisplayName("성공: 유효한 임시 토큰으로 재가입한다")
        void resignUp_Success() throws Exception {
            // given
            OauthReq.resignation request = new OauthReq.resignation();
            request.setTempToken("valid_temp_token");

            String requestBody = objectMapper.writeValueAsString(request);

            // when
            ResultActions result = mockMvc.perform(
                    post("/oauth/resign_up")
                            .content(requestBody)
                            .contentType(MediaType.APPLICATION_JSON)
            );

            // then
            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.isSuccess").value(true))
                    .andExpect(jsonPath("$.result").exists())
                    .andDo(print());
        }

        @Test
        @DisplayName("실패: tempToken이 null인 경우 400 에러가 발생한다")
        void resignUp_NullTempToken_Fail() throws Exception {
            // given
            String requestBody = """
                {
                    "tempToken": null
                }
                """;

            // when
            ResultActions result = mockMvc.perform(
                    post("/oauth/resign_up")
                            .content(requestBody)
                            .contentType(MediaType.APPLICATION_JSON)
            );

            // then
            result.andExpect(status().isBadRequest())
                    .andDo(print());
        }

        @Test
        @DisplayName("실패: 유효하지 않은 임시 토큰으로 재가입 시도 시 에러가 발생한다")
        void resignUp_InvalidTempToken_Fail() throws Exception {
            // given
            OauthReq.resignation request = new OauthReq.resignation();
            request.setTempToken("invalid_temp_token");

            String requestBody = objectMapper.writeValueAsString(request);

            // when
            ResultActions result = mockMvc.perform(
                    post("/oauth/resign_up")
                            .content(requestBody)
                            .contentType(MediaType.APPLICATION_JSON)
            );

            // then
            result.andExpect(status().is4xxClientError())
                    .andDo(print());
        }
    }
}