package com.dodo.member;

import com.dodo.config.auth.CustomAuthentication;
import com.dodo.image.domain.Image;
import com.dodo.member.domain.MemberContext;
import com.dodo.member.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("api/v1/members/")
@RequiredArgsConstructor
@Slf4j
public class MemberController {

    private final MemberService memberService;

    @PostMapping("register")
    public MemberCreateResponseData register(
          @RequestBody MemberCreateRequestData request) {
        return new MemberCreateResponseData(memberService.register(request));
    }

    @PostMapping("login")
    public MemberLoginResponseData login(
            @RequestBody MemberLoginRequestData request) {
        return new MemberLoginResponseData(memberService.login(request));
    }

    @CustomAuthentication
    @GetMapping("me")
    public MemberData getMyData(
            @RequestAttribute MemberContext memberContext
    ) {
        return memberService.getMyData(memberContext);
    }

    @CustomAuthentication
    @GetMapping("test")
    public String testHandler(
            @RequestAttribute MemberContext memberContext) {
        log.info("in testHandler : memberId = {}", memberContext.getMemberId());
        return "OK";
    }

    @CustomAuthentication
    @GetMapping("check-password")
    public boolean checkPassword(
            @RequestAttribute MemberContext memberContext,
            @RequestParam String password
    ) {
        return memberService.checkPassword(memberContext, password);
    }

    @CustomAuthentication
    @PostMapping("change-password")
    public boolean changePassword(
            @RequestAttribute MemberContext memberContext,
            @RequestBody PasswordChangeRequestData passwordChangeRequestData
    ) {
        return memberService.changePassword(memberContext, passwordChangeRequestData);
    }

    @CustomAuthentication
    @PostMapping("member-update")
    public ProfileChangeResponseData changeProfile(
            @RequestAttribute MemberContext memberContext,
            @RequestPart(required = false) MultipartFile img,
            @RequestPart(required = false) MemberUpdateRequestData requestData
    ) throws IOException {
        return memberService.changeProfile(memberContext, img, requestData);
    }

    @CustomAuthentication
    @GetMapping("simple-profile")
    public ProfileRequestData getProfile(
            @RequestAttribute MemberContext memberContext
    ) {
        return memberService.getProfile(memberContext);
    }

    @CustomAuthentication
    @GetMapping("image")
    public Image getImage(
            @RequestAttribute MemberContext memberContext
    ) {
        return memberService.getImage(memberContext);
    }
}
