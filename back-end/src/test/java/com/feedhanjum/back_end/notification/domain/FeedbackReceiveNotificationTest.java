package com.feedhanjum.back_end.notification.domain;

import com.feedhanjum.back_end.feedback.domain.Feedback;
import com.feedhanjum.back_end.feedback.domain.FeedbackType;
import com.feedhanjum.back_end.member.domain.Member;
import com.feedhanjum.back_end.team.domain.Team;
import com.feedhanjum.back_end.test.util.DomainTestUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.feedhanjum.back_end.test.util.DomainTestUtils.createFeedbackWithId;
import static org.assertj.core.api.Assertions.assertThat;

class FeedbackReceiveNotificationTest {

    @Test
    @DisplayName("익명 피드백은 보낸사람 이름이 '익명'이어야 한다")
    void test1() {
        // given
        Member sender = DomainTestUtils.createMemberWithId("sender");
        Member receiver = DomainTestUtils.createMemberWithId("receiver");
        Team team = DomainTestUtils.createTeamWithId("team", sender);
        Feedback feedback = createFeedbackWithId(sender, receiver, team, FeedbackType.ANONYMOUS);

        // when
        FeedbackReceiveNotification notification = new FeedbackReceiveNotification(feedback);

        //then
        assertThat(notification.getSenderName()).isEqualTo("익명");
    }

    @Test
    @DisplayName("실명 피드백은 보낸사람 이름이 보여야 한다")
    void test2() {
        // given
        Member sender = DomainTestUtils.createMemberWithId("sender");
        Member receiver = DomainTestUtils.createMemberWithId("receiver");
        Team team = DomainTestUtils.createTeamWithId("team", sender);
        Feedback feedback = createFeedbackWithId(sender, receiver, team, FeedbackType.IDENTIFIED);

        // when
        FeedbackReceiveNotification notification = new FeedbackReceiveNotification(feedback);

        //then
        assertThat(notification.getSenderName()).isEqualTo("sender");
    }
}