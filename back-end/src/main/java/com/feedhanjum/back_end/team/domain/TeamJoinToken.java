package com.feedhanjum.back_end.team.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;


@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class TeamJoinToken {
    public static final Integer EXPIRATION_HOURS = 24;
    @Id
    private String token;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;

    private LocalDateTime expireDate;

    private TeamJoinToken(String token, Team team) {
        this.token = token;
        this.team = team;
        this.expireDate = LocalDateTime.now().plusHours(EXPIRATION_HOURS);
    }

    private static String createToken() {
        return UUID.randomUUID().toString();
    }

    // 새로운 토큰 발급
    static TeamJoinToken createToken(Team team) {
        return new TeamJoinToken(createToken(), team);
    }

}
