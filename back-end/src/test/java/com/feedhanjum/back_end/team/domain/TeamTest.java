package com.feedhanjum.back_end.team.domain;

import com.feedhanjum.back_end.feedback.domain.FeedbackType;
import com.feedhanjum.back_end.member.domain.Member;
import com.feedhanjum.back_end.member.domain.ProfileImage;
import com.feedhanjum.back_end.team.exception.TeamMembershipNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TeamTest {

    private final AtomicLong nextId = new AtomicLong(1L);

    private Member createMember(String name) {
        Member member = new Member(name, name + "@email.com", new ProfileImage("blue", "default.png"));
        ReflectionTestUtils.setField(member, "id", nextId.getAndIncrement());
        return member;
    }

    @Test
    @DisplayName("팀 생성 시, 리더가 팀에 가입됨을 확인")
    void newTeam_성공() {
        // given
        Member leader = createMember("currentLeader");


        // when
        Team team = new Team("team1", leader, LocalDate.now().minusDays(1), LocalDate.now().plusDays(1), FeedbackType.ANONYMOUS);

        // then
        assertThat(team.isTeamMember(leader)).isTrue();
    }

    @Test
    @DisplayName("팀 리더 변경 시, 정상 입력에 따른 리더 변경 확인")
    void changeLeader_성공() {
        // given
        Member currentLeader = createMember("currentLeader");
        Team team = new Team("team1", currentLeader, LocalDate.now().minusDays(1), LocalDate.now().plusDays(1), FeedbackType.ANONYMOUS);
        Member newLeader = createMember("newLeader");
        team.join(newLeader);

        // when
        team.changeLeader(currentLeader, newLeader);

        // then
        assertThat(team.getLeader()).isEqualTo(newLeader);
    }

    @Test
    @DisplayName("팀 리더 변경 시, 리더가 아닌 멤버의 요청에 대한 검증 확인")
    void changeLeader_실패() {
        // given
        Member currentLeader = createMember("currentLeader");
        Team team = new Team("team1", currentLeader, LocalDate.now().minusDays(1), LocalDate.now().plusDays(1), FeedbackType.ANONYMOUS);
        Member notLeader = createMember("notLeader");
        Member newLeader = createMember("newLeader");
        team.join(notLeader);
        team.join(newLeader);

        // when
        assertThatThrownBy(() -> team.changeLeader(notLeader, newLeader))
                .isInstanceOf(SecurityException.class);

        // then
        assertThat(team.getLeader()).isNotEqualTo(newLeader);
    }

    @Test
    @DisplayName("팀 리더 변경 시, 팀 멤버가 아닌 새로운 리더에 대한 검증 확인")
    void changeLeader_실패2() {
        // given
        Member currentLeader = createMember("currentLeader");
        Team team = new Team("team1", currentLeader, LocalDate.now().minusDays(1), LocalDate.now().plusDays(1), FeedbackType.ANONYMOUS);
        Member newLeader = createMember("newLeader");

        // when
        assertThatThrownBy(() -> team.changeLeader(currentLeader, newLeader))
                .isInstanceOf(TeamMembershipNotFoundException.class);

        // then
        assertThat(team.getLeader()).isNotEqualTo(newLeader);
    }
}