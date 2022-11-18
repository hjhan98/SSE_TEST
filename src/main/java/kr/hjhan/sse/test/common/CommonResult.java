package kr.hjhan.sse.test.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import org.springframework.data.domain.Slice;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
public class CommonResult<T> {


    @Getter
    @Setter
    @NoArgsConstructor
    public static class CommonWrapper<C> {
        private C content;

        public CommonWrapper(C content) {
            this.content = content;
        }
    }

    private CommonStatus status;

    private T data;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer code;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String message;

    public static <T> CommonResult<T> of(@NonNull CommonStatus status, T data) {

        if(data instanceof Iterable && !(data instanceof Slice)) {
            throw new RuntimeException("Iterable 인터페이스 상속받은 경우 wrap 메소드를 사용해주세요.");
        }

        return CommonResult.<T>builder()
                .status(status)
                .data(data).build();
    }

    public static <C> CommonResult<CommonWrapper<C>> wrap(@NonNull CommonStatus status, C data) {
        return CommonResult.<CommonWrapper<C>>builder()
                .status(status)
                .data(new CommonWrapper<>(data)).build();
    }

    public static <T> CommonResult<T> of(@NonNull CommonStatus status, int code, T data, String message) {
        return CommonResult.<T>builder()
                .code(code)
                .status(status)
                .message(message)
                .data(data).build();
    }

    public static CommonResult of(@NonNull CommonStatus status) {
        return CommonResult.builder()
                .status(status)
                .data(null).build();
    }

}
