package com.feedhanjum.back_end.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class RegularFeedbackRequest {
    @Id
    @Column(name = "regular_feedback_request_id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    private LocalDateTime requestTime;

    @ManyToOne
    @JoinColumn(name = "requester_id")
    private Member requester;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_member_id")
    private ScheduleMember scheduleMember;

    public RegularFeedbackRequest(LocalDateTime requestTime, Member requester, ScheduleMember scheduleMember) {
        this.requestTime = requestTime;
        this.requester = requester;
        setScheduleMember(scheduleMember);
    }

    public void setScheduleMember(ScheduleMember scheduleMember) {
        if (this.scheduleMember != null) {
            this.scheduleMember.getRegularFeedbackRequests().remove(this);
        }
        this.scheduleMember = scheduleMember;
        if (scheduleMember != null && !scheduleMember.getRegularFeedbackRequests().contains(this)) {
            scheduleMember.getRegularFeedbackRequests().add(this);
        }
    }
}
