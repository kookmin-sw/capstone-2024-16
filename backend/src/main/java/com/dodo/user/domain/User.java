package com.dodo.user.domain;

import com.dodo.image.domain.Image;
import com.dodo.roomuser.domain.RoomUser;
import com.dodo.sea.domain.SeaCreature;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@Builder
@AllArgsConstructor
@Table(name = "user")
public class User {

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
    private String firebaseToken;

    @OneToMany(mappedBy = "user")
    private List<RoomUser> roomUsers;

    @Setter
    @OneToMany
    private List<SeaCreature> seaCreatures;

    @ManyToOne
    private Image image;

    public void updateMileage(Integer mileage) {
        this.mileage = mileage;
    }
}
