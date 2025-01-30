package com.feedhanjum.back_end.feedback.service;

import com.feedhanjum.back_end.event.EventPublisher;
import com.feedhanjum.back_end.feedback.domain.Feedback;
import com.feedhanjum.back_end.feedback.domain.FeedbackCategory;
import com.feedhanjum.back_end.feedback.domain.FeedbackType;
import com.feedhanjum.back_end.feedback.domain.ObjectiveFeedback;
import com.feedhanjum.back_end.feedback.event.FrequentFeedbackCreatedEvent;
import com.feedhanjum.back_end.feedback.repository.FeedbackRepository;
import com.feedhanjum.back_end.member.domain.Member;
import com.feedhanjum.back_end.member.repository.MemberRepository;
import com.feedhanjum.back_end.team.domain.FrequentFeedbackRequest;
import com.feedhanjum.back_end.team.domain.Team;
import com.feedhanjum.back_end.team.domain.TeamMember;
import com.feedhanjum.back_end.team.event.FrequentFeedbackRequestCreatedEvent;
import com.feedhanjum.back_end.team.repository.FrequentFeedbackRequestRepository;
import com.feedhanjum.back_end.team.repository.TeamMemberRepository;
import com.feedhanjum.back_end.team.repository.TeamRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

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
    private FrequentFeedbackRequestRepository frequentFeedbackRequestRepository;
    @Mock
    private EventPublisher eventPublisher;
    @InjectMocks
    private FeedbackService feedbackService;

    @Nested
    @DisplayName("sendFrequentFeedback 메서드 테스트")
    class SendFrequentFeedbackTest {
        @Test
        @DisplayName("피드백 전송 성공")
        void test1() {
            // given
            Long senderId = 1L;
            Long receiverId = 2L;
            Long teamId = 3L;
            Member sender = mock();
            Member receiver = mock();
            Team team = mock();

            FeedbackType feedbackType = FeedbackType.IDENTIFIED;
            FeedbackCategory feedbackCategory = FeedbackCategory.POSITIVE;
            List<String> objectiveFeedbacks = feedbackCategory.getObjectiveFeedbacks().subList(0, 2)
                    .stream().map(ObjectiveFeedback::getContent).toList();
            String subjectiveFeedback = "좋아요";
            when(memberRepository.findById(senderId)).thenReturn(Optional.of(sender));
            when(memberRepository.findById(receiverId)).thenReturn(Optional.of(receiver));
            when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));
            when(feedbackRepository.save(any(Feedback.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // when
            Feedback feedback = feedbackService.sendFrequentFeedback(senderId, receiverId, teamId, feedbackType, feedbackCategory, objectiveFeedbacks, subjectiveFeedback);
            // then
            assertThat(feedback.getSender()).isEqualTo(sender);
            assertThat(feedback.getReceiver()).isEqualTo(receiver);
            assertThat(feedback.getTeam()).isEqualTo(team);
            assertThat(feedback.getFeedbackType()).isEqualTo(feedbackType);
            assertThat(feedback.getFeedbackCategory()).isEqualTo(feedbackCategory);
            assertThat(feedback.getObjectiveFeedbacks()).extracting(ObjectiveFeedback::getContent)
                    .containsExactlyInAnyOrderElementsOf(objectiveFeedbacks);
            assertThat(feedback.getSubjectiveFeedback()).isEqualTo(subjectiveFeedback);
            assertThat(feedback.isLiked()).isFalse();

            verify(eventPublisher).publishEvent(any(FrequentFeedbackCreatedEvent.class));
        }

        @Test
        @DisplayName("피드백 전송 실패 - sender가 없을 경우")
        void test2() {
            // given
            Long senderId = 1L;
            Long receiverId = 2L;
            Long teamId = 3L;

            FeedbackType feedbackType = FeedbackType.IDENTIFIED;
            FeedbackCategory feedbackCategory = FeedbackCategory.POSITIVE;
            List<String> objectiveFeedbacks = feedbackCategory.getObjectiveFeedbacks().subList(0, 2)
                    .stream().map(ObjectiveFeedback::getContent).toList();
            String subjectiveFeedback = "좋아요";
            when(memberRepository.findById(senderId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> feedbackService.sendFrequentFeedback(senderId, receiverId, teamId, feedbackType, feedbackCategory, objectiveFeedbacks, subjectiveFeedback))
                    .isInstanceOf(EntityNotFoundException.class);

            verify(eventPublisher, never()).publishEvent(any(FrequentFeedbackCreatedEvent.class));
        }

        @Test
        @DisplayName("피드백 전송 실패 - receiver가 없을 경우")
        void test3() {
            // given
            Long senderId = 1L;
            Long receiverId = 2L;
            Long teamId = 3L;
            Member sender = mock();

            FeedbackType feedbackType = FeedbackType.IDENTIFIED;
            FeedbackCategory feedbackCategory = FeedbackCategory.POSITIVE;
            List<String> objectiveFeedbacks = feedbackCategory.getObjectiveFeedbacks().subList(0, 2)
                    .stream().map(ObjectiveFeedback::getContent).toList();
            String subjectiveFeedback = "좋아요";
            when(memberRepository.findById(senderId)).thenReturn(Optional.of(sender));
            when(memberRepository.findById(receiverId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> feedbackService.sendFrequentFeedback(senderId, receiverId, teamId, feedbackType, feedbackCategory, objectiveFeedbacks, subjectiveFeedback))
                    .isInstanceOf(EntityNotFoundException.class);

            verify(eventPublisher, never()).publishEvent(any(FrequentFeedbackCreatedEvent.class));
        }

        @Test
        @DisplayName("피드백 전송 실패 - team이 없을 경우")
        void test4() {
            // given
            Long senderId = 1L;
            Long receiverId = 2L;
            Long teamId = 3L;
            Member sender = mock();
            Member receiver = mock();

            FeedbackType feedbackType = FeedbackType.IDENTIFIED;
            FeedbackCategory feedbackCategory = FeedbackCategory.POSITIVE;
            List<String> objectiveFeedbacks = feedbackCategory.getObjectiveFeedbacks().subList(0, 2)
                    .stream().map(ObjectiveFeedback::getContent).toList();
            String subjectiveFeedback = "좋아요";
            when(memberRepository.findById(senderId)).thenReturn(Optional.of(sender));
            when(memberRepository.findById(receiverId)).thenReturn(Optional.of(receiver));
            when(teamRepository.findById(teamId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> feedbackService.sendFrequentFeedback(senderId, receiverId, teamId, feedbackType, feedbackCategory, objectiveFeedbacks, subjectiveFeedback))
                    .isInstanceOf(EntityNotFoundException.class);

            verify(eventPublisher, never()).publishEvent(any(FrequentFeedbackCreatedEvent.class));
        }

        @Test
        @DisplayName("피드백 전송 실패 - 보기에 없는 객관식 피드백이 있을 경우")
        void test5() {
            // given
            Long senderId = 1L;
            Long receiverId = 2L;
            Long teamId = 3L;
            Member sender = mock();
            Member receiver = mock();
            Team team = mock();

            FeedbackType feedbackType = FeedbackType.IDENTIFIED;
            FeedbackCategory feedbackCategory = FeedbackCategory.POSITIVE;
            List<String> objectiveFeedbacks = List.of("존재하지 않는 객관식 피드백 값");
            String subjectiveFeedback = "좋아요";
            when(memberRepository.findById(senderId)).thenReturn(Optional.of(sender));
            when(memberRepository.findById(receiverId)).thenReturn(Optional.of(receiver));
            when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));

            // when & then
            assertThatThrownBy(() -> feedbackService.sendFrequentFeedback(senderId, receiverId, teamId, feedbackType, feedbackCategory, objectiveFeedbacks, subjectiveFeedback))
                    .isInstanceOf(IllegalArgumentException.class);

            verify(eventPublisher, never()).publishEvent(any(FrequentFeedbackCreatedEvent.class));
        }

        @Test
        @DisplayName("피드백 전송 실패 - 카테고리에 맞지 않는 객관식 피드백이 있을 경우")
        void test6() {
            // given
            Long senderId = 1L;
            Long receiverId = 2L;
            Long teamId = 3L;
            Member sender = mock();
            Member receiver = mock();
            Team team = mock();

            FeedbackType feedbackType = FeedbackType.IDENTIFIED;
            FeedbackCategory feedbackCategory = FeedbackCategory.POSITIVE;
            List<String> objectiveFeedbacks = FeedbackCategory.CONSTRUCTIVE.getObjectiveFeedbacks().subList(0, 1)
                    .stream().map(ObjectiveFeedback::getContent).toList();
            String subjectiveFeedback = "좋아요";
            when(memberRepository.findById(senderId)).thenReturn(Optional.of(sender));
            when(memberRepository.findById(receiverId)).thenReturn(Optional.of(receiver));
            when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));

            // when & then
            assertThatThrownBy(() -> feedbackService.sendFrequentFeedback(senderId, receiverId, teamId, feedbackType, feedbackCategory, objectiveFeedbacks, subjectiveFeedback))
                    .isInstanceOf(IllegalArgumentException.class);

            verify(eventPublisher, never()).publishEvent(any(FrequentFeedbackCreatedEvent.class));
        }

        @Test
        @DisplayName("피드백 전송 실패 - 객관식 피드백 개수가 1~5개가 아닌 경우")
        void test7() {
            // given
            Long senderId = 1L;
            Long receiverId = 2L;
            Long teamId = 3L;
            Member sender = mock();
            Member receiver = mock();
            Team team = mock();

            FeedbackType feedbackType = FeedbackType.IDENTIFIED;
            FeedbackCategory feedbackCategory = FeedbackCategory.POSITIVE;
            List<String> objectiveFeedbacks = FeedbackCategory.CONSTRUCTIVE.getObjectiveFeedbacks().subList(0, 6)
                    .stream().map(ObjectiveFeedback::getContent).toList();
            String subjectiveFeedback = "좋아요";
            when(memberRepository.findById(senderId)).thenReturn(Optional.of(sender));
            when(memberRepository.findById(receiverId)).thenReturn(Optional.of(receiver));
            when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));

            // when & then
            assertThatThrownBy(() -> feedbackService.sendFrequentFeedback(senderId, receiverId, teamId, feedbackType, feedbackCategory, objectiveFeedbacks, subjectiveFeedback))
                    .isInstanceOf(IllegalArgumentException.class);

            verify(eventPublisher, never()).publishEvent(any(FrequentFeedbackCreatedEvent.class));
        }

    }

    @Nested
    @DisplayName("requestFrequentFeedback 메서드 테스트")
    class RequestFrequentFeedbackTest {
        @Test
        @DisplayName("피드백 요청 성공")
        void test1() {
            // given
            Long senderId = 1L;
            String teamName = "team";
            Long receiverId = 2L;
            Long teamId = 3L;
            String requestedContent = "좋아요";
            Member sender = mock();
            Member receiver = mock();
            Team team = mock();
            TeamMember senderTeamMember = mock();
            TeamMember receiverTeamMember = mock();

            when(team.getId()).thenReturn(teamId);
            when(memberRepository.findById(senderId)).thenReturn(Optional.of(sender));
            when(memberRepository.findById(receiverId)).thenReturn(Optional.of(receiver));
            when(teamRepository.findByName(teamName)).thenReturn(Optional.of(team));
            when(teamMemberRepository.findByMemberIdAndTeamId(senderId, teamId)).thenReturn(Optional.of(senderTeamMember));
            when(teamMemberRepository.findByMemberIdAndTeamId(receiverId, teamId)).thenReturn(Optional.of(receiverTeamMember));

            // when
            FrequentFeedbackRequest result = feedbackService
                    .requestFrequentFeedback(senderId, teamName, receiverId, requestedContent);

            // then
            assertThat(result.getRequestedContent()).isEqualTo(requestedContent);
            assertThat(result.getTeamMember()).isEqualTo(receiverTeamMember);
            assertThat(result.getRequester()).isEqualTo(sender);
            assertThat(result.getCreatedAt()).isCloseTo(LocalDateTime.now(), within(1, ChronoUnit.SECONDS));
            verify(frequentFeedbackRequestRepository, times(1)).save(result);
            verify(eventPublisher, times(1)).publishEvent(any(FrequentFeedbackRequestCreatedEvent.class));

        }

        @Test
        @DisplayName("피드백 요청 실패 - sender가 없을 경우")
        void test2() {
            // given
            Long senderId = 1L;
            String teamName = "team";
            Long receiverId = 2L;
            String requestedContent = "좋아요";

            when(memberRepository.findById(senderId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> feedbackService.requestFrequentFeedback(senderId, teamName, receiverId, requestedContent))
                    .isInstanceOf(EntityNotFoundException.class);

            verify(eventPublisher, never()).publishEvent(any(FrequentFeedbackRequestCreatedEvent.class));

        }

        @Test
        @DisplayName("피드백 요청 실패 - receiver가 없을 경우")
        void test3() {
            // given
            Long senderId = 1L;
            String teamName = "team";
            Long receiverId = 2L;
            String requestedContent = "좋아요";
            Member sender = mock();

            when(memberRepository.findById(senderId)).thenReturn(Optional.of(sender));
            when(memberRepository.findById(receiverId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> feedbackService.requestFrequentFeedback(senderId, teamName, receiverId, requestedContent))
                    .isInstanceOf(EntityNotFoundException.class);

            verify(eventPublisher, never()).publishEvent(any(FrequentFeedbackRequestCreatedEvent.class));

        }

        @Test
        @DisplayName("피드백 요청 실패 - team이 없을 경우")
        void test4() {
            // given
            Long senderId = 1L;
            String teamName = "team";
            Long receiverId = 2L;
            String requestedContent = "좋아요";
            Member sender = mock();
            Member receiver = mock();

            when(memberRepository.findById(senderId)).thenReturn(Optional.of(sender));
            when(memberRepository.findById(receiverId)).thenReturn(Optional.of(receiver));
            when(teamRepository.findByName(teamName)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> feedbackService.requestFrequentFeedback(senderId, teamName, receiverId, requestedContent))
                    .isInstanceOf(EntityNotFoundException.class);

            verify(eventPublisher, never()).publishEvent(any(FrequentFeedbackRequestCreatedEvent.class));

        }

        @Test
        @DisplayName("피드백 요청 실패 - sender가 team에 속하지 않았을 경우")
        void test5() {
            // given
            Long senderId = 1L;
            Long receiverId = 2L;
            Long teamId = 3L;
            String teamName = "team";
            String requestedContent = "좋아요";
            Member sender = mock();
            Member receiver = mock();
            Team team = mock();

            when(team.getId()).thenReturn(teamId);
            when(memberRepository.findById(senderId)).thenReturn(Optional.of(sender));
            when(memberRepository.findById(receiverId)).thenReturn(Optional.of(receiver));
            when(teamRepository.findByName(teamName)).thenReturn(Optional.of(team));
            when(teamMemberRepository.findByMemberIdAndTeamId(senderId, teamId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> feedbackService.requestFrequentFeedback(senderId, teamName, receiverId, requestedContent))
                    .isInstanceOf(EntityNotFoundException.class);


            verify(eventPublisher, never()).publishEvent(any(FrequentFeedbackRequestCreatedEvent.class));
        }

        @Test
        @DisplayName("피드백 요청 실패 - receiver가 team에 속하지 않았을 경우")
        void test6() {
            // given
            Long senderId = 1L;
            Long receiverId = 2L;
            Long teamId = 3L;
            String teamName = "team";
            String requestedContent = "좋아요";
            Member sender = mock();
            Member receiver = mock();
            Team team = mock();
            TeamMember senderTeamMember = mock();

            when(team.getId()).thenReturn(teamId);
            when(memberRepository.findById(senderId)).thenReturn(Optional.of(sender));
            when(memberRepository.findById(receiverId)).thenReturn(Optional.of(receiver));
            when(teamRepository.findByName(teamName)).thenReturn(Optional.of(team));
            when(teamMemberRepository.findByMemberIdAndTeamId(senderId, teamId)).thenReturn(Optional.of(senderTeamMember));
            when(teamMemberRepository.findByMemberIdAndTeamId(receiverId, teamId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> feedbackService.requestFrequentFeedback(senderId, teamName, receiverId, requestedContent))
                    .isInstanceOf(EntityNotFoundException.class);


            verify(eventPublisher, never()).publishEvent(any(FrequentFeedbackRequestCreatedEvent.class));

        }
    }

    @Nested
    @DisplayName("getFrequentFeedbackRequests 메서드 테스트")
    class GetFrequentFeedbackRequestTest {
        @Test
        @DisplayName("수시 피드백 요청 조회 성공")
        void test1() {
            // given
            Long receiverId = 1L;
            Long teamId = 2L;
            String teamName = "team";
            Member receiver = mock();
            Team team = mock();
            TeamMember teamMember = mock();
            List<FrequentFeedbackRequest> requests = List.of(mock(), mock());


            when(memberRepository.findById(receiverId)).thenReturn(Optional.of(receiver));
            when(teamRepository.findByName(teamName)).thenReturn(Optional.of(team));
            when(team.getId()).thenReturn(teamId);
            when(teamMemberRepository.findByMemberIdAndTeamId(receiverId, team.getId())).thenReturn(Optional.of(teamMember));
            when(frequentFeedbackRequestRepository.findByTeamMember(teamMember)).thenReturn(requests);


            // when
            List<FrequentFeedbackRequest> result = feedbackService.getFrequentFeedbackRequests(receiverId, teamName);

            // then
            assertThat(result).isEqualTo(requests);
        }

        @Test
        @DisplayName("수시 피드백 요청 조회 실패 - receiver가 없을 경우")
        void test2() {
            // given
            Long receiverId = 1L;
            String teamName = "team";

            when(memberRepository.findById(receiverId)).thenReturn(Optional.empty());


            // when & then
            assertThatThrownBy(() -> feedbackService.getFrequentFeedbackRequests(receiverId, teamName))
                    .isInstanceOf(EntityNotFoundException.class);

        }

        @Test
        @DisplayName("수시 피드백 요청 조회 실패 - team이 없을 경우")
        void test3() {
            // given
            Long receiverId = 1L;
            String teamName = "team";
            Member receiver = mock();


            when(memberRepository.findById(receiverId)).thenReturn(Optional.of(receiver));
            when(teamRepository.findByName(teamName)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> feedbackService.getFrequentFeedbackRequests(receiverId, teamName))
                    .isInstanceOf(EntityNotFoundException.class);

        }

        @Test
        @DisplayName("수시 피드백 요청 조회 실패 - receiver가 team에 속하지 않았을 경우")
        void test4() {
            // given
            Long receiverId = 1L;
            Long teamId = 2L;
            String teamName = "team";
            Member receiver = mock();
            Team team = mock();


            when(memberRepository.findById(receiverId)).thenReturn(Optional.of(receiver));
            when(teamRepository.findByName(teamName)).thenReturn(Optional.of(team));
            when(team.getId()).thenReturn(teamId);
            when(teamMemberRepository.findByMemberIdAndTeamId(receiverId, team.getId())).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> feedbackService.getFrequentFeedbackRequests(receiverId, teamName))
                    .isInstanceOf(EntityNotFoundException.class);

        }
    }
}