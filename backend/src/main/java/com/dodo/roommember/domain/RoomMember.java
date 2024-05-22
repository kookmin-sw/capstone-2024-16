package com.dodo.roommember.domain;

import com.dodo.certification.domain.Certification;
import com.dodo.room.domain.Room;
import com.dodo.member.domain.Member;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Builder
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor
public class RoomMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Builder.Default
    @Setter
    private Boolean isManager = false;

    @Builder.Default
    @Setter
    private Integer certificateTime = 0;

    @ManyToOne
    private Member member;

    @CreatedDate
    private LocalDateTime createdTime;

    @ManyToOne
    private Room room;

    @OneToMany(mappedBy = "roomMember")
    private List<Certification> certification;

    public RoomMember(Member member, Room room) {
        this.room = room;
        this.member = member;
    }
}
