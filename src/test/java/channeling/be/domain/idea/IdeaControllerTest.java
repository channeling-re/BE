package channeling.be.domain.idea;

import channeling.be.domain.channel.domain.Channel;
import channeling.be.domain.channel.domain.repository.ChannelRepository;
import channeling.be.domain.idea.domain.Idea;
import channeling.be.domain.idea.domain.repository.IdeaRepository;
import channeling.be.domain.member.domain.Member;
import channeling.be.domain.video.domain.Video;
import channeling.be.domain.video.domain.repository.VideoRepository;
import channeling.be.global.annotation.WithMockJwtUser;
import channeling.be.infrastructure.jwt.JwtUtil;
import channeling.be.response.code.status.ErrorStatus;
import channeling.be.response.exception.handler.ChannelHandler;
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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * IdeaController 통합 테스트
 *
 * @Transactional을 사용하여 각 테스트가 자동으로 롤백됩니다.
 * - DB 정리 코드 불필요
 * - 빠른 실행 속도
 * - 완벽한 테스트 격리
 */
@Slf4j
@Transactional
@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@DisplayName("IdeaController 통합 테스트")
public class IdeaControllerTest extends IntegrationTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private IdeaRepository ideaRepository;

    @Autowired
    private ChannelRepository channelRepository;

    @Autowired
    private VideoRepository videoRepository;

    @Nested
    @DisplayName("PATCH /ideas/{idea-id}/bookmarks - 아이디어 북마크 토글")
    class ChangeIdeaBookmark {

        @Test
        @WithMockJwtUser
        @DisplayName("성공: 북마크되지 않은 아이디어를 북마크한다")
        void changeIdeaBookmark_AddBookmark_Success(Member member) throws Exception {
            // given
            String accessToken = jwtUtil.createAccessToken(member);
            Channel testChannel = channelRepository.findByMember(member)
                    .orElseThrow(() -> new ChannelHandler(ErrorStatus._CHANNEL_NOT_FOUND));
            List<Video> videoList = videoRepository.findByChannel(testChannel);
            Video testVideo = videoList.get(0);

            // 북마크되지 않은 아이디어 생성
            Idea testIdea = Idea.builder()
                    .video(testVideo)
                    .title("테스트 아이디어")
                    .content("테스트 내용입니다.")
                    .hashTag("#테스트 #아이디어")
                    .isBookMarked(false)  // 북마크 안 됨
                    .build();
            testIdea = ideaRepository.save(testIdea);

            // when
            ResultActions result = mockMvc.perform(
                    patch("/ideas/{idea-id}/bookmarks", testIdea.getId())
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
            );

            // then
            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.isSuccess").value(true))
                    .andExpect(jsonPath("$.result.ideaId").value(testIdea.getId()))
                    .andExpect(jsonPath("$.result.isBookmarked").value(true))  // 북마크됨
                    .andDo(print());

            // DB에서 북마크 상태 확인
            Idea updatedIdea = ideaRepository.findById(testIdea.getId())
                    .orElseThrow();
            assertThat(updatedIdea.getIsBookMarked()).isTrue();
        }

        @Test
        @WithMockJwtUser
        @DisplayName("성공: 북마크된 아이디어의 북마크를 해제한다")
        void changeIdeaBookmark_RemoveBookmark_Success(Member member) throws Exception {
            // given
            String accessToken = jwtUtil.createAccessToken(member);
            Channel testChannel = channelRepository.findByMember(member)
                    .orElseThrow(() -> new ChannelHandler(ErrorStatus._CHANNEL_NOT_FOUND));
            List<Video> videoList = videoRepository.findByChannel(testChannel);
            Video testVideo = videoList.get(0);

            // 북마크된 아이디어 생성
            Idea testIdea = Idea.builder()
                    .video(testVideo)
                    .title("북마크된 아이디어")
                    .content("이미 북마크된 아이디어입니다.")
                    .hashTag("#북마크 #테스트")
                    .isBookMarked(true)  // 북마크됨
                    .build();
            testIdea = ideaRepository.save(testIdea);

            // when
            ResultActions result = mockMvc.perform(
                    patch("/ideas/{idea-id}/bookmarks", testIdea.getId())
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
            );

            // then
            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.isSuccess").value(true))
                    .andExpect(jsonPath("$.result.ideaId").value(testIdea.getId()))
                    .andExpect(jsonPath("$.result.isBookmarked").value(false))  // 북마크 해제됨
                    .andDo(print());

            // DB에서 북마크 해제 확인
            Idea updatedIdea = ideaRepository.findById(testIdea.getId())
                    .orElseThrow();
            assertThat(updatedIdea.getIsBookMarked()).isFalse();
        }

        @Test
        @WithMockJwtUser
        @DisplayName("성공: 북마크를 여러 번 토글한다")
        void changeIdeaBookmark_MultipleToggle_Success(Member member) throws Exception {
            // given
            String accessToken = jwtUtil.createAccessToken(member);
            Channel testChannel = channelRepository.findByMember(member)
                    .orElseThrow(() -> new ChannelHandler(ErrorStatus._CHANNEL_NOT_FOUND));
            List<Video> videoList = videoRepository.findByChannel(testChannel);
            Video testVideo = videoList.get(0);

            Idea testIdea = Idea.builder()
                    .video(testVideo)
                    .title("토글 테스트")
                    .content("북마크 토글 테스트")
                    .hashTag("#토글")
                    .isBookMarked(false)
                    .build();
            testIdea = ideaRepository.save(testIdea);

            Long ideaId = testIdea.getId();

            // when & then 1: false -> true
            mockMvc.perform(
                            patch("/ideas/{idea-id}/bookmarks", ideaId)
                                    .header("Authorization", "Bearer " + accessToken)
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result.isBookmarked").value(true));

            // when & then 2: true -> false
            mockMvc.perform(
                            patch("/ideas/{idea-id}/bookmarks", ideaId)
                                    .header("Authorization", "Bearer " + accessToken)
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result.isBookmarked").value(false));

            // when & then 3: false -> true
            mockMvc.perform(
                            patch("/ideas/{idea-id}/bookmarks", ideaId)
                                    .header("Authorization", "Bearer " + accessToken)
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result.isBookmarked").value(true));
        }

        @Test
        @DisplayName("실패: 인증되지 않은 사용자가 북마크 토글 시 401 에러가 발생한다")
        void changeIdeaBookmark_WithoutAuth_Fail() throws Exception {
            // when
            ResultActions result = mockMvc.perform(
                    patch("/ideas/{idea-id}/bookmarks", 1L)
                            .contentType(MediaType.APPLICATION_JSON)
            );

            // then
            result.andExpect(status().isUnauthorized())
                    .andDo(print());
        }

        @Test
        @WithMockJwtUser
        @DisplayName("실패: 존재하지 않는 아이디어 ID로 북마크 시도 시 404 에러가 발생한다")
        void changeIdeaBookmark_NotFound_Fail(Member member) throws Exception {
            // given
            String accessToken = jwtUtil.createAccessToken(member);
            Long nonExistentIdeaId = 999999L;

            // when
            ResultActions result = mockMvc.perform(
                    patch("/ideas/{idea-id}/bookmarks", nonExistentIdeaId)
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
            );

            // then
            result.andExpect(status().isBadRequest())
                    .andDo(print());
        }
    }

    @Nested
    @DisplayName("GET /ideas/bookmarks - 북마크된 아이디어 목록 조회")
    class GetBookmarkedIdeaList {

        @Test
        @WithMockJwtUser
        @DisplayName("성공: 북마크된 아이디어 목록을 정상적으로 조회한다")
        void getBookmarkedIdeaList_Success(Member member) throws Exception {
            // given
            String accessToken = jwtUtil.createAccessToken(member);
            Channel testChannel = channelRepository.findByMember(member)
                    .orElseThrow(() -> new ChannelHandler(ErrorStatus._CHANNEL_NOT_FOUND));
            List<Video> videoList = videoRepository.findByChannel(testChannel);
            Video testVideo = videoList.get(0);

            // 북마크된 아이디어 여러 개 생성
            for (int i = 1; i <= 5; i++) {
                Idea idea = Idea.builder()
                        .video(testVideo)
                        .title("북마크 아이디어 " + i)
                        .content("북마크된 내용 " + i)
                        .hashTag("#북마크" + i)
                        .isBookMarked(true)
                        .build();
                ideaRepository.save(idea);
            }

            // 북마크되지 않은 아이디어도 생성
            Idea unbookmarkedIdea = Idea.builder()
                    .video(testVideo)
                    .title("북마크 안된 아이디어")
                    .content("북마크되지 않은 내용")
                    .hashTag("#북마크안됨")
                    .isBookMarked(false)
                    .build();
            ideaRepository.save(unbookmarkedIdea);

            int page = 1;
            int size = 6;

            // when
            ResultActions result = mockMvc.perform(
                    get("/ideas/bookmarks")
                            .header("Authorization", "Bearer " + accessToken)
                            .param("page", String.valueOf(page))
                            .param("size", String.valueOf(size))
                            .contentType(MediaType.APPLICATION_JSON)
            );

            // then
            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.isSuccess").value(true))
                    .andExpect(jsonPath("$.result.page").value(page))
                    .andExpect(jsonPath("$.result.size").value(size))
                    .andExpect(jsonPath("$.result.total").exists())
                    .andExpect(jsonPath("$.result.hasNextPage").exists())
                    .andExpect(jsonPath("$.result.bookmarkedIdeaList").isArray())
                    .andExpect(jsonPath("$.result.bookmarkedIdeaList[0].ideaId").exists())
                    .andExpect(jsonPath("$.result.bookmarkedIdeaList[0].title").exists())
                    .andExpect(jsonPath("$.result.bookmarkedIdeaList[0].content").exists())
                    .andExpect(jsonPath("$.result.bookmarkedIdeaList[0].hashTag").exists())
                    .andExpect(jsonPath("$.result.bookmarkedIdeaList[0].isBookmarked").value(true))
                    .andDo(print());
        }

        @Test
        @WithMockJwtUser
        @DisplayName("성공: 기본 페이지 파라미터로 북마크 목록을 조회한다")
        void getBookmarkedIdeaList_WithDefaultParams_Success(Member member) throws Exception {
            // given
            String accessToken = jwtUtil.createAccessToken(member);

            // when
            ResultActions result = mockMvc.perform(
                    get("/ideas/bookmarks")
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
            );

            // then
            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.isSuccess").value(true))
                    .andExpect(jsonPath("$.result.page").value(1))  // 기본값
                    .andExpect(jsonPath("$.result.size").value(6))  // 기본값
                    .andExpect(jsonPath("$.result.bookmarkedIdeaList").isArray())
                    .andDo(print());
        }

        @Test
        @WithMockJwtUser
        @DisplayName("성공: 북마크된 아이디어가 없는 경우 빈 배열을 반환한다")
        void getBookmarkedIdeaList_Empty_Success(Member member) throws Exception {
            // given
            String accessToken = jwtUtil.createAccessToken(member);
            Channel testChannel = channelRepository.findByMember(member)
                    .orElseThrow(() -> new ChannelHandler(ErrorStatus._CHANNEL_NOT_FOUND));
            List<Video> videoList = videoRepository.findByChannel(testChannel);
            Video testVideo = videoList.get(0);

            // 북마크되지 않은 아이디어만 생성
            Idea idea = Idea.builder()
                    .video(testVideo)
                    .title("북마크 안된 아이디어")
                    .content("북마크되지 않은 내용")
                    .hashTag("#북마크안됨")
                    .isBookMarked(false)
                    .build();
            ideaRepository.save(idea);

            // when
            ResultActions result = mockMvc.perform(
                    get("/ideas/bookmarks")
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
            );

            // then
            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.isSuccess").value(true))
                    .andExpect(jsonPath("$.result.total").value(0))
                    .andExpect(jsonPath("$.result.bookmarkedIdeaList").isEmpty())
                    .andExpect(jsonPath("$.result.hasNextPage").value(false))
                    .andDo(print());
        }

        @Test
        @WithMockJwtUser
        @DisplayName("성공: 페이지네이션이 정상적으로 동작한다")
        void getBookmarkedIdeaList_Pagination_Success(Member member) throws Exception {
            // given
            String accessToken = jwtUtil.createAccessToken(member);
            Channel testChannel = channelRepository.findByMember(member)
                    .orElseThrow(() -> new ChannelHandler(ErrorStatus._CHANNEL_NOT_FOUND));
            List<Video> videoList = videoRepository.findByChannel(testChannel);
            Video testVideo = videoList.get(0);

            // 10개의 북마크된 아이디어 생성
            for (int i = 1; i <= 10; i++) {
                Idea idea = Idea.builder()
                        .video(testVideo)
                        .title("아이디어 " + i)
                        .content("내용 " + i)
                        .hashTag("#테스트" + i)
                        .isBookMarked(true)
                        .build();
                ideaRepository.save(idea);
            }

            int page = 1;
            int size = 3;  // 페이지당 3개

            // when: 첫 페이지 조회
            ResultActions result = mockMvc.perform(
                    get("/ideas/bookmarks")
                            .header("Authorization", "Bearer " + accessToken)
                            .param("page", String.valueOf(page))
                            .param("size", String.valueOf(size))
                            .contentType(MediaType.APPLICATION_JSON)
            );

            // then
            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.result.page").value(page))
                    .andExpect(jsonPath("$.result.size").value(size))
                    .andExpect(jsonPath("$.result.total").value(10))
                    .andExpect(jsonPath("$.result.hasNextPage").value(true))
                    .andExpect(jsonPath("$.result.bookmarkedIdeaList.length()").value(size))
                    .andDo(print());
        }

        @Test
        @WithMockJwtUser
        @DisplayName("성공: 마지막 페이지에서는 hasNextPage가 false이다")
        void getBookmarkedIdeaList_LastPage_Success(Member member) throws Exception {
            // given
            String accessToken = jwtUtil.createAccessToken(member);
            Channel testChannel = channelRepository.findByMember(member)
                    .orElseThrow(() -> new ChannelHandler(ErrorStatus._CHANNEL_NOT_FOUND));
            List<Video> videoList = videoRepository.findByChannel(testChannel);
            Video testVideo = videoList.get(0);

            // 5개의 북마크된 아이디어 생성
            for (int i = 1; i <= 5; i++) {
                Idea idea = Idea.builder()
                        .video(testVideo)
                        .title("아이디어 " + i)
                        .content("내용 " + i)
                        .hashTag("#테스트" + i)
                        .isBookMarked(true)
                        .build();
                ideaRepository.save(idea);
            }

            int page = 2;
            int size = 3;  // 페이지당 3개, 총 5개이므로 2페이지가 마지막

            // when
            ResultActions result = mockMvc.perform(
                    get("/ideas/bookmarks")
                            .header("Authorization", "Bearer " + accessToken)
                            .param("page", String.valueOf(page))
                            .param("size", String.valueOf(size))
                            .contentType(MediaType.APPLICATION_JSON)
            );

            // then
            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.result.page").value(page))
                    .andExpect(jsonPath("$.result.hasNextPage").value(false))  // 마지막 페이지
                    .andExpect(jsonPath("$.result.bookmarkedIdeaList.length()").value(2))  // 남은 2개
                    .andDo(print());
        }

        @Test
        @DisplayName("실패: 인증되지 않은 사용자가 북마크 목록 조회 시 401 에러가 발생한다")
        void getBookmarkedIdeaList_WithoutAuth_Fail() throws Exception {
            // when
            ResultActions result = mockMvc.perform(
                    get("/ideas/bookmarks")
                            .contentType(MediaType.APPLICATION_JSON)
            );

            // then
            result.andExpect(status().isUnauthorized())
                    .andDo(print());
        }
    }
}