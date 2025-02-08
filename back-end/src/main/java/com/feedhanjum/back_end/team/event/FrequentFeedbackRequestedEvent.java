package com.feedhanjum.back_end.team.event;

public record FrequentFeedbackRequestedEvent(Long senderId, Long teamId, Long receiverId) {
}
