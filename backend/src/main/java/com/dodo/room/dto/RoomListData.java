package com.dodo.room.dto;

import com.dodo.certification.domain.CertificationStatus;
import com.dodo.image.domain.Image;
import com.dodo.room.domain.Room;
import com.dodo.room.domain.RoomType;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class RoomListData {
    private Long roomId;
    private String name;
    private Image image;
    private Long maxMember;
    private Long nowMember;
    private String password;
    private CertificationStatus status;
    private RoomType roomType;
    private Integer frequency;

    private Boolean isManager = false;

    public RoomListData(Room room) {
        this.roomId = room.getId();
        this.name = room.getName();
        this.image = room.getImage();
        this.roomType = room.getRoomType();
        this.maxMember = room.getMaxMember();
        this.nowMember = room.getNowMember();
        this.password = room.getPassword();
        this.frequency = room.getFrequency();
        this.status = CertificationStatus.FAIL;

    }
    public static RoomListData updateStatus(RoomListData roomListData, CertificationStatus certificationStatus){
        roomListData.status = certificationStatus;

        return roomListData;
    }

    public void updateIsManager(Boolean isManager) {
        this.isManager = isManager;
    }
}