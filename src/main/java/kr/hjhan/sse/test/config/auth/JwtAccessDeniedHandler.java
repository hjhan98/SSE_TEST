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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;

/**
 * 필요한 권한이 존재하지 않은 경우, 403 Forbidden 에러 리턴
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

    private final MessageSource messageSource;
    private static final String CODE = "auth.forbidden";

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpStatus.FORBIDDEN.value());
        try (OutputStream os = response.getOutputStream()) {
            ObjectMapper objectMapper = new ObjectMapper();
            CommonResult commonResult = CommonResult.of(CommonStatus.ERROR,
                    HttpStatus.FORBIDDEN,null,
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
