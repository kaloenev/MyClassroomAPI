package com.alibou.security.lessons;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ThemaRepository extends JpaRepository<Thema, Integer> {
    Thema getThemaByThemaID(int Id);
}
