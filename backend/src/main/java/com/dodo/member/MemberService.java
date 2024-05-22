package com.dodo.member;

import com.dodo.exception.NotFoundException;
import com.dodo.image.ImageRepository;
import com.dodo.image.ImageService;
import com.dodo.image.domain.Image;
import com.dodo.token.TokenService;
import com.dodo.member.domain.AuthenticationType;
import com.dodo.member.domain.PasswordAuthentication;
import com.dodo.member.domain.Member;
import com.dodo.member.domain.MemberContext;
import com.dodo.member.dto.*;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RequiredArgsConstructor
@Service
@Slf4j
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordAuthenticationRepository passwordAuthenticationRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final ImageRepository imageRepository;
    private final ImageService imageService;


    @Transactional
    public Member register(MemberCreateRequestData request) {
        if(memberRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("이메일이 이미 존재합니다");
        }

        // TODO
        // 기본 이미지 설정
        // 나중에 바꿔야 함
        Image image = imageRepository.findById(1L).get();


        log.info("{}", request.getType());
        Member member = Member.builder()
                .authenticationType(request.getType())
                .email(request.getEmail())
                .name(request.getEmail().split("@")[0])
                .mileage(0)
                .image(image)
                .introduceMessage("")
                .build();

        memberRepository.save(member);

        if(request instanceof MemberCreateRequestData.PasswordMemberCreateRequestData) {
            // 비밀번호 로그인

            var req = (MemberCreateRequestData.PasswordMemberCreateRequestData)request;
            String password = passwordEncoder.encode(req.getPassword());
            passwordAuthenticationRepository.save(new PasswordAuthentication(member, password));
        } else {
            // 소셜로그인

        }

        return member;
    }

    public String login(MemberLoginRequestData request) {
        log.info("{}", request.getAuthenticationType());
        Long memberId = getMemberId(request);
        return tokenService.makeToken(memberId);
    }


    private Long getMemberId(MemberLoginRequestData request) {
        if(request.getAuthenticationType() == AuthenticationType.PASSWORD) {
            // 비밀번호 로그인
            MemberLoginRequestData.PasswordLoginRequestData req = (MemberLoginRequestData.PasswordLoginRequestData)request;
            Member member = memberRepository.findByEmail(req.getEmail())
                    .orElseThrow(() -> new NotFoundException("유저를 찾을 수 없습니다"));
            PasswordAuthentication passwordAuthentication = passwordAuthenticationRepository.findByMember(member)
                    .orElseThrow(() -> new NotFoundException("비밀번호 인증 정보를 찾을 수 없습니다"));
            if (passwordEncoder.matches(
                    req.getPassword(),
                    passwordAuthentication.getPassword())
            ) {
                return member.getId();
            }
            throw new NotFoundException();
        } else {

        }


        return 0L;
    }

    public MemberData getMyData(MemberContext memberContext) {
        Member member = memberRepository.findById(memberContext.getMemberId())
                .orElseThrow(() -> new NotFoundException("유저를 찾을 수 없습니다"));

        return new MemberData(member);
    }

    public void update(MemberContext memberContext, MemberData memberData) {
        Member member = memberRepository.findById(memberContext.getMemberId())
                .orElseThrow(() -> new NotFoundException("유저를 찾을 수 없습니다"));

        member.update(memberData.getName(), memberData.getImage(), memberData.getIntroduceMessage());
        memberRepository.save(member);
    }

    @PostConstruct
    public void makeInitialData() {
        Image image = imageRepository.findById(1L).get();
        Member member = Member.builder()
                .authenticationType(AuthenticationType.PASSWORD)
                .email("hello@hello.com")
                .name("hello")
                .mileage(999999999)
                .image(image)
                .introduceMessage("")
                .build();

        memberRepository.save(member);
        String password = passwordEncoder.encode("123");

        passwordAuthenticationRepository.save(new PasswordAuthentication(member, password));
    }

    public boolean checkPassword(MemberContext memberContext, String password) {
        Member member = getMember(memberContext);
        PasswordAuthentication passwordAuthentication = passwordAuthenticationRepository.findByMember(member).get();
        if(!passwordEncoder.matches(password, passwordAuthentication.getPassword())) {
            throw new RuntimeException("비밀번호가 일치하지 않습니다.");
        }
        return true;
    }

    @Transactional
    public boolean changePassword(MemberContext memberContext, PasswordChangeRequestData passwordChangeRequestData) {
        Member member = getMember(memberContext);
        PasswordAuthentication passwordAuthentication = passwordAuthenticationRepository.findByMember(member).get();
        if(!passwordEncoder.matches(passwordChangeRequestData.getCurrentPassword(), passwordAuthentication.getPassword())) {
            throw new RuntimeException("나의 비밀번호가 일치하지 않습니다.");
        }
        if(!passwordChangeRequestData.getChangePassword1().equals(passwordChangeRequestData.getChangePassword2())) {
            throw new RuntimeException("새로운 비밀번호 1, 2가 일치하지 않습니다.");
        }
        if(passwordChangeRequestData.getCurrentPassword().equals(passwordChangeRequestData.getChangePassword1())) {
            throw new RuntimeException("현재 비밀번호와 새로운 비밀번호가 일치합니다.");
        }
        passwordAuthentication.setPassword(passwordEncoder.encode(passwordChangeRequestData.getChangePassword1()));
        return true;
    }


    @Transactional
    public ProfileChangeResponseData changeProfile(MemberContext memberContext, MultipartFile img, MemberUpdateRequestData requestData) throws IOException {
        Member member = getMember(memberContext);
        if(img != null) {
            Image image = imageService.save(img);
            member.setImage(image);
        }
        if(requestData != null) {
            if(requestData.getName() != null) member.setName(requestData.getName());
            if(requestData.getIntroduceMessage() != null) member.setIntroduceMessage(requestData.getIntroduceMessage());
        }
        return new ProfileChangeResponseData(member);
    }

    public ProfileRequestData getProfile(MemberContext memberContext) {
        Member member = getMember(memberContext);
        return new ProfileRequestData(member);
    }

    public Image getImage(MemberContext memberContext) {
        Member member = getMember(memberContext);
        return member.getImage();
    }

    @Transactional
    public Member getMember(MemberContext memberContext) {
        return memberRepository.findById(memberContext.getMemberId())
                .orElseThrow(() -> new NotFoundException("유저정보가 올바르지 않습니다."));
    }
}
