package com.alibou.security.user;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Integer> {

  Optional<User> findByEmail(String email);
  Optional<User> findByUsername(String username);
  Optional<User> findByResetToken(String token);

  User findUserByTokens_token(String token);

  User findUserById(Integer id);

}
