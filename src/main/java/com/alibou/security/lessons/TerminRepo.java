package com.alibou.security.lessons;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TerminRepo extends JpaRepository<Termin, Integer> {
    Termin getTerminByTerminID(Integer id);
}
