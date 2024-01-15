package com.alibou.security.lessons;

public enum LessonStatus {
    NOT_STARTED, STARTED, FINISHED;

    @Override
    public String toString() {
        if (this == NOT_STARTED) return "Upcoming";
        else if (this == STARTED) return "Active";
        else return "Inactive";
    }
}
