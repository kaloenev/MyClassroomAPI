package com.alibou.security.user;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepo extends JpaRepository<Review, Integer> {
    List<Review> getDistinct3ByOrderByRatingDescMessageDesc();

    List<Review> getByLesson_lessonID(int id, Pageable pageable);
}
