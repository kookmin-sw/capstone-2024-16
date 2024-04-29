package com.dodo.room;

import com.dodo.config.auth.CustomAuthentication;
import com.dodo.fcm.FCMNotificationRequestDto;
import com.dodo.fcm.FCMNotificationService;
import com.dodo.room.domain.Category;
import com.dodo.room.dto.RoomData;
import com.dodo.room.dto.UserData;
import com.dodo.tag.repository.RoomTagRepository;
import com.dodo.tag.service.RoomTagService;
import com.dodo.user.domain.UserContext;
import lombok.RequiredArgsConstructor;
import com.dodo.room.domain.Room;
import com.dodo.exception.NotFoundException;
import com.dodo.room.dto.RoomData;
import com.dodo.room.dto.UserData;
import com.dodo.roomuser.RoomUserRepository;
import com.dodo.roomuser.RoomUserService;
import com.dodo.roomuser.domain.RoomUser;
import com.dodo.user.UserRepository;
import com.dodo.user.domain.User;
import com.dodo.user.domain.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping("/api/v1/room")
@RequiredArgsConstructor
@CustomAuthentication
@Slf4j
public class RoomController {

    private final RoomService roomService;
    private final RoomUserService roomUserService;
    private final RoomUserRepository roomUserRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final RoomTagService roomTagService;
    private final RoomTagRepository roomTagRepository;
    private final FCMNotificationService fcmNotificationService;

    @GetMapping("/list")
    public List<RoomData> getMyRoomList(
            @RequestAttribute UserContext userContext
    ) {
        return roomService.getMyRoomList(userContext);
    }


    // TODO
    // UserData 라는 이름으로 괜찮을지,,
    @GetMapping("/users")
    public List<UserData> getUsersInTheRoom(
            @RequestAttribute UserContext userContext,
            @RequestParam Long roomId) {
        return roomService.getUsers(userContext, roomId);
    }

    // 카테고리로 방 찾기
    @GetMapping("/get-rooms-by-category")
    public List<RoomData> getRoomsByCategory(@RequestParam Category category) {
        return roomService.getRoomListByCategory(category);
    }

    // 인증방 생성
    @PostMapping("/create-room")
    @ResponseBody
    public RoomData createRoom(@RequestBody RoomData roomData, @RequestAttribute UserContext userContext){
        Room room = roomService.creatChatRoom(roomData.getName(), roomData.getPwd(),
                roomData.getMaxUser(), roomData.getCategory(), roomData.getInfo(),
                roomData.getCertificationType(), roomData.getCanChat(),
                roomData.getNumOfVoteSuccess(), roomData.getNumOfVoteSuccess(),
                roomData.getFrequency(), roomData.getPeriodicity(), roomData.getEndDay());

        roomUserService.createRoomUser(userContext, room.getId());
        roomUserService.setManager(userContext, room);
        roomTagService.saveRoomTag(room, roomData.getTag());

        log.info("CREATE Chat RoomId: {}", room.getId());

        return RoomData.of(room);
    }

    // 인증방 처음 입장
    @CustomAuthentication
    @PostMapping("/enter/{roomId}")
    public String roomEnter(@PathVariable Long roomId, @RequestAttribute UserContext userContext){

        roomService.plusUserCnt(roomId);
        roomUserService.createRoomUser(userContext, roomId);

        log.info("createRoomUser");
        Room room = roomRepository.findById(roomId).orElseThrow(NotFoundException::new);

        return "number of user : " + RoomData.of(room).getNowUsers();
    }

    // 비공개 인증방 입장시 비밀번호 확인 절차
    @GetMapping("/confirmPwd/{roomId}")
    @ResponseBody
    public Boolean confirmPwd(@PathVariable Long roomId, @RequestParam String roomPwd){
        return roomService.confirmPwd(roomId, roomPwd);
    }

    // 인증방 나가기
    @CustomAuthentication
    @GetMapping("/room-out/{roomId}")
    public String roomOut(@PathVariable Long roomId, @RequestAttribute UserContext userContext){

        roomService.minusUserCnt(roomId);
        roomUserService.deleteChatRoomUser(roomId, userContext.getUserId());


        // 확인
        Room room = roomRepository.findById(roomId).orElseThrow(NotFoundException::new);
        User user = userRepository.findById(userContext.getUserId()).orElseThrow(NotFoundException::new);

        RoomUser roomUser = roomUserRepository.findByUserAndRoom(user, room)
                .orElse(null);
        if (roomUser == null) {
            return "해당 유저는 인증방에서 삭제되었습니다." + "\n" +
                    "nowUser = " + room.getNowUser();
        }
        else {
            return "유저가 인증방에서 삭제되지 않았습니다.." ;
        }
    }

    // 인증방 해체하기
    @CustomAuthentication
    @GetMapping("/delete-room/{roomId}")
    public String roomDelete(@PathVariable Long roomId, @RequestAttribute UserContext userContext) {

        if (!roomUserRepository.findByUserAndRoom(
                        userRepository.findById(userContext.getUserId()).orElseThrow(NotFoundException::new),
                        roomRepository.findById(roomId).orElseThrow(NotFoundException::new))
                .orElseThrow(NotFoundException::new).getIsManager()) {
            return "방장이 아닙니다.";
        }

        roomUserRepository.deleteAllInBatch(roomUserRepository.findAllByRoomId(roomId).orElseThrow(NotFoundException::new));
        roomTagRepository.deleteAllInBatch(roomTagRepository.findAllByRoom(roomRepository.findById(roomId).orElseThrow(NotFoundException::new)).orElseThrow(NotFoundException::new));
        roomRepository.deleteById(roomId);

        log.info("삭제 완료");
        // 확인
        List<RoomUser> roomUserList = roomUserRepository.findAllByRoomId(roomId)
                .orElse(null);
        log.info("roomUserList null");
        if (roomUserList.isEmpty()) {
            return "인증방 해체 완료";
        }

        return "Error";
    }

    // 공지 수정하기
    @CustomAuthentication
    @PostMapping("/edit-info")
    public RoomData editInfo(@RequestBody RoomData roomData, @RequestAttribute UserContext userContext, @RequestParam String txt) {
        Room room = roomRepository.findById(roomData.getRoomId()).orElseThrow(NotFoundException::new);
        User user = userRepository.findById(userContext.getUserId()).orElseThrow(NotFoundException::new);
        RoomUser roomUser = roomUserRepository.findByUserAndRoom(user, room).orElseThrow(NotFoundException::new);

        if (!roomUser.getIsManager()) {
            return RoomData.of(room);
        }

        roomService.editInfo(room.getId(), txt);

        return RoomData.of(room);
    }

    // 방장 위임하기
    @CustomAuthentication
    @PostMapping("/delegate")
    public Boolean delegate(@RequestBody RoomData roomData, @RequestParam Long userId, @RequestAttribute UserContext userContext){
        Room room = roomRepository.findById(roomData.getRoomId()).orElseThrow(NotFoundException::new);
        User manager = userRepository.findById(userContext.getUserId()).orElseThrow(NotFoundException::new);
        User user = userRepository.findById(userId).orElseThrow(NotFoundException::new);

        roomService.delegate(room, manager, user);

        return roomUserRepository.findByUserAndRoom(user, room).orElseThrow(NotFoundException::new).getIsManager();
    }

    // 목표 날짜가 돼서 인증방 해체
    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
    @GetMapping("/room-expired")
    public void expired() {
        roomService.expired();
    }

    // 방장의 인증방 설정 수정
    @CustomAuthentication
    @PostMapping("/update")
    @ResponseBody
    public RoomData update(@RequestBody RoomData roomData, @RequestAttribute UserContext userContext, @RequestParam Long roomId, @RequestParam List<String> tags){
        roomService.update(roomId, userContext, roomData, tags);

        return RoomData.of(roomRepository.findById(roomId).orElseThrow(NotFoundException::new));
    }

    // 유저 추방
    @CustomAuthentication
    @PostMapping("/repel")
    public String repel(@RequestParam Long roomId, @RequestAttribute UserContext userContext, @RequestParam Long userId) {
        roomService.repel(roomId, userContext, userId);


        // 확인
        RoomUser roomUser = roomUserRepository.findByUserAndRoom(userRepository.findById(userId).get(), roomRepository.findById(roomId).get())
                .orElse(null);

        if (roomUser == null) {
            return "해당 유저는 인증방에서 삭제되었습니다." + "\n" +
                    "nowUser = " + roomRepository.findById(roomId).get().getNowUser();
        }
        else {
            return "유저가 인증방에서 삭제되지 않았습니다.." ;
        }
    }

    // 인증방 제목으로 검색
    @GetMapping("/search-room")
    @ResponseBody
    public List<RoomData> getRoomListByNameAndTag(@RequestParam String string) {
        List<RoomData> roomListByName = roomService.getRoomListByName(string);
        List<RoomData> roomListByTag = roomTagService.getRoomListByTag(string);
        try {
            List<RoomData> roomListById = roomService.getRoomListById(Long.parseLong(string));

            return Stream.of(roomListByName, roomListByTag, roomListById)
                    .flatMap(Collection::stream)
                    .distinct()
                    .collect(Collectors.toList());

        } catch (NumberFormatException e) {

            return Stream.of(roomListByName, roomListByTag)
                    .flatMap(Collection::stream)
                    .distinct()
                    .collect(Collectors.toList());
        }


    }

}
