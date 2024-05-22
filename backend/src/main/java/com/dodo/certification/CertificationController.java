package com.dodo.certification;

import com.dodo.certification.dto.*;
import com.dodo.config.auth.CustomAuthentication;
import com.dodo.config.auth.NotAuth;
import com.dodo.member.domain.MemberContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/certification")
@RequiredArgsConstructor
@CustomAuthentication
@Slf4j
public class CertificationController {
    private final CertificationService certificationService;

    // 인증 생성, 인증은 아직 X
    @PostMapping("/upload")
    public CertificationUploadResponseData makeCertification(
            @RequestAttribute MemberContext memberContext,
            @RequestParam Long roomId,
            @RequestParam MultipartFile img
    ) throws IOException {
        return certificationService.makeCertification(memberContext, roomId, img);
    }


    // 인증방의 인증들 리스트 불러오기
    @GetMapping("/list/{roomId}")
    public List<CertificationListResponseData> getList(
            @RequestAttribute MemberContext memberContext,
            @PathVariable Long roomId
    ) {
        return certificationService.getList(memberContext, roomId);
    }

    // 특정 인증 클릭했을 때 나오는 디테일 화면 보여주기
    @GetMapping("/detail/{certificationId}")
    public CertificationDetailResponseData getCertificationDetail(
            @RequestAttribute MemberContext memberContext,
            @PathVariable Long certificationId
    ) {
        return certificationService.getCertificationDetail(memberContext, certificationId);
    }


    // 투표
    @PostMapping("/vote")
    public CertificationDetailResponseData voting(
            @RequestAttribute MemberContext memberContext,
            @RequestBody VoteRequestData requestData
    ) {
        return certificationService.voting(memberContext, requestData);
    }


    // 방장 승인
    @PostMapping("/approval")
    public CertificationDetailResponseData approval(
            @RequestAttribute MemberContext memberContext,
            @RequestBody ApprovalRequestData requestData
            ) {
        return certificationService.approval(memberContext, requestData);
    }

    // AI 에서 결과값 받아오기
    @PostMapping("/ai-result")
    @NotAuth
    public String aiResult(
            @RequestBody AiResponseData aiResponseData
            ) {
        certificationService.analyze(aiResponseData);
        return "OK";
    }
}
