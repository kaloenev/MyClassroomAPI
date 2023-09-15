package com.alibou.security.user;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepo extends JpaRepository<Review, Integer> {
    List<Review> findTop3ByOrderByRatingDescMessageDesc();

    List<Review> findTopByLesson_lessonID(int id, Pageable pageable);
}
