package com.dodo.member;

import com.dodo.member.domain.PasswordAuthentication;
import com.dodo.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PasswordAuthenticationRepository extends JpaRepository<PasswordAuthentication, Long> {
    Optional<PasswordAuthentication> findByMember(Member member);
}
