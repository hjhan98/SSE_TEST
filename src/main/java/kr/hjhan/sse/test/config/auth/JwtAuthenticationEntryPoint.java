package kr.hjhan.sse.test.config.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.hjhan.sse.test.common.CommonResult;
import kr.hjhan.sse.test.common.CommonStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;

/**
 * 유효한 자격증명을 제공하지 않고 접근 시, 401 UnAuthorized 에러 리턴.
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final MessageSource messageSource;
    private static final String CODE = "auth.unauthorized";

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException e) throws IOException {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        try (OutputStream os = response.getOutputStream()) {
            ObjectMapper objectMapper = new ObjectMapper();
            CommonResult commonResult = CommonResult.of(CommonStatus.ERROR,
                    HttpStatus.UNAUTHORIZED.value(),null,
                    messageSource.getMessage(CODE,
                            null,
                            null,
                            LocaleContextHolder.getLocale())
            );
            objectMapper.writeValue(os, commonResult);
            os.flush();
        }
    }
}
