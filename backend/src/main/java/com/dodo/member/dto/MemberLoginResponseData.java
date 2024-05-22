package com.dodo.member.dto;

import lombok.Getter;

@Getter
public class MemberLoginResponseData {
    String token;

    public MemberLoginResponseData(String token) {
        this.token = token;
    }
}
