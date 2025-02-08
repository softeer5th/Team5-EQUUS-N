package com.feedhanjum.back_end.notification.event.handler;

import com.feedhanjum.back_end.notification.service.InAppNotificationService;
import com.feedhanjum.back_end.team.event.FrequentFeedbackRequestedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@RequiredArgsConstructor
@Component
public class FrequentFeedbackRequestCreatedHandler {
    private final InAppNotificationService inAppNotificationService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(FrequentFeedbackRequestedEvent event) {
        inAppNotificationService.createNotification(event);
    }
}
