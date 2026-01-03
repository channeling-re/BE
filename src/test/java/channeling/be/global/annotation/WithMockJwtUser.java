package channeling.be.global.annotation;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.security.test.context.support.WithSecurityContext;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = MockJwtSecurityContextFactory.class)
@ExtendWith(MockJwtUserParameterResolver.class)
public @interface WithMockJwtUser {
    String googleId() default "test_google_id";
}
