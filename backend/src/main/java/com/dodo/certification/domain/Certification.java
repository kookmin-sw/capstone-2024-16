package com.dodo.certification.domain;

import com.dodo.image.domain.Image;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Data
public class Certification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "room_member_id")
    private com.dodo.roommember.domain.RoomMember roomMember;

    @Enumerated(EnumType.STRING)
    private CertificationStatus status;

    @CreatedDate
    private LocalDateTime createdTime;

    @ManyToOne
    private Image image;

    private Integer voteUp;
    private Integer voteDown;

    public void addVoteUp() { voteUp++; }
    public void subVoteUp() { voteUp--; }
    public void addVoteDown() { voteDown++; }
    public void subVoteDown() { voteDown--; }
}
