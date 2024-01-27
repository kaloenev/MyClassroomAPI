package com.alibou.security.userFunctions;

import com.alibou.security.chatroom.ChatNotification;
import com.alibou.security.coursesServiceController.*;
import com.alibou.security.emailing.EmailDetails;
import com.alibou.security.emailing.EmailService;
import com.alibou.security.exceptionHandling.CustomException;
import com.alibou.security.lessons.CourseTermin;
import com.alibou.security.lessons.Lesson;
import com.alibou.security.lessons.LessonTermin;
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

    private final MessageRepo messageRepo;

    private final MessageContactRepo messageContactRepo;

    private List<ChatUser> chatUsers;

    public void saveUser(ChatUser user) {
        chatUsers.add(user);
    }

    public void disconnect(ChatUser user) {
        chatUsers.removeIf(chatUser -> Objects.equals(chatUser.getId(), user.getId()));
    }

    public List<ChatUser> findConnectedUsers() {
        return chatUsers;
    }

    public List<MessageContactsResponse> getContacts(String token) throws CustomException {
        Student student = studentRepository.findStudentByTokens_token(token.substring(7));
        Teacher teacher;
        List<MessageContact> contacts;
        if (student == null) {
            teacher = teacherRepository.findTeacherByTokens_token(token.substring(7));
            if (teacher == null) throw new CustomException(HttpStatus.FORBIDDEN, "Моля логнете се отново");
            contacts = teacher.getMessages();
        } else {
            contacts = student.getMessages();
        }
        List<MessageContactsResponse> responses = new ArrayList<>();
        for (MessageContact contact : contacts) {
            List<Message> messages = contact.getMessages();
            List<ChatNotification> chatNotifications = new ArrayList<>();
            for (Message message : messages) {
                ChatNotification chatNotification;
                if (message.isStudentTheSender()) {
                    chatNotification = ChatNotification.builder().content(message.getContent())
                            .date(message.getDate()).time(message.getTime()).recipientId(contact.getTeacher().getId().toString())
                            .senderId(contact.getStudent().getId().toString()).build();
                } else {
                    chatNotification = ChatNotification.builder().content(message.getContent())
                            .date(message.getDate()).time(message.getTime()).recipientId(contact.getStudent().getId().toString())
                            .senderId(contact.getTeacher().getId().toString()).build();
                }
                chatNotifications.add(chatNotification);
            }
            MessageContactsResponse messageContactsResponse;
            if (student == null) {
                //TODO Add dates as well if the message is not from today
                messageContactsResponse = MessageContactsResponse.builder().contactId(contact.getMessageID())
                        .messages(chatNotifications).name(contact.getStudent().getFirstname() + " " + contact.getStudent().getLastname())
                        .dateTime(chatNotifications.get(0).getTime()).build();
            } else {
                messageContactsResponse = MessageContactsResponse.builder().contactId(contact.getMessageID())
                        .messages(chatNotifications).name(contact.getTeacher().getFirstname() + " " + contact.getTeacher().getLastname())
                        .dateTime(chatNotifications.get(0).getTime()).build();
            }
            responses.add(messageContactsResponse);
        }
        return responses;
    }

    public void sendMessage(int senderId, String content, int receiverID) throws CustomException {
        //TODO Check security
        var user = userRepository.findUserById(senderId);
        var user1 = userRepository.findUserById(receiverID);
        if (user.getRole().equals(Role.STUDENT)) {
            if (user1.getRole().equals(Role.STUDENT))
                throw new CustomException(HttpStatus.FORBIDDEN, "Can not send message to another student");
            Student student = studentRepository.findStudentById(senderId);
            Teacher teacher = teacherRepository.findTeacherById(receiverID);
            List<MessageContact> messageContactList = student.getMessages();
            int counter = messageContactList.size();
            for (MessageContact messageContact : messageContactList) {
                if (Objects.equals(messageContact.getTeacher().getUsername(), teacher.getUsername())) {
                    Message message = new Message();
                    message.setDateTime(new Timestamp(System.currentTimeMillis()));
                    message.setContent(content);
                    message.setStudentTheSender(true);
                    messageRepo.save(message);
                    messageContact.addNewMessage(message);
                    messageContactRepo.save(messageContact);
                    student.saveMessage(messageContact);
                    teacher.saveMessage(messageContact);
                    break;

                } else if (counter == 1) {
                    MessageContact messageContact1 = new MessageContact();
                    messageContact1.setStudent(student);
                    messageContact.setTeacher(teacher);
                    List<Message> messages = new ArrayList<>();
                    Message message = new Message();
                    message.setDateTime(new Timestamp(System.currentTimeMillis()));
                    message.setContent(content);
                    message.setStudentTheSender(true);
                    messages.add(message);
                    messageRepo.save(message);
                    messageContact.setMessages(messages);
                    messageContactRepo.save(messageContact);
                    student.saveMessage(messageContact);
                    teacher.saveMessage(messageContact);
                }
                counter--;
            }
        } else {
            if (user1.getRole().equals(Role.TEACHER))
                throw new CustomException(HttpStatus.FORBIDDEN, "Can not send message to another teacher");
            Student student = studentRepository.findStudentById(receiverID);
            Teacher teacher = teacherRepository.findTeacherById(senderId);
            List<MessageContact> messageContactList = teacher.getMessages();
            int counter = messageContactList.size();
            for (MessageContact messageContact : messageContactList) {
                if (Objects.equals(messageContact.getStudent().getUsername(), student.getUsername())) {
                    Message message = new Message();
                    message.setDateTime(new Timestamp(System.currentTimeMillis()));
                    message.setContent(content);
                    message.setStudentTheSender(false);
                    messageRepo.save(message);
                    messageContact.addNewMessage(message);
                    messageContactRepo.save(messageContact);
                    student.saveMessage(messageContact);
                    teacher.saveMessage(messageContact);
                    break;

                } else if (counter == 1) {
                    MessageContact messageContact1 = new MessageContact();
                    messageContact1.setStudent(student);
                    messageContact.setTeacher(teacher);
                    List<Message> messages = new ArrayList<>();
                    Message message = new Message();
                    message.setDateTime(new Timestamp(System.currentTimeMillis()));
                    message.setContent(content);
                    message.setStudentTheSender(false);
                    messages.add(message);
                    messageRepo.save(message);
                    messageContact.setMessages(messages);
                    messageContactRepo.save(messageContact);
                    student.saveMessage(messageContact);
                    teacher.saveMessage(messageContact);
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

    public TeacherResponse getTeacherPage(int teacherID, String token) throws CustomException {
        Teacher teacher = (Teacher) userRepository.findUserById(teacherID);
        List<ReviewResponse> reviewResponses = new ArrayList<>();
        for (Review review : teacher.getReviews()) {
            ReviewResponse reviewResponse = new ReviewResponse(review);
            reviewResponses.add(reviewResponse);
        }
        TeacherResponse teacherResponse = new TeacherResponse(teacher, reviewResponses);
        List<Teacher> likedTeachers = new ArrayList<>();
        if (token != null) {
            Student student = studentRepository.findStudentByTokens_token(token.substring(7));
            if (student != null) {
                likedTeachers = student.getFavouriteTeachers();
            }
        }
        for (Teacher teacher1 : likedTeachers) {
            if (teacher1.getId() == teacherID) {
                teacherResponse.setLikedByStudent(true);
                break;
            }
        }
        return teacherResponse;
    }

    public UserResponse getUser(HttpServletRequest httpServletRequest) {
        String token = httpServletRequest.getHeader("Authorization");
        Teacher teacher = teacherRepository.findTeacherByTokens_token(token.substring(7));
        if (teacher != null) {
            if (teacher.isVerified()) {
                return new UserResponse(teacher.getId(), teacher.getFirstname(), teacher.getLastname(),
                        teacher.getRole().toString(), teacher.isVerified(), true);
            } else if (teacher.getTimeOfVerificationRequest() != null) {
                return new UserResponse(teacher.getId(), teacher.getFirstname(), teacher.getLastname(),
                        teacher.getRole().toString(), false, true);
            } else {
                return new UserResponse(teacher.getId(), teacher.getFirstname(), teacher.getLastname(),
                        teacher.getRole().toString(), false, false);
            }
        } else {
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


    public List<CalendarResponse> getCalendarStudent(String token) throws CustomException {
        //TODO Add multiple dates for courses in the calendar
        Student student = studentRepository.findStudentByTokens_token(token.substring(7));
        List<CalendarResponse> responses = new ArrayList<>();
        for (CourseTermin courseTermin : student.getCourses()) {
            Lesson lesson = courseTermin.getLesson();
            CalendarResponse calendarResponse = new CalendarResponse(lesson.getTitle(), courseTermin.getDateTime().toString(),
                    new Timestamp(courseTermin.getDateTime().getTime() + lesson.getLength() * 60000L).toString());
            responses.add(calendarResponse);
        }
        for (LessonTermin lessonTermin : student.getPrivateLessons()) {
            Lesson lesson = lessonTermin.getLesson();
            CalendarResponse calendarResponse = new CalendarResponse(lesson.getTitle(), lessonTermin.getDateTime().toString(),
                    new Timestamp(lessonTermin.getDateTime().getTime() + lesson.getLength() * 60000L).toString());
            responses.add(calendarResponse);
        }
        return responses;
    }

    public List<CalendarResponse> getCalendarTeacher(String token) throws CustomException {
        //TODO Add multiple dates for courses in the calendar
        Teacher teacher = teacherRepository.findTeacherByTokens_token(token.substring(7));
        List<CalendarResponse> responses = new ArrayList<>();
        for (Lesson lesson : teacher.getLessons()) {
            if (!lesson.isPrivateLesson()) {
                for (CourseTermin courseTermin : lesson.getCourseTermins()) {
                    CalendarResponse calendarResponse = new CalendarResponse(lesson.getTitle(), courseTermin.getDateTime().toString(),
                            new Timestamp(courseTermin.getDateTime().getTime() + lesson.getLength()).toString());
                    responses.add(calendarResponse);
                }
            } else {
                for (LessonTermin lessonTermin : lesson.getLessonTermins()) {
                    CalendarResponse calendarResponse = new CalendarResponse(lesson.getTitle(), lessonTermin.getDateTime().toString(),
                            new Timestamp(lessonTermin.getDateTime().getTime() + lesson.getLength()).toString());
                    responses.add(calendarResponse);
                }
            }
        }
        return responses;
    }

    public PagedResponse getFavouriteTeachers(String token, int page) {
        Student student = studentRepository.findStudentByTokens_token(token.substring(7));
        List<TeacherResponse> teacherResponses = new ArrayList<>();
        List<Teacher> teachers = student.getFavouriteTeachers();
        int elementCounter = 0;
        for (Teacher teacher : teachers) {
            if (elementCounter >= page * 6) {
                break;
            }
            if (elementCounter >= page * 6 - 6) {
                TeacherResponse teacherResponse = TeacherResponse.builder().id(teacher.getId()).firstName(teacher.getFirstname())
                        .secondName(teacher.getLastname()).numberOfReviews(teacher.getNumberOfReviews()).rating(teacher.getRating())
                        .specialties(teacher.getSpecialties()).build();
                teacherResponses.add(teacherResponse);
            }
            elementCounter++;
        }
        return new PagedResponse(teachers.size(), 6, teacherResponses);
    }
}
