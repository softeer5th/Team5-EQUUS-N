package com.feedhanjum.back_end.feedback.service;

import com.feedhanjum.back_end.feedback.domain.Retrospect;
import com.feedhanjum.back_end.feedback.repository.RetrospectRepository;
import com.feedhanjum.back_end.member.domain.Member;
import com.feedhanjum.back_end.member.repository.MemberRepository;
import com.feedhanjum.back_end.team.domain.Team;
import com.feedhanjum.back_end.team.repository.TeamRepository;
import jakarta.annotation.Nullable;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class RetrospectService {
    private static final int PAGE_SIZE = 10;
    private static final String SORT_PROPERTY = "id";
    private final RetrospectRepository retrospectRepository;
    private final MemberRepository memberRepository;
    private final TeamRepository teamRepository;

    public RetrospectService(RetrospectRepository retrospectRepository, MemberRepository memberRepository, TeamRepository teamRepository) {
        this.retrospectRepository = retrospectRepository;
        this.memberRepository = memberRepository;
        this.teamRepository = teamRepository;
    }

    /**
     * @throws EntityNotFoundException writerId에 해당하는 Member가 없거나 teamId에 해당하는 Team이 없을 때
     */
    @Transactional
    public Retrospect writeRetrospect(String content, Long writerId, Long teamId) {
        Optional<Member> writer = memberRepository.findById(writerId);
        Optional<Team> team = teamRepository.findById(teamId);

        writer.orElseThrow(() -> new EntityNotFoundException("writerId에 해당하는 Member가 없습니다."));
        team.orElseThrow(() -> new EntityNotFoundException("teamId에 해당하는 Team이 없습니다."));

        Retrospect retrospect = new Retrospect(content, writer.get(), team.get());
        retrospectRepository.save(retrospect);
        return retrospect;
    }

    /**
     * @throws EntityNotFoundException  writerId에 해당하는 Member가 없거나 teamName에 해당하는 Team이 없을 때
     * @throws IllegalArgumentException page가 0 미만일 때
     */
    @Transactional(readOnly = true)
    public Page<Retrospect> getRetrospects(Long writerId, @Nullable String teamName, int page, Sort.Direction sortOrder) {
        if (page < 0) {
            throw new IllegalArgumentException("page는 0 이상의 값을 가져야 합니다.");
        }

        Member writer = memberRepository.findById(writerId)
                .orElseThrow(() -> new EntityNotFoundException("writerId에 해당하는 Member가 없습니다."));

        PageRequest pageRequest = PageRequest.of(page, PAGE_SIZE, Sort.by(sortOrder, SORT_PROPERTY));
        if (teamName == null) {
            return retrospectRepository.findByWriter(writer, pageRequest);
        }
        Team team = teamRepository.findByName(teamName)
                .orElseThrow(() -> new EntityNotFoundException("teamName에 해당하는 Team이 없습니다."));
        return retrospectRepository.findByWriterAndTeam(writer, team, pageRequest);
    }
}
