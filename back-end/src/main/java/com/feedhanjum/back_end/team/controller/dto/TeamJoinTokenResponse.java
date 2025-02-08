package com.feedhanjum.back_end.team.controller.dto;


import com.feedhanjum.back_end.team.domain.TeamJoinToken;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

@Schema(description = "팀 가입 토큰 응답")
public record TeamJoinTokenResponse(
        @Schema(description = "팀 가입 토큰. 팀 가입 시 사용")
        @NotNull
        String token,
        @Schema(description = "팀 가입 토큰 유효기간")
        @NotNull
        @Future
        LocalDateTime validUntil
) {
    public static TeamJoinTokenResponse from(TeamJoinToken joinToken) {
        return new TeamJoinTokenResponse(joinToken.getToken(), joinToken.getExpireDate());
    }
}
