package com.dodo.member.dto;

import com.dodo.member.domain.Member;
import lombok.Getter;

@Getter
public class MemberCreateResponseData {
    private final Long memberId;
    public MemberCreateResponseData(Member member) {
        this.memberId = member.getId();
    }
}
