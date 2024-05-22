package com.dodo.member.dto;

import lombok.Data;

@Data
public class MemberUpdateRequestData {
    private String name;
    private String introduceMessage;
}
