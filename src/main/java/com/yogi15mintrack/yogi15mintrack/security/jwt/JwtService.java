package com.yogi15mintrack.yogi15mintrack.security.jwt;

import com.yogi15mintrack.yogi15mintrack.users.UserRepository;
import com.yogi15mintrack.yogi15mintrack.users.dto.UserLoginRequest;
import com.yogi15mintrack.yogi15mintrack.exceptions.EntityNotFoundException;
import com.yogi15mintrack.yogi15mintrack.security.CustomUserDetail;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class JwtService {
    private final String JWT_SECRET_KEY = "mySecretKeyForJWTTokenGenerationThatIsAtLeast256BitsLong";
    private final Long JWT_EXPIRATION = 1800000L;
    private final String ROLE = "role";
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;


    public String generateToken(CustomUserDetail userDetail) {
        return buildToken(userDetail, JWT_EXPIRATION);
    }

    private String buildToken(CustomUserDetail userDetail, long jwtExpiration) {
        return Jwts
                .builder()
                .claim(ROLE, userDetail.getAuthorities().toString())
                .subject(userDetail.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSignKey())
                .compact();
    }

    public String extractUsername (String token) {
        return extractAllClaims(token).getSubject();
    }

    public boolean isValidToken(String token) {
        try {
            extractAllClaims(token);
            return true;
        } catch (Exception exception) {
            return false;
        }
    }

    private Claims extractAllClaims(String token) {
        return Jwts
                .parser()
                .verifyWith(getSignKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSignKey() {
        byte[] bytes = Decoders.BASE64.decode(JWT_SECRET_KEY);
        return Keys.hmacShaKeyFor(bytes);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public class JwtResponse {
        private String token;
    }
    public JwtResponse loginAuthentication (UserLoginRequest userLoginRequest){
        userRepository.findByUsername(userLoginRequest.username()).orElseThrow(() -> new EntityNotFoundException("User", "username", userLoginRequest.username()));
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(userLoginRequest.username(), userLoginRequest.password()));
        CustomUserDetail userDetail = (CustomUserDetail) authentication.getPrincipal();
        String token = this.generateToken(userDetail);
        return new JwtResponse(token);
    }
}

