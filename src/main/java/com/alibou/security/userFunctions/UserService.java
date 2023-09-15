package com.alibou.security.userFunctions;

import com.alibou.security.coursesServiceController.ReviewResponse;
import com.alibou.security.coursesServiceController.TeacherResponse;
import com.alibou.security.coursesServiceController.TimePair;
import com.alibou.security.emailing.EmailDetails;
import com.alibou.security.emailing.EmailService;
import com.alibou.security.exceptionHandling.CustomException;
import com.alibou.security.token.TokenRepository;
import com.alibou.security.user.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class UserService {
    private final TokenRepository tokenRepository;

    private final UserRepository userRepository;

    private final TeacherRepository teacherRepository;

    private final EmailService emailService;

    public void sendMessage(String token, String content, int receiverID) throws CustomException {
        var token1= tokenRepository.findByToken(token);
        if (token1.isEmpty()) throw new CustomException(HttpStatus.FORBIDDEN, "Invalid token");
        User user = token1.get().getUser();
        User user1 = userRepository.findUserById(receiverID);
        if (user.getRole().equals(Role.STUDENT)) {
            if (user1.getRole().equals(Role.STUDENT)) throw new CustomException(HttpStatus.FORBIDDEN, "Can not send message to another student");
            Student student = (Student) user;
            Teacher teacher = (Teacher) user1;
            List<MessageContact> messageContactList = student.getMessages();
            int counter = messageContactList.size();
            for (MessageContact messageContact : messageContactList) {
                if (Objects.equals(messageContact.getTeacher().getUsername(), teacher.getUsername())) {
                    Message message = new Message();
                    message.setDateTime(new Timestamp(System.currentTimeMillis()));
                    message.setContent(content);
                    message.setStudentTheSender(true);
                    messageContact.addNewMessage(message);
                    student.saveMessage(messageContact);
                    teacher.saveMessage(messageContact);
                    // Web hooks
                    break;
                }
                else if (counter == 1) {
                    MessageContact messageContact1 = new MessageContact();
                    messageContact1.setStudent(student);
                    messageContact.setTeacher(teacher);
                    List<Message> messages = new ArrayList<>();
                    Message message = new Message();
                    message.setDateTime(new Timestamp(System.currentTimeMillis()));
                    message.setContent(content);
                    message.setStudentTheSender(true);
                    messages.add(message);
                    messageContact.setMessages(messages);
                    student.saveMessage(messageContact);
                    teacher.saveMessage(messageContact);
                    // Web hooks
                }
                counter--;
            }
        }
        else {
            if (user1.getRole().equals(Role.TEACHER)) throw new CustomException(HttpStatus.FORBIDDEN, "Can not send message to another teacher");
            Teacher teacher = (Teacher) user;
            Student student = (Student) user1;
            List<MessageContact> messageContactList = teacher.getMessages();
            int counter = messageContactList.size();
            for (MessageContact messageContact : messageContactList) {
                if (Objects.equals(messageContact.getStudent().getUsername(), student.getUsername())) {
                    Message message = new Message();
                    message.setDateTime(new Timestamp(System.currentTimeMillis()));
                    message.setContent(content);
                    message.setStudentTheSender(false);
                    messageContact.addNewMessage(message);
                    student.saveMessage(messageContact);
                    teacher.saveMessage(messageContact);
                    // Web hooks
                    break;
                }
                else if (counter == 1) {
                    MessageContact messageContact1 = new MessageContact();
                    messageContact1.setStudent(student);
                    messageContact.setTeacher(teacher);
                    List<Message> messages = new ArrayList<>();
                    Message message = new Message();
                    message.setDateTime(new Timestamp(System.currentTimeMillis()));
                    message.setContent(content);
                    message.setStudentTheSender(false);
                    messages.add(message);
                    messageContact.setMessages(messages);
                    student.saveMessage(messageContact);
                    teacher.saveMessage(messageContact);
                    // Web hooks
                }
                counter--;
            }
        }
    }

    public void verifyTeacher(String token, String name, String surname, MultipartFile requestFile, Gender gender, City city,
                              String description, String specialties, Degree degree, String school, String university,
                              String experience) throws IOException, CustomException {
        var jwt = tokenRepository.findByToken(token);
        if (jwt.isEmpty()) throw new CustomException(HttpStatus.FORBIDDEN, "Invalid token");
        Teacher teacher = (Teacher) jwt.get().getUser();
        teacher.setFirstname(name);
        teacher.setLastname(surname);

        String fileName = "image_" + teacher.getId();
        File newFile = new File(fileName);
        requestFile.transferTo(newFile);
        teacher.setPictureLocation(fileName);

        teacher.setGender(gender);
        teacher.setCity(city);
        teacher.setDescription(description);
        teacher.setSpecialties(specialties);
        teacher.setDegree(degree);
        teacher.setSchool(school);
        teacher.setUniversity(university);
        teacher.verifyAccount();

        EmailDetails emailDetails = new EmailDetails();
        emailDetails.setRecipient("kaloyan.enev@gmail.com");
        emailDetails.setSubject("Teacher experience verification for: " + teacher.getId());
        emailDetails.setMsgBody(experience);
        emailService.sendSimpleMail(emailDetails);
    }

    public void verifyTeacherExperience(int id, String experience) {
        Teacher teacher = teacherRepository.findTeacherById(id);
        teacher.setExperience(experience);
    }

    public void likeTeacher(String token, int teacherID) throws CustomException {
        var token1= tokenRepository.findByToken(token);
        if (token1.isEmpty()) throw new CustomException(HttpStatus.FORBIDDEN, "Invalid token");
        Student student = (Student) token1.get().getUser();
        student.saveTeacherToLiked((Teacher) userRepository.findUserById(teacherID));
    }

    public void dislikeTeacher(String token, int teacherID) throws CustomException {
        var token1= tokenRepository.findByToken(token);
        if (token1.isEmpty()) throw new CustomException(HttpStatus.FORBIDDEN, "Invalid token");
        Student student = (Student) token1.get().getUser();
        student.removeTeacherFromLiked((Teacher) userRepository.findUserById(teacherID));
    }

    public VerificationFormResponse getVerificationForm() {
        return new VerificationFormResponse(City.values(), Degree.values());
    }

    public TeacherResponse getTeacherPage(int teacherID) {
        Teacher teacher = (Teacher) userRepository.findUserById(teacherID);
        List<ReviewResponse> reviewResponses = new ArrayList<>();
        for (Review review : teacher.getReviews()) {
            ReviewResponse reviewResponse = new ReviewResponse(review);
            reviewResponses.add(reviewResponse);
        }
        return new TeacherResponse(teacher, reviewResponses);
    }


    public List<TimePair> getCalendar(String token) {
        //TODO implement
        return null;
    }
}
