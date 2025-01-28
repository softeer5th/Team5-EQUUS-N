package com.feedhanjum.back_end.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Schedule {
    @Id
    @Column(name = "schedule_id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    private String name;

    private LocalDateTime startTime;
    
    private LocalDateTime endTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;

    @OneToMany(mappedBy = "schedule")
    private List<ScheduleMember> scheduleMembers = new ArrayList<>();

    public Schedule(String name, LocalDateTime startTime, LocalDateTime endTime, Team team) {
        this.name = name;
        this.startTime = startTime;
        this.endTime = endTime;
        setTeam(team);
    }

    public void setTeam(Team team) {
        if (this.team != null) {
            this.team.getSchedules().remove(this);
        }
        this.team = team;
        if(team != null && !team.getSchedules().contains(this)) {
            team.getSchedules().add(this);
        }
    }
}
