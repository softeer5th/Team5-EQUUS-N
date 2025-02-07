package com.feedhanjum.back_end.schedule.service;

import com.feedhanjum.back_end.member.domain.Member;
import com.feedhanjum.back_end.member.repository.MemberQueryRepository;
import com.feedhanjum.back_end.member.repository.MemberRepository;
import com.feedhanjum.back_end.schedule.domain.Schedule;
import com.feedhanjum.back_end.schedule.domain.ScheduleMember;
import com.feedhanjum.back_end.schedule.exception.ScheduleAlreadyExistException;
import com.feedhanjum.back_end.schedule.exception.ScheduleIsAlreadyEndException;
import com.feedhanjum.back_end.schedule.exception.ScheduleMembershipNotFoundException;
import com.feedhanjum.back_end.schedule.repository.ScheduleMemberRepository;
import com.feedhanjum.back_end.schedule.repository.ScheduleQueryRepository;
import com.feedhanjum.back_end.schedule.repository.ScheduleRepository;
import com.feedhanjum.back_end.schedule.repository.dto.ScheduleProjectionDto;
import com.feedhanjum.back_end.schedule.service.dto.ScheduleMemberNestedDto;
import com.feedhanjum.back_end.schedule.service.dto.ScheduleNestedDto;
import com.feedhanjum.back_end.schedule.service.dto.ScheduleRequestDto;
import com.feedhanjum.back_end.team.domain.Team;
import com.feedhanjum.back_end.team.exception.TeamMembershipNotFoundException;
import com.feedhanjum.back_end.team.repository.TeamMemberRepository;
import com.feedhanjum.back_end.team.repository.TeamRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final Clock clock;
    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final ScheduleRepository scheduleRepository;
    private final ScheduleMemberRepository scheduleMemberRepository;
    private final MemberRepository memberRepository;
    private final ScheduleQueryRepository scheduleQueryRepository;
    private final MemberQueryRepository memberQueryRepository;

    /**
     * 일정을 생성하는 메소드
     *
     * @throws EntityNotFoundException         사용자 혹은 팀이 존재하지 않는 경우
     * @throws TeamMembershipNotFoundException 해당 사용자가 팀에 가입한 상태가 아닌경우
     * @throws ScheduleAlreadyExistException   해당 일정의 시작 시간에 일정이 존재하는 경우
     * @throws RuntimeException                내부 서버 오류: 방금 조회한 사용자 ID가 사라짐
     */
    @Transactional
    public void createSchedule(Long memberId, Long teamId, ScheduleRequestDto requestDto) {
        Team team = teamRepository.findById(teamId).orElseThrow(() -> new EntityNotFoundException("팀을 찾을 수 없습니다."));
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new EntityNotFoundException("해당 사용자를 찾을 수 없습니다."));
        teamMemberRepository.findByMemberIdAndTeamId(memberId, teamId).orElseThrow(() -> new TeamMembershipNotFoundException("해당 팀에 존재하는 사람만 일정을 생성할 수 있습니다."));

        validateScheduleDuplicate(teamId, requestDto);
        validateScheduleTimeIntoTeamTime(requestDto, team);
        validateEndTimeIsAfterNow(requestDto.endTime());

        Schedule schedule = scheduleRepository.save(
                new Schedule(
                        requestDto.name(),
                        requestDto.startTime(),
                        requestDto.endTime(),
                        team,
                        member));

        memberQueryRepository.findMembersByTeamId(teamId).forEach(m -> scheduleMemberRepository.save(new ScheduleMember(schedule, m)));
        ScheduleMember scheduleMember = scheduleMemberRepository.findByMemberIdAndScheduleId(memberId, schedule.getId()).orElseThrow(() -> new RuntimeException("내부 서버 에러: 방금 조회한 사용자 ID가 사라짐"));
        scheduleMember.setTodos(requestDto.todos());
    }

    /**
     * @throws SecurityException                   일정 내용을 수정하려는 사람이 팀장, 일정의 주인이 아닌 경우
     * @throws EntityNotFoundException             팀, 사용자 또는 일정을 찾을 수 없는 경우
     * @throws ScheduleMembershipNotFoundException 회원이 해당 일정과 연관이 없을 경우
     * @throws IllegalArgumentException            일정의 시간 설정이 잘못된 경우
     */

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void updateSchedule(Long memberId, Long teamId, Long scheduleId, ScheduleRequestDto requestDto) {
        Team team = teamRepository.findById(teamId).orElseThrow(() -> new EntityNotFoundException("팀을 찾을 수 없습니다."));
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new EntityNotFoundException("해당 사용자를 찾을 수 없습니다."));
        Schedule schedule = scheduleRepository.findById(scheduleId).orElseThrow(() -> new EntityNotFoundException("해당 일정을 찾을 수 없습니다."));
        validateIsEnded(schedule);
        ScheduleMember scheduleMember = scheduleMemberRepository.findByMemberIdAndScheduleId(memberId, scheduleId)
                .orElseThrow(() -> new ScheduleMembershipNotFoundException("해당 일정을 찾을 수 없습니다."));

        changeScheduleInfo(requestDto, schedule, member, team);
        scheduleMember.setTodos(requestDto.todos());
    }

    @Transactional(readOnly = true)
    public ScheduleNestedDto getSchedule(Long memberId, Long teamId, Long scheduleId) {
        Team team = teamRepository.findById(teamId).orElseThrow(() -> new EntityNotFoundException("팀을 찾을 수 없습니다."));
        memberRepository.findById(memberId).orElseThrow(() -> new EntityNotFoundException("해당 사용자를 찾을 수 없습니다."));
        teamMemberRepository.findByMemberIdAndTeamId(memberId, teamId).orElseThrow(() -> new TeamMembershipNotFoundException("해당 팀에 속해있지 않습니다."));

        Schedule schedule = scheduleRepository.findById(scheduleId).orElseThrow(() -> new EntityNotFoundException("해당 일정을 찾을 수 없습니다."));
        if (!schedule.getTeam().equals(team)) {
            throw new TeamMembershipNotFoundException("해당 팀에 속해있지 않습니다.");
        }
        List<ScheduleProjectionDto> scheduleTodoList = scheduleQueryRepository.findScheduleTodoList(scheduleId, null);

        return getScheduleNestedDtos(scheduleTodoList).stream().findFirst().orElse(null);
    }

    @Transactional(readOnly = true)
    public List<ScheduleNestedDto> getScheduleDurations(Long memberId, Long teamId, LocalDate startDay, LocalDate endDay) {
        memberRepository.findById(memberId).orElseThrow(() -> new EntityNotFoundException("해당 사용자를 찾을 수 없습니다."));
        if (teamId != null) {
            memberRepository.findById(memberId).orElseThrow(() -> new EntityNotFoundException("해당 사용자를 찾을 수 없습니다."));
            teamMemberRepository.findByMemberIdAndTeamId(memberId, teamId).orElseThrow(() -> new TeamMembershipNotFoundException("해당 팀에 속해있지 않습니다."));
        }
        if(startDay.isAfter(endDay)){
            throw new IllegalArgumentException("시작 날짜는 종료 날짜 이전이어야 합니다.");
        }
        LocalDateTime startTime = startDay.atStartOfDay();
        LocalDateTime endTime = endDay.atTime(LocalTime.MAX);
        List<ScheduleProjectionDto> schedulesByTeamIdAndDuration = scheduleQueryRepository.findSchedulesByTeamIdAndDuration(memberId, teamId, startTime, endTime);

        return getScheduleNestedDtos(schedulesByTeamIdAndDuration);
    }

    @Transactional(readOnly = true)
    public ScheduleNestedDto getNearestSchedule(Long memberId, Long teamId) {
        validateTeamMember(memberId, teamId);

        LocalDateTime now = LocalDateTime.now(clock);

        ScheduleNestedDto nextSchedule = getScheduleNestedDtos(scheduleQueryRepository.findScheduleByClosestNextStartTime(teamId, now))
                .stream().findFirst().orElse(null);
        ScheduleNestedDto previousSchedule = getScheduleNestedDtos(scheduleQueryRepository.findScheduleByClosestPreviousEndTime(teamId, now))
                .stream().findFirst().orElse(null);
        if (nextSchedule != null && nextSchedule.getStartTime().isBefore(now.plusDays(1)))
            return nextSchedule;
        if (previousSchedule != null && previousSchedule.getEndTime().isAfter(now.minusDays(1))) {
            return previousSchedule;
        }
        return nextSchedule;
    }

    private List<ScheduleNestedDto> getScheduleNestedDtos(List<ScheduleProjectionDto> schedules) {
        Map<Long, ScheduleNestedDto> scheduleNestedDtoMap = new HashMap<>();
        Map<Long, ScheduleMemberNestedDto> scheduleMemberNestedDtoMap = new HashMap<>();

        for (ScheduleProjectionDto dto : schedules) {
            Long scheduleId = dto.getScheduleId();
            Long scheduleMemberId = dto.getScheduleMemberId();

            ScheduleNestedDto scheduleNestedDto = scheduleNestedDtoMap.computeIfAbsent(scheduleId, id -> new ScheduleNestedDto(dto));
            ScheduleMemberNestedDto scheduleMemberNestedDto = scheduleMemberNestedDtoMap.computeIfAbsent(scheduleMemberId, id -> new ScheduleMemberNestedDto(dto));

            scheduleNestedDto.addScheduleMemberNestedDto(scheduleMemberNestedDto);
            scheduleMemberNestedDto.addTodo(dto.getTodo());
        }
        return new ArrayList<>(scheduleNestedDtoMap.values());
    }

    private void validateTeamMember(Long memberId, Long teamId) {
        teamRepository.findById(teamId).orElseThrow(() -> new EntityNotFoundException("팀을 찾을 수 없습니다."));
        memberRepository.findById(memberId).orElseThrow(() -> new EntityNotFoundException("해당 사용자를 찾을 수 없습니다."));
        teamMemberRepository.findByMemberIdAndTeamId(memberId, teamId).orElseThrow(() -> new TeamMembershipNotFoundException("해당 팀에 속해있지 않습니다."));
    }

    private void validateIsEnded(Schedule schedule) {
        if (schedule.getEndTime().isBefore(LocalDateTime.now(clock))) {
            throw new ScheduleIsAlreadyEndException("해당 일정은 이미 종료되었습니다.");
        }
    }

    private void changeScheduleInfo(ScheduleRequestDto requestDto, Schedule schedule, Member member, Team team) {
        changeName(schedule, requestDto, member, team);
        changeTime(schedule, requestDto, member, team);
    }

    private void changeTime(Schedule schedule, ScheduleRequestDto requestDto, Member member, Team team) {
        if (schedule.isTimeDifferent(requestDto.startTime(), requestDto.endTime())) {
            validateOwnerOrLeader(schedule, member, team);
            if (schedule.isStartTimeDifferent(requestDto.startTime())) {
                validateScheduleDuplicate(team.getId(), requestDto);
            }
            validateScheduleTimeIntoTeamTime(requestDto, team);
            validateEndTimeIsAfterNow(requestDto.endTime());
            schedule.setTime(requestDto.startTime(), requestDto.endTime());
        }
    }

    private void changeName(Schedule schedule, ScheduleRequestDto requestDto, Member member, Team team) {
        if (schedule.isNameDifferent(requestDto.name())) {
            validateOwnerOrLeader(schedule, member, team);
            schedule.changeName(requestDto.name());
        }
    }


    private void validateEndTimeIsAfterNow(LocalDateTime endTime) {
        if (LocalDateTime.now(clock).isAfter(endTime)) {
            throw new IllegalArgumentException("일정의 종료 시점은 현재보다 미래여야 합니다.");
        }
    }

    private void validateScheduleTimeIntoTeamTime(ScheduleRequestDto requestDto, Team team) {
        if (team.getStartDate().isAfter(requestDto.startTime().toLocalDate())) {
            throw new IllegalArgumentException("일정 시작 시간이 팀의 시작 시간 이후여야 합니다.");
        }
        if (team.getEndDate() != null &&
                team.getEndDate().isBefore(requestDto.endTime().toLocalDate())) {
            throw new IllegalArgumentException("일정 종료 시간이 팀의 종료 시간 이전이어야 합니다.");
        }
    }

    private void validateOwnerOrLeader(Schedule schedule, Member member, Team team) {
        if (!(schedule.getOwner().equals(member) || team.getLeader().equals(member))) {
            throw new SecurityException("일정을 생성한 사람, 혹은 팀장만 일정을 수정할 수 있습니다.");
        }
    }

    private void validateScheduleDuplicate(Long teamId, ScheduleRequestDto requestDto) {
        if (scheduleRepository.findByTeamIdAndStartTime(teamId, requestDto.startTime()).isPresent())
            throw new ScheduleAlreadyExistException("이미 같은 시작시간에 일정이 있습니다.");
    }
}
