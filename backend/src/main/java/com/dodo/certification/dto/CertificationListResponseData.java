package com.dodo.certification.dto;

import com.dodo.certification.CertificationService;
import com.dodo.certification.domain.Certification;
import com.dodo.image.domain.Image;
import com.dodo.member.domain.Member;
import lombok.Data;

import java.util.List;

@Data
public class CertificationListResponseData {
    private Long memberId;
    private Long roomMemberId;
    private List<Long> certificationIdList;
    private String memberName;
    private Image memberImage;
    private Integer max;
    private Integer success;
    private Integer wait;
    private Boolean certification;

    public CertificationListResponseData(CertificationService.CertificationGroup group) {
        Member member = group.getRoomMember().getMember();
        this.memberId = member.getId();
        this.roomMemberId = group.getRoomMember().getId();
        this.certificationIdList = group.getCertificationList().stream().map(Certification::getId).toList();
        this.memberName = member.getName();
        this.memberImage = member.getImage();
        this.max = group.getRoomMember().getRoom().getFrequency();
        this.success = group.getSuccess();
        this.wait = group.getWait();

        if(max.equals(success)) {
            this.certification = Boolean.TRUE;
        } else {
            this.certification = Boolean.FALSE;
        }

    }

}
