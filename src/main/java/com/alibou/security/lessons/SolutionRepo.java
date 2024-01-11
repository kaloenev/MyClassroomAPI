package com.alibou.security.lessons;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SolutionRepo extends JpaRepository<Solution, Integer> {
    Solution getSolutionBySolutionID(int id);
}
