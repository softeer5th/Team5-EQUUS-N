package com.feedhanjum.back_end.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Feedback {
    @Id
    @Column(name = "feedback_id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Enumerated(EnumType.STRING)
    private FeedbackType feedbackType;

    // 객관식 피드백
    // private ObjectiveFeedback objectiveFeedback

    private String subjectiveFeedback;

    private boolean liked = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id")
    private Member sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id")
    private Member receiver;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;

    public Feedback(FeedbackType feedbackType, String subjectiveFeedback, Member sender, Member receiver, Team team) {
        this.feedbackType = feedbackType;
        this.subjectiveFeedback = subjectiveFeedback;
        this.sender = sender;
        this.team = team;
        setReceiver(receiver);
    }

    public void setReceiver(Member receiver) {
        if(this.receiver != null) {
            this.receiver.getFeedbacks().remove(this);
        }
        this.receiver = receiver;
        if(receiver != null && !receiver.getFeedbacks().contains(this)) {
            receiver.getFeedbacks().add(this);
        }
    }

    public void like(){
        if(!liked) {
            this.liked = true;
            this.sender.addLikedCount();
        }
    }

    public void unlike(){
        if(liked) {
            this.sender.removeLikedCount();
            this.liked = false;
        }
    }
}
