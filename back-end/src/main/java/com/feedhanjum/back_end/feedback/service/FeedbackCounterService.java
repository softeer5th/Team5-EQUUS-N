package com.feedhanjum.back_end.feedback.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class FeedbackCounterService {
    private final Map<Long, Long> feedbackCreatedCounter = new ConcurrentHashMap<>();

    public void incrementCounter(Long key) {
        feedbackCreatedCounter.merge(key, 1L, Long::sum);
    }

    public Map<Long, Long> consumeAndClearCounters() {
        Map<Long, Long> currentCounters = new HashMap<>();
        for (Long key : feedbackCreatedCounter.keySet()) {
            Long value = feedbackCreatedCounter.remove(key);
            if (value != null) {
                currentCounters.put(key, value);
            }
        }
        return currentCounters;
    }
}
