package com.dodo.certification.domain;

import com.dodo.user.domain.User;
import jakarta.persistence.*;
import lombok.NoArgsConstructor;

import javax.annotation.processing.Generated;

@Entity
@NoArgsConstructor
public class Vote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private User user;

    @OneToOne
    private Certification certification;
}