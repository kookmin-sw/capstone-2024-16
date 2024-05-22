package com.dodo.member.domain;

import com.dodo.image.domain.Image;
import com.dodo.sea.domain.SeaCreature;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Getter @Setter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // password or social
    @Enumerated(EnumType.STRING)
    private AuthenticationType authenticationType;

    @Column(name = "email", unique = true)
    private String email;
    private String name;
    private Integer mileage;
    private String introduceMessage;

    @OneToMany(mappedBy = "member")
    private List<com.dodo.roommember.domain.RoomMember> roomMembers;

    @Setter
    @OneToMany
    private List<SeaCreature> seaCreatures;

    @ManyToOne
    private Image image;

    public void updateMileage(Integer mileage) {
        this.mileage = mileage;
    }

    public void update(String name, Image image, String introduceMessage) {
        this.name = name;
        this.image = image;
        this.introduceMessage = introduceMessage;
    }
}
