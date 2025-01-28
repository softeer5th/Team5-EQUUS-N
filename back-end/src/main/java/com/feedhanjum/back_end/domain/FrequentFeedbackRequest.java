package com.feedhanjum.back_end.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class FrequentFeedbackRequest {
    @Id
    @Column(name = "frequent_feedback_request_id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    private LocalDateTime requestTime;

    private String requestedContent;

    @ManyToOne
    @JoinColumn(name = "requester_id")
    private Member requester;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_member_id")
    private TeamMember teamMember;

    public FrequentFeedbackRequest(LocalDateTime requestTime, String requestedContent, TeamMember teamMember, Member requester) {
        this.requestTime = requestTime;
        this.requestedContent = requestedContent;
        this.requester = requester;
        setTeamMember(teamMember);
    }

    public void setTeamMember(TeamMember teamMember) {
        if (this.teamMember != null) {
            this.teamMember.getFrequentFeedbackRequests().remove(this);
        }
        this.teamMember = teamMember;
        if (teamMember != null && !teamMember.getFrequentFeedbackRequests().contains(this)) {
            teamMember.getFrequentFeedbackRequests().add(this);
        }
    }

}
