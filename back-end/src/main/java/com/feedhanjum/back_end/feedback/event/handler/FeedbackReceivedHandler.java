package com.feedhanjum.back_end.feedback.event.handler;

import com.feedhanjum.back_end.feedback.event.FeedbackReceivedEvent;
import com.feedhanjum.back_end.feedback.service.FeedbackCounterService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FeedbackReceivedHandler {

    private final FeedbackCounterService feedbackCounterService;

    @Async
    @EventListener
    public void on(FeedbackReceivedEvent event) {
        feedbackCounterService.incrementCounter(event.receiverId());
    }
}
