package com.alibou.security.lessons;

import com.alibou.security.token.Token;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssignmentRepo extends JpaRepository<Assignment, Integer> {
    Assignment getAssignmentByAssignmentID(int id);
}
