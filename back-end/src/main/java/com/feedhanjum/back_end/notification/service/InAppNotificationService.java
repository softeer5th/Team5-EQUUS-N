package com.feedhanjum.back_end.notification.service;

import com.feedhanjum.back_end.event.EventPublisher;
import com.feedhanjum.back_end.feedback.domain.Feedback;
import com.feedhanjum.back_end.feedback.event.FeedbackLikedEvent;
import com.feedhanjum.back_end.feedback.event.FeedbackReportCreatedEvent;
import com.feedhanjum.back_end.feedback.event.FrequentFeedbackCreatedEvent;
import com.feedhanjum.back_end.feedback.event.RegularFeedbackCreatedEvent;
import com.feedhanjum.back_end.feedback.repository.FeedbackRepository;
import com.feedhanjum.back_end.member.domain.Member;
import com.feedhanjum.back_end.member.repository.MemberRepository;
import com.feedhanjum.back_end.notification.controller.dto.notification.InAppNotificationDto;
import com.feedhanjum.back_end.notification.domain.*;
import com.feedhanjum.back_end.notification.event.FeedbackReceiveNotificationUnreadEvent;
import com.feedhanjum.back_end.notification.event.InAppNotificationCreatedEvent;
import com.feedhanjum.back_end.notification.repository.InAppNotificationQueryRepository;
import com.feedhanjum.back_end.notification.repository.InAppNotificationRepository;
import com.feedhanjum.back_end.schedule.domain.Schedule;
import com.feedhanjum.back_end.schedule.event.RegularFeedbackRequestCreatedEvent;
import com.feedhanjum.back_end.schedule.event.ScheduleCreatedEvent;
import com.feedhanjum.back_end.schedule.repository.ScheduleRepository;
import com.feedhanjum.back_end.team.domain.Team;
import com.feedhanjum.back_end.team.domain.TeamMember;
import com.feedhanjum.back_end.team.event.FrequentFeedbackRequestedEvent;
import com.feedhanjum.back_end.team.event.TeamLeaderChangedEvent;
import com.feedhanjum.back_end.team.repository.FrequentFeedbackRequestRepository;
import com.feedhanjum.back_end.team.repository.TeamMemberRepository;
import com.feedhanjum.back_end.team.repository.TeamRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class InAppNotificationService {
    private final InAppNotificationRepository inAppNotificationRepository;
    private final InAppNotificationQueryRepository inAppNotificationQueryRepository;
    private final MemberRepository memberRepository;
    private final ScheduleRepository scheduleRepository;
    private final EventPublisher eventPublisher;
    private final FrequentFeedbackRequestRepository frequentFeedbackRequestRepository;
    private final FeedbackRepository feedbackRepository;
    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;


    @Transactional(readOnly = true)
    public List<InAppNotificationDto> getInAppNotifications(Long receiverId) {
        return inAppNotificationQueryRepository.getInAppNotifications(receiverId).stream()
                .map(InAppNotificationDto::from).toList();
    }

    /**
     * @throw EntityNotFoundException receiverId에 해당하는 엔티티가 없을 때
     */
    @Transactional
    public void readInAppNotifications(Long receiverId, List<Long> notificationIds) {
        Member receiver = memberRepository.findById(receiverId).orElseThrow(() -> new EntityNotFoundException("없는 사용자"));
        List<InAppNotification> notifications = inAppNotificationRepository.findAllById(notificationIds);
        for (InAppNotification notification : notifications) {
            notification.read(receiver);
        }
    }

    @Transactional
    public void createNotification(FrequentFeedbackRequestedEvent event) {
        Long senderId = event.senderId();
        Long receiverId = event.receiverId();
        Long teamId = event.teamId();

        Member sender = memberRepository.findById(senderId).orElseThrow();

        InAppNotification notification = new FrequentFeedbackRequestNotification(receiverId, sender.getName(), teamId);
        inAppNotificationRepository.save(notification);
        eventPublisher.publishEvent(new InAppNotificationCreatedEvent(notification.getId()));
    }

    @Transactional
    public void createNotification(RegularFeedbackRequestCreatedEvent event) {
        Long receiverId = event.receiverId();
        Long scheduleId = event.scheduleId();

        Member receiver = memberRepository.findById(receiverId)
                .orElseThrow(EntityNotFoundException::new);
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(EntityNotFoundException::new);

        InAppNotification notification = new RegularFeedbackRequestNotification(receiver, schedule);
        inAppNotificationRepository.save(notification);
        eventPublisher.publishEvent(new InAppNotificationCreatedEvent(notification.getId()));
    }

    @Transactional
    public void createNotification(FeedbackLikedEvent event) {
        Long feedbackId = event.feedbackId();

        Feedback feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(EntityNotFoundException::new);

        InAppNotification notification = new HeartReactionNotification(feedback);
        inAppNotificationRepository.save(notification);
        eventPublisher.publishEvent(new InAppNotificationCreatedEvent(notification.getId()));
    }

    @Transactional
    public void createNotification(FrequentFeedbackCreatedEvent event) {
        Long feedbackId = event.feedbackId();

        Feedback feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(EntityNotFoundException::new);

        InAppNotification notification = new FeedbackReceiveNotification(feedback);
        inAppNotificationRepository.save(notification);
        eventPublisher.publishEvent(new InAppNotificationCreatedEvent(notification.getId()));
    }

    @Transactional
    public void createNotification(RegularFeedbackCreatedEvent event) {
        Long feedbackId = event.feedbackId();

        Feedback feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(EntityNotFoundException::new);

        InAppNotification notification = new FeedbackReceiveNotification(feedback);
        inAppNotificationRepository.save(notification);
        eventPublisher.publishEvent(new InAppNotificationCreatedEvent(notification.getId()));
    }

    @Transactional
    public void createNotification(FeedbackReportCreatedEvent event) {
        Long receiverId = event.receiverId();
        Long endedTeamId = event.endedTeamId();

        Member receiver = memberRepository.findById(receiverId)
                .orElseThrow(EntityNotFoundException::new);
        Team team = null;
        if (endedTeamId != null)
            team = teamRepository.findById(endedTeamId)
                    .orElseThrow(EntityNotFoundException::new);

        InAppNotification notification = new FeedbackReportCreateNotification(receiver, team);
        inAppNotificationRepository.save(notification);
        eventPublisher.publishEvent(new InAppNotificationCreatedEvent(notification.getId()));
    }

    @Transactional
    public void createNotification(TeamLeaderChangedEvent event) {
        Long teamId = event.teamId();

        Team team = teamRepository.findById(teamId)
                .orElseThrow(EntityNotFoundException::new);


        InAppNotification notification = new TeamLeaderChangeNotification(team);
        inAppNotificationRepository.save(notification);
        eventPublisher.publishEvent(new InAppNotificationCreatedEvent(notification.getId()));
    }

    @Transactional
    public void createNotification(ScheduleCreatedEvent event) {
        Long scheduleId = event.scheduleId();

        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(EntityNotFoundException::new);

        List<TeamMember> teamMembers = schedule.getTeam().getTeamMembers();
        for (TeamMember teamMember : teamMembers) {
            Member receiver = teamMember.getMember();
            InAppNotification notification = new ScheduleCreateNotification(receiver, schedule);
            inAppNotificationRepository.save(notification);
            eventPublisher.publishEvent(new InAppNotificationCreatedEvent(notification.getId()));
        }
    }


    @Transactional
    public void createNotification(FeedbackReceiveNotificationUnreadEvent event) {
        Long notificationId = event.notificationId();

        InAppNotification feedbackReceiveNotification = inAppNotificationRepository.findById(notificationId)
                .orElseThrow(EntityNotFoundException::new);

        if (!(feedbackReceiveNotification instanceof FeedbackReceiveNotification)) {
            throw new RuntimeException("피드백 도착 알림이 아닙니다.");
        }
        // TODO: FeedbackReceiveNotificationUnreadEvent를 생성시키는 배치 작업에서는 receiver별로 안읽은지 24시간이 지난 feedbackReceive 알림이 여러개 있다면 가장 최신의 것만 이벤트로 발행해야 함
        // 기존에 존재하던 24시간 지난 '피드백 도착' 알림은 삭제
        inAppNotificationRepository.removeAllByReceiverIdAndTypeAndIdLessThanEqual(feedbackReceiveNotification.getReceiverId(),
                NotificationType.FEEDBACK_RECEIVE, feedbackReceiveNotification.getId());

        InAppNotification notification = new UnreadFeedbackExistNotification((FeedbackReceiveNotification) feedbackReceiveNotification);
        inAppNotificationRepository.save(notification);
        eventPublisher.publishEvent(new InAppNotificationCreatedEvent(notification.getId()));
    }
}
