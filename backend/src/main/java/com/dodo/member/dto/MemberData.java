package com.dodo.member.dto;

import com.dodo.image.domain.Image;
import com.dodo.member.domain.AuthenticationType;
import com.dodo.member.domain.Member;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class MemberData {
    private Long memberId;
    private AuthenticationType authenticationType;
    private String email;
    private String name;
    private Integer mileage;
    private String introduceMessage;
    private Image image;

    public MemberData(Member member) {
        this.memberId = member.getId();
        this.authenticationType = member.getAuthenticationType();
        this.email = member.getEmail();
        this.name = member.getName();
        this.mileage = member.getMileage();
        this.introduceMessage = member.getIntroduceMessage();
        this.image = member.getImage();
    }
}
