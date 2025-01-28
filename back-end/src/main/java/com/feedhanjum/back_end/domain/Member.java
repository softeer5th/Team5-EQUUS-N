package com.feedhanjum.back_end.domain;

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
    private Long othersLikedCount;

    // 프로필 사진 정보 - 타입 미지정
    // private String profile;

    @OneToMany(mappedBy = "member")
    private final List<TeamMember> teamMembers = new ArrayList<>();

    @OneToMany(mappedBy = "member")
    private final List<ScheduleMember> scheduleMembers = new ArrayList<>();

    @OneToMany(mappedBy = "receiver")
    private final List<Feedback> feedbacks = new ArrayList<>();

    @OneToMany(mappedBy = "writer")
    private final List<Retrospect> retrospects = new ArrayList<>();

    public Member(String name, String email) {
        this.name = name;
        this.email = email;
    }

    public void addLikedCount(){
        othersLikedCount += 1;
    }
    public void removeLikedCount(){
        othersLikedCount -= 1;
    }
}
