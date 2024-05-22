package com.dodo.config.auth.service;

import com.dodo.token.TokenService;
import com.dodo.member.PasswordAuthenticationRepository;
import com.dodo.member.MemberRepository;
import com.dodo.member.domain.MemberContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final MemberRepository memberRepository;
    private final PasswordAuthenticationRepository passwordAuthenticationRepository;
    private final TokenService tokenService;

    public MemberContext authenticate(String accessToken) {
        return tokenService.verify(accessToken);
    }
}
