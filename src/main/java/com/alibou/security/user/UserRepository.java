package com.alibou.security.user;

import java.util.List;
import java.util.Optional;

import com.alibou.security.token.Token;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

    Optional<User> findByResetToken(String token);

    User findUserByTokens_token(String token);

    User findUserById(Integer id);
    @Query("""
            select tokens from User l inner join l.tokens tokens
            where l.id = :userId
            order by tokens.timestamp desc""")
    List<Token> findLastToken(@Param("userId") int userId);

}
