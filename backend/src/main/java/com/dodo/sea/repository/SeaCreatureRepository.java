package com.dodo.sea.repository;

import com.dodo.sea.domain.Creature;
import com.dodo.sea.domain.SeaCreature;
import com.dodo.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SeaCreatureRepository extends JpaRepository<SeaCreature, Long> {
    Optional<List<SeaCreature>> findAllByMember(Member member);
    Optional<SeaCreature> findByMemberAndCreature(Member member, Creature creature);
    Optional<SeaCreature> findById(Long id);
}
