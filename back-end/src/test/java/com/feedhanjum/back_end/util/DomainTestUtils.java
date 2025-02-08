package com.feedhanjum.back_end.util;

import com.feedhanjum.back_end.feedback.domain.FeedbackType;
import com.feedhanjum.back_end.member.domain.Member;
import com.feedhanjum.back_end.member.domain.ProfileImage;
import com.feedhanjum.back_end.team.domain.Team;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicLong;

public class DomainTestUtils {
    private static final AtomicLong nextId = new AtomicLong(1);

    public static Member createMemberWithoutId(String name) {
        return new Member(name, name + "email@com", new ProfileImage("red", "default.png"));
    }

    public static Member createMemberWithId(String name) {
        Long id = nextId.getAndIncrement();
        Member member = createMemberWithoutId(name);
        ReflectionTestUtils.setField(member, "id", id);
        return member;
    }

    public static Team createTeamWithoutId(String name, Member leader) {
        return createTeamWithoutId(name, leader, LocalDate.now().minusDays(1), LocalDate.now().plusDays(1));
    }

    public static Team createTeamWithoutId(String name, Member leader, LocalDate startDate, LocalDate endDate) {
        return new Team(name, leader, startDate, endDate, FeedbackType.ANONYMOUS);
    }

    public static Team createTeamWithId(String name, Member leader, LocalDate startDate, LocalDate endDate) {
        Long id = nextId.getAndIncrement();
        Team team = createTeamWithoutId(name, leader, startDate, endDate);
        ReflectionTestUtils.setField(team, "id", id);
        return team;
    }

    public static Team createTeamWithId(String name, Member leader) {
        return createTeamWithId(name, leader, LocalDate.now().minusDays(1), LocalDate.now().plusDays(1));
    }
}
