package com.dodo.certification.domain;

import com.dodo.member.domain.Member;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@Data
public class Vote {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Member member;

    @Enumerated(EnumType.STRING)
    private VoteStatus voteStatus;

    @ManyToOne
    private Certification certification;

    public Vote(Member member, Certification certification) {
        this.member = member;
        this.certification = certification;
        this.voteStatus = VoteStatus.NONE;
    }
}
