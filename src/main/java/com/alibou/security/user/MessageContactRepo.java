package com.alibou.security.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MessageContactRepo extends JpaRepository<MessageContact, Integer> {
    MessageContact getMessageContactByMessageID(int id);

    @Query("""
            select m from MessageContact m inner join m.teacher.tokens tokens
            where m.student.id = :id and tokens.token = :token""")
    MessageContact getMessageContactByStudent_IdAndTeacher_Tokens_Token(@Param("id") int id, @Param("token") String token);

    @Query("""
            select m from MessageContact m inner join m.student.tokens tokens
            where m.teacher.id = :id and tokens.token = :token""")
    MessageContact getMessageContactByTeacher_IdAndStudent_Tokens_Token(@Param("id") int id, @Param("token") String token);

    @Query("select m from MessageContact m inner join m.messages messages where m.teacher.id = :id order by messages.dateTime")
    List<MessageContact> getMessageContactsByTeacher_Id(@Param("id") int id);
    @Query("select m from MessageContact m inner join m.messages messages where m.student.id = :id order by messages.dateTime")
    List<MessageContact> getMessageContactsByStudent_Id(@Param("id") int id);

    //TODO Add case for both names in searchTerm
    @Query("select m from MessageContact m inner join m.messages messages where m.teacher.id = :id and " +
            "(m.student.firstname = :searchTerm or m.student.lastname = :searchTerm) order by messages.dateTime")
    List<MessageContact> getMessageContactsByTeacher_IdAndSearch(@Param("id") int id, @Param("searchTerm") String searchTerm);
    @Query("select m from MessageContact m inner join m.messages messages where m.student.id = :id and " +
            "(m.teacher.firstname = :searchTerm or m.teacher.lastname = :searchTerm) order by messages.dateTime")
    List<MessageContact> getMessageContactsByStudent_IdAndSearch(@Param("id") int id, @Param("searchTerm") String searchTerm);
}
