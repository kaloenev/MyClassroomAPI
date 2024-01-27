package com.alibou.security.lessons;

import com.alibou.security.token.Token;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AssignmentRepo extends JpaRepository<Assignment, Integer> {
    Assignment getAssignmentByAssignmentID(int id);

    @Query("select a.solutions from Assignment a where a.assignmentID = :id")
    List<Solution> getAssignment_SolututionsByAssignmentID(int id);
}
