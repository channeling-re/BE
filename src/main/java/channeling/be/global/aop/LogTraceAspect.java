package channeling.be.global.aop;

import channeling.be.infrastructure.log.LogTrace;
import channeling.be.infrastructure.log.TraceStatus;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

@Slf4j
@Aspect
public class LogTraceAspect {
    private final LogTrace logTrace;
    public LogTraceAspect(LogTrace logTrace) {
        this.logTrace = logTrace;
    }
    @Around(
            "execution(* channeling.be.domain..*(..)) || execution(* channeling.be.infrastructure.kafka..*(..))" +
                    "|| execution(* channeling.be.infrastructure.youtube..*(..))"

    )
    public Object execute(ProceedingJoinPoint joinPoint) throws Throwable {
        TraceStatus status = null;
        try {
            String message = joinPoint.getSignature().toShortString();
            status = logTrace.begin(message);
            //로직 호출
            Object result = joinPoint.proceed();
            logTrace.end(status);
            return result;
        } catch (Exception e) {
            logTrace.exception(status, e);
            throw e;
        }
    }

}
