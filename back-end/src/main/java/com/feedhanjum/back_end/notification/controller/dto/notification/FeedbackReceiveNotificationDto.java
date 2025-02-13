package com.feedhanjum.back_end.notification.controller.dto.notification;

import com.feedhanjum.back_end.notification.domain.FeedbackReceiveNotification;
import com.feedhanjum.back_end.notification.domain.NotificationType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;


@Schema(description = "피드백 수신 알림")
@Getter
public class FeedbackReceiveNotificationDto extends InAppNotificationDto {
    @Schema(description = "알림 타입", allowableValues = NotificationType.FEEDBACK_RECEIVE)
    private final String type = NotificationType.FEEDBACK_RECEIVE;
    @Schema(description = "보낸 사람 이름. 익명 피드백의 경우 '익명'")
    private final String senderName;
    @Schema(description = "팀 이름")
    private final String teamName;

    @Builder
    public FeedbackReceiveNotificationDto(Long notificationId, Long receiverId, LocalDateTime createdAt, boolean isRead, String senderName, String teamName) {
        super(notificationId, receiverId, createdAt, isRead);
        this.senderName = senderName;
        this.teamName = teamName;
    }

    public static FeedbackReceiveNotificationDto from(FeedbackReceiveNotification notification) {
        return builder()
                .notificationId(notification.getId())
                .receiverId(notification.getReceiverId())
                .createdAt(notification.getCreatedAt())
                .isRead(notification.isRead())
                .senderName(notification.getSenderName())
                .teamName(notification.getTeamName())
                .build();
    }
}
