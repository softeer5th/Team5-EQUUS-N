package com.feedhanjum.back_end.feedback.service;

import com.feedhanjum.back_end.event.EventPublisher;
import com.feedhanjum.back_end.feedback.domain.Feedback;
import com.feedhanjum.back_end.feedback.domain.FeedbackFeeling;
import com.feedhanjum.back_end.feedback.domain.FeedbackType;
import com.feedhanjum.back_end.feedback.domain.ObjectiveFeedback;
import com.feedhanjum.back_end.feedback.event.FeedbackLikedEvent;
import com.feedhanjum.back_end.feedback.event.FrequentFeedbackCreatedEvent;
import com.feedhanjum.back_end.feedback.event.RegularFeedbackCreatedEvent;
import com.feedhanjum.back_end.feedback.exception.NoRegularFeedbackRequestException;
import com.feedhanjum.back_end.feedback.repository.FeedbackRepository;
import com.feedhanjum.back_end.member.domain.Member;
import com.feedhanjum.back_end.member.domain.ProfileImage;
import com.feedhanjum.back_end.member.repository.MemberRepository;
import com.feedhanjum.back_end.schedule.domain.RegularFeedbackRequest;
import com.feedhanjum.back_end.schedule.domain.Schedule;
import com.feedhanjum.back_end.schedule.domain.ScheduleMember;
import com.feedhanjum.back_end.schedule.event.RegularFeedbackRequestCreatedEvent;
import com.feedhanjum.back_end.schedule.repository.RegularFeedbackRequestRepository;
import com.feedhanjum.back_end.schedule.repository.ScheduleMemberRepository;
import com.feedhanjum.back_end.schedule.repository.ScheduleRepository;
import com.feedhanjum.back_end.team.domain.FrequentFeedbackRequest;
import com.feedhanjum.back_end.team.domain.Team;
import com.feedhanjum.back_end.team.event.FrequentFeedbackRequestedEvent;
import com.feedhanjum.back_end.team.exception.TeamMembershipNotFoundException;
import com.feedhanjum.back_end.team.repository.FrequentFeedbackRequestRepository;
import com.feedhanjum.back_end.team.repository.TeamMemberRepository;
import com.feedhanjum.back_end.team.repository.TeamRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FeedbackServiceTest {
    @Mock
    private MemberRepository memberRepository;
    @Mock
    private TeamRepository teamRepository;
    @Mock
    private FeedbackRepository feedbackRepository;
    @Mock
    private TeamMemberRepository teamMemberRepository;
    @Mock
    private ScheduleRepository scheduleRepository;
    @Mock
    private ScheduleMemberRepository scheduleMemberRepository;
    @Mock
    private FrequentFeedbackRequestRepository frequentFeedbackRequestRepository;
    @Mock
    private RegularFeedbackRequestRepository regularFeedbackRequestRepository;
    @Mock
    private EventPublisher eventPublisher;
    @InjectMocks
    private FeedbackService feedbackService;

    private final Clock clock = Clock.fixed(Instant.parse("2025-01-10T12:00:00Z"), ZoneId.systemDefault());

    private final AtomicLong nextId = new AtomicLong(1L);

    private Member createMember(String name) {
        Member member = new Member(name, name + "@test.com", new ProfileImage("bg-" + name, "profile-" + name));
        ReflectionTestUtils.setField(member, "id", nextId.getAndIncrement());
        return member;
    }

    private Team createTeam(String name, Member leader) {
        Team team = new Team(name, leader, LocalDate.now(clock).minusDays(1), LocalDate.now(clock).plusDays(1), FeedbackType.ANONYMOUS);
        ReflectionTestUtils.setField(team, "id", nextId.getAndIncrement());
        return team;
    }

    private Schedule createSchedule(String name, Team team, Member leader, boolean isEnd) {
        LocalDateTime start, end;
        if (isEnd) {
            start = LocalDateTime.now(clock).minusHours(1);
            end = LocalDateTime.now(clock).minusMinutes(10);
        } else {
            start = LocalDateTime.now(clock);
            end = LocalDateTime.now(clock).plusHours(1);
        }
        Schedule schedule = new Schedule(name, start, end, team, leader);
        ReflectionTestUtils.setField(schedule, "id", nextId.getAndIncrement());
        return schedule;
    }

    private Feedback createFeedback(Member sender, Member receiver, Team team) {
        Feedback feedback = Feedback.builder()
                .sender(sender)
                .receiver(receiver)
                .team(team)
                .feedbackType(FeedbackType.IDENTIFIED)
                .feedbackFeeling(FeedbackFeeling.POSITIVE)
                .objectiveFeedbacks(FeedbackFeeling.POSITIVE.getObjectiveFeedbacks().subList(0, 2))
                .subjectiveFeedback("좋아요")
                .build();
        ReflectionTestUtils.setField(feedback, "id", nextId.getAndIncrement());
        return feedback;
    }

    @Nested
    @DisplayName("sendFrequentFeedback 메서드 테스트")
    class SendFrequentFeedbackTest {
        @Test
        @DisplayName("수시 피드백 전송 성공")
        void test1() {
            // given
            Long senderId = 1L;
            Long receiverId = 2L;
            Long teamId = 3L;
            Member sender = mock();
            Member receiver = mock();
            Team team = mock();

            FeedbackType feedbackType = FeedbackType.IDENTIFIED;
            FeedbackFeeling feedbackFeeling = FeedbackFeeling.POSITIVE;
            List<ObjectiveFeedback> objectiveFeedbacks = feedbackFeeling.getObjectiveFeedbacks().subList(0, 2);
            String subjectiveFeedback = "좋아요";
            when(memberRepository.findById(senderId)).thenReturn(Optional.of(sender));
            when(memberRepository.findById(receiverId)).thenReturn(Optional.of(receiver));
            when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));
            when(feedbackRepository.save(any(Feedback.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // when
            Feedback feedback = feedbackService.sendFrequentFeedback(senderId, receiverId, teamId, feedbackType, feedbackFeeling, objectiveFeedbacks, subjectiveFeedback);
            // then
            assertThat(feedback.getSender()).isEqualTo(sender);
            assertThat(feedback.getReceiver()).isEqualTo(receiver);
            assertThat(feedback.getTeam()).isEqualTo(team);
            assertThat(feedback.getFeedbackType()).isEqualTo(feedbackType);
            assertThat(feedback.getFeedbackFeeling()).isEqualTo(feedbackFeeling);
            assertThat(feedback.getObjectiveFeedbacks())
                    .containsExactlyInAnyOrderElementsOf(objectiveFeedbacks);
            assertThat(feedback.getSubjectiveFeedback()).isEqualTo(subjectiveFeedback);
            assertThat(feedback.isLiked()).isFalse();

            verify(eventPublisher).publishEvent(any(FrequentFeedbackCreatedEvent.class));
        }

        @Test
        @DisplayName("수시 피드백 전송 실패 - sender가 없을 경우")
        void test2() {
            // given
            Long senderId = 1L;
            Long receiverId = 2L;
            Long teamId = 3L;

            FeedbackType feedbackType = FeedbackType.IDENTIFIED;
            FeedbackFeeling feedbackFeeling = FeedbackFeeling.POSITIVE;
            List<ObjectiveFeedback> objectiveFeedbacks = feedbackFeeling.getObjectiveFeedbacks().subList(0, 2);
            String subjectiveFeedback = "좋아요";
            when(memberRepository.findById(senderId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> feedbackService.sendFrequentFeedback(senderId, receiverId, teamId, feedbackType, feedbackFeeling, objectiveFeedbacks, subjectiveFeedback))
                    .isInstanceOf(EntityNotFoundException.class);

            verify(eventPublisher, never()).publishEvent(any(FrequentFeedbackCreatedEvent.class));
        }

        @Test
        @DisplayName("수시 피드백 전송 실패 - receiver가 없을 경우")
        void test3() {
            // given
            Long senderId = 1L;
            Long receiverId = 2L;
            Long teamId = 3L;
            Member sender = mock();

            FeedbackType feedbackType = FeedbackType.IDENTIFIED;
            FeedbackFeeling feedbackFeeling = FeedbackFeeling.POSITIVE;
            List<ObjectiveFeedback> objectiveFeedbacks = feedbackFeeling.getObjectiveFeedbacks().subList(0, 2);
            String subjectiveFeedback = "좋아요";
            when(memberRepository.findById(senderId)).thenReturn(Optional.of(sender));
            when(memberRepository.findById(receiverId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> feedbackService.sendFrequentFeedback(senderId, receiverId, teamId, feedbackType, feedbackFeeling, objectiveFeedbacks, subjectiveFeedback))
                    .isInstanceOf(EntityNotFoundException.class);

            verify(eventPublisher, never()).publishEvent(any(FrequentFeedbackCreatedEvent.class));
        }

        @Test
        @DisplayName("수시 피드백 전송 실패 - team이 없을 경우")
        void test4() {
            // given
            Long senderId = 1L;
            Long receiverId = 2L;
            Long teamId = 3L;
            Member sender = mock();
            Member receiver = mock();

            FeedbackType feedbackType = FeedbackType.IDENTIFIED;
            FeedbackFeeling feedbackFeeling = FeedbackFeeling.POSITIVE;
            List<ObjectiveFeedback> objectiveFeedbacks = feedbackFeeling.getObjectiveFeedbacks().subList(0, 2);
            String subjectiveFeedback = "좋아요";
            when(memberRepository.findById(senderId)).thenReturn(Optional.of(sender));
            when(memberRepository.findById(receiverId)).thenReturn(Optional.of(receiver));
            when(teamRepository.findById(teamId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> feedbackService.sendFrequentFeedback(senderId, receiverId, teamId, feedbackType, feedbackFeeling, objectiveFeedbacks, subjectiveFeedback))
                    .isInstanceOf(EntityNotFoundException.class);

            verify(eventPublisher, never()).publishEvent(any(FrequentFeedbackCreatedEvent.class));
        }

        @Test
        @DisplayName("수시 피드백 전송 실패 - 기분에 맞지 않는 객관식 피드백이 있을 경우")
        void test6() {
            // given
            Long senderId = 1L;
            Long receiverId = 2L;
            Long teamId = 3L;
            Member sender = mock();
            Member receiver = mock();
            Team team = mock();

            FeedbackType feedbackType = FeedbackType.IDENTIFIED;
            FeedbackFeeling feedbackFeeling = FeedbackFeeling.POSITIVE;
            List<ObjectiveFeedback> objectiveFeedbacks = FeedbackFeeling.CONSTRUCTIVE.getObjectiveFeedbacks().subList(0, 1);
            String subjectiveFeedback = "좋아요";
            when(memberRepository.findById(senderId)).thenReturn(Optional.of(sender));
            when(memberRepository.findById(receiverId)).thenReturn(Optional.of(receiver));
            when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));

            // when & then
            assertThatThrownBy(() -> feedbackService.sendFrequentFeedback(senderId, receiverId, teamId, feedbackType, feedbackFeeling, objectiveFeedbacks, subjectiveFeedback))
                    .isInstanceOf(IllegalArgumentException.class);

            verify(eventPublisher, never()).publishEvent(any(FrequentFeedbackCreatedEvent.class));
        }

        @Test
        @DisplayName("수시 피드백 전송 실패 - 객관식 피드백 개수가 1~5개가 아닌 경우")
        void test7() {
            // given
            Long senderId = 1L;
            Long receiverId = 2L;
            Long teamId = 3L;
            Member sender = mock();
            Member receiver = mock();
            Team team = mock();

            FeedbackType feedbackType = FeedbackType.IDENTIFIED;
            FeedbackFeeling feedbackFeeling = FeedbackFeeling.POSITIVE;
            List<ObjectiveFeedback> objectiveFeedbacks = FeedbackFeeling.CONSTRUCTIVE.getObjectiveFeedbacks().subList(0, 6);
            String subjectiveFeedback = "좋아요";
            when(memberRepository.findById(senderId)).thenReturn(Optional.of(sender));
            when(memberRepository.findById(receiverId)).thenReturn(Optional.of(receiver));
            when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));

            // when & then
            assertThatThrownBy(() -> feedbackService.sendFrequentFeedback(senderId, receiverId, teamId, feedbackType, feedbackFeeling, objectiveFeedbacks, subjectiveFeedback))
                    .isInstanceOf(IllegalArgumentException.class);

            verify(eventPublisher, never()).publishEvent(any(FrequentFeedbackCreatedEvent.class));
        }

    }

    @Nested
    @DisplayName("requestFrequentFeedback 메서드 테스트")
    class RequestFrequentFeedbackTest {
        @Test
        @DisplayName("수시 피드백 요청 성공")
        void test1() {
            // given
            String requestedContent = "좋아요";
            Member sender = createMember("sender");
            Member receiver = createMember("receiver");
            Team team = createTeam("team", sender);
            team.join(receiver);

            when(memberRepository.findById(sender.getId())).thenReturn(Optional.of(sender));
            when(memberRepository.findById(receiver.getId())).thenReturn(Optional.of(receiver));
            when(teamRepository.findById(team.getId())).thenReturn(Optional.of(team));

            // when
            feedbackService
                    .requestFrequentFeedback(sender.getId(), team.getId(), receiver.getId(), requestedContent);

            // then
            List<FrequentFeedbackRequest> requests = team.getFeedbackRequests(receiver);
            assertThat(requests).hasSize(1);
            FrequentFeedbackRequest result = requests.get(0);
            assertThat(result.getRequestedContent()).isEqualTo(requestedContent);
            assertThat(result.getTeam()).isEqualTo(team);
            assertThat(result.getReceiver()).isEqualTo(receiver);
            assertThat(result.getSender()).isEqualTo(sender);
            assertThat(result.getCreatedAt()).isCloseTo(LocalDateTime.now(), within(1, ChronoUnit.SECONDS));
            verify(eventPublisher, times(1)).publishEvent(any(FrequentFeedbackRequestedEvent.class));

        }

        @Test
        @DisplayName("수시 피드백 요청 실패 - sender가 없을 경우")
        void test2() {
            // given
            Long senderId = 1L;
            Long receiverId = 2L;
            Long teamId = 3L;
            String requestedContent = "좋아요";

            when(memberRepository.findById(senderId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> feedbackService.requestFrequentFeedback(senderId, teamId, receiverId, requestedContent))
                    .isInstanceOf(EntityNotFoundException.class);

            verify(eventPublisher, never()).publishEvent(any(FrequentFeedbackRequestedEvent.class));

        }

        @Test
        @DisplayName("수시 피드백 요청 실패 - receiver가 없을 경우")
        void test3() {
            // given
            Long senderId = 1L;
            Long receiverId = 2L;
            Long teamId = 3L;
            String requestedContent = "좋아요";
            Member sender = mock();

            when(memberRepository.findById(senderId)).thenReturn(Optional.of(sender));
            when(memberRepository.findById(receiverId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> feedbackService.requestFrequentFeedback(senderId, teamId, receiverId, requestedContent))
                    .isInstanceOf(EntityNotFoundException.class);

            verify(eventPublisher, never()).publishEvent(any(FrequentFeedbackRequestedEvent.class));

        }

        @Test
        @DisplayName("수시 피드백 요청 실패 - team이 없을 경우")
        void test4() {
            // given
            Long senderId = 1L;
            Long receiverId = 2L;
            Long teamId = 3L;
            String requestedContent = "좋아요";
            Member sender = mock();
            Member receiver = mock();

            when(memberRepository.findById(senderId)).thenReturn(Optional.of(sender));
            when(memberRepository.findById(receiverId)).thenReturn(Optional.of(receiver));
            when(teamRepository.findById(teamId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> feedbackService.requestFrequentFeedback(senderId, teamId, receiverId, requestedContent))
                    .isInstanceOf(EntityNotFoundException.class);

            verify(eventPublisher, never()).publishEvent(any(FrequentFeedbackRequestedEvent.class));

        }

        @Test
        @DisplayName("수시 피드백 요청 실패 - sender가 team에 속하지 않았을 경우")
        void test5() {
            // given
            String requestedContent = "좋아요";
            Member sender = createMember("sender");
            Member receiver = createMember("receiver");
            Team team = createTeam("team", receiver);

            when(memberRepository.findById(sender.getId())).thenReturn(Optional.of(sender));
            when(memberRepository.findById(receiver.getId())).thenReturn(Optional.of(receiver));
            when(teamRepository.findById(team.getId())).thenReturn(Optional.of(team));

            // when & then
            assertThatThrownBy(() -> feedbackService.requestFrequentFeedback(sender.getId(), team.getId(), receiver.getId(), requestedContent))
                    .isInstanceOf(TeamMembershipNotFoundException.class);


            verify(eventPublisher, never()).publishEvent(any(FrequentFeedbackRequestedEvent.class));
        }

        @Test
        @DisplayName("수시 피드백 요청 실패 - receiver가 team에 속하지 않았을 경우")
        void test6() {
            // given
            String requestedContent = "좋아요";
            Member sender = createMember("sender");
            Member receiver = createMember("receiver");
            Team team = createTeam("team", sender);

            when(memberRepository.findById(sender.getId())).thenReturn(Optional.of(sender));
            when(memberRepository.findById(receiver.getId())).thenReturn(Optional.of(receiver));
            when(teamRepository.findById(team.getId())).thenReturn(Optional.of(team));

            // when & then
            assertThatThrownBy(() -> feedbackService.requestFrequentFeedback(sender.getId(), team.getId(), receiver.getId(), requestedContent))
                    .isInstanceOf(TeamMembershipNotFoundException.class);


            verify(eventPublisher, never()).publishEvent(any(FrequentFeedbackRequestedEvent.class));

        }
    }


    @Nested
    @DisplayName("sendRegularFeedback 메서드 테스트")
    class SendRegularFeedbackTest {
        @Test
        @DisplayName("정기 피드백 전송 성공")
        void test1() {
            // given
            Long senderId = 1L;
            Long receiverId = 2L;
            Long scheduleId = 3L;
            Member sender = mock();
            Member receiver = mock();
            Team team = mock();
            Schedule schedule = mock();
            ScheduleMember senderMember = mock();
            ScheduleMember receiverMember = mock();
            RegularFeedbackRequest request = mock();


            when(scheduleMemberRepository.findByMemberIdAndScheduleId(senderId, scheduleId)).thenReturn(Optional.of(senderMember));
            when(scheduleMemberRepository.findByMemberIdAndScheduleId(receiverId, scheduleId)).thenReturn(Optional.of(receiverMember));

            when(senderMember.getMember()).thenReturn(sender);
            when(receiverMember.getMember()).thenReturn(receiver);
            when(senderMember.getSchedule()).thenReturn(schedule);

            when(regularFeedbackRequestRepository.findByRequesterAndScheduleMember(receiver, senderMember)).thenReturn(Optional.of(request));
            when(schedule.getTeam()).thenReturn(team);

            when(feedbackRepository.save(any(Feedback.class))).thenAnswer(invocation -> invocation.getArgument(0));

            FeedbackType feedbackType = FeedbackType.IDENTIFIED;
            FeedbackFeeling feedbackFeeling = FeedbackFeeling.POSITIVE;
            List<ObjectiveFeedback> objectiveFeedbacks = feedbackFeeling.getObjectiveFeedbacks().subList(0, 2);
            String subjectiveFeedback = "좋아요";


            // when
            Feedback feedback = feedbackService.sendRegularFeedback(senderId, receiverId, scheduleId, feedbackType, feedbackFeeling, objectiveFeedbacks, subjectiveFeedback);
            // then
            assertThat(feedback.getSender()).isEqualTo(sender);
            assertThat(feedback.getReceiver()).isEqualTo(receiver);
            assertThat(feedback.getTeam()).isEqualTo(team);
            assertThat(feedback.getFeedbackType()).isEqualTo(feedbackType);
            assertThat(feedback.getFeedbackFeeling()).isEqualTo(feedbackFeeling);
            assertThat(feedback.getObjectiveFeedbacks())
                    .containsExactlyInAnyOrderElementsOf(objectiveFeedbacks);
            assertThat(feedback.getSubjectiveFeedback()).isEqualTo(subjectiveFeedback);
            assertThat(feedback.isLiked()).isFalse();

            verify(regularFeedbackRequestRepository).delete(request);
            verify(eventPublisher).publishEvent(any(RegularFeedbackCreatedEvent.class));
        }

        @Test
        @DisplayName("정기 피드백 전송 실패 - sender가 일정에 속해있지 않을 경우")
        void test2() {
            // given
            Long senderId = 1L;
            Long receiverId = 2L;
            Long scheduleId = 3L;

            when(scheduleMemberRepository.findByMemberIdAndScheduleId(senderId, scheduleId)).thenReturn(Optional.empty());

            FeedbackType feedbackType = FeedbackType.IDENTIFIED;
            FeedbackFeeling feedbackFeeling = FeedbackFeeling.POSITIVE;
            List<ObjectiveFeedback> objectiveFeedbacks = feedbackFeeling.getObjectiveFeedbacks().subList(0, 2);
            String subjectiveFeedback = "좋아요";

            // when & then
            assertThatThrownBy(() -> feedbackService
                    .sendRegularFeedback(senderId, receiverId, scheduleId, feedbackType, feedbackFeeling, objectiveFeedbacks, subjectiveFeedback))
                    .isInstanceOf(EntityNotFoundException.class);

            verify(eventPublisher, never()).publishEvent(any(RegularFeedbackCreatedEvent.class));

        }

        @Test
        @DisplayName("정기 피드백 전송 실패 - receiver가 없을 경우")
        void test3() {
            // given
            Long senderId = 1L;
            Long receiverId = 2L;
            Long scheduleId = 3L;

            when(scheduleMemberRepository.findByMemberIdAndScheduleId(senderId, scheduleId)).thenReturn(Optional.empty());

            FeedbackType feedbackType = FeedbackType.IDENTIFIED;
            FeedbackFeeling feedbackFeeling = FeedbackFeeling.POSITIVE;
            List<ObjectiveFeedback> objectiveFeedbacks = feedbackFeeling.getObjectiveFeedbacks().subList(0, 2);
            String subjectiveFeedback = "좋아요";

            // when & then
            assertThatThrownBy(() -> feedbackService
                    .sendRegularFeedback(senderId, receiverId, scheduleId, feedbackType, feedbackFeeling, objectiveFeedbacks, subjectiveFeedback))
                    .isInstanceOf(EntityNotFoundException.class);

            verify(eventPublisher, never()).publishEvent(any(RegularFeedbackCreatedEvent.class));
        }

        @Test
        @DisplayName("정기 피드백 전송 실패 - 정기 피드백 요청이 없을 경우")
        void test4() {
            // given
            Long senderId = 1L;
            Long receiverId = 2L;
            Long scheduleId = 3L;
            Member sender = mock();
            Member receiver = mock();
            Schedule schedule = mock();
            ScheduleMember senderMember = mock();
            ScheduleMember receiverMember = mock();


            when(scheduleMemberRepository.findByMemberIdAndScheduleId(senderId, scheduleId)).thenReturn(Optional.of(senderMember));
            when(scheduleMemberRepository.findByMemberIdAndScheduleId(receiverId, scheduleId)).thenReturn(Optional.of(receiverMember));

            when(senderMember.getMember()).thenReturn(sender);
            when(receiverMember.getMember()).thenReturn(receiver);
            when(senderMember.getSchedule()).thenReturn(schedule);

            when(regularFeedbackRequestRepository.findByRequesterAndScheduleMember(receiver, senderMember)).thenReturn(Optional.empty());

            FeedbackType feedbackType = FeedbackType.IDENTIFIED;
            FeedbackFeeling feedbackFeeling = FeedbackFeeling.POSITIVE;
            List<ObjectiveFeedback> objectiveFeedbacks = feedbackFeeling.getObjectiveFeedbacks().subList(0, 2);
            String subjectiveFeedback = "좋아요";

            // when & then
            assertThatThrownBy(() -> feedbackService.sendRegularFeedback(senderId, receiverId, scheduleId, feedbackType, feedbackFeeling, objectiveFeedbacks, subjectiveFeedback))
                    .isInstanceOf(NoRegularFeedbackRequestException.class);

            verify(regularFeedbackRequestRepository, never()).delete(any());
            verify(eventPublisher, never()).publishEvent(any(RegularFeedbackCreatedEvent.class));
        }

        @Test
        @DisplayName("정기 피드백 전송 실패 - 기분에 맞지 않는 객관식 피드백이 있을 경우")
        void test6() {
            // given
            Long senderId = 1L;
            Long receiverId = 2L;
            Long scheduleId = 3L;
            Member sender = mock();
            Member receiver = mock();
            Team team = mock();
            Schedule schedule = mock();
            ScheduleMember senderMember = mock();
            ScheduleMember receiverMember = mock();
            RegularFeedbackRequest request = mock();


            when(scheduleMemberRepository.findByMemberIdAndScheduleId(senderId, scheduleId)).thenReturn(Optional.of(senderMember));
            when(scheduleMemberRepository.findByMemberIdAndScheduleId(receiverId, scheduleId)).thenReturn(Optional.of(receiverMember));

            when(senderMember.getMember()).thenReturn(sender);
            when(receiverMember.getMember()).thenReturn(receiver);
            when(senderMember.getSchedule()).thenReturn(schedule);

            when(regularFeedbackRequestRepository.findByRequesterAndScheduleMember(receiver, senderMember)).thenReturn(Optional.of(request));
            when(schedule.getTeam()).thenReturn(team);

            FeedbackType feedbackType = FeedbackType.ANONYMOUS;
            FeedbackFeeling feedbackFeeling = FeedbackFeeling.CONSTRUCTIVE;
            List<ObjectiveFeedback> objectiveFeedbacks = FeedbackFeeling.POSITIVE.getObjectiveFeedbacks().subList(0, 2);
            String subjectiveFeedback = "좋아요";

            // when & then
            assertThatThrownBy(() -> feedbackService.sendRegularFeedback(senderId, receiverId, scheduleId, feedbackType, feedbackFeeling, objectiveFeedbacks, subjectiveFeedback))
                    .isInstanceOf(IllegalArgumentException.class);

            verify(eventPublisher, never()).publishEvent(any(RegularFeedbackCreatedEvent.class));
        }

        @Test
        @DisplayName("정기 피드백 전송 실패 - 객관식 피드백 개수가 1~5개가 아닌 경우")
        void test7() {
            // given
            Long senderId = 1L;
            Long receiverId = 2L;
            Long scheduleId = 3L;
            Member sender = mock();
            Member receiver = mock();
            Team team = mock();
            Schedule schedule = mock();
            ScheduleMember senderMember = mock();
            ScheduleMember receiverMember = mock();
            RegularFeedbackRequest request = mock();


            when(scheduleMemberRepository.findByMemberIdAndScheduleId(senderId, scheduleId)).thenReturn(Optional.of(senderMember));
            when(scheduleMemberRepository.findByMemberIdAndScheduleId(receiverId, scheduleId)).thenReturn(Optional.of(receiverMember));

            when(senderMember.getMember()).thenReturn(sender);
            when(receiverMember.getMember()).thenReturn(receiver);
            when(senderMember.getSchedule()).thenReturn(schedule);

            when(regularFeedbackRequestRepository.findByRequesterAndScheduleMember(receiver, senderMember)).thenReturn(Optional.of(request));
            when(schedule.getTeam()).thenReturn(team);

            FeedbackType feedbackType = FeedbackType.ANONYMOUS;
            FeedbackFeeling feedbackFeeling = FeedbackFeeling.CONSTRUCTIVE;
            List<ObjectiveFeedback> objectiveFeedbacks = feedbackFeeling.getObjectiveFeedbacks().subList(0, 6);
            String subjectiveFeedback = "좋아요";

            // when & then
            assertThatThrownBy(() -> feedbackService.sendRegularFeedback(senderId, receiverId, scheduleId, feedbackType, feedbackFeeling, objectiveFeedbacks, subjectiveFeedback))
                    .isInstanceOf(IllegalArgumentException.class);

            verify(eventPublisher, never()).publishEvent(any(RegularFeedbackCreatedEvent.class));
        }
    }

    @Nested
    @DisplayName("likeFeedback 메서드 테스트")
    class LikeFeedbackTest {
        @Test
        @DisplayName("피드백 좋아요 성공")
        void test1() {
            // given
            Member receiver = createMember("receiver");
            Team team = createTeam("team", receiver);
            Feedback feedback = createFeedback(createMember("sender"), receiver, team);

            when(feedbackRepository.findById(feedback.getId())).thenReturn(Optional.of(feedback));
            when(memberRepository.findById(receiver.getId())).thenReturn(Optional.of(receiver));

            // when
            feedbackService.likeFeedback(feedback.getId(), receiver.getId());

            // then
            assertThat(feedback.isLiked()).isTrue();
            verify(eventPublisher).publishEvent(any(FeedbackLikedEvent.class));
        }

        @Test
        @DisplayName("피드백 좋아요 실패 - feedback이 없을 경우")
        void test2() {
            // given
            Long feedbackId = 1L;
            Long memberId = 2L;

            when(feedbackRepository.findById(feedbackId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> feedbackService.likeFeedback(feedbackId, memberId))
                    .isInstanceOf(EntityNotFoundException.class);

        }

        @Test
        @DisplayName("피드백 좋아요 실패 - receiver가 없을 경우")
        void test3() {
            // given
            Long feedbackId = 1L;
            Long memberId = 2L;
            Feedback feedback = mock();

            when(feedbackRepository.findById(feedbackId)).thenReturn(Optional.of(feedback));
            when(memberRepository.findById(memberId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> feedbackService.likeFeedback(feedbackId, memberId))
                    .isInstanceOf(EntityNotFoundException.class);

        }

        @Test
        @DisplayName("피드백 좋아요 실패 - receiver가 아닐 경우")
        void test4() {
            // given
            Member member = createMember("not-receiver");
            Feedback feedback = createFeedback(createMember("sender"), createMember("receiver"), createTeam("team", member));

            when(feedbackRepository.findById(feedback.getId())).thenReturn(Optional.of(feedback));
            when(memberRepository.findById(member.getId())).thenReturn(Optional.of(member));

            // when & then
            assertThatThrownBy(() -> feedbackService.likeFeedback(feedback.getId(), member.getId()))
                    .isInstanceOf(SecurityException.class);

        }
    }

    @Nested
    @DisplayName("unlikeFeedback 메서드 테스트")
    class UnlikeFeedbackTest {
        @Test
        @DisplayName("피드백 좋아요 취소 성공")
        void test1() {
            // given
            Member receiver = createMember("receiver");
            Team team = createTeam("team", receiver);
            Feedback feedback = createFeedback(createMember("sender"), receiver, team);
            feedback.like(receiver);

            when(feedbackRepository.findById(feedback.getId())).thenReturn(Optional.of(feedback));
            when(memberRepository.findById(receiver.getId())).thenReturn(Optional.of(receiver));

            // when
            feedbackService.unlikeFeedback(feedback.getId(), receiver.getId());

            // then
            assertThat(feedback.isLiked()).isFalse();
        }

        @Test
        @DisplayName("피드백 좋아요 실패 - feedback이 없을 경우")
        void test2() {
            // given
            Long feedbackId = 1L;
            Long memberId = 2L;

            when(feedbackRepository.findById(feedbackId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> feedbackService.unlikeFeedback(feedbackId, memberId))
                    .isInstanceOf(EntityNotFoundException.class);

            verify(eventPublisher, never()).publishEvent(any(FeedbackLikedEvent.class));
        }

        @Test
        @DisplayName("피드백 좋아요 취소 실패 - receiver가 없을 경우")
        void test3() {
            // given
            Long feedbackId = 1L;
            Long memberId = 2L;
            Feedback feedback = mock();

            when(feedbackRepository.findById(feedbackId)).thenReturn(Optional.of(feedback));
            when(memberRepository.findById(memberId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> feedbackService.unlikeFeedback(feedbackId, memberId))
                    .isInstanceOf(EntityNotFoundException.class);

        }

        @Test
        @DisplayName("피드백 좋아요 취소 실패 - receiver가 아닐 경우")
        void test4() {
            // given
            Member member = createMember("not-receiver");
            Feedback feedback = createFeedback(createMember("sender"), createMember("receiver"), createTeam("team", member));

            when(feedbackRepository.findById(feedback.getId())).thenReturn(Optional.of(feedback));
            when(memberRepository.findById(member.getId())).thenReturn(Optional.of(member));

            // when & then
            assertThatThrownBy(() -> feedbackService.unlikeFeedback(feedback.getId(), member.getId()))
                    .isInstanceOf(SecurityException.class);
        }
    }

    @Nested
    @DisplayName("createRegularFeedbackRequests 메서드 테스트")
    class CreateRegularFeedbackRequestsTest {
        @Test
        @DisplayName("정기 피드백 요청 생성 성공")
        void test1() {
            // given
            Member member1 = createMember("member1");
            Member member2 = createMember("member2");
            Member member3 = createMember("member3");
            Team team = createTeam("team", member1);
            Schedule schedule = createSchedule("schedule", team, member1, true);
            ScheduleMember scheduleMember1 = new ScheduleMember(schedule, member1);
            ScheduleMember scheduleMember2 = new ScheduleMember(schedule, member2);
            ScheduleMember scheduleMember3 = new ScheduleMember(schedule, member3);

            when(scheduleRepository.findByIdWithMembers(schedule.getId())).thenReturn(Optional.of(schedule));
            // when
            feedbackService.createRegularFeedbackRequests(schedule.getId());

            // then
            ArgumentCaptor<List<RegularFeedbackRequest>> requestsCaptor = ArgumentCaptor.captor();
            verify(regularFeedbackRequestRepository).saveAll(requestsCaptor.capture());
            List<RegularFeedbackRequest> requests = requestsCaptor.getValue();
            assertThat(requests).hasSize(6);
            assertThat(requests)
                    .filteredOn(r -> r.getScheduleMember() == scheduleMember1)
                    .extracting(RegularFeedbackRequest::getRequester)
                    .containsExactlyInAnyOrder(member2, member3);
            assertThat(requests)
                    .filteredOn(r -> r.getScheduleMember() == scheduleMember2)
                    .extracting(RegularFeedbackRequest::getRequester)
                    .containsExactlyInAnyOrder(member1, member3);

            assertThat(requests)
                    .filteredOn(r -> r.getScheduleMember() == scheduleMember3)
                    .extracting(RegularFeedbackRequest::getRequester)
                    .containsExactlyInAnyOrder(member1, member2);

            ArgumentCaptor<RegularFeedbackRequestCreatedEvent> eventCaptor = ArgumentCaptor.captor();
            verify(eventPublisher, times(3)).publishEvent(eventCaptor.capture());
            List<RegularFeedbackRequestCreatedEvent> events = eventCaptor.getAllValues();
            assertThat(events).extracting(RegularFeedbackRequestCreatedEvent::receiverId)
                    .containsExactlyInAnyOrder(member1.getId(), member2.getId(), member3.getId());
            assertThat(events).extracting(RegularFeedbackRequestCreatedEvent::scheduleId)
                    .containsOnly(schedule.getId());
        }

        @Test
        @DisplayName("정기 피드백 요청 생성 실패 - schedule이 없을 경우")
        void test2() {
            // given
            Long scheduleId = 1L;

            when(scheduleRepository.findByIdWithMembers(scheduleId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> feedbackService.createRegularFeedbackRequests(scheduleId))
                    .isInstanceOf(EntityNotFoundException.class);

            verify(regularFeedbackRequestRepository, never()).saveAll(any());
            verify(eventPublisher, never()).publishEvent(any(RegularFeedbackRequestCreatedEvent.class));
        }

    }

    @Nested
    @DisplayName("skipRegularFeedback 메서드 테스트")
    class SkipRegularFeedbackTest {
        @Test
        @DisplayName("정기 피드백 건너뛰기 성공")
        void test1() {
            // given
            Long scheduleId = 1L;
            Long memberId = 2L;
            ScheduleMember scheduleMember = mock();

            when(scheduleMemberRepository.findByMemberIdAndScheduleId(memberId, scheduleId)).thenReturn(Optional.of(scheduleMember));

            // when
            feedbackService.skipRegularFeedback(scheduleId, memberId);

            // then
            verify(regularFeedbackRequestRepository).deleteAllByScheduleMember(scheduleMember);
        }

        @Test
        @DisplayName("정기 피드백 건너뛰기 실패 - 일정에 속한 member가 없을 경우")
        void test2() {
            // given
            Long scheduleId = 1L;
            Long memberId = 2L;

            when(scheduleMemberRepository.findByMemberIdAndScheduleId(memberId, scheduleId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> feedbackService.skipRegularFeedback(scheduleId, memberId))
                    .isInstanceOf(EntityNotFoundException.class);

            verify(regularFeedbackRequestRepository, never()).deleteAllByScheduleMember(any());
        }
    }


    @Nested
    @DisplayName("rejectAllFrequentFeedbackRequests 메서드 테스트")
    class RejectAllFrequentFeedbackRequestsTest {
        @Test
        @DisplayName("모든 수시 피드백 요청 거절 성공")
        void test1() {
            // given
            Member receiver = createMember("receiver");
            Team team = createTeam("team", receiver);
            Member sender = createMember("sender");
            team.join(sender);
            team.requestFeedback(sender, receiver, "좋아요");
            team.requestFeedback(sender, receiver, "좋아요2");

            when(teamRepository.findById(team.getId())).thenReturn(Optional.of(team));
            when(memberRepository.findById(receiver.getId())).thenReturn(Optional.of(receiver));

            // when
            feedbackService.rejectAllFrequentFeedbackRequests(receiver.getId(), team.getId());

            // then
            assertThat(team.getFeedbackRequests(receiver)).isEmpty();
        }

        @Test
        @DisplayName("모든 수시 피드백 요청 거절 실패 - member가 team에 속하지 않았을 경우")
        void test2() {
            // given
            Member receiver = createMember("receiver");
            Team team = createTeam("team", createMember("notReceiver"));

            when(teamRepository.findById(team.getId())).thenReturn(Optional.of(team));
            when(memberRepository.findById(receiver.getId())).thenReturn(Optional.of(receiver));

            // when & then
            assertThatThrownBy(() -> feedbackService.rejectAllFrequentFeedbackRequests(receiver.getId(), team.getId()))
                    .isInstanceOf(TeamMembershipNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("deleteRelatedFrequentFeedbackRequest 메서드 테스트")
    class DeleteRelatedFrequentFeedbackRequestTest {
        @Test
        @DisplayName("연관 수시 피드백 요청 삭제 성공")
        void test1() {
            // given
            Member sender = createMember("sender");
            Member receiver = createMember("receiver");
            Team team = createTeam("team", sender);
            team.join(receiver);
            team.requestFeedback(sender, receiver, "좋아요");

            Feedback feedback = createFeedback(sender, receiver, team);

            when(feedbackRepository.findById(feedback.getId())).thenReturn(Optional.of(feedback));

            // when
            feedbackService.deleteRelatedFrequentFeedbackRequest(feedback.getId());

            // then
            assertThat(team.getFeedbackRequests(receiver)).isEmpty();
        }


        @Test
        @DisplayName("연관 수시 피드백 요청 삭제 실패 - feedback이 없을 경우")
        void test2() {
            // given
            Long feedbackId = 1L;

            when(feedbackRepository.findById(feedbackId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> feedbackService.deleteRelatedFrequentFeedbackRequest(feedbackId))
                    .isInstanceOf(EntityNotFoundException.class);

            verify(frequentFeedbackRequestRepository, never()).delete(any());
        }

        @Test
        @DisplayName("연관 수시 피드백 요청 삭제 실패 - sender가 team에 속하지 않았을 경우")
        void test3() {
            // given
            Member receiver = createMember("receiver");
            Team team = createTeam("team", receiver);
            Member sender = createMember("sender");
            Feedback feedback = createFeedback(sender, receiver, team);

            when(feedbackRepository.findById(feedback.getId())).thenReturn(Optional.of(feedback));

            // when & then
            assertThatThrownBy(() -> feedbackService.deleteRelatedFrequentFeedbackRequest(feedback.getId()))
                    .isInstanceOf(TeamMembershipNotFoundException.class);

            verify(frequentFeedbackRequestRepository, never()).delete(any());
        }
    }
}