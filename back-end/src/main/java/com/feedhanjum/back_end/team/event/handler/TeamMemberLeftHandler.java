package com.feedhanjum.back_end.team.event.handler;

import com.feedhanjum.back_end.team.event.TeamMemberLeftEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@RequiredArgsConstructor
@Component
public class TeamMemberLeftHandler {

    // 동기식으로 작동
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void removeRemainScheduleMember(TeamMemberLeftEvent event) {
        // TODO: 남은 일정에 기록된 할 일 제거 로직 필요
    }
}
