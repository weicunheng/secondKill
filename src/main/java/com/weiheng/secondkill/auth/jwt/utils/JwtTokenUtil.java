package com.weiheng.secondkill.auth.jwt.utils;

import com.weiheng.secondkill.auth.jwt.properties.JwtSecurityProperties;
import com.weiheng.secondkill.common.constant.SecurityConstant;
import com.weiheng.secondkill.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Map;

import static java.lang.String.format;

@Component
@RequiredArgsConstructor
public class JwtTokenUtil {

    @Autowired
    private JwtSecurityProperties jwtSecurityProperties;

    private final Logger logger = LoggerFactory.getLogger(JwtTokenUtil.class);

    public String generateAccessToken(User user) {
        String jwtIssuer = "example.io";
        return Jwts.builder()
                .setSubject(format("%s,%s", user.getId(), user.getUserName()))
                .setIssuer(jwtIssuer)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000)) // 1 week
                .signWith(SignatureAlgorithm.HS256, jwtSecurityProperties.getBase64Secret())
                .compact();
    }

    public String getUserId(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(jwtSecurityProperties.getBase64Secret())
                .parseClaimsJws(token)
                .getBody();

        return claims.getSubject().split(",")[0];
    }

    public String getUsername(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(jwtSecurityProperties.getBase64Secret())
                .parseClaimsJws(token)
                .getBody();

        return claims.getSubject().split(",")[1];
    }

    public Date getExpirationDate(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(jwtSecurityProperties.getBase64Secret())
                .parseClaimsJws(token)
                .getBody();

        return claims.getExpiration();
    }

    public boolean validate(String token) {
        try {
            Jwts.parser().setSigningKey(jwtSecurityProperties.getBase64Secret()).parseClaimsJws(token);
            return true;
        } catch (SignatureException ex) {
            logger.error("Invalid JWT signature - {}", ex.getMessage());
        } catch (MalformedJwtException ex) {
            logger.error("Invalid JWT token - {}", ex.getMessage());
        } catch (ExpiredJwtException ex) {
            logger.error("Expired JWT token - {}", ex.getMessage());
        } catch (UnsupportedJwtException ex) {
            logger.error("Unsupported JWT token - {}", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            logger.error("JWT claims string is empty - {}", ex.getMessage());
        }
        return false;
    }

    //解析JWT, 获得Claims
    private Claims getClaimsFromToken(String token) {
        Claims claims;
        try {
            claims = Jwts.parser()
                    .setSigningKey(jwtSecurityProperties.getBase64Secret())
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            //如果过期,在异常中调用, 返回claims, 否则无法解析过期的token
            claims = e.getClaims();
        } catch (Exception e) {
            claims = null;
        }
        return claims;
    }

    //根据Claims生成JWT
    public String generateToken(Map<String, Object> claims) {
        return Jwts.builder()
                .setClaims(claims)
                .setExpiration(new Date(System.currentTimeMillis() + SecurityConstant.EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS256, SecurityConstant.JWT_SIGN_KEY)
                .compact();
    }

    //刷新JWT
    public String refreshToken(String token) {
        String refreshedToken;
        try {
            //获得Token的Claims, 由于在生成JWT的时候会根据当前时间更新过期时间, 我们只需要手动修改
            //放在自定义属性中的创建时间就可以了
            Claims claims = getClaimsFromToken(token);
            //利用修改后的claims再次生成token, 就不需要我们每次都去查用户的信息和权限了
            refreshedToken = generateToken(claims);
        } catch (Exception e) {
            refreshedToken = null;
        }
        return refreshedToken;
    }

}
