package com.example.demo.security.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.Claim;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequiredArgsConstructor

@ConstructorBinding
@ConfigurationProperties(prefix = "jwt")
public class JwtTokenProvider {
    private final String secretKey;
    private final Integer tokenExpirationAfterMilliseconds;

    public String generateJwtToken(UserDetails userDetails){
        // get claims from authenticated user
        List<String> claims = getClaimsFromUser(userDetails);
        // generate JWT token
        return JWT.create()
                .withIssuer("AppName")
                .withAudience("AppName Administration")
                .withIssuedAt(new Date())
                .withSubject(userDetails.getUsername())
                .withArrayClaim("authorities", claims.toArray(new String[0]))  // convert List< String> to String[]
                .withExpiresAt(new Date(System.currentTimeMillis() + tokenExpirationAfterMilliseconds))
                .sign(Algorithm.HMAC512(secretKey));
    }

    public Set<SimpleGrantedAuthority> getAuthoritiesFromToken(String token) {
        return Stream.of(getClaims(token).get("authorities"))
                .map(claims -> claims.asArray(String.class))
                .flatMap(strings -> Stream.of(strings))
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toSet());
    }

    public boolean isTokenValid(String token){
        String username;
        try{
            username = getJWTVerifier().verify(token).getSubject();
        }catch (TokenExpiredException | JWTDecodeException | SignatureVerificationException e){
            return false;
        }
        return ((username != null) && (username.trim() != "") && (!isTokenExpired(token)));
    }

    public String getSubject(String token){
        return getJWTVerifier().verify(token).getSubject();
    }

    public boolean isTokenExpired(String token){
        Date expiration = getJWTVerifier().verify(token).getExpiresAt();
        return expiration.before(new Date());
    }

    private List< String> getClaimsFromUser(UserDetails userDetails) {
        List<String> authorities = new ArrayList<>();
        for(GrantedAuthority authority: userDetails.getAuthorities()){
            authorities.add(authority.getAuthority());
        }
        return authorities;
    }

    private Map<String, Claim> getClaims(String token){
        JWTVerifier verifier = getJWTVerifier();
        return verifier.verify(token).getClaims();
    }

    private JWTVerifier getJWTVerifier(){
        JWTVerifier verifier;
        try{
            Algorithm algo = Algorithm.HMAC512(secretKey);
            verifier = JWT.require(algo).withIssuer("AppName").build();
        }catch (JWTVerificationException e){
            throw new JWTVerificationException("Token cannot be verified");
        }
        return verifier;
    }
}
