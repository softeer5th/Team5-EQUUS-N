package com.feedhanjum.back_end.feedback.infra;

import com.feedhanjum.back_end.feedback.service.FeedbackCounterService;
import com.feedhanjum.back_end.feedback.service.FeedbackService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class FeedbackReportCreateScheduler {
    private final FeedbackCounterService feedbackCounterService;
    private final FeedbackService feedbackService;

    @Scheduled(cron = "0 */5 * * * *")
    public void createFeedbackReport() {
        Map<Long, Long> feedbackCreatedCounter = feedbackCounterService.consumeAndClearCounters();
        feedbackService.createFeedbackReports(feedbackCreatedCounter);
    }
}
