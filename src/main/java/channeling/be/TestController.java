package channeling.be;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class TestController {

    @GetMapping("/log-test")
    public String logTest() {
        log.trace("TRACE 로그입니다.");
        log.debug("DEBUG 로그입니다.");
        log.info("INFO 로그입니다.");
        log.warn("WARN 로그입니다.");

        try {
            // 의도적으로 에러 발생
            int x = 10 / 0;
        } catch (Exception e) {
            log.error("ERROR 로그 - 테스트용 예외 발생", e);
        }

        return "log test done";
    }
}
