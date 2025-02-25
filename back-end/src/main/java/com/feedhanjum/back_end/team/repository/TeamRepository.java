package com.feedhanjum.back_end.team.repository;

import com.feedhanjum.back_end.team.domain.Team;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TeamRepository extends JpaRepository<Team, Long> {
    @Lock(LockModeType.OPTIMISTIC_FORCE_INCREMENT)
    @Query("select t from Team t where t.id = :id")
    Optional<Team> findByIdOptimisticLockForceIncrement(@Param("id") Long id);
}
