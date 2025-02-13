package com.feedhanjum.back_end.notification.domain;

import com.feedhanjum.back_end.feedback.domain.Feedback;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@DiscriminatorValue(NotificationType.FEEDBACK_RECEIVE)
public class FeedbackReceiveNotification extends InAppNotification {
    private static final String ANONYMOUS_SENDER_NAME = "익명";
    private String senderName;
    private String teamName;
    private Long teamId;

    public FeedbackReceiveNotification(Feedback receivedFeedback) {
        super(receivedFeedback.getReceiver().getId());
        if (receivedFeedback.getFeedbackType().isAnonymous())
            this.senderName = ANONYMOUS_SENDER_NAME;
        else
            this.senderName = receivedFeedback.getSender().getName();
        this.teamName = receivedFeedback.getTeam().getName();
        this.teamId = receivedFeedback.getTeam().getId();
    }
}
