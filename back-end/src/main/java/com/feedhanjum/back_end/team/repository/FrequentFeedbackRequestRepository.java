package com.feedhanjum.back_end.team.repository;

import com.feedhanjum.back_end.team.domain.FrequentFeedbackRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FrequentFeedbackRequestRepository extends JpaRepository<FrequentFeedbackRequest, Long> {
}