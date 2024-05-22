package com.dodo.member.dto;


import com.dodo.image.domain.Image;
import com.dodo.member.domain.Member;
import lombok.Data;

@Data
public class ProfileRequestData {
    private Image image;
    private String name;
    private String introduceMessage;

    public ProfileRequestData(Member member) {
        this.image = member.getImage();
        this.name = member.getName();
        this.introduceMessage = member.getIntroduceMessage();
    }
}
