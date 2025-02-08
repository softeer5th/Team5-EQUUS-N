package com.feedhanjum.back_end.team.service;

import com.feedhanjum.back_end.event.EventPublisher;
import com.feedhanjum.back_end.feedback.domain.FeedbackType;
import com.feedhanjum.back_end.member.domain.Member;
import com.feedhanjum.back_end.member.domain.ProfileImage;
import com.feedhanjum.back_end.member.repository.MemberRepository;
import com.feedhanjum.back_end.team.domain.Team;
import com.feedhanjum.back_end.team.event.TeamMemberLeftEvent;
import com.feedhanjum.back_end.team.exception.TeamLeaderMustExistException;
import com.feedhanjum.back_end.team.exception.TeamMembershipNotFoundException;
import com.feedhanjum.back_end.team.repository.TeamQueryRepository;
import com.feedhanjum.back_end.team.repository.TeamRepository;
import com.feedhanjum.back_end.team.service.dto.TeamCreateDto;
import com.feedhanjum.back_end.team.service.dto.TeamUpdateDto;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static com.feedhanjum.back_end.util.DomainTestUtils.createMemberWithId;
import static com.feedhanjum.back_end.util.DomainTestUtils.createTeamWithId;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class TeamServiceTest {

    @Mock
    private Clock clock;

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private TeamQueryRepository teamQueryRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private EventPublisher eventPublisher;

    @InjectMocks
    private TeamService teamService;

    @Test
    @DisplayName("내가 가입한 팀 목록 조회 성공")
    void getMyTeams_팀조회() {
        //given
        Long userId = 1L;
        when(clock.instant()).thenReturn(LocalDate.of(2023, 1, 1).atStartOfDay(Clock.systemDefaultZone().getZone()).toInstant());
        when(clock.getZone()).thenReturn(Clock.systemDefaultZone().getZone());
        Member leader = new Member("haha", "haha@hoho", new ProfileImage("blue", "image1"));
        Team team = new Team("haha", leader, LocalDate.now(clock).plusDays(1), LocalDate.now(clock).plusDays(10), FeedbackType.ANONYMOUS);
        when(teamQueryRepository.findTeamByMemberId(userId)).thenReturn(List.of(team));

        //when
        List<Team> result = teamService.getMyTeams(userId);

        //then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(team.getId());
    }

    @Nested
    @DisplayName("팀 생성 테스트")
    class CreateTeam {
        @Test
        @DisplayName("프로젝트 기간이 올바른 경우 팀 생성 성공")
        void createTeam_팀생성() {
            //given
            Member leader = createMemberWithId("leader");
            String teamName = "haha";
            when(clock.instant()).thenReturn(LocalDate.of(2023, 1, 1).atStartOfDay(Clock.systemDefaultZone().getZone()).toInstant());
            when(clock.getZone()).thenReturn(Clock.systemDefaultZone().getZone());
            LocalDate startDate = LocalDate.now(clock).plusDays(1);
            LocalDate endDate = LocalDate.now(clock).plusDays(10);
            FeedbackType feedbackType = FeedbackType.ANONYMOUS;

            when(memberRepository.findById(leader.getId())).thenReturn(Optional.of(leader));

            //when
            Team team = teamService.createTeam(leader.getId(), new TeamCreateDto(teamName, startDate, endDate, feedbackType));

            //then
            assertThat(team.getName()).isEqualTo(teamName);
            assertThat(team.getLeader()).isEqualTo(leader);
            assertThat(team.getStartDate()).isEqualTo(startDate);
            assertThat(team.getEndDate()).isEqualTo(endDate);
            assertThat(team.getFeedbackType()).isEqualTo(feedbackType);
        }

        @Test
        @DisplayName("프로젝트 기간이 올바르지 않을 경우 팀 생성 실패")
        void createTeam_프로젝트기간오류() {
            //given
            Member leader = createMemberWithId("leader");
            String teamName = "haha";
            when(clock.instant()).thenReturn(LocalDate.of(2023, 1, 1).atStartOfDay(Clock.systemDefaultZone().getZone()).toInstant());
            when(clock.getZone()).thenReturn(Clock.systemDefaultZone().getZone());
            LocalDate startDate = LocalDate.now(clock).plusDays(10);
            LocalDate endDate = LocalDate.now(clock).plusDays(1);
            FeedbackType feedbackType = FeedbackType.ANONYMOUS;

            when(memberRepository.findById(leader.getId())).thenReturn(Optional.of(leader));

            //when & then
            assertThatThrownBy(() -> teamService.createTeam(leader.getId(), new TeamCreateDto(teamName, startDate, endDate, feedbackType)))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("프로젝트 시작 시간이 종료 시간보다 앞서야 합니다.");
        }
    }

    @Nested
    @DisplayName("팀원 제거 테스트")
    class RemoveMember {

        @Test
        @DisplayName("팀원 제거 성공")
        void removeTeamMember_팀원제거_정상() {
            //given
            Member leader = createMemberWithId("leader");
            Team team = createTeamWithId("team", leader);
            Member member = createMemberWithId("member");
            team.join(member);

            when(teamRepository.findById(team.getId())).thenReturn(Optional.of(team));
            when(memberRepository.findById(leader.getId())).thenReturn(Optional.of(leader));
            when(memberRepository.findById(member.getId())).thenReturn(Optional.of(member));

            //when
            teamService.removeTeamMember(leader.getId(), team.getId(), member.getId());

            //then
            assertThat(team.isTeamMember(member)).isFalse();
        }

        @Test
        @DisplayName("팀원 제거 실패 - 존재하지 않는 팀")
        void removeTeamMember_팀원제거_실패_팀X() {
            //given
            Long leaderId = 1L;
            Long teamId = 100L;
            Long memberIdToRemove = 1L;
            when(teamRepository.findById(teamId)).thenReturn(Optional.empty());

            //when & then
            assertThatThrownBy(() -> teamService.removeTeamMember(leaderId, teamId, memberIdToRemove))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("팀을 찾을 수 없습니다.");
        }

        @Test
        @DisplayName("팀원 제거 실패 - 팀장 제거 시도")
        void removeTeamMember_팀원제거_실패_팀장제거() {
            //given
            Member leader = createMemberWithId("leader");
            Team team = createTeamWithId("team", leader);
            team.join(createMemberWithId("member"));

            when(teamRepository.findById(team.getId())).thenReturn(Optional.of(team));
            when(memberRepository.findById(leader.getId())).thenReturn(Optional.of(leader));

            //when & then
            assertThatThrownBy(() -> teamService.removeTeamMember(leader.getId(), team.getId(), leader.getId()))
                    .isInstanceOf(TeamLeaderMustExistException.class);
        }

        @Test
        @DisplayName("팀원 제거 실패 - 팀장이 아닌 경우")
        void removeTeamMember_팀원제거_실패_팀장아님() {
            //given
            Team team = createTeamWithId("team", createMemberWithId("leader"));
            Member notLeader = createMemberWithId("notLeader");
            team.join(notLeader);
            Member member = createMemberWithId("member");
            team.join(member);

            when(teamRepository.findById(team.getId())).thenReturn(Optional.of(team));
            when(memberRepository.findById(notLeader.getId())).thenReturn(Optional.of(notLeader));
            when(memberRepository.findById(member.getId())).thenReturn(Optional.of(member));

            //when & then
            assertThatThrownBy(() -> teamService.removeTeamMember(notLeader.getId(), team.getId(), member.getId()))
                    .isInstanceOf(SecurityException.class);
        }

        @Test
        @DisplayName("팀원 제거 실패 - 제거하려는 사용자가 팀원이 아닌 경우")
        void removeTeamMember_팀원제거_실패_팀원아님() {
            //given
            Member leader = createMemberWithId("leader");
            Team team = createTeamWithId("team", leader);
            Member notMember = createMemberWithId("notMember");

            when(teamRepository.findById(team.getId())).thenReturn(Optional.of(team));
            when(memberRepository.findById(leader.getId())).thenReturn(Optional.of(leader));
            when(memberRepository.findById(notMember.getId())).thenReturn(Optional.of(notMember));

            //when & then
            assertThatThrownBy(() -> teamService.removeTeamMember(leader.getId(), team.getId(), notMember.getId()))
                    .isInstanceOf(TeamMembershipNotFoundException.class);
        }

    }


    @Nested
    @DisplayName("팀 리더 위임")
    class deleteTeamLeader {
        @Test
        @DisplayName("팀 리더 위임 정상 동작")
        void delegateTeamLeader_정상() {
            // given
            Member currentLeader = createMemberWithId("currentLeader");
            Team team = createTeamWithId("team", currentLeader);
            Member newLeader = createMemberWithId("newLeader");
            team.join(newLeader);

            when(teamRepository.findById(team.getId())).thenReturn(Optional.of(team));
            when(memberRepository.findById(currentLeader.getId())).thenReturn(Optional.of(currentLeader));
            when(memberRepository.findById(newLeader.getId())).thenReturn(Optional.of(newLeader));


            // when
            teamService.delegateTeamLeader(currentLeader.getId(), team.getId(), newLeader.getId());

            // then
            assertThat(team.getLeader()).isEqualTo(newLeader);
        }

        @Test
        @DisplayName("팀을 찾을 수 없는 경우 예외 발생")
        void delegateTeamLeader_팀없음() {
            // given
            Long currentLeaderId = 1L;
            Long teamId = 1L;
            Long newLeaderId = 2L;
            when(teamRepository.findById(teamId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> teamService.delegateTeamLeader(currentLeaderId, teamId, newLeaderId))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessage("팀을 찾을 수 없습니다.");
        }

        @Test
        @DisplayName("현재 사용자가 팀장이 아닌 경우 예외 발생")
        void delegateTeamLeader_팀장아님() {
            // given
            Member leader = createMemberWithId("leader");
            Team team = createTeamWithId("team", leader);
            Member notLeader = createMemberWithId("notLeader");
            team.join(notLeader);
            Member newLeader = createMemberWithId("newLeader");
            team.join(newLeader);

            when(teamRepository.findById(team.getId())).thenReturn(Optional.of(team));
            when(memberRepository.findById(notLeader.getId())).thenReturn(Optional.of(notLeader));
            when(memberRepository.findById(newLeader.getId())).thenReturn(Optional.of(newLeader));

            // when & then
            assertThatThrownBy(() -> teamService.delegateTeamLeader(notLeader.getId(), team.getId(), newLeader.getId()))
                    .isInstanceOf(SecurityException.class);
        }

    }

    @Nested
    @DisplayName("팀에서 나가기 테스트")
    class leaveTeam {
        @Test
        @DisplayName("일반 회원 탈퇴 - 정상 처리")
        void leaveTeam_일반회원탈퇴() {
            // given
            Member leader = createMemberWithId("leader");
            Team team = createTeamWithId("team", leader);
            Member notLeader = createMemberWithId("notLeader");
            team.join(notLeader);

            when(teamRepository.findById(team.getId())).thenReturn(Optional.of(team));
            when(memberRepository.findById(notLeader.getId())).thenReturn(Optional.of(notLeader));

            // when
            teamService.leaveTeam(notLeader.getId(), team.getId());
            // then
            assertThat(team.isTeamMember(notLeader)).isFalse();
            verify(eventPublisher).publishEvent(new TeamMemberLeftEvent(team.getId(), notLeader.getId()));
        }

        @Test
        @DisplayName("나가려는 팀이 없을 시 예외 발생")
        void leaveTeam_팀정보없음() {
            // given
            Long userId = 1L;
            Long teamId = 1L;
            when(teamRepository.findById(teamId)).thenReturn(Optional.empty());
            // when, then
            assertThatThrownBy(() -> teamService.leaveTeam(userId, teamId))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessage("팀을 찾을 수 없습니다.");
        }

        @Test
        @DisplayName("나가려는 팀과의 관계가 없을 시 예외 발생")
        void leaveTeam_멤버십정보없음() {
            // given
            Member leader = createMemberWithId("leader");
            Team team = createTeamWithId("team", leader);
            Member notMember = createMemberWithId("notMember");

            when(memberRepository.findById(notMember.getId())).thenReturn(Optional.of(notMember));
            when(teamRepository.findById(team.getId())).thenReturn(Optional.of(team));

            // when, then
            assertThatThrownBy(() -> teamService.leaveTeam(notMember.getId(), team.getId()))
                    .isInstanceOf(TeamMembershipNotFoundException.class);
        }

        @Test
        @DisplayName("팀장 탈퇴 불가 예외 발생")
        void leaveTeam_팀장탈퇴불가() {
            // given
            Member leader = createMemberWithId("leader");
            Team team = createTeamWithId("team", leader);
            team.join(createMemberWithId("member"));
            when(teamRepository.findById(team.getId())).thenReturn(Optional.of(team));
            when(memberRepository.findById(leader.getId())).thenReturn(Optional.of(leader));

            // when, then
            assertThatThrownBy(() -> teamService.leaveTeam(leader.getId(), team.getId()))
                    .isInstanceOf(TeamLeaderMustExistException.class);
        }

        @Test
        @DisplayName("마지막 회원 탈퇴 - 성공")
        void leaveTeam_마지막멤버탈퇴() {
            // given
            Member leader = createMemberWithId("leader");
            Team team = createTeamWithId("team", leader);
            when(teamRepository.findById(team.getId())).thenReturn(Optional.of(team));
            when(memberRepository.findById(leader.getId())).thenReturn(Optional.of(leader));
            // when
            teamService.leaveTeam(leader.getId(), team.getId());
            // then
            assertThat(team.memberCount()).isZero();
            verify(teamRepository).delete(team);
            verify(eventPublisher).publishEvent(new TeamMemberLeftEvent(team.getId(), leader.getId()));
        }
    }

    @Nested
    @DisplayName("팀 정보 업데이트")
    class UpdateTeamInfo {

        @Test
        @DisplayName("팀 정보 업데이트 성공")
        void updateTeamInfo_성공() {
            // given
            Member leader = createMemberWithId("leader");
            LocalDate startDate = LocalDate.of(2025, 1, 1);
            LocalDate endDate = LocalDate.of(2025, 1, 2);
            TeamUpdateDto teamUpdateDto = new TeamUpdateDto("haha", startDate, endDate, FeedbackType.IDENTIFIED);
            Team team = createTeamWithId("team", leader, startDate, endDate);

            when(teamRepository.findById(team.getId())).thenReturn(Optional.of(team));
            when(memberRepository.findById(leader.getId())).thenReturn(Optional.of(leader));

            // when
            Team updatedTeam = teamService.updateTeamInfo(leader.getId(), team.getId(), teamUpdateDto);

            // then
            assertThat(updatedTeam.getName()).isEqualTo("haha");
            assertThat(updatedTeam.getStartDate()).isEqualTo(startDate);
            assertThat(updatedTeam.getEndDate()).isEqualTo(endDate);
            assertThat(updatedTeam.getFeedbackType()).isEqualTo(FeedbackType.IDENTIFIED);
        }

        @Test
        @DisplayName("시작 시간이 종료 시간보다 늦은 경우 예외 발생")
        void updateTeamInfo_잘못된기간() {
            // given
            Member member = createMemberWithId("member");
            LocalDate startDate = LocalDate.of(2025, 1, 3);
            LocalDate endDate = LocalDate.of(2025, 1, 2);
            Team team = createTeamWithId("team", member);
            TeamUpdateDto teamUpdateDto = new TeamUpdateDto("haha", startDate, endDate, FeedbackType.IDENTIFIED);

            when(teamRepository.findById(team.getId())).thenReturn(Optional.of(team));
            when(memberRepository.findById(member.getId())).thenReturn(Optional.of(member));

            // when, then
            assertThatThrownBy(() -> teamService.updateTeamInfo(member.getId(), team.getId(), teamUpdateDto))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("팀이 존재하지 않을 경우 예외 발생")
        void updateTeamInfo_팀없음() {
            // given
            Long memberId = 1L;
            Long teamId = 10L;
            LocalDate startDate = LocalDate.of(2025, 1, 1);
            LocalDate endDate = LocalDate.of(2025, 1, 2);
            TeamUpdateDto teamUpdateDto = new TeamUpdateDto("haha", startDate, endDate, FeedbackType.ANONYMOUS);

            when(teamRepository.findById(teamId)).thenReturn(Optional.empty());

            // when, then
            assertThatThrownBy(() -> teamService.updateTeamInfo(memberId, teamId, teamUpdateDto))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("팀을 찾을 수 없습니다");
        }

        @Test
        @DisplayName("요청자가 팀장이 아닐 경우 예외 발생")
        void updateTeamInfo_팀장아님() {
            // given
            Member notLeader = createMemberWithId("notLeader");
            LocalDate startDate = LocalDate.of(2025, 1, 1);
            LocalDate endDate = LocalDate.of(2025, 1, 2);
            Team team = createTeamWithId("team", createMemberWithId("leader"), startDate, endDate);
            TeamUpdateDto teamUpdateDto = new TeamUpdateDto("hoho", startDate, endDate, FeedbackType.ANONYMOUS);

            when(memberRepository.findById(notLeader.getId())).thenReturn(Optional.of(notLeader));
            when(teamRepository.findById(team.getId())).thenReturn(Optional.of(team));

            // when, then
            assertThatThrownBy(() -> teamService.updateTeamInfo(notLeader.getId(), team.getId(), teamUpdateDto))
                    .isInstanceOf(SecurityException.class)
                    .hasMessageContaining("팀장이 아닙니다");
        }
    }
}