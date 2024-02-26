package com.alibou.security.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MessageContactRepo extends JpaRepository<MessageContact, Integer> {
    MessageContact getMessageContactByMessageID(int id);

    MessageContact getMessageContactByStudent_Id(int id);

    MessageContact getMessageContactByTeacher_Id(int id);

    @Query("select m from MessageContact m inner join m.messages messages where m.teacher.id = :id order by messages.dateTime")
    List<MessageContact> getMessageContactsByTeacher_Id(@Param("id") int id);
    @Query("select m from MessageContact m inner join m.messages messages where m.student.id = :id order by messages.dateTime")
    List<MessageContact> getMessageContactsByStudent_Id(@Param("id") int id);
}
