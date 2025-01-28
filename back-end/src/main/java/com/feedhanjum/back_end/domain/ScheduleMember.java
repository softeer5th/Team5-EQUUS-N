package com.feedhanjum.back_end.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class ScheduleMember {
    @Id
    @Column(name = "schedule_member_id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Enumerated(EnumType.STRING)
    private ScheduleRole role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id")
    private Schedule schedule;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @OneToMany(mappedBy = "scheduleMember")
    private List<RegularFeedbackRequest> regularFeedbackRequests;

    @OneToMany(mappedBy = "scheduleMember")
    private List<Todo> todos;

    public ScheduleMember(ScheduleRole role, Schedule schedule, Member member) {
        this.role = role;
        setSchedule(schedule);
        setMember(member);
    }

    public void setSchedule(Schedule schedule) {
        if (this.schedule != null) {
            this.schedule.getScheduleMembers().remove(this);
        }
        this.schedule = schedule;
        if (schedule != null && !schedule.getScheduleMembers().contains(this)) {
            schedule.getScheduleMembers().add(this);
        }
    }

    public void setMember(Member member) {
        if (this.member != null) {
            this.member.getScheduleMembers().remove(this);
        }
        this.member = member;
        if (member != null && !member.getScheduleMembers().contains(this)) {
            member.getScheduleMembers().add(this);
        }
    }
}
