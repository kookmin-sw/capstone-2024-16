package com.dodo.roommember;

import com.dodo.room.domain.Room;
import com.dodo.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RoomMemberRepository extends JpaRepository<com.dodo.roommember.domain.RoomMember, Long> {
    Optional<com.dodo.roommember.domain.RoomMember> findByMemberAndRoom(Member member, Room room);
    Optional<List<com.dodo.roommember.domain.RoomMember>> findAllByMember(Member member);
    Optional<List<com.dodo.roommember.domain.RoomMember>> findAllByRoomId(Long roomId);
    Optional<List<com.dodo.roommember.domain.RoomMember>> findAllByMemberAndRoom(Member member, Room room);
}
