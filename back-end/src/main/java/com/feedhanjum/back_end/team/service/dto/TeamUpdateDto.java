package com.feedhanjum.back_end.team.service.dto;

import com.feedhanjum.back_end.feedback.domain.FeedbackType;
import com.feedhanjum.back_end.team.controller.dto.TeamUpdateRequest;

import java.time.LocalDateTime;

public record TeamUpdateDto(
        String teamName,
        LocalDateTime startTime,
        LocalDateTime endTime,
        FeedbackType feedbackType
) {
    public TeamUpdateDto(TeamUpdateRequest request) {
        this(request.name(), request.startTime(), request.endTime(), request.feedbackType());
    }
}
