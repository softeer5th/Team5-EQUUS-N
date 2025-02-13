package com.feedhanjum.back_end.feedback.domain;

public enum FeedbackType {
    ANONYMOUS, IDENTIFIED;

    public boolean isAnonymous() {
        return this == ANONYMOUS;
    }
}
