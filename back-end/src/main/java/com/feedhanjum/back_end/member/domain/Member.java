package com.feedhanjum.back_end.member.domain;

import com.feedhanjum.back_end.feedback.domain.Feedback;
import com.feedhanjum.back_end.feedback.domain.Retrospect;
import com.feedhanjum.back_end.schedule.domain.ScheduleMember;
import com.feedhanjum.back_end.team.domain.TeamMember;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Member {
    @Id
    @Column(name = "member_id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;
    private String name;
    private String email;

    private String profileBackgroundColor;

    private String profileImage;

    @OneToMany(mappedBy = "member")
    private final List<TeamMember> teamMembers = new ArrayList<>();

    @OneToMany(mappedBy = "member")
    private final List<ScheduleMember> scheduleMembers = new ArrayList<>();

    @OneToMany(mappedBy = "receiver")
    private final List<Feedback> feedbacks = new ArrayList<>();

    @OneToMany(mappedBy = "writer")
    private final List<Retrospect> retrospects = new ArrayList<>();

    public Member(String name, String email, String profileBackgroundColor, String profileImage) {
        this.name = name;
        this.email = email;
        this.profileBackgroundColor = profileBackgroundColor;
        this.profileImage = profileImage;
    }

    public void changeProfile(String profileBackgroundColor, String image) {
        this.profileBackgroundColor = profileBackgroundColor;
        this.profileImage = image;
    }
}
