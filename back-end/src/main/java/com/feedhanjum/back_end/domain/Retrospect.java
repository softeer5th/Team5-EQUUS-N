package com.feedhanjum.back_end.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Retrospect {
    @Id
    @Column(name = "retrospect_id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    private String content;

    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "writer_id")
    private Member writer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;

    public Retrospect(String content, Member writer, Team team) {
        this.content = content;
        this.team = team;
        setWriter(writer);
    }

    public void setWriter(Member writer) {
        if (this.writer != null) {
            this.writer.getRetrospects().remove(this);
        }
        this.writer = writer;
        if(writer != null && !writer.getRetrospects().contains(this)) {
            writer.getRetrospects().add(this);
        }
    }
}
