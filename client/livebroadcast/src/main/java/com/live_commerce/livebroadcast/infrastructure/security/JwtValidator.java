//package com.live_commerce.livebroadcast.infrastructure.security;
//
//import com.live_commerce.livebroadcast.application.exception.CustomException;
//import com.live_commerce.livebroadcast.application.exception.LiveBroadcastExceptionCode;
//import io.jsonwebtoken.*;
//import io.jsonwebtoken.security.Keys;
//import jakarta.annotation.PostConstruct;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Component;
//
//import java.security.Key;
//import java.util.Base64;
//
//@Component
//public class JwtValidator {
//
//    @Value("${service.jwt.secret-key}")
//    private String secretKey;
//
//    private Key key;
//
//    @PostConstruct
//    public void init() {
//        System.out.println("JWT Secret Key = [" + secretKey + "]");
//        byte[] keyBytes = Base64.getDecoder().decode(secretKey.trim());
//        this.key = Keys.hmacShaKeyFor(keyBytes);
//    }
//
//    public Claims parseClaims(String token) {
//        try {
//            return Jwts.parserBuilder()
//                    .setSigningKey(key)
//                    .build()
//                    .parseClaimsJws(token)
//                    .getBody();
//        } catch (ExpiredJwtException e) {
//            throw new CustomException(LiveBroadcastExceptionCode.EXPIRED_TOKEN);
//        } catch (SecurityException | MalformedJwtException | SignatureException e) {
//            throw new CustomException(LiveBroadcastExceptionCode.INVALID_TOKEN);
//        } catch (UnsupportedJwtException | IllegalArgumentException e) {
//            throw new CustomException(LiveBroadcastExceptionCode.UNSUPPORTED_TOKEN);
//        }
//    }
//
//}
