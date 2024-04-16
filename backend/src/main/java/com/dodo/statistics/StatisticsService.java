package com.dodo.statistics;

import com.dodo.certification.CertificationRepository;
import com.dodo.certification.domain.Certification;
import com.dodo.certification.domain.CertificationStatus;
import com.dodo.exception.NotFoundException;
import com.dodo.room.RoomRepository;
import com.dodo.room.domain.Category;
import com.dodo.room.domain.Periodicity;
import com.dodo.room.domain.Room;
import com.dodo.roomuser.RoomUserRepository;
import com.dodo.roomuser.domain.RoomUser;
import com.dodo.statistics.dto.ReportResponseData;
import com.dodo.statistics.dto.SimpleReportResponseData;
import com.dodo.statistics.dto.WeeklyGoalResponseData;
import com.dodo.user.UserRepository;
import com.dodo.user.domain.User;
import com.dodo.user.domain.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatisticsService {
    private final UserRepository userRepository;
    private final RoomUserRepository roomUserRepository;
    private final CertificationRepository certificationRepository;
    private final RoomRepository roomRepository;

    public ReportResponseData getReport(UserContext userContext) {
        User user = userRepository.findById(userContext.getUserId())
                .orElseThrow(() -> new NotFoundException("유저를 찾을 수 없습니다"));
        List<RoomUser> roomUserList = roomUserRepository.findAllByUser(user)
                .orElse(new ArrayList<>());


        // 이번달 저번달 달성률
        Float roomUserSize = (float) roomUserList.size();
        AtomicReference<Float> lastMonth = new AtomicReference<>(0F);
        AtomicReference<Float> thisMonth = new AtomicReference<>(0F);

        roomUserList.forEach(roomUser -> {
                    Room room = roomUser.getRoom();
                    SimpleReportResponseData data = getSimpleReport(userContext, room.getId());
                    lastMonth.updateAndGet(v -> v + data.getLastMonth());
                    thisMonth.updateAndGet(v -> v + data.getThisMonth());
                });


        // 가장 열심히 한 분야
        List<Certification> certificationList = certificationRepository.findAllByRoomUserIn(roomUserList)
                .orElse(new ArrayList<>());
        Integer allCategoryStatus = certificationList.size();
        Map<Category, Long> categoryStatus = certificationList.stream()
                .collect(Collectors.groupingBy(c -> c.getRoomUser().getRoom().getCategory(), Collectors.counting()));


        // 가장 많이 활동한 방에서 나는?
        RoomUser maxRoomUser = roomUserRepository.findAllByUser(user)
                .orElse(new ArrayList<>())
                .stream()
                .max((ru1, ru2) -> (int) (certificationRepository.countAllByRoomUser(ru1) - certificationRepository.countAllByRoomUser(ru2)))
                .get();

        //TODO findAllByRoomUserRoom 이거 작동 하나..?
        Map<User, Long> CertificationListFromUser = certificationRepository.findAllByRoomUserRoom(maxRoomUser)
                .orElse(new ArrayList<>())
                .stream()
                .collect(Collectors.groupingBy(c -> c.getRoomUser().getUser(), Collectors.counting()));

        List<User> keys = new ArrayList<>(CertificationListFromUser.keySet());

        keys.sort(Comparator.comparing(CertificationListFromUser::get));
        Float mostActivity = (float) keys.indexOf(maxRoomUser.getUser()) / (float) keys.size();

        return new ReportResponseData(lastMonth.get() / roomUserSize, thisMonth.get() / roomUserSize, categoryStatus, allCategoryStatus, mostActivity);
    }

    public SimpleReportResponseData getSimpleReport(UserContext userContext, Long roomId) {
        User user = userRepository.findById(userContext.getUserId())
                .orElseThrow(() -> new NotFoundException("유저를 찾을 수 없습니다"));
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new NotFoundException("인증방을 찾을 수 없습니다"));
        RoomUser roomuser = roomUserRepository.findByUserAndRoom(user, room)
                .orElseThrow(() -> new NotFoundException("인증방에 소속되어있지 않습니다"));

        List<Certification> certificationList = certificationRepository.findAllByRoomUser(roomuser)
                .orElse(new ArrayList<>());

        LocalDateTime now = LocalDateTime.now();


        if(room.getPeriodicity() == Periodicity.DAILY) {
            // 일일 인증

            // 지난 달
            LocalDateTime lastStart = YearMonth.now().minusMonths(1).atDay(1).atTime(LocalTime.MIN);
            LocalDateTime lastEnd = YearMonth.now().minusMonths(1).atEndOfMonth().atTime(LocalTime.MAX);
            long lastMonthCount = getCertificationSuccessCount(certificationList, lastStart, lastEnd);
            long lastMonthSize = lastEnd.getDayOfMonth();

            // 이번 달
            LocalDateTime start = YearMonth.now().atDay(1).atTime(LocalTime.MIN);
            LocalDateTime end = YearMonth.now().atDay(now.getDayOfMonth()).atTime(LocalTime.MAX);
            long thisMonthCount = getCertificationSuccessCount(certificationList, start, end);
            long thisMonthSize = lastEnd.getDayOfMonth();

            return new SimpleReportResponseData(lastMonthCount, lastMonthSize, thisMonthCount, thisMonthSize);
        } else {
            // 주간 인증

            long frequency = room.getFrequency();

            long lastMonthCount = 0;
            long lastMonthSize = 0;
            LocalDateTime start = YearMonth.now().minusMonths(1).atEndOfMonth().atTime(LocalTime.MIN);
            if(start.getDayOfWeek().getValue() != 1) {
                start = start.plusDays(8 - start.getDayOfWeek().getValue());
            }
            while(true) {
                LocalDateTime end = start.plusDays(6).with(LocalDate.MAX);
                if(end.getMonth().equals(now.getMonth())) break;
                lastMonthCount += getCertificationSuccessCount(certificationList, start, end);
                lastMonthCount += frequency;
                start = start.plusDays(7);
            }

            // 이번 달

            long thisMonthCount = 0;
            long thisMonthSize = 0;
            while(true) {
                LocalDateTime end = start.plusDays(6).with(LocalDate.MAX);
                if(end.isAfter(now)) break;
                thisMonthCount += getCertificationSuccessCount(certificationList, start, end);
                thisMonthCount += frequency;
                start = start.plusDays(7);
            }

            return new SimpleReportResponseData(lastMonthCount, lastMonthSize, thisMonthCount, thisMonthSize);
        }
    }

    private long getCertificationSuccessCount(List<Certification> certificationList, LocalDateTime start, LocalDateTime end) {
        return certificationList.stream()
                .filter(certification -> {
                    if(certification.getStatus() == CertificationStatus.SUCCESS) {
                        LocalDateTime time = certification.getCreatedTime();
                        return time.isAfter(start) && time.isBefore(end);
                    }
                    return false;
                }).count();
    }


    // TODO
    // 테스트 필요함
    public List<WeeklyGoalResponseData> getWeeklyGoal(UserContext userContext) {
        User user = userRepository.findById(userContext.getUserId())
                .orElseThrow(() -> new NotFoundException("유저를 찾을 수 없습니다"));
        List<RoomUser> roomUser = roomUserRepository.findAllByUser(user)
                .orElse(new ArrayList<>());
        List<Certification> certificationList = certificationRepository.findAllByRoomUserIn(roomUser)
                .orElse(new ArrayList<>());
        List<LocalDateTime> thisWeek = getThisWeek();
        List<WeeklyGoalResponseData> result = getResultList(thisWeek.get(0));

        certificationList.stream()
                // 인증 기록중에 이번주 필터
                .filter(certification -> {
                    LocalDateTime time = certification.getCreatedTime();
                    return time.isAfter(thisWeek.get(0)) && time.isBefore(getThisWeek().get(1));
                })
                .forEach(
                        certification -> {
                            if(certification.getStatus() == CertificationStatus.SUCCESS) {
                                result.get(certification.getCreatedTime().getDayOfWeek().getValue()).setFlag(true);
                            }
                        }
                );
        return result;
    }


    // 이번주의 시작과 끝을 반환
    private List<LocalDateTime> getThisWeek() {
        LocalDateTime now = LocalDateTime.now();
        DayOfWeek dayOfWeek = now.getDayOfWeek();
        LocalDateTime sunday = now.plusDays(7 - dayOfWeek.getValue()).with(LocalTime.MAX);
        LocalDateTime monday = now.minusDays(6).with(LocalTime.MIN);
        return Arrays.asList(monday, sunday);
    }

    private List<WeeklyGoalResponseData> getResultList(LocalDateTime thisWeekStart) {
        List<WeeklyGoalResponseData> result = new ArrayList<>();

        // 일주일 만들기
        for(int i = 0; i < 7; i++) {
            result.add(new WeeklyGoalResponseData(thisWeekStart.plusDays(i)));
        }
        return result;
    }
}