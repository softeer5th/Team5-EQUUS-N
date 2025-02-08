package com.feedhanjum.back_end.team.repository;

import com.feedhanjum.back_end.team.domain.TeamMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {
    Optional<TeamMember> findByMemberIdAndTeamId(Long memberId, Long teamId);
}
