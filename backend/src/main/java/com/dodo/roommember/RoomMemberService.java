package com.dodo.roommember;

import com.dodo.certification.domain.Certification;
import com.dodo.certification.domain.CertificationStatus;
import com.dodo.exception.NotFoundException;
import com.dodo.room.RoomRepository;
import com.dodo.room.domain.Room;
import com.dodo.room.dto.RoomListData;
import com.dodo.member.MemberRepository;
import com.dodo.member.domain.Member;
import com.dodo.member.domain.MemberContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoomMemberService {

    private final RoomMemberRepository roomMemberRepository;
    private final MemberRepository memberRepository;
    private final RoomRepository roomRepository;

    public void createRoomMember(MemberContext memberContext, Long roomId) {
        Room room = roomRepository.findById(roomId).orElseThrow(NotFoundException::new);
        Member member = memberRepository.findById(memberContext.getMemberId()).orElseThrow(NotFoundException::new);
        com.dodo.roommember.domain.RoomMember roomMember = com.dodo.roommember.domain.RoomMember.builder()
                .member(member)
                .room(room)
                .build();

        roomMemberRepository.save(roomMember);
    }

    public void setManager(MemberContext memberContext, Room room){
        Member member = memberRepository.findById(memberContext.getMemberId()).orElseThrow(NotFoundException::new);
        com.dodo.roommember.domain.RoomMember roomMember = roomMemberRepository.findByMemberAndRoom(member, room).orElseThrow(NotFoundException::new);

        roomMember.setIsManager(true);
        roomMemberRepository.save(roomMember);
    }

    // 룸유저 연결 엔티티 삭제
    public void deleteChatRoomMember(Long roomId, Long memberId){
        Room room = roomRepository.findById(roomId).orElseThrow(NotFoundException::new);
        Member member = memberRepository.findById(memberId).orElseThrow(NotFoundException::new);

        com.dodo.roommember.domain.RoomMember roomMember = roomMemberRepository.findByMemberAndRoom(member, room)
                .orElse(null);
        if (roomMember == null) {
            log.info("없는 유저입니다.");
            return;
        }
        if (roomMember.getIsManager()) {
            List<com.dodo.roommember.domain.RoomMember> roomMembers = roomMemberRepository.findAllByRoomId(roomId).get();
            if (roomMembers.size() > 1) {
                com.dodo.roommember.domain.RoomMember ru = roomMembers.get(1);
                ru.setIsManager(true);
                roomMemberRepository.save(ru);
            }
        }

        roomMemberRepository.delete(roomMember);

        log.info("삭제한 room : {}, member : {}", roomMember.getRoom().getId(), roomMember.getMember().getId());
    }

    // TODO
    // 유저가 방에 처음 입장하면 인증 정보가 없음. 그래서 certificationList가 empty일 때 상태를 지정해놓음. (임시)
    public CertificationStatus getCertificationStatus(MemberContext memberContext, RoomListData roomListData){
        Member member = memberRepository.findById(memberContext.getMemberId()).orElseThrow(NotFoundException::new);
        Room room = roomRepository.findById(roomListData.getRoomId()).orElseThrow(NotFoundException::new);
        List<Certification> certificationList = roomMemberRepository.findByMemberAndRoom(member, room).orElseThrow(NotFoundException::new)
                .getCertification();

        // 기본 FAIL 로 설정해둠
        if (certificationList == null || certificationList.isEmpty()) { return CertificationStatus.FAIL; }

        return certificationList.get(certificationList.size() - 1).getStatus();
    }
}
