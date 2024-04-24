package com.dodo.room;

import com.dodo.exception.NotFoundException;
import com.dodo.room.domain.Category;
import com.dodo.room.domain.CertificationType;
import com.dodo.room.domain.Periodicity;
import com.dodo.room.domain.Room;
import com.dodo.room.dto.RoomData;
import com.dodo.room.dto.UserData;
import com.dodo.roomuser.RoomUserRepository;
import com.dodo.roomuser.RoomUserService;
import com.dodo.roomuser.domain.RoomUser;
import com.dodo.tag.domain.RoomTag;
import com.dodo.tag.repository.RoomTagRepository;
import com.dodo.tag.service.RoomTagService;
import com.dodo.user.UserRepository;
import com.dodo.user.domain.User;
import com.dodo.user.domain.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoomService {

    private final UserRepository userRepository;
    private final RoomUserRepository roomUserRepository;
    private final RoomRepository roomRepository;
    private final RoomUserService roomUserService;
    private final RoomTagRepository roomTagRepository;
    private final RoomTagService roomTagService;

    public List<RoomData> getMyRoomList(UserContext userContext) {
        User user = userRepository.findById(userContext.getUserId())
                        .orElseThrow(NotFoundException::new);
        return roomUserRepository.findAllByUser(user)
                .orElseThrow(NotFoundException::new)
                .stream()
                .map(RoomUser::getRoom)
                .map(RoomData::new)
                .toList();
    }

    public List<UserData> getUsers(UserContext userContext, Long roomId) {
        return roomUserRepository.findAllByRoomId(roomId)
                .orElseThrow(NotFoundException::new)
                .stream()
                .map(UserData::new) //TODO
                .toList();
    }

    public List<RoomData> getRoomListByCategory(Category category) {
        return roomRepository.findAllByCategory(category)
                .orElseThrow(NotFoundException::new)
                .stream()
                .map(RoomData::of)
                .toList();

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

    // 채팅방 생성
    public Room creatChatRoom(String roomName, String roomPwd, Long maxUserCnt, Category category,
                              String info, CertificationType certificationType,
                              Boolean canChat, Integer numOfVoteSuccess, Integer numOfVoteFail,
                              Integer frequency, Periodicity periodicity, LocalDateTime endDate){
        Room room = Room.builder()
                .name(roomName)
                .password(roomPwd)
                .maxUser(maxUserCnt)
                .nowUser(1L)
                .category(category)
                .info(info)
                .certificationType(certificationType)
                .periodicity(periodicity)
                .canChat(canChat)
                .endDay(endDate)
                .frequency(frequency)
                .numOfVoteSuccess(numOfVoteSuccess).numOfVoteFail(numOfVoteFail)
                .build();

        roomRepository.save(room);
        return room;
    }

    // 인증방 비밀번호 조회
    public Boolean confirmPwd(Long roomId, String roomPwd){
        return roomPwd.equals(roomRepository.findById(roomId).orElseThrow(NotFoundException::new).getPassword());
    }

    // 채팅방 인원 증가
    public void plusUserCnt(Long roomId){
        log.info("plus room Id : {}", roomId);
        Room room = roomRepository.findById(roomId).orElseThrow(NotFoundException::new);
        room.setNowUser(room.getNowUser()+1);
    }

    // 채팅방 인원 감소
    public void minusUserCnt(Long roomId){
        log.info("room Id : {}", roomId);
        Room room = roomRepository.findById(roomId).orElseThrow(NotFoundException::new);
        room.setNowUser(room.getNowUser()-1);
    }

    // 채팅방 삭제
    public void deleteRoom(Long roomId){
        Room room = roomRepository.findById(roomId).
                orElse(null);
        if (room == null){
            System.out.println("room = null");
            return;
        }
        roomRepository.deleteById(roomId);
    }

    // 채팅방 공지 수정
    public void editInfo(Long roomId, String txt){
        Room room = roomRepository.findById(roomId).orElseThrow(NotFoundException::new);
        room.setInfo(txt);
        roomRepository.save(room);
    }

    // 방장 권한 위임
    public void delegate(Room room, User manager, User user){
        RoomUser roomManager = roomUserRepository.findByUserAndRoom(manager, room).orElseThrow(NotFoundException::new);
        RoomUser roomUser = roomUserRepository.findByUserAndRoom(user, room).orElseThrow(NotFoundException::new);

        roomManager.setIsManager(false);
        roomUser.setIsManager(true);

        roomUserRepository.save(roomManager);
        roomUserRepository.save(roomUser);
    }

    // 목표 날짜가 돼서 인증방 해체
    public void expired(){
        List<Room> roomList = roomRepository.findAll();

        for (Room room : roomList){

            if (room.getEndDay().toLocalDate().isEqual(LocalDate.now(ZoneId.of("Asia/Seoul")))){

                roomUserRepository.findAllByRoomId(room.getId()).orElseThrow(NotFoundException::new).
                        forEach(roomUser -> roomUserService.deleteChatRoomUser(roomUser.getRoom().getId(), roomUser.getUser().getId()));
                deleteRoom(room.getId());
            }

        }
    }

    // 방장의 채팅방 설정 수정
    public void update(Long roomId, UserContext userContext, RoomData roomData, List<String> tags) {
        User user = userRepository.findById(userContext.getUserId()).orElseThrow(NotFoundException::new);
        Room room = roomRepository.findById(roomId).orElseThrow(NotFoundException::new);
        RoomUser roomUser = roomUserRepository.findByUserAndRoom(user, room).orElseThrow(NotFoundException::new);
        List<RoomTag> roomTags = roomTagRepository.findAllByRoom(room).orElseThrow(NotFoundException::new);

        if (!roomUser.getIsManager()) {
            log.info("you are not manager");
        }
        else {
            log.info("roomData's maxUser : {}", roomData.getMaxUser());
            room.update(
                    roomData.getName(),
                    roomData.getPwd(),
                    roomData.getInfo(),
                    roomData.getEndDay(),
                    roomData.getMaxUser(),
                    roomData.getCanChat(),
                    roomData.getNumOfVoteSuccess(),
                    roomData.getNumOfVoteFail(),
                    roomData.getImage(),
                    roomData.getPeriodicity(),
                    roomData.getFrequency(),
                    roomData.getCertificationType()
            );
            roomTagRepository.deleteAllInBatch(roomTags);
            roomTagService.saveRoomTag(room, tags);

            log.info("room name : {}", room.getName());
        }
    }

    // 유저 추방
    public void repel(Long roomId, UserContext userContext, Long userId){
        Room room = roomRepository.findById(roomId).orElseThrow(NotFoundException::new);
        User manager = userRepository.findById(userContext.getUserId()).orElseThrow(NotFoundException::new);
        RoomUser roomManager = roomUserRepository.findByUserAndRoom(manager, room).orElseThrow(NotFoundException::new);

        if (!roomManager.getIsManager()) {
            log.info("not manager");
        } else {
            roomUserService.deleteChatRoomUser(roomId, userId);
            minusUserCnt(roomId);
        }

    }

}
