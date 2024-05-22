package com.dodo.statistics;

import com.dodo.config.auth.CustomAuthentication;
import com.dodo.statistics.dto.*;
import com.dodo.member.domain.MemberContext;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/report")
@RequiredArgsConstructor
@CustomAuthentication
public class StatisticsController {
    private final StatisticsService statisticsService;

    @GetMapping("/simple/{roomId}")
    public SimpleReportResponseData getSimpleReport(
            @RequestAttribute MemberContext memberContext,
            @PathVariable Long roomId
    ) {
        return statisticsService.getSimpleReport(memberContext, roomId);
    }

    @GetMapping("/me")
    public ReportResponseData getReport(
            @RequestAttribute MemberContext memberContext
    ) {
        return statisticsService.getReport(memberContext);
    }

    @GetMapping("/weekly-goal")
    public List<DailyGoalResponseData> getWeeklyGoal(
            @RequestAttribute MemberContext memberContext
    ) {
        return statisticsService.getWeeklyGoal(memberContext);
    }

    @GetMapping("/room-profile/{roomMemberId}")
    public RoomProfileData getRoomProfile(
            @RequestAttribute MemberContext memberContext,
            @PathVariable Long roomMemberId
    ) {
        return statisticsService.getRoomProfile(memberContext, roomMemberId);
    }

    @GetMapping("/album")
    public List<AlbumResponseData> getAlbum(
            @RequestAttribute MemberContext memberContext
    ) {
        return statisticsService.getAlbum(memberContext);
    }
}
