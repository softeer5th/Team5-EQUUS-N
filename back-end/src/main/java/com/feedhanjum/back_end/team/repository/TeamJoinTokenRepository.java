package com.feedhanjum.back_end.team.repository;

import com.feedhanjum.back_end.team.domain.TeamJoinToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamJoinTokenRepository extends JpaRepository<TeamJoinToken, String> {
}