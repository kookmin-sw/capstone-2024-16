package com.dodo.sea.service;

import com.dodo.exception.NotFoundException;
import com.dodo.exception.UnauthorizedException;
import com.dodo.image.ImageService;
import com.dodo.image.domain.Image;
import com.dodo.sea.domain.Creature;
import com.dodo.sea.domain.SeaCreature;
import com.dodo.sea.dto.CreatureData;
import com.dodo.sea.dto.InventoryCreatureData;
import com.dodo.sea.dto.SeaCreatureData;
import com.dodo.sea.repository.CreatureRepository;
import com.dodo.sea.repository.SeaCreatureRepository;
import com.dodo.member.MemberRepository;
import com.dodo.member.domain.Member;
import com.dodo.member.domain.MemberContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CreatureService {
    private final CreatureRepository creatureRepository;
    private final ImageService imageService;
    private final SeaCreatureRepository seaCreatureRepository;
    private final MemberRepository memberRepository;


    public CreatureData createCreature(String name, String info, Integer price
                                       ,MultipartFile img
                                       ) throws IOException {

        Image image = imageService.save(img);

        Creature creature = Creature.builder()
                .name(name)
                .info(info)
                .price(price)
                .image(image)
                .build();

        creatureRepository.save(creature);

        return new CreatureData(creature);
    }

    // 바다생물 배치 (이동 및 활성/비활성화)
    public void updateCreature(SeaCreatureData seaCreatureData) {

        SeaCreature seaCreature = seaCreatureRepository.findById(seaCreatureData.getSeaCreatureId()).orElseThrow(NotFoundException::new);

        seaCreature.move(seaCreatureData.getCoordinate_x(), seaCreatureData.getCoordinate_y());
        seaCreature.activate(seaCreatureData.getIsActivate());

        seaCreatureRepository.save(seaCreature);

    }


    // 바다생물 구매
    public Boolean purchaseCreature(MemberContext memberContext, CreatureData creatureData) {

        Member member = memberRepository.findById(memberContext.getMemberId()).orElseThrow(NotFoundException::new);
        Creature creature = creatureRepository.findById(creatureData.getCreatureId()).orElseThrow(NotFoundException::new);

        // 유저가 갖고 있는 마일리지가 적은데 구매하려 했을 때 false를 return
        if (creature.getPrice() > member.getMileage()) {
            return false;
        }

        // 기존에 있는 생물을 구매하지 못하게
        if (seaCreatureRepository.findByMemberAndCreature(member, creature).isPresent()) {
            return false;
        }

        member.updateMileage(member.getMileage() - creature.getPrice()); // 유저의 마일리지 차감

        SeaCreature seaCreature = SeaCreature.builder()
                .creature(creature)
                .member(member)
                .isActivate(false)
                .build();

        memberRepository.save(member);
        seaCreatureRepository.save(seaCreature);

        return true;
    }

    // 상점에서 모든 생물을 보여주기 위한 함수
    public List<CreatureData> getAllCreature(MemberContext memberContext) {
        Member member = memberRepository.findById(memberContext.getMemberId()).orElseThrow(NotFoundException::new);


        List<CreatureData> creatureDataList = creatureRepository.findAll(sortByPrice()).stream()
                .map(CreatureData::new)
                .toList();

        for(CreatureData creatureData : creatureDataList){
            creatureData.updateOwn(seaCreatureRepository.findByMemberAndCreature(member, creatureRepository.findById(creatureData.getCreatureId()).orElseThrow(NotFoundException::new)).orElse(null) != null);
            creatureData.updateCreatureId(seaCreatureRepository.findById(creatureData.getCreatureId()).orElseThrow(() -> new NotFoundException("없는 아이템입니다.")).getId());
        }

        return creatureDataList;
    }

    // 유저가 보유한 생물을 보여주기 위한 함수
    public List<InventoryCreatureData> getMemberCreature(MemberContext memberContext) {
        Member member = memberRepository.findById(memberContext.getMemberId()).orElseThrow(NotFoundException::new);

        List<InventoryCreatureData> inventoryCreatureDataList = new ArrayList<>();
        for (SeaCreature seaCreature : seaCreatureRepository.findAllByMember(member).orElseThrow(NotFoundException::new)){
            Creature creature = seaCreature.getCreature();
            InventoryCreatureData inventoryCreatureData = new InventoryCreatureData(creature, seaCreature);

            inventoryCreatureDataList.add(inventoryCreatureData);
        }
        return inventoryCreatureDataList;
    }

    // 유저가 바다를 클릭했을 때 보여줄 함수
    public List<SeaCreatureData> getSeaCreatures(MemberContext memberContext){
        Member member = memberRepository.findById(memberContext.getMemberId()).orElseThrow(NotFoundException::new);

        return seaCreatureRepository.findAllByMember(member).orElseThrow(NotFoundException::new).stream()
                .filter(SeaCreature::getIsActivate)
                .map(SeaCreatureData::new)
                .toList();
    }

    public void deleteCreature(MemberContext memberContext, Long creatureId){
        if(memberContext.getMemberId()  != 1L){
            throw new UnauthorizedException("권한이 없습니다.");
        }
        creatureRepository.deleteById(creatureId);
    }

    private Sort sortByPrice(){
        return Sort.by(
                Order.desc("price"),
                Order.desc("id")
        );
    }
}
