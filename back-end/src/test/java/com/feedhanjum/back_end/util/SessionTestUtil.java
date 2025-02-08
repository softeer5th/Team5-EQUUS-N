package com.feedhanjum.back_end.util;

import com.feedhanjum.back_end.auth.infra.SessionConst;
import com.feedhanjum.back_end.member.domain.Member;
import org.springframework.mock.web.MockHttpSession;

public class SessionTestUtil {

    public static MockHttpSession withLoginUser(Member member) {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SessionConst.MEMBER_ID, member.getId());
        return session;
    }
}
