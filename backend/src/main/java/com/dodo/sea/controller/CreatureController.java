package com.dodo.sea.controller;

import com.dodo.config.auth.CustomAuthentication;
import com.dodo.exception.NotFoundException;
import com.dodo.sea.dto.CreatureData;
import com.dodo.sea.dto.InventoryCreatureData;
import com.dodo.sea.dto.SeaCreatureData;
import com.dodo.sea.repository.SeaCreatureRepository;
import com.dodo.sea.service.CreatureService;
import com.dodo.member.MemberRepository;
import com.dodo.member.domain.Member;
import com.dodo.member.domain.MemberContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/creature")
@Slf4j
public class CreatureController {
    private final CreatureService creatureService;
    private final MemberRepository memberRepository;
    private final SeaCreatureRepository seaCreatureRepository;


    // TODO
    // 일단 이미지는 주석처리 해놨음. 주석만 빼면 됨
    // membercontext로 admin인지 아닌지를 판별해서 업데이트 하도록 하는게 좋을 듯함.
    @PostMapping("/create")
    @ResponseBody
    //@CustomAuthentication
    public CreatureData createCreature(//@RequestAttribute MemberContext memberContext,
                                       @RequestParam MultipartFile img,
                                       @RequestParam String name,
                                       @RequestParam String info,
                                       @RequestParam Integer price) throws IOException {

        return creatureService.createCreature(name, info, price, img);
    }

    @PostMapping("/purchase")
    @ResponseBody
    @CustomAuthentication
    public Boolean purchase(@RequestAttribute MemberContext memberContext, @RequestBody CreatureData creatureData) {

        return creatureService.purchaseCreature(memberContext, creatureData); // false면 구매 불가, true면 구매 완료.
    }

    @GetMapping("/store")
    @CustomAuthentication
    public List<CreatureData> store(@RequestAttribute MemberContext memberContext){

        return creatureService.getAllCreature(memberContext);
    }

    @GetMapping("/inventory")
    @CustomAuthentication
    public List<InventoryCreatureData> getMemberInventory(@RequestAttribute MemberContext memberContext){
        return creatureService.getMemberCreature(memberContext);
    }

    // 바다 미리보기에서 저장버튼으로 유저의 바다를 업데이트 하는 postmapping
    @PostMapping("/update-sea")
    public String moveCreature(@RequestBody SeaCreatureData seaCreatureData) {
        creatureService.updateCreature(seaCreatureData);

        return "200 OK";
    }

    // 유저가 바다를 클릭했을 때 보여줄 함수
    @GetMapping("/sea")
    @CustomAuthentication
    public List<SeaCreatureData> displaySea(@RequestAttribute MemberContext memberContext){
        return creatureService.getSeaCreatures(memberContext);
    }

    // TODO
    // 임시 마일리지 얻는 함수
    @PostMapping("/member-get-mileage")
    @CustomAuthentication
    public void getMileage(@RequestAttribute MemberContext memberContext){
        Member member = memberRepository.findById(memberContext.getMemberId()).orElseThrow(NotFoundException::new);
        member.updateMileage(10000);
        memberRepository.save(member);
    }

    @PostMapping("/delete")
    @CustomAuthentication
    public String deleteCreature(@RequestAttribute MemberContext memberContext, @RequestParam Long creatureId){
        creatureService.deleteCreature(memberContext, creatureId);

        return "OK";
    }
}
