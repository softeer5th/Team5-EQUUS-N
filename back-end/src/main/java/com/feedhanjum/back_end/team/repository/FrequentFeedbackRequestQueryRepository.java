package com.feedhanjum.back_end.team.repository;

import com.feedhanjum.back_end.team.domain.FrequentFeedbackRequest;
import com.feedhanjum.back_end.team.domain.QFrequentFeedbackRequest;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@RequiredArgsConstructor
@Repository
public class FrequentFeedbackRequestQueryRepository {
    private final QFrequentFeedbackRequest frequentFeedbackRequest = QFrequentFeedbackRequest.frequentFeedbackRequest;
    private final JPAQueryFactory queryFactory;


    public List<FrequentFeedbackRequest> getFrequentFeedbackRequests(Long receiverId, Long teamId) {
        return queryFactory.selectFrom(frequentFeedbackRequest)
                .join(frequentFeedbackRequest.sender).fetchJoin()
                .join(frequentFeedbackRequest.receiver).fetchJoin()
                .join(frequentFeedbackRequest.team).fetchJoin()
                .where(frequentFeedbackRequest.receiver.id.eq(receiverId)
                        .and(frequentFeedbackRequest.team.id.eq(teamId)))
                .fetch();
    }
}
