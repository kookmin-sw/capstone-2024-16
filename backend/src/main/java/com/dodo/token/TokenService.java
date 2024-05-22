package com.dodo.token;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.dodo.member.domain.MemberContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TokenService {
    private final TokenProperties tokenProperties;

    public String makeToken(Long memberId) {
        Algorithm algorithm = Algorithm.HMAC256(tokenProperties.getSecretKey());
        return JWT.create()
                .withClaim("memberId", memberId)
                // withClaim("roomId", roomId)
                .sign(algorithm);
    }

    public MemberContext verify(String token) {
        Algorithm algorithm = Algorithm.HMAC256(tokenProperties.getSecretKey());
        DecodedJWT decodedToken = JWT.require(algorithm).build().verify(token);
        long memberId = decodedToken.getClaim("memberId").asLong();
//        long roomId = decodedToken.getClaim("roomId").asLong();
        return new MemberContext(memberId);
    }
}
