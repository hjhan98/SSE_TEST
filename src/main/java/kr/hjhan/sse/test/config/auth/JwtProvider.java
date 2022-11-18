package kr.hjhan.sse.test.config.auth;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import kr.hjhan.sse.test.common.Token;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Component
public class JwtProvider implements InitializingBean {

    @Value("${jwt.secret}")
    private String secretKey;
    @Value("${jwt.key}")
    private String AUTHORITIES_KEY;

    private final Long accessTokenPeriod = 6 * 30 * 24 * 60 * 60 * 1000L; // 1시간 * 24시간 * 30일
    private final Long refreshTokenPeriod = 14 * 24 * 60 * 60 * 1000L;  // 14일
    private Key key;

    @Override
    public void afterPropertiesSet() {
        byte[] keyLongs = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyLongs);
    }

    /**
     * Access 토큰 생성 : authnetication 객체에 포함된 권한 정보를 담은 토큰을 생성
     * id(user), userRole(Authority), 발행일자, 유효기간, 암호화 (알고리즘: HS512, 암호키 : Base64)를 이용.
     */
    public Token createTokens(String id, List<GrantedAuthority> roles) {
        // User 구분을 위해 권한을 string으로 변환
        String authorities = roles.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        // 생성, 만료 날짜
        Date now = new Date();
        Date accessTokenExpiredDate = new Date(now.getTime() + accessTokenPeriod);
        Date refreshTokenExpiredDate = new Date(now.getTime() + refreshTokenPeriod);

        // AccessToken 생성
        String accessToken = Jwts.builder()
                .setHeaderParam(Header.TYPE, Header.JWT_TYPE)
                .setSubject(id)       // UserId
                .claim(AUTHORITIES_KEY, authorities)        // 권한 셋팅
                .setIssuedAt(now)                    // 발행일자
                .setExpiration(accessTokenExpiredDate)
                .signWith(key, SignatureAlgorithm.HS512)    // 암호화
                .compact();


        // RefreshToken 생성
        String refreshToken = Jwts.builder()
                .setHeaderParam(Header.TYPE, Header.JWT_TYPE)
                .setSubject(id)       // UserId
                .claim(AUTHORITIES_KEY, authorities)        // 권한 셋팅
                .setIssuedAt(now)                           // 발행일자
                .setExpiration(refreshTokenExpiredDate)
                .signWith(key, SignatureAlgorithm.HS512)    // 암호화
                .compact();

        // Token Response 객체 생성
        Token resDto = Token.builder().grantType("Bearer")
                .accessToken(accessToken)
//                .refreshToken(refreshToken)
                .accessTokenExpireDate(accessTokenPeriod).build();

        return resDto;
    }

    /**
     * jwt로 인증정보 조회
     * 토큰에 담겨있는 권한정보를 이용해서, authentication 반환
     */
    public Authentication getAuthentication(String token) {
        // Jwt 토큰 복호화
        Claims claims = parseClaims(token);

        // String화된 권한을 컬렉션으로 변환
        Collection<? extends GrantedAuthority> authorities = Collections.emptyList();
        // *** 5/13 프론트 요청으로 사용자 권한 삭제 ***
//                    Arrays.stream(claims.get(AUTHORITIES_KEY).toString().split(","))
//                            .map(SimpleGrantedAuthority::new)
//                            .collect(Collectors.toList());

        User principal = new User(claims.getSubject(), "", authorities);

        // Authentication 형태로 반환
        return new UsernamePasswordAuthenticationToken(principal, token, authorities);

    }

    /**
     * Claim 파싱
     */
    private Claims parseClaims(String token) {
        try {
            return Jwts
                    .parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        }
    }

    /**
     * 토큰 검증 : 만료된 Refresh Token인지 확인
     */
    public boolean validateToken(String token) {
        boolean result = false;
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            result = true;
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            log.info("잘못된 형식의 토큰입니다.");
        } catch (IllegalArgumentException e) {
            log.info("JWT 토큰이 잘못되었습니다.");
        } catch (ExpiredJwtException e) {
            log.info("만료된 토큰입니다.");
        }

        return result;
    }
}

