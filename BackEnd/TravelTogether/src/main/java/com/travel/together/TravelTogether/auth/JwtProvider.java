package com.travel.together.TravelTogether.auth;
//
//import com.travel.together.TravelTogether.auth.entity.User;
//import io.jsonwebtoken.Jwts;
//import io.jsonwebtoken.SignatureAlgorithm;
//import org.hibernate.annotations.Comment;
//import org.springframework.stereotype.Component;

import java.util.Date;

//@Component
public class JwtProvider {
//    private final String secretKey = "yourSecretKey";
//    private final long expirationMs = 3600000; // 1시간
//
//    // JWT 생성
//    public String generateToken(User user) {
//        return Jwts.builder()
//                .setSubject(user.getEmail())
//                .setIssuedAt(new Date())
//                .setExpiration(new Date(System.currentTimeMillis() + expirationMs))
//                .signWith(SignatureAlgorithm.HS256, secretKey)
//                .compact();
//    }
//
//    // JWT 검증
//    public boolean validateToken(String token) {
//        try {
//            Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token);
//            return true;
//        } catch (Exception e) {
//            return false;
//        }
//    }
}
