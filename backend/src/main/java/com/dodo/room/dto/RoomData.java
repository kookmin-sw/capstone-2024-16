package com.dodo.room.dto;

import com.dodo.certification.domain.CertificationStatus;
import com.dodo.image.domain.Image;
import com.dodo.room.domain.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Data
@RequiredArgsConstructor
public class RoomData {
    private Long roomId;
    private String name;
    private Image image;
    private Long maxMember;
    private Long nowMember;
    private LocalDateTime endDay;
    private Periodicity periodicity;
    private String pwd;
    private Category category;
    private String info;
    private Boolean canChat;
    private Integer numOfVoteSuccess;
    private Integer numOfVoteFail;
    private Integer numOfGoal;
    private String stringGoal;
    private List<String> goal;
    private Integer nowGoal;
    private Boolean isFull;

    public CertificationStatus status;

    private RoomType roomType;
    private CertificationType certificationType;
    private Integer frequency;

    private List<String> tag;

    private Boolean isManager = false;

    public static RoomData of(Room room) {
        RoomData roomData = new RoomData();

        roomData.roomId = room.getId();
        roomData.name = room.getName();
        roomData.maxMember = room.getMaxMember();
        roomData.nowMember = room.getNowMember();
        roomData.endDay = room.getEndDay();
        roomData.periodicity = room.getPeriodicity();
        roomData.pwd = room.getPassword();
        roomData.category = room.getCategory();
        roomData.info = room.getInfo();
        roomData.canChat = room.getCanChat();
        roomData.numOfVoteSuccess = room.getNumOfVoteSuccess();
        roomData.numOfVoteFail = room.getNumOfVoteFail();
        roomData.certificationType = room.getCertificationType();
        roomData.frequency = room.getFrequency();
        roomData.isFull = room.getIsFull();
        roomData.goal = Arrays.asList(room.getGoal().split(","));
        roomData.numOfGoal = room.getNumOfGoal();
        roomData.roomType = room.getRoomType();
        roomData.image = room.getImage();
        roomData.nowGoal = room.getNowGoal();

        return roomData;
    }

    public void updateTag(List<String> tag){
        this.tag = tag;
    }

    public void updateIsManager(Boolean isManager){
        this.isManager = isManager;
    }
}
