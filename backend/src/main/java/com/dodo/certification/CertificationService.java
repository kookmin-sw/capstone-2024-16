package com.dodo.certification;

import com.dodo.certification.domain.Certification;
import com.dodo.certification.domain.CertificationStatus;
import com.dodo.certification.domain.Vote;
import com.dodo.certification.domain.VoteStatus;
import com.dodo.certification.dto.*;
import com.dodo.exception.NotFoundException;
import com.dodo.exception.UnauthorizedException;
import com.dodo.image.ImageService;
import com.dodo.image.domain.Image;
import com.dodo.room.RoomRepository;
import com.dodo.room.domain.Category;
import com.dodo.room.domain.Periodicity;
import com.dodo.room.domain.Room;
import com.dodo.room.domain.RoomType;
import com.dodo.statistics.StatisticsService;
import com.dodo.member.MemberRepository;
import com.dodo.member.domain.Member;
import com.dodo.member.domain.MemberContext;
import jakarta.transaction.Transactional;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CertificationService {

    @Value("${ai.server-url}")
    private String AI_SERVER_URL;


    private final CertificationRepository certificationRepository;
    private final MemberRepository memberRepository;
    private final RoomRepository roomRepository;
    private final com.dodo.roommember.RoomMemberRepository roomMemberRepository;
    private final VoteRepository voteRepository;
    private final ImageService imageService;
    private final StatisticsService statisticsService;

    private static final int DAILY_SUCCESS_UPDATE_MILEAGE = 10;
    private static final int WEEKLY_SUCCESS_UPDATE_MILEAGE = 50;

    public CertificationUploadResponseData makeCertification(MemberContext memberContext, Long roomId, MultipartFile img) throws IOException {
        Member member = getMember(memberContext);
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new NotFoundException("인증방 정보를 찾을 수 없습니다"));
        com.dodo.roommember.domain.RoomMember roomMember = roomMemberRepository.findByMemberAndRoom(member, room)
                .orElseThrow(() -> new NotFoundException("인증방에 소속되어 있지 않습니다"));
        Image image = imageService.save(img);

        Certification certification = certificationRepository.save(Certification.builder()
                .status(CertificationStatus.WAIT)
                .roomMember(roomMember)
                .image(image)
                .voteUp(0)
                .voteDown(0)
                .build());

        // 기상인증인 경우
        // TODO -> 기준 시각정보가 룸에 있어야 함
        if(room.getCategory() == Category.WAKEUP) {
            if(checkTime(room)) {
                certification.setStatus(CertificationStatus.SUCCESS);
            } else {
                certification.setStatus(CertificationStatus.FAIL);
            }
        }

        // AI인증방인 경우 AI에 요청 보내기
        if(room.getRoomType() == RoomType.AI) {
            transferToAi(room, certification);
        }

        upCertificateTime(roomId, memberContext);
        return new CertificationUploadResponseData(certification);
    }

    // 기상 시간 체크하고 기준 시간과 비교함
    private boolean checkTime(Room room) {
        // TODO -> 방에서 기준 시각 가져와야 함
        LocalDateTime standard = null;
        int standardMinute = standard.getMinute() + standard.getHour() * 60;
        int startMinute = (standardMinute - 30 + 1440) % 1440;
        int endMinute = (standardMinute + 30) % 1440;

        LocalDateTime now = LocalDateTime.now();
        int nowMinute = now.getMinute() + now.getHour() * 60;

        // 24시에 걸친 경우
        if(standardMinute - 30 < 0 || standardMinute + 30 > 1440) return startMinute <= nowMinute || nowMinute <= endMinute;
        return startMinute <= nowMinute && nowMinute <= endMinute;
    }

    // TODO
    //  AI api URL
    private void transferToAi(Room room, Certification certification) {
        RestTemplate rt = new RestTemplate();

        AiRequestData aiRequestData = AiRequestData.builder()
                .certificationId(certification.getId())
                .category(room.getCategory())
                .image(certification.getImage().getUrl())
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");

        HttpEntity<AiRequestData> entity = new HttpEntity<AiRequestData>(aiRequestData, headers);

        rt.exchange(AI_SERVER_URL, HttpMethod.POST, entity, String.class);
    }



    public CertificationDetailResponseData getCertificationDetail(
            MemberContext memberContext,
            Long certificationId
    ) {
        Member member = getMember(memberContext);
        Certification certification = certificationRepository.findById(certificationId)
                .orElseThrow(() -> new NotFoundException("인증 정보를 찾을 수 없습니다"));
        Vote vote = voteRepository.findByMemberAndCertification(member, certification).orElse(null);
        Room room = certification.getRoomMember().getRoom();
        return new CertificationDetailResponseData(certification, vote, room);
    }

    @Transactional
    public CertificationDetailResponseData voting(MemberContext memberContext, VoteRequestData requestData) {
        Member member = getMember(memberContext);
        Certification certification = certificationRepository.findById(requestData.getCertificationId())
                .orElseThrow(() -> new NotFoundException("인증 정보를 찾을 수 없습니다"));
        Room room = certification.getRoomMember().getRoom();

        Vote vote = voteRepository.findByMemberAndCertification(member, certification)
                .orElse(new Vote(member, certification));

        if(vote.getVoteStatus() == VoteStatus.NONE) {
            if(requestData.getVoteStatus() == VoteStatus.UP) certification.addVoteUp();
            else certification.addVoteDown();
        }
        if(vote.getVoteStatus() == VoteStatus.UP && requestData.getVoteStatus() == VoteStatus.DOWN) {
            certification.addVoteDown();
            certification.subVoteUp();
        }
        if(vote.getVoteStatus() == VoteStatus.DOWN && requestData.getVoteStatus() == VoteStatus.UP) {
            certification.addVoteUp();
            certification.subVoteDown();
        }

        vote.setVoteStatus(requestData.getVoteStatus());
        voteRepository.save(vote);


        // TODO -> 인증 완료, 실패시 알림 제공
        if(certification.getVoteUp().equals(room.getNumOfVoteSuccess())) {
            certification.setStatus(CertificationStatus.SUCCESS);
            successCertificationToUpdateMileage(certification);
        }

        if(certification.getVoteDown().equals(room.getNumOfVoteFail())) {
            certification.setStatus(CertificationStatus.FAIL);
            downCertificateTime(room.getId(), memberContext);
        }

        return new CertificationDetailResponseData(certification, vote, room);
    }

    // 방장 승인, 거부
    @Transactional
    public CertificationDetailResponseData approval(MemberContext memberContext, ApprovalRequestData requestData) {
        Member member = getMember(memberContext);
        Certification certification = certificationRepository.findById(requestData.getCertificationId())
                .orElseThrow(() -> new NotFoundException("인증 정보를 찾을 수 없습니다"));
        Room room = certification.getRoomMember().getRoom();
        com.dodo.roommember.domain.RoomMember roomMember = roomMemberRepository.findByMemberAndRoom(member, room)
                .orElseThrow(() -> new NotFoundException("인증방에 소속되어 있지 않습니다"));
        if(roomMember.getIsManager()) {
            certification.setStatus(requestData.getStatus());
            if(certification.getStatus() == CertificationStatus.SUCCESS) {
                successCertificationToUpdateMileage(certification);
            }
        } else {
            throw new UnauthorizedException("방장이 아닙니다");
        }

        return new CertificationDetailResponseData(certification, null, room);
    }

    // 인증방의 인증 리스트 불러오기
    public List<CertificationListResponseData> getList(MemberContext memberContext, Long roomId) {
        Member member = getMember(memberContext);
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new NotFoundException("인증방을 찾을 수 없습니다"));

        List<com.dodo.roommember.domain.RoomMember> roomMemberList = roomMemberRepository.findAllByRoomId(roomId)
                .orElseThrow(() -> new NotFoundException("인증방의 회원을 찾을 수 엇습니다"));


        LocalDateTime today = LocalDateTime.now();

        List<CertificationGroup> groupList = new ArrayList<>();

        // -> 인증 기록들 중에 오늘인것 찾는다.
        // -> 주간 인증의 경우 이번주의 인증 기록들을 찾는다.
        // -> 같은 유저것들로 묶는다.
        if(room.getPeriodicity() == Periodicity.DAILY) {

            String todayString = today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            Map<com.dodo.roommember.domain.RoomMember, List<Certification>> certificationMap = certificationRepository.findAllByRoomMemberIn(roomMemberList)
                    .orElse(new ArrayList<>())
                    .stream()
                    .filter(c -> c.getCreatedTime()
                            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                            .equals(todayString))
                    .collect(Collectors.groupingBy(Certification::getRoomMember));

            grouping(roomMemberList, groupList, certificationMap);

        } else if(room.getPeriodicity() == Periodicity.WEEKLY){

            List<LocalDateTime> thisWeek = getThisWeek();
            Map<com.dodo.roommember.domain.RoomMember, List<Certification>> certificationMap = certificationRepository.findAllByRoomMemberIn(roomMemberList)
                    .orElse(new ArrayList<>())
                    .stream()
                    .filter(c -> {
                        LocalDateTime ctime = c.getCreatedTime();
                        return ctime.isAfter(thisWeek.get(0)) && ctime.isBefore(thisWeek.get(1));
                    })
                    .collect(Collectors.groupingBy(Certification::getRoomMember));

            grouping(roomMemberList, groupList, certificationMap);

        }

        return groupList.stream()
                .map(CertificationListResponseData::new)
                .toList();
    }

    // 일단위, 주단위로 모인 인증들을 토대로 인증방에서 어떻게 보여줄지 그룹핑함
    // -> 맵의 리스트를 돌며 wait, success 개수를 센다.
    // -> roommember와 함께 클래스에 넣어서 리스트를 만든다.
    private List<CertificationListResponseData> grouping(List<com.dodo.roommember.domain.RoomMember> roomMemberList, List<CertificationGroup> groupList, Map<com.dodo.roommember.domain.RoomMember, List<Certification>> certificationMap) {
        roomMemberList.forEach(
                roomMember -> {
                    CertificationGroup group = new CertificationGroup(roomMember);
                    List<Certification> certificationList = certificationMap.get(roomMember);
                    if(certificationList != null) {
                        certificationList
                                .forEach(c -> {
                                    if(c.getStatus() == CertificationStatus.WAIT) group.addWait();
                                    else if(c.getStatus() == CertificationStatus.SUCCESS) group.addSuccess();
                                    if(c.getStatus() == CertificationStatus.WAIT || c.getStatus() == CertificationStatus.SUCCESS) group.addCertification(c);
                                });
                    }
                    groupList.add(group);
                }
        );

        return groupList.stream()
                .map(CertificationListResponseData::new)
                .toList();
    }

    // AI로부터 정보를 받아와 인증 여부를 파악함.
    @Data
    @EqualsAndHashCode
    public static class CertificationGroup {
        private com.dodo.roommember.domain.RoomMember roomMember;
        private List<Certification> certificationList;
        private Integer wait;
        private Integer success;
        public CertificationGroup(com.dodo.roommember.domain.RoomMember roomMember) {
            this.roomMember = roomMember;
            this.wait = 0;
            this.success = 0;
            this.certificationList = new ArrayList<>();
        }
        public void addWait() { this.wait += 1; }
        public void addSuccess() { this.success += 1; }
        public void addCertification(Certification certification) {
            this.certificationList.add(certification);
        }
    }

    public void analyze(AiResponseData aiResponseData) {
        Category category = aiResponseData.getCategory();
        Certification certification = certificationRepository.findById(aiResponseData.getCertificationId())
                .orElseThrow(() -> new NotFoundException("인증 정보를 찾을 수 없습니다"));
        if(aiResponseData.getCode() == 500) {
            // AI서버에서 인식 못함
            return ;
        }

        if(category == Category.STUDY) {
            Integer minute = extractTimeAndConvertToMinute(aiResponseData);
        } else if(category == Category.GYM) {

        }
    }


    private Integer extractTimeAndConvertToMinute(AiResponseData aiResponseData) {
        List<String> resultList = aiResponseData.getResult().get();

        for (String str : resultList) {
            if (str.contains(":")) {
                int idx = str.indexOf(":");


            }
        }
        return null;
    }

    private Member getMember(MemberContext memberContext) {
        return memberRepository.findById(memberContext.getMemberId())
                .orElseThrow(() -> new NotFoundException("유저를 찾을 수 없습니다"));
    }

    private List<LocalDateTime> getThisWeek() {
        LocalDateTime now = LocalDateTime.now();
        DayOfWeek dayOfWeek = now.getDayOfWeek();
        LocalDateTime sunday = now.plusDays(7 - dayOfWeek.getValue()).with(LocalTime.MAX);
        LocalDateTime monday = now.minusDays(6).with(LocalTime.MIN);
        return Arrays.asList(monday, sunday);
    }

    // 성공한 인증에 대해서
    // 일간인증인지 주간인증인지 파악하고
    // 조건을 만족한다면 마일리지를 제공
    private void successCertificationToUpdateMileage(Certification certification) {
        Member successMember = certification.getRoomMember().getMember();
        Room room = certification.getRoomMember().getRoom();

        if(room.getPeriodicity() == Periodicity.DAILY) {
            // 일간

            successMember.updateMileage(successMember.getMileage() + DAILY_SUCCESS_UPDATE_MILEAGE);
        } else {
            // 주간
            // 주간 인증횟수 -> 주 n회 인증방이라면 n번쨰 인증 성공 시 50웑을 준다.

            com.dodo.roommember.domain.RoomMember roomMember = certification.getRoomMember();
            List<LocalDateTime> thisWeek = statisticsService.getThisWeek();
            List<Certification> certificationList = certificationRepository.findAllByRoomMember(roomMember)
                    .orElse(new ArrayList<>());
            long count = certificationList.stream()
                    .filter(ct -> ct.getStatus() == CertificationStatus.SUCCESS
                   && ct.getCreatedTime().isAfter(thisWeek.get(0))
                   && ct.getCreatedTime().isBefore(thisWeek.get(1))
                    ).count();
            if(count == room.getFrequency()) {
                successMember.updateMileage(successMember.getMileage() + WEEKLY_SUCCESS_UPDATE_MILEAGE);
            }
        }
    }

    public void upCertificateTime(Long roomId, MemberContext memberContext){
        Room room = roomRepository.findById(roomId).orElseThrow(NotFoundException::new);
        Member member = getMember(memberContext);
        com.dodo.roommember.domain.RoomMember roomMember = roomMemberRepository.findByMemberAndRoom(member, room).orElseThrow(NotFoundException::new);

        roomMember.setCertificateTime(roomMember.getCertificateTime() + 1);
        roomMemberRepository.save(roomMember);
    }

    public void downCertificateTime(Long roomId, MemberContext memberContext){
        Room room = roomRepository.findById(roomId).orElseThrow(NotFoundException::new);
        Member member = getMember(memberContext);
        com.dodo.roommember.domain.RoomMember roomMember = roomMemberRepository.findByMemberAndRoom(member, room).orElseThrow(NotFoundException::new);

        roomMember.setCertificateTime(roomMember.getCertificateTime() - 1);
        roomMemberRepository.save(roomMember);
    }

    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
    public void initDailyCertificateTime(){
        List<com.dodo.roommember.domain.RoomMember> roomMemberList = roomMemberRepository.findAll().stream()
                .filter(roomMember -> roomMember.getRoom().getPeriodicity() == Periodicity.DAILY)
                .toList();
        for (com.dodo.roommember.domain.RoomMember roomMember : roomMemberList){
            roomMember.setCertificateTime(0);
            roomMemberRepository.save(roomMember);
        }
    }

    @Scheduled(cron = "0 0 0 ? * 1", zone = "Asia/Seoul")
    public void initWeeklyCertificateTime(){
        List<com.dodo.roommember.domain.RoomMember> roomMemberList = roomMemberRepository.findAll().stream()
                .filter(roomMember -> roomMember.getRoom().getPeriodicity() == Periodicity.WEEKLY)
                .toList();
        for (com.dodo.roommember.domain.RoomMember roomMember : roomMemberList){
            roomMember.setCertificateTime(0);
            roomMemberRepository.save(roomMember);
        }
    }

}
