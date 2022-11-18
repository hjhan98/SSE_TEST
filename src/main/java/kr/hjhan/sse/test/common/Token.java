package kr.hjhan.sse.test.common;

import lombok.*;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Token {

    private String grantType;

    private String accessToken;

    private Long accessTokenExpireDate;

}
