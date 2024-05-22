package com.dodo.statistics.dto;

import com.dodo.image.domain.Image;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RoomProfileData {
    private Long roomMemberId;
    private String memberName;
    private Image image;
    private String since;
    private Long success;
    private Long allSuccess;
    private Long lately;
    private Long allLately;
}
