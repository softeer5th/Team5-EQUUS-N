package com.feedhanjum.back_end.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Todo {

    @Id
    @Column(name = "todo_id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_member_id")
    private ScheduleMember scheduleMember;

    private String content;

    public Todo(ScheduleMember scheduleMember, String content) {
        this.content = content;
        setScheduleMember(scheduleMember);
    }

    public void setScheduleMember(ScheduleMember scheduleMember) {
        if (this.scheduleMember != null) {
            this.scheduleMember.getTodos().remove(this);
        }
        this.scheduleMember = scheduleMember;
        if (scheduleMember != null && !scheduleMember.getTodos().contains(this)) {
            scheduleMember.getTodos().add(this);
        }
    }
}
