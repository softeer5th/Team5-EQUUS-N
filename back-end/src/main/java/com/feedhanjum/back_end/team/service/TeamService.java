package com.feedhanjum.back_end.team.service;

import com.feedhanjum.back_end.member.domain.Member;
import com.feedhanjum.back_end.member.repository.MemberQueryRepository;
import com.feedhanjum.back_end.member.repository.MemberRepository;
import com.feedhanjum.back_end.team.domain.Team;
import com.feedhanjum.back_end.team.domain.TeamMember;
import com.feedhanjum.back_end.team.exception.TeamLeaderMustExistException;
import com.feedhanjum.back_end.team.exception.TeamMembershipNotFoundException;
import com.feedhanjum.back_end.team.repository.TeamMemberRepository;
import com.feedhanjum.back_end.team.repository.TeamQueryRepository;
import com.feedhanjum.back_end.team.repository.TeamRepository;
import com.feedhanjum.back_end.team.service.dto.TeamCreateDto;
import com.feedhanjum.back_end.team.service.dto.TeamUpdateDto;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TeamService {
    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final MemberRepository memberRepository;
    private final TeamQueryRepository teamQueryRepository;
    private final MemberQueryRepository memberQueryRepository;

    /**
     * @throws IllegalArgumentException 프로젝트 기간의 시작일이 종료일보다 앞서지 않을 경우
     * @throws EntityNotFoundException  팀 생성 요청한 리더가 존재하지 않을 경우
     */
    @Transactional
    public Team createTeam(Long leaderId, TeamCreateDto teamCreateDto) {
        validateProjectDuration(teamCreateDto.startDate(), teamCreateDto.endDate());
        Member leader = memberRepository.findById(leaderId)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));
        Team team = new Team(teamCreateDto.teamName(), leader, teamCreateDto.startDate(), teamCreateDto.endDate(), teamCreateDto.feedbackType());
        teamRepository.save(team);

        TeamMember teamMember = new TeamMember(team, leader);
        teamMemberRepository.save(teamMember);
        return team;
    }

    @Transactional(readOnly = true)
    public Team getTeam(Long teamId) {
        return teamRepository.findById(teamId).orElseThrow(() -> new EntityNotFoundException("해당 팀을 찾을 수 없습니다."));
    }

    @Transactional(readOnly = true)
    public List<Team> getMyTeams(Long userId) {
        return teamQueryRepository.findTeamByMemberId(userId);
    }

    /**
     * 팀장이 팀원을 제거한다.
     *
     * @throws EntityNotFoundException 해당 팀 또는 팀원 정보가 없을 경우
     * @throws SecurityException 요청자가 팀장이 아닐 경우
     * @throws IllegalArgumentException 팀장(자기 자신)을 제거하려 할 경우
     * @throws TeamMembershipNotFoundException 해당 팀원이 팀에 가입된 사용자가 아닌 경우
     */
    @Transactional
    public void removeTeamMember(Long memberId, Long teamId, Long memberIdToRemove) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new EntityNotFoundException("팀을 찾을 수 없습니다."));
        validateUserIsTeamLeader(memberId, team);
        if (team.getLeader().getId().equals(memberIdToRemove)) {
            throw new IllegalArgumentException("팀장은 제거할 수 없습니다.");
        }
        TeamMember membership = teamMemberRepository.findByMemberIdAndTeamId(memberIdToRemove, teamId)
                .orElseThrow(() -> new TeamMembershipNotFoundException("해당 팀원 정보를 찾을 수 없습니다."));
        deleteTeamMemberAndRemainingScheduleMember(membership);
    }

    /**
     * @throws EntityNotFoundException         해당 팀 또는 회원이 존재하지 않을 경우
     * @throws SecurityException               현재 사용자가 팀장이 아닐 경우
     * @throws TeamMembershipNotFoundException 새 팀장이 팀의 구성원이 아닐 경우
     */
    @Transactional
    public void delegateTeamLeader(Long currentLeaderId, Long teamId, Long newLeaderId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new EntityNotFoundException("팀을 찾을 수 없습니다."));
        validateUserIsTeamLeader(currentLeaderId, team);
        TeamMember newLeaderMembership = teamMemberRepository.findByMemberIdAndTeamId(newLeaderId, teamId)
                .orElseThrow(() -> new TeamMembershipNotFoundException("새 팀장이 팀의 구성원이 아닙니다."));
        Member newLeader = newLeaderMembership.getMember();
        team.changeLeader(newLeader);
    }

    /**
     * @throws IllegalArgumentException 시작 시간이 종료 시간보다 앞서지 않을 경우
     * @throws EntityNotFoundException  팀이 존재하지 않는 경우
     * @throws SecurityException        요청자가 팀장이 아닐 경우
     */
    @Transactional
    public Team updateTeamInfo(Long memberId, Long teamId, TeamUpdateDto teamUpdateDto) {
        validateProjectDuration(teamUpdateDto.startDate(), teamUpdateDto.endDate());
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new EntityNotFoundException("팀을 찾을 수 없습니다."));
        validateUserIsTeamLeader(memberId, team);
        team.updateInfo(teamUpdateDto.teamName(), teamUpdateDto.startDate(), teamUpdateDto.endDate(), teamUpdateDto.feedbackType());
        return team;
    }


    /**
     * @throws EntityNotFoundException      팀 가입 정보가 없을 경우
     * @throws TeamLeaderMustExistException 팀장은 탈퇴할 수 없으므로 발생
     */
    @Transactional
    public void leaveTeam(Long userId, Long teamId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new EntityNotFoundException("팀을 찾을 수 없습니다."));
        Long memberCount = memberQueryRepository.countMembersByTeamId(teamId);
        if (team.getLeader().getId().equals(userId) && memberCount >= 2) {
            throw new TeamLeaderMustExistException("팀장은 반드시 팀에 존재해야 합니다. 팀장직을 다른사람에게 위임해 주세요.");
        }
        TeamMember membership = teamMemberRepository.findByMemberIdAndTeamId(userId, teamId)
                .orElseThrow(() -> new EntityNotFoundException("팀을 찾을 수 없습니다."));
        deleteTeamMemberAndRemainingScheduleMember(membership);
        if (memberCount == 1) {
            deleteTeam(teamId);
        }
    }

    private void deleteTeamMemberAndRemainingScheduleMember(TeamMember membership) {
        teamMemberRepository.delete(membership);
        // 남은 일정에 기록된 할 일 제거 로직 필요 Todo
    }

    private void validateUserIsTeamLeader(Long leaderId, Team team) {
        if (!team.getLeader().getId().equals(leaderId)) {
            throw new SecurityException("현재 사용자는 팀장이 아닙니다.");
        }
    }

    private void validateProjectDuration(LocalDate startDate, LocalDate endDate) {
        if (endDate != null && !startDate.isBefore(endDate)) {
            throw new IllegalArgumentException("프로젝트 시작 시간이 종료 시간보다 앞서야 합니다.");
        }
    }

    private void deleteTeam(Long teamId) {
        // 팀 삭제 로직 Todo
    }
}
