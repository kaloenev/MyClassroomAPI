package com.alibou.security.userFunctions;

import com.alibou.security.chatroom.ChatNotification;
import com.alibou.security.coursesServiceController.*;
import com.alibou.security.emailing.EmailDetails;
import com.alibou.security.emailing.EmailService;
import com.alibou.security.exceptionHandling.CustomException;
import com.alibou.security.lessons.Assignment;
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

    public List<ChatNotification> getMessages(String token, int id) throws CustomException {
        //TODO Add check if the user has access to this messageContact
        Student student = studentRepository.findStudentByTokens_token(token.substring(7));
        Teacher teacher;
        if (student == null) {
            teacher = teacherRepository.findTeacherByTokens_token(token.substring(7));
            if (teacher == null) throw new CustomException(HttpStatus.FORBIDDEN, "Моля логнете се отново");
        }
        MessageContact contact = messageContactRepo.getMessageContactByMessageID(id);
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
        return chatNotifications;
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

    //TODO Add check if student is not null and throw exception accordingly
    public void editStudentProfile(StudentProfileRequest request, String token) throws CustomException {
        Student student = studentRepository.findStudentByTokens_token(token.substring(7));
        student.setGender(Gender.valueOf(request.getGender()));
        student.setFirstname(request.getName());
        student.setLastname(request.getSurname());
        String notifications = request.isClientService() + String.valueOf(request.isMarketingService())
                + request.isReminders() + request.isChatNotifications() + request.isSavedCoursesNotifications();
        student.setNotificationModev2(notifications);
        student.setPictureLocation(request.getImageLocation());
    }

    public StudentProfileResponse getStudentProfile(String token) throws CustomException {
        Student student = studentRepository.findStudentByTokens_token(token.substring(7));
        String pictureLocation = "";
        String[] notifications = new String[]{"false", "false", "false", "false", "false"};
        String name = "";
        String surname = "";
        String gender = "";
        if (student.getPictureLocation() != null) pictureLocation = student.getPictureLocation();
        if (student.getNotificationModev2() != null) notifications = student.getNotificationModev2().split(",");
        if (student.getGender() != null) gender = student.getGender().toString();
        if (student.getFirstname() != null) name = student.getFirstname();
        if (student.getLastname() != null) surname = student.getLastname();
        return new StudentProfileResponse(student.getId(), name, surname, gender, pictureLocation, Boolean.parseBoolean(notifications[0]),
                Boolean.parseBoolean(notifications[1]), Boolean.parseBoolean(notifications[2]), Boolean.parseBoolean(notifications[3]),
                Boolean.parseBoolean(notifications[4]));
    }


    public List<CalendarResponse> getCalendarStudent(String token) throws CustomException {
        //TODO Add multiple dates for courses in the calendar
        Student student = studentRepository.findStudentByTokens_token(token.substring(7));
        List<CalendarResponse> responses = new ArrayList<>();
        for (CourseTermin courseTermin : student.getCourses()) {
            Lesson lesson = courseTermin.getLesson();
            CalendarResponse calendarResponse = CalendarResponse.builder().title(lesson.getTitle()).className("course")
                    .startDate(courseTermin.getDate()).startTime(courseTermin.getTime()).start(courseTermin.getDateTime().toString())
                    .endTime(new Timestamp(courseTermin.getDateTime().getTime() + lesson.getLength() * 60000L).toString().substring(11, 15))
                    .end(new Timestamp(courseTermin.getDateTime().getTime() + lesson.getLength() * 60000L).toString())
                    .dayOfTheWeek(courseTermin.getDateTime().toLocalDateTime().getDayOfWeek().name()).build();
            responses.add(calendarResponse);
        }
        for (LessonTermin lessonTermin : student.getPrivateLessons()) {
            Lesson lesson = lessonTermin.getLesson();
            CalendarResponse calendarResponse = CalendarResponse.builder().title(lesson.getTitle()).className("privateLesson")
                    .startDate(lessonTermin.getDate()).startTime(lessonTermin.getTime()).start(lessonTermin.getDateTime().toString())
                    .endTime(new Timestamp(lessonTermin.getDateTime().getTime() + lesson.getLength() * 60000L).toString().substring(11, 15))
                    .end(new Timestamp(lessonTermin.getDateTime().getTime() + lesson.getLength() * 60000L).toString())
                    .dayOfTheWeek(lessonTermin.getDateTime().toLocalDateTime().getDayOfWeek().name()).build();
            responses.add(calendarResponse);
        }
        for (Assignment assignment : student.getAssignments()) {
            CalendarResponse calendarResponse = CalendarResponse.builder().title(assignment.getTitle()).className("assignment")
                    .startDate(assignment.getDate()).startTime(assignment.getTime()).start(assignment.getDueDateTime().toString())
                    .endTime(assignment.getTime()).end(assignment.getDueDateTime().toString())
                    .dayOfTheWeek(assignment.getDueDateTime().toLocalDateTime().getDayOfWeek().name()).build();
            responses.add(calendarResponse);
        }
        return responses;
    }

    public List<CalendarResponse> getCalendarTeacher(String token) throws CustomException {
        //TODO Add multiple dates for courses in the calendar
        //TODO Maybe remove redundant variables (ask Veni which are needed)
        Teacher teacher = teacherRepository.findTeacherByTokens_token(token.substring(7));
        List<CalendarResponse> responses = new ArrayList<>();
        for (Lesson lesson : teacher.getLessons()) {
            if (lesson.isDraft()) continue;
            if (!lesson.isPrivateLesson()) {
                int date = -1;
                CalendarResponse calendarResponse = null;
                String dateHasStudents = null;
                List<CourseTermin> courseTermins = lesson.getCourseTermins();
                int counter = 1;
                for (CourseTermin courseTermin : courseTermins) {
                    if (date != courseTermin.getDateTime().toLocalDateTime().getDayOfMonth() && counter != courseTermins.size()) {
                        date = courseTermin.getDateTime().toLocalDateTime().getDayOfMonth();
                        if (counter != 1) responses.add(calendarResponse);
                        dateHasStudents = null;
                        calendarResponse = CalendarResponse.builder().title(lesson.getTitle()).className(dateHasStudents)
                                .startDate(courseTermin.getDate()).startTime(courseTermin.getTime())
                                .start(courseTermin.getDateTime().toString())
                                .endTime(new Timestamp(courseTermin.getDateTime().getTime() + lesson.getLength() * 60000L)
                                        .toString().substring(11, 15))
                                .enrolledStudents(String.valueOf(courseTermin.getStudentsUpperBound() - courseTermin.getPlacesRemaining()))
                                .end(new Timestamp(courseTermin.getDateTime().getTime() + lesson.getLength() * 60000L).toString())
                                .dayOfTheWeek(courseTermin.getDateTime().toLocalDateTime().getDayOfWeek().name()).build();
                    }
                    if (!courseTermin.isEmpty()) {
                        dateHasStudents = "hasStudents";
                        if (calendarResponse != null) {
                            calendarResponse.setClassName(dateHasStudents);
                        }
                    }
                    if (counter == courseTermins.size()) {
                        calendarResponse = CalendarResponse.builder().title(lesson.getTitle()).className(dateHasStudents)
                                .startDate(courseTermin.getDate()).startTime(courseTermin.getTime())
                                .start(courseTermin.getDateTime().toString())
                                .endTime(new Timestamp(courseTermin.getDateTime().getTime() + lesson.getLength() * 60000L)
                                        .toString().substring(11, 15))
                                .enrolledStudents(String.valueOf(courseTermin.getStudentsUpperBound() - courseTermin.getPlacesRemaining()))
                                .end(new Timestamp(courseTermin.getDateTime().getTime() + lesson.getLength() * 60000L).toString())
                                .dayOfTheWeek(courseTermin.getDateTime().toLocalDateTime().getDayOfWeek().name()).build();
                        responses.add(calendarResponse);
                    }
                    counter++;
                }
            } else {
                int date = -1;
                CalendarResponse calendarResponse = null;
                String dateHasStudents = null;
                String numberOfStudents = "0";
                List<LessonTermin> lessonTermins = lesson.getLessonTermins();
                int counter = 1;
                for (LessonTermin lessonTermin : lessonTermins) {
                    if (date != lessonTermin.getDateTime().toLocalDateTime().getDayOfMonth() && counter != lessonTermins.size()) {
                        date = lessonTermin.getDateTime().toLocalDateTime().getDayOfMonth();
                        if (counter != 1) responses.add(calendarResponse);
                        dateHasStudents = null;
                        numberOfStudents = "0";
                        calendarResponse = CalendarResponse.builder().title(lesson.getTitle()).className(dateHasStudents)
                                .startDate(lessonTermin.getDate()).startTime(lessonTermin.getTime()).start(lessonTermin.getDateTime().toString())
                                .endTime(new Timestamp(lessonTermin.getDateTime().getTime() + lesson.getLength() * 60000L).toString().substring(11, 15))
                                .end(new Timestamp(lessonTermin.getDateTime().getTime() + lesson.getLength() * 60000L).toString())
                                .enrolledStudents(numberOfStudents)
                                .dayOfTheWeek(lessonTermin.getDateTime().toLocalDateTime().getDayOfWeek().name()).build();
                    }
                    if (lessonTermin.isEmpty()) {
                        dateHasStudents = "hasStudents";
                        numberOfStudents = "1";
                        if (calendarResponse != null) {
                            calendarResponse.setClassName(dateHasStudents);
                            calendarResponse.setEnrolledStudents(numberOfStudents);
                        }
                    }
                    if (counter == lessonTermins.size()) {
                        calendarResponse = CalendarResponse.builder().title(lesson.getTitle()).className(dateHasStudents)
                                .startDate(lessonTermin.getDate()).startTime(lessonTermin.getTime()).start(lessonTermin.getDateTime().toString())
                                .endTime(new Timestamp(lessonTermin.getDateTime().getTime() + lesson.getLength() * 60000L).toString().substring(11, 15))
                                .end(new Timestamp(lessonTermin.getDateTime().getTime() + lesson.getLength() * 60000L).toString())
                                .enrolledStudents(numberOfStudents)
                                .dayOfTheWeek(lessonTermin.getDateTime().toLocalDateTime().getDayOfWeek().name()).build();
                        responses.add(calendarResponse);
                    }
                    counter++;
                }
            }
        }
        return responses;
    }

    //TODO Add each instance of the course to the calendar (per day of the week) and checks if student/teacher exists

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
