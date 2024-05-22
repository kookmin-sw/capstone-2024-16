package com.dodo.room;

import com.dodo.exception.NotFoundException;
import com.dodo.image.ImageRepository;
import com.dodo.image.ImageService;
import com.dodo.image.domain.Image;
import com.dodo.room.domain.*;
import com.dodo.room.dto.RoomData;
import com.dodo.room.dto.RoomJoinData;
import com.dodo.room.dto.RoomListData;
import com.dodo.tag.domain.RoomTag;
import com.dodo.tag.domain.Tag;
import com.dodo.tag.repository.RoomTagRepository;
import com.dodo.tag.service.RoomTagService;
import com.dodo.member.MemberRepository;
import com.dodo.member.domain.Member;
import com.dodo.member.domain.MemberContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static com.dodo.room.dto.RoomListData.updateStatus;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoomService {

    private final MemberRepository memberRepository;
    private final com.dodo.roommember.RoomMemberRepository roomMemberRepository;
    private final RoomRepository roomRepository;
    private final com.dodo.roommember.RoomMemberService roomMemberService;
    private final RoomTagRepository roomTagRepository;
    private final RoomTagService roomTagService;
    private final ImageRepository imageRepository;
    private final ImageService imageService;

    public List<RoomListData> getMyRoomList(MemberContext memberContext) {
        Member member = getMember(memberContext);

        return roomMemberRepository.findAllByMember(member)
                .orElse(new ArrayList<>())
                .stream()
                .map(com.dodo.roommember.domain.RoomMember::getRoom)
                .map(RoomListData::new)
                .map(RoomListData -> updateStatus(RoomListData, roomMemberService.getCertificationStatus(memberContext, RoomListData)))
                .map(RoomListData -> updateIsManager(RoomListData, member))
                .toList();
    }

    public List<RoomData> getRoomListByCategory(Category category) {
        List<RoomData> roomDataList =  roomRepository.findAllByCategory(category)
                .orElseThrow(NotFoundException::new)
                .stream()
                .map(RoomData::of)
                .sorted(Comparator.comparing(RoomData::getIsFull).reversed()
                        .thenComparing(RoomData::getNowMember).reversed())
                .toList();

        for(RoomData roomData : roomDataList){
            List<String> tags = getTags(roomData.getRoomId());
            roomData.updateTag(tags);
        }

        return roomDataList;
    }

    // 인증방 제목으로 검색
    public List<RoomData> getRoomListByName(String name){
        return roomRepository.findAllByNameContaining(name).orElseThrow(NotFoundException::new)
                .stream()
                .map(RoomData::of)
                .toList();
    }

    // 인증방 아이디로 검색
    public List<RoomData> getRoomListById(Long roomId){
        return roomRepository.findAllById(roomId).orElseThrow(NotFoundException::new)
                .stream()
                .map(RoomData::of)
                .toList();
    }

    // 인증방 생성
    public Room createRoom(String roomName, String roomPwd, Long maxMember, Category category,
                                 String info, CertificationType certificationType,
                                 Boolean canChat, Integer numOfVoteSuccess, Integer numOfVoteFail,
                                 Integer frequency, Periodicity periodicity, LocalDateTime endDate, RoomType roomType){
        Boolean isFull = maxMember == 1;
        Room room = Room.builder()
                .name(roomName)
                .password(roomPwd)
                .maxMember(maxMember)
                .nowMember(1L)
                .category(category)
                .info(info)
                .certificationType(certificationType)
                .periodicity(periodicity)
                .canChat(canChat)
                .endDay(endDate)
                .frequency(frequency)
                .numOfVoteSuccess(numOfVoteSuccess).numOfVoteFail(numOfVoteFail)
                .roomType(roomType)
                .isFull(isFull)
                .image(imageRepository.findById(1L).get())
                .build();

        roomRepository.save(room);
        return room;
    }

    // 그룹 인증방 생성
    public Room createGroupRoom(String roomName, String roomPwd, Long maxMember, Category category,
                                 String info, CertificationType certificationType, Integer numOfGoal, String goal,
                                 Boolean canChat, Integer numOfVoteSuccess, Integer numOfVoteFail,
                                 Integer frequency, Periodicity periodicity, LocalDateTime endDate){
        Boolean isFull = maxMember == 1;
        Room room = Room.builder()
                .name(roomName)
                .password(roomPwd)
                .maxMember(maxMember)
                .nowMember(1L)
                .category(category)
                .info(info)
                .certificationType(certificationType)
                .periodicity(periodicity)
                .canChat(canChat)
                .endDay(endDate)
                .frequency(frequency)
                .numOfVoteSuccess(numOfVoteSuccess).numOfVoteFail(numOfVoteFail)
                .roomType(RoomType.GROUP)
                .numOfGoal(numOfGoal)
                .goal(goal)
                .nowGoal(0)
                .isFull(isFull)
                .build();

        roomRepository.save(room);
        return room;
    }

    // 인증방 비밀번호 조회
    public Boolean confirmPwd(Long roomId, String roomPwd){
        return roomPwd.equals(roomRepository.findById(roomId).orElseThrow(NotFoundException::new).getPassword());
    }

    // 채팅방 인원 증가
    public void plusMemberCnt(Long roomId){
        log.info("plus room Id : {}", roomId);
        Room room = roomRepository.findById(roomId).orElseThrow(NotFoundException::new);
        room.setNowMember(room.getNowMember()+1);
    }

    // 채팅방 인원 감소
    public void minusMemberCnt(Long roomId){
        Room room = roomRepository.findById(roomId).orElseThrow(NotFoundException::new);
        room.setNowMember(room.getNowMember()-1);

        log.info("인원 : {}", room.getNowMember());
    }

    // 인증방 해체
    public void deleteRoom(Long roomId){

        roomMemberRepository.deleteAllInBatch(roomMemberRepository.findAllByRoomId(roomId).orElseThrow(NotFoundException::new));
        roomTagRepository.deleteAllInBatch(roomTagRepository.findAllByRoom(roomRepository.findById(roomId).orElseThrow(NotFoundException::new)).orElseThrow(NotFoundException::new));
        roomRepository.deleteById(roomId);

    }

    // 채팅방 공지 수정
    public void editInfo(Long roomId, String txt){
        Room room = roomRepository.findById(roomId).orElseThrow(NotFoundException::new);
        room.setInfo(txt);
        roomRepository.save(room);
    }

    // 방장 권한 위임
    public void delegate(Room room, Member manager, Member member){
        com.dodo.roommember.domain.RoomMember roomManager = roomMemberRepository.findByMemberAndRoom(manager, room).orElseThrow(NotFoundException::new);
        com.dodo.roommember.domain.RoomMember roomMember = roomMemberRepository.findByMemberAndRoom(member, room).orElseThrow(NotFoundException::new);

        roomManager.setIsManager(false);
        roomMember.setIsManager(true);

        roomMemberRepository.save(roomManager);
        roomMemberRepository.save(roomMember);
    }

    // 목표 날짜가 돼서 인증방 해체
    public void expired(){
        List<Room> roomList = roomRepository.findAll();

        for (Room room : roomList){

            if (room.getEndDay().toLocalDate().isEqual(LocalDate.now(ZoneId.of("Asia/Seoul")))){

                roomMemberRepository.findAllByRoomId(room.getId()).orElseThrow(NotFoundException::new).
                        forEach(roomMember -> roomMemberService.deleteChatRoomMember(roomMember.getRoom().getId(), roomMember.getMember().getId()));
                deleteRoom(room.getId());
            }

        }
    }

    // 방장의 인증방 설정 수정
    public void update(Long roomId, MemberContext memberContext, RoomData roomData) {
        Member member = memberRepository.findById(memberContext.getMemberId()).orElseThrow(NotFoundException::new);
        Room room = roomRepository.findById(roomId).orElseThrow(NotFoundException::new);
        com.dodo.roommember.domain.RoomMember roomMember = roomMemberRepository.findByMemberAndRoom(member, room).orElseThrow(NotFoundException::new);
        List<RoomTag> roomTags = roomTagRepository.findAllByRoom(room).orElseThrow(NotFoundException::new);

        if (!roomMember.getIsManager()) {
            log.info("you are not manager");
        }
        else {
            log.info("roomData's maxMember : {}", roomData.getMaxMember());
            room.update(
                    roomData.getName(),
                    roomData.getInfo(),
                    roomData.getMaxMember(),
                    roomData.getCanChat(),
                    roomData.getNumOfVoteSuccess(),
                    roomData.getNumOfVoteFail(),
                    roomData.getPeriodicity(),
                    roomData.getFrequency(),
                    roomData.getCertificationType(),
                    roomData.getPwd()
            );
            roomTagRepository.deleteAllInBatch(roomTags);
            roomTagService.saveRoomTag(room, roomData.getTag());

            log.info("room name : {}", room.getName());
        }
    }

    // 인증방 이미지 변경
    @Transactional
    public RoomData changeRoomImage(Long roomId, MultipartFile image) throws IOException {

        Room room = roomRepository.findById(roomId).orElseThrow(NotFoundException::new);

        if(image == null) {room.setImage(imageRepository.findById(1L).get());}
        else {
            Image img = imageService.save(image);
            room.setImage(img);}

        return RoomData.of(room);
    }

    // 유저 추방
    public void repel(Long roomId, MemberContext memberContext, Long memberId){
        Room room = roomRepository.findById(roomId).orElseThrow(NotFoundException::new);
        Member manager = memberRepository.findById(memberContext.getMemberId()).orElseThrow(NotFoundException::new);
        com.dodo.roommember.domain.RoomMember roomManager = roomMemberRepository.findByMemberAndRoom(manager, room).orElseThrow(NotFoundException::new);

        if (!roomManager.getIsManager()) {
            log.info("not manager");
        } else {
            roomMemberService.deleteChatRoomMember(roomId, memberId);
            minusMemberCnt(roomId);
        }

    }

    public RoomData getRoomInfo(Long roomId, MemberContext memberContext){
        Room room = roomRepository.findById(roomId).orElseThrow(NotFoundException::new);
        Member member = getMember(memberContext);
        com.dodo.roommember.domain.RoomMember roomMember = getRoomMember(member, room);
        RoomData roomData = RoomData.of(room);

        List<RoomTag> roomTag = roomTagRepository.findAllByRoom(room).orElseThrow(NotFoundException::new);
        List<String> tags = roomTag.stream()
                .map(RoomTag::getTag)
                .map(Tag::getName)
                .toList();
        roomData.updateTag(tags);
        roomData.updateIsManager(roomMember.getIsManager());
        return roomData;
    }

    public RoomData getRoomData(RoomData roomData, MemberContext memberContext, Room room) {
        roomMemberService.createRoomMember(memberContext, room.getId());
        roomMemberService.setManager(memberContext, room);
        roomTagService.saveRoomTag(room, roomData.getTag());


        log.info("CREATE Chat RoomId: {}", room.getId());

        RoomData roomData2 = RoomData.of(room);
        roomData2.updateIsManager(true);
        return roomData2;
    }

    public RoomJoinData getRoomDetatil(Long roomId, MemberContext memberContext) {
        Room room = getRoom(roomId);
        Member member = getMember(memberContext);
        com.dodo.roommember.domain.RoomMember roomMember = roomMemberRepository.findByMemberAndRoom(member, room).orElse(null);

        RoomJoinData roomJoinData = new RoomJoinData(room);
        roomJoinData.updateIsIn(roomMember != null);

        List<String> tags = getTags(roomId);
        roomJoinData.updateTag(tags);
        
        return roomJoinData;
    }

    public RoomListData updateIsManager(RoomListData roomListData, Member member){
        com.dodo.roommember.domain.RoomMember roomMember = roomMemberRepository.findByMemberAndRoom(member, roomRepository.findById(roomListData.getRoomId()).orElseThrow(NotFoundException::new)).orElseThrow(NotFoundException::new);

        roomListData.updateIsManager(roomMember.getIsManager());
        return roomListData;
    }

    public void upMilestone(Long roomId) {

        Room room = getRoom(roomId);
        room.setNowGoal(room.getNowGoal() + 1);
        roomRepository.save(room);

    }

    public Member getMember(MemberContext memberContext) {
        return memberRepository.findById(memberContext.getMemberId()).orElseThrow(NotFoundException::new);
    }
    public Room getRoom(Long roomId){
        return roomRepository.findById(roomId).orElseThrow(NotFoundException::new);
    }
    public com.dodo.roommember.domain.RoomMember getRoomMember(Member member, Room room){
        return roomMemberRepository.findByMemberAndRoom(member, room).orElseThrow(NotFoundException::new);
    }
    public List<String> getTags(Long roomId){
        List<RoomTag> roomTags = roomTagRepository.findAllByRoom(roomRepository.findById(roomId).orElseThrow(NotFoundException::new)).orElseThrow(NotFoundException::new);
        return roomTags.stream()
               .map(RoomTag::getTag)
               .map(Tag::getName)
               .toList();
    }

}
