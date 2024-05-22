package com.dodo.room.dto;

import com.dodo.image.domain.Image;
import com.dodo.room.domain.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@RequiredArgsConstructor
public class RoomJoinData {
    private Long roomId;
    private String name;
    private Image image;
    private Long maxMember;
    private Long nowMember;
    private String info;
    private LocalDateTime endDay;
    private String password;
    private Boolean canChat;
    private RoomType roomType;
    private CertificationType certificationType;
    private Periodicity periodicity;
    private Integer frequency;


    private Boolean isIn;
    private List<String> tag;

    public RoomJoinData(Room room) {
        this.roomId = room.getId();
        this.name = room.getName();
        this.image = room.getImage();
        this.roomType = room.getRoomType();
        this.maxMember = room.getMaxMember();
        this.nowMember = room.getNowMember();
        this.password = room.getPassword();
        this.info = room.getInfo();
        this.endDay = room.getEndDay();
        this.canChat = room.getCanChat();
        this.certificationType = room.getCertificationType();
        this.periodicity = room.getPeriodicity();
        this.frequency = room.getFrequency();
    }

    public void updateIsIn(Boolean isIn) {
        this.isIn = isIn;
    }
    public void updateTag(List<String> tag){
        this.tag = tag;
    }
}
