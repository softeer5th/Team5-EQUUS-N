package com.feedhanjum.back_end.notification.domain;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@DiscriminatorValue(NotificationType.FREQUENT_FEEDBACK_REQUEST)
public class FrequentFeedbackRequestNotification extends InAppNotification {
    private String senderName;
    private Long teamId;

    public FrequentFeedbackRequestNotification(Long receiverId, String senderName, Long teamId) {
        super(receiverId);
        this.senderName = senderName;
        this.teamId = teamId;
    }
}
