package com.feedhanjum.back_end.member.repository;

import com.feedhanjum.back_end.member.domain.Member;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.feedhanjum.back_end.member.domain.QMember.member;
import static com.feedhanjum.back_end.team.domain.QTeamMember.teamMember;

@Repository
@RequiredArgsConstructor
public class MemberQueryRepository {
    private final JPAQueryFactory jpaQueryFactory;

    public List<Member> findMembersByTeamId(Long teamId) {
        return jpaQueryFactory.select(member)
                .from(teamMember)
                .join(teamMember.member, member)
                .fetchJoin()
                .where(teamMember.team.id.eq(teamId))
                .fetch();
    }
}
