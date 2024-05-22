package com.dodo.certification;

import com.dodo.certification.domain.Certification;
import com.dodo.certification.domain.Vote;
import com.dodo.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VoteRepository extends JpaRepository<Vote, Long> {
    Optional<Vote> findByMemberAndCertification(Member member, Certification certification);
}
