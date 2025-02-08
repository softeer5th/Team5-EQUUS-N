package com.feedhanjum.back_end.team.domain;

import com.feedhanjum.back_end.member.domain.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class FrequentFeedbackRequest {
    public static final int MIN_REQUESTED_CONTENT_BYTE = 0;
    public static final int MAX_REQUESTED_CONTENT_BYTE = 400;
    @Id
    @Column(name = "frequent_feedback_request_id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    private LocalDateTime createdAt;

    private String requestedContent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id")
    private Member sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id")
    private Member receiver;

    FrequentFeedbackRequest(String requestedContent, Member sender, Team team, Member receiver) {
        this.requestedContent = requestedContent;
        this.sender = sender;
        this.team = team;
        this.receiver = receiver;
        this.createdAt = LocalDateTime.now();
    }

}
