package com.feedhanjum.back_end.feedback.repository;

import com.feedhanjum.back_end.feedback.domain.Feedback;
import com.feedhanjum.back_end.feedback.domain.QFeedback;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.ComparableExpressionBase;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.annotation.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;

@Repository
public class FeedbackQueryRepository {
    private final QFeedback feedback = QFeedback.feedback;
    private final ComparableExpressionBase<?> sortProperty = feedback.id;
    private final JPAQueryFactory queryFactory;

    public FeedbackQueryRepository(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }


    public Page<Feedback> findReceivedFeedbacks(Long receiverId, @Nullable Long teamId, boolean filterHelpful, Pageable pageable, Sort.Direction sortOrder) {
        Objects.requireNonNull(receiverId);
        Objects.requireNonNull(pageable);
        Objects.requireNonNull(sortOrder);

        BooleanBuilder predicate = new BooleanBuilder();
        predicate.and(feedback.receiver.id.eq(receiverId));
        if (teamId != null) {
            predicate.and(feedback.team.id.eq(teamId));
        }
        if (filterHelpful) {
            predicate.and(feedback.liked.isTrue());
        }

        List<Feedback> result = queryFactory
                .selectFrom(feedback)
                .where(predicate)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(sortOrder == Sort.Direction.ASC ? sortProperty.asc() : sortProperty.desc())
                .fetch();
        Long total = queryFactory.select(feedback.count())
                .from(feedback)
                .where(predicate)
                .fetchOne();
        if (total == null) {
            total = (long) result.size();
        }
        return new PageImpl<>(result, pageable, total);
    }

    public Page<Feedback> findSentFeedbacks(Long senderId, @Nullable Long teamId, boolean filterHelpful, Pageable pageable, Sort.Direction sortOrder) {
        Objects.requireNonNull(senderId);
        Objects.requireNonNull(pageable);
        Objects.requireNonNull(sortOrder);

        BooleanBuilder predicate = new BooleanBuilder();
        predicate.and(feedback.sender.id.eq(senderId));
        if (teamId != null) {
            predicate.and(feedback.team.id.eq(teamId));
        }
        if (filterHelpful) {
            predicate.and(feedback.liked.isTrue());
        }

        List<Feedback> result = queryFactory
                .selectFrom(feedback)
                .where(predicate)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(sortOrder == Sort.Direction.ASC ? sortProperty.asc() : sortProperty.desc())
                .fetch();
        Long total = queryFactory.select(feedback.count())
                .from(feedback)
                .where(predicate)
                .fetchOne();
        if (total == null) {
            total = (long) result.size();
        }
        return new PageImpl<>(result, pageable, total);
    }


    public List<Feedback> findReceivedFeedbacks(Long receiverId) {
        Objects.requireNonNull(receiverId);

        return queryFactory
                .selectFrom(feedback)
                .where(feedback.receiver.id.eq(receiverId))
                .fetch();
    }

    public Long findReceivedFeedbackCount(Long receiverId) {
        Objects.requireNonNull(receiverId);

        return queryFactory
                .select(feedback.count())
                .from(feedback)
                .where(feedback.receiver.id.eq(receiverId))
                .fetchOne();
    }

    public Long findSentFeedbackCount(Long senderId) {
        Objects.requireNonNull(senderId);

        return queryFactory
                .select(feedback.count())
                .from(feedback)
                .where(feedback.sender.id.eq(senderId))
                .fetchOne();
    }


}
