package com.dodo.member.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MemberContext {
    private Long memberId;
    private Long roomId;

    public MemberContext(Long memberId, Long roomId) {
        this.memberId = memberId;
        this.roomId = roomId;
    }

    public MemberContext(Long memberId) {
        this.memberId = memberId;
    }
}
