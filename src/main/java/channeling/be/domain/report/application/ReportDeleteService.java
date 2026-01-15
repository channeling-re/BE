package channeling.be.domain.report.application;

import channeling.be.domain.TrendKeyword.domain.repository.TrendKeywordRepository;
import channeling.be.domain.idea.domain.repository.IdeaRepository;
import channeling.be.domain.member.domain.Member;
import channeling.be.domain.report.domain.Report;
import channeling.be.domain.report.domain.repository.ReportRepository;
import channeling.be.domain.video.domain.Video;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
@Slf4j
public class ReportDeleteService {
    private final ReportRepository reportRepository;
    private final IdeaRepository ideaRepository;
    private final TrendKeywordRepository trendKeywordRepository;

    @Transactional
    public void deleteExistingReport(Report report, Video video, Member member) {
        log.info("기존 리포트 삭제 시작 - reportId: {}", report.getId());
        
        // 연관된 북마크 하지 않은 아이디어 리스트 삭제
        ideaRepository.deleteAllByVideoWithoutBookmarked(video.getId(), member.getId());
        // 연관된 댓글 리스트 삭제
        report.getComments().clear();
        // 연관되 키워드 리스트 가져오기
        trendKeywordRepository.deleteAllByReportAndMember(report.getId(), member.getId());
        // Task는 Cascade.REMOVE로 자동 삭제됨
        // 리포트 삭제
        reportRepository.deleteById(report.getId());
        
        // 명시적 flush로 삭제 즉시 실행
        reportRepository.flush();
        log.info("기존 리포트 삭제 완료 - reportId: {}", report.getId());
    }
}