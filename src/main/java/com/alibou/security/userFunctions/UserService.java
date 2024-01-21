package com.alibou.security.userFunctions;

import com.alibou.security.coursesServiceController.*;
import com.alibou.security.emailing.EmailDetails;
import com.alibou.security.emailing.EmailService;
import com.alibou.security.exceptionHandling.CustomException;
import com.alibou.security.token.TokenRepository;
import com.alibou.security.user.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class UserService {
    private final TokenRepository tokenRepository;

    private final UserRepository userRepository;

    private final TeacherRepository teacherRepository;

    private final StudentRepository studentRepository;

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
                    // TODO Web hooks
                }
                counter--;
            }
        }
    }

    public int verifyTeacher(String token, String name, String surname, Gender gender, City city,
                              String description, String subjects, Degree degree, String school, String university,
                              String specialty, ExperienceRequest[] experience) throws IOException, CustomException {
        Teacher teacher = teacherRepository.findTeacherByTokens_token(token.substring(7));
        teacher.setFirstname(name);
        teacher.setLastname(surname);
        teacher.setGender(gender);
        teacher.setCity(city);
        teacher.setDescription(description);
        teacher.setSpecialties(subjects);
        teacher.setDegree(degree);
        teacher.setSchool(school);
        teacher.setUniversity(university);
        teacher.verifyAccount();
        teacherRepository.save(teacher);

        EmailDetails emailDetails = new EmailDetails();
        emailDetails.setRecipient("kaloyan.enev@gmail.com");
        emailDetails.setSubject("Teacher experience verification for: " + teacher.getId());
        emailDetails.setMsgBody("Uni: " + university + " \n Specialty:" + specialty + " \n Experience" + Arrays.toString(experience) +
                "\n Teacher email:" + teacher.getEmail());
        emailService.sendSimpleMail(emailDetails);
        return teacher.getId();
    }

    //TODO Add exceptions, също форбидън трябва да препраща към логин
    public void saveTeacherImage(String token, String fileName) throws CustomException {
        Teacher teacher = teacherRepository.findTeacherByTokens_token(token.substring(7));
        if (teacher == null) throw new CustomException(HttpStatus.FORBIDDEN, "Моля логнете се отново");
        teacher.setPictureLocation(fileName);
        teacherRepository.save(teacher);
    }

    public void saveStudentImage(String token, String fileName) throws CustomException {
        Student student = studentRepository.findStudentByTokens_token(token.substring(7));
        if (student == null) throw new CustomException(HttpStatus.FORBIDDEN, "Моля логнете се отново");
        student.setPictureLocation(fileName);
        studentRepository.save(student);
    }

    public String getTeacherImage(int teacherId) throws CustomException {
        Teacher teacher = teacherRepository.findTeacherById(teacherId);
        if (teacher == null) throw new CustomException(HttpStatus.NOT_FOUND, "Няма намерен учител с това id");
        return teacher.getPictureLocation();
    }

    public String getStudentImage(int studentId) throws CustomException {
        Student student = studentRepository.findStudentById(studentId);
        if (student == null) throw new CustomException(HttpStatus.NOT_FOUND, "Няма намерен студент с това id");
        return student.getPictureLocation();
    }

    public void verifyTeacherExperience(int id, String experience) {
        Teacher teacher = teacherRepository.findTeacherById(id);
        teacher.setExperience(experience);
    }

    public void likeTeacher(String token, int teacherID) {
        Student student = studentRepository.findStudentByTokens_token(token.substring(7));
        Teacher teacher = teacherRepository.findTeacherById(teacherID);
        student.saveTeacherToLiked(teacher);
        studentRepository.save(student);
        teacher.addIsLikedByStudent(student);
        teacherRepository.save(teacher);
    }

    public void dislikeTeacher(String token, int teacherID) throws CustomException {
        Student student = studentRepository.findStudentByTokens_token(token.substring(7));
        Teacher teacher = teacherRepository.findTeacherById(teacherID);
        student.removeTeacherFromLiked(teacher);
        studentRepository.save(student);
        teacher.removeStudentFromIsLiked(student);
        teacherRepository.save(teacher);
    }

    public VerificationFormResponse getVerificationForm() {
        return new VerificationFormResponse(City.values(), Degree.values());
    }

    public TeacherResponse getTeacherPage(int teacherID) throws CustomException {
        Teacher teacher = (Teacher) userRepository.findUserById(teacherID);
        List<ReviewResponse> reviewResponses = new ArrayList<>();
        for (Review review : teacher.getReviews()) {
            ReviewResponse reviewResponse = new ReviewResponse(review);
            reviewResponses.add(reviewResponse);
        }
        return new TeacherResponse(teacher, reviewResponses);
    }

    public UserResponse getUser(HttpServletRequest httpServletRequest) {
        String token = httpServletRequest.getHeader("Authorization");
        Teacher teacher = teacherRepository.findTeacherByTokens_token(token.substring(7));
        if (teacher != null) {
            if (teacher.isVerified()) {
                return new UserResponse(teacher.getId(), teacher.getFirstname(), teacher.getLastname(),
                        teacher.getRole().toString(), teacher.isVerified(), true);
            }
            else if (teacher.getTimeOfVerificationRequest() != null) {
                return new UserResponse(teacher.getId(), teacher.getFirstname(), teacher.getLastname(),
                        teacher.getRole().toString(), false, true);
            }
            else {
                return new UserResponse(teacher.getId(), teacher.getFirstname(), teacher.getLastname(),
                        teacher.getRole().toString(), false, false);
            }
        }
        else {
            User user = userRepository.findUserByTokens_token(token.substring(7));
            return new UserResponse(user.getId(), user.getFirstname(), user.getLastname(), user.getRole().toString(), true, true);
        }
    }

    public void editStudentProfile() {

    }

    public UserProfileResponse getUserProfile(HttpServletRequest httpServletRequest) {
        //TODO implement
        String token = httpServletRequest.getHeader("Authorization");
        User user = userRepository.findUserByTokens_token(token.substring(7));
        return null;
    }


    public List<TimePair> getCalendar(String token) {
        //TODO implement
        return null;
    }

    public PagedResponse getFavouriteTeachers(String token, int page) {
        Student student = studentRepository.findStudentByTokens_token(token.substring(7));
        List<TeacherResponse> teacherResponses = new ArrayList<>();
        List<Teacher> teachers = student.getFavouriteTeachers();
        int elementCounter = 0;
        for (Teacher teacher : teachers) {
            if (elementCounter >= page * 12) {
                break;
            }
            if (elementCounter >= page * 12 - 12) {
                TeacherResponse teacherResponse = TeacherResponse.builder().id(teacher.getId()).firstName(teacher.getFirstname())
                        .secondName(teacher.getLastname()).numberOfReviews(teacher.getNumberOfReviews()).rating(teacher.getRating()).build();
                teacherResponses.add(teacherResponse);
            }
            elementCounter++;
        }
        return new PagedResponse(teachers.size(), 12, teacherResponses);
    }
}
