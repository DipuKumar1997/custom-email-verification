package com.example.spring_radis_token_validation;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import java.security.Key;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import javax.crypto.SecretKey;

@Service
public class JwtService {
    @Value ( "${security.jwt.secret.key}" )
    private String secretKey;
    @Value ( "${security.jwt.secret.key.expiration-key}" )
    private Long expiration_time;
    
    public <T> T extractClaims(String token , Function<Claims, T> claimsResolver){
        final Claims claims = extractAllClaims ( token );
        T apply = claimsResolver.apply ( claims );
        System.out.println ("T printing ");
        return  apply;
    }
    private boolean isTokenExpired(String token) {
        return extractClaims(token, Claims::getExpiration).before(new Date());
    }
    private  Claims extractAllClaims(String token){
        return Jwts.parserBuilder ()
                .setSigningKey ( getSignKey() )
                .build ()
                .parseClaimsJws( token )
                .getBody ();
    }

    private Key getSignKey() {
        byte[] keyBytes = Decoders.BASE64.decode ( secretKey );
        System.out.println ( Arrays.toString ( keyBytes ) );
        SecretKey key = Keys.hmacShaKeyFor ( keyBytes );
        System.out.println ("key--"+ key);
        return  key;
    }
    private  String buildToken(
            Map<String  , Object> extraClaims,
            UserDetails userDetails,
            long expiration_time
    ){
        return  Jwts.builder ()
                .setClaims ( extraClaims )
                .setSubject ( userDetails.getUsername () )
                .setIssuedAt ( new Date (System.currentTimeMillis ()) )
                .setExpiration ( new Date (System.currentTimeMillis ()+expiration_time) )
                .signWith ( getSignKey (), SignatureAlgorithm.HS256 )
                .compact ();
    }
    //    -----------------------Email Verify start -------------------
    private  String buildTokenForEmailVerify(Map<String  , Object> extraClaims, long expiration_time,Long userId ){
        return  Jwts.builder ()
                .setClaims ( extraClaims )
                .setSubject ( String.valueOf ( userId ) )
                .claim ( "purpose","EMAIL_VERIFY" )
                .setIssuedAt ( new Date (System.currentTimeMillis ()) )
                .setExpiration ( new Date (System.currentTimeMillis ()+expiration_time) )
                .signWith ( getSignKey (), SignatureAlgorithm.HS256 )
                .compact ();
    }
    public String generateTokenForEmailVerify(Map<String, Object> extraClaims,Long userId) {
        return buildTokenForEmailVerify(extraClaims, expiration_time,userId);
    }
    public  String generateTokenFOrEmailVerify(Long userId){
        return generateTokenForEmailVerify ( new HashMap<> (),userId );
    }
    //    -----------------------Email Verify end -------------------
    public  String generateToken(UserDetails userDetails){
        return generateToken ( new HashMap<> (),userDetails );
    }
    public String generateToken(
            Map<String, Object> extraClaims,
            UserDetails userDetails
    ) {
        return buildToken(extraClaims, userDetails, expiration_time);
    }

    public  boolean isTokenValid(String token , UserDetails userDetails){
        final String userName = extractUsername(token);
        return userName.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    private String extractUsername(String token) {
        return extractClaims ( token,Claims::getSubject );
    }
    public Claims verify(String token) {
        if (isTokenExpired(token)) {
            throw new SecurityException("Token expired");
        }
        return extractAllClaims(token);
    }
}
