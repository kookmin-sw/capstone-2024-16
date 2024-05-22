package com.dodo.certification;

import com.dodo.certification.domain.Certification;
import com.dodo.room.domain.Room;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CertificationRepository extends JpaRepository<Certification, Long> {
    Optional<List<Certification>> findAllByRoomMemberIn(List<com.dodo.roommember.domain.RoomMember> roomMemberList);
    Optional<List<Certification>> findAllByRoomMember(com.dodo.roommember.domain.RoomMember roommember);
    Optional<List<Certification>> findAllByRoomMemberId(Long roomMemberId);
    Optional<List<Certification>> findAllByRoomMemberRoom(Room room);
    Long countAllByRoomMember(com.dodo.roommember.domain.RoomMember roomMember);

    Optional<Certification> findByRoomMember(com.dodo.roommember.domain.RoomMember roomMember);
}
