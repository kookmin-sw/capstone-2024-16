package com.dodo.roomuser;

import com.dodo.room.domain.Room;
import com.dodo.roomuser.domain.RoomUser;
import com.dodo.user.UserRepository;
import com.dodo.user.domain.User;
import com.dodo.user.domain.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoomUserService {

    private final RoomUserRepository roomUserRepository;
    private final UserRepository userRepository;

    public void createRoomUser(UserContext userContext, Room room) {
        User user = userRepository.findById(userContext.getUserId()).get();
        RoomUser roomUser = RoomUser.builder()
                .user(user)
                .room(room)
                .build();

        roomUserRepository.save(roomUser);
    }

    public void setManager(UserContext userContext, Room room){
        User user = userRepository.findById(userContext.getUserId()).get();
        RoomUser roomUser = roomUserRepository.findByUserAndRoom(user, room).get();

        roomUser.setIsManager(true);
        roomUserRepository.save(roomUser);
    }

    // 룸유저 연결 엔티티 삭제
    public void deleteChatRoomUser(Room room, User user){
        RoomUser roomUser = roomUserRepository.findByUserAndRoom(user, room)
                .orElse(null);
        if (roomUser == null) {
            System.out.println("roomUser = null");
            return;
        }
        roomUserRepository.delete(roomUser);

        log.info("삭제한 room : {}, user : {}", roomUser.getRoom().getId(), roomUser.getUser().getId());
    }
}
