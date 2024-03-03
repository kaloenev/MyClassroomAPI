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
import org.hibernate.Hibernate;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
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
    private final SimpMessagingTemplate messagingTemplate;


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
            contacts = messageContactRepo.getMessageContactsByTeacher_Id(teacher.getId());
        } else {
            contacts = messageContactRepo.getMessageContactsByStudent_Id(student.getId());
        }
        List<MessageContactsResponse> responses = new ArrayList<>();
        for (MessageContact contact : contacts) {
            Message message = messageRepo.findTopByContact_MessageIDOrderByDateTimeDesc(contact.getMessageID());
            MessageContactsResponse messageContactsResponse;
            if (student == null) {
                //TODO Add dates as well if the message is not from today
                String name;
                if (contact.getStudent().getFirstname() == null) {
                    name = String.valueOf(contact.getStudent().getId());
                }
                else {
                    name = contact.getStudent().getFirstname() + " " + contact.getStudent().getLastname();
                }
                messageContactsResponse = MessageContactsResponse.builder().receiverId(contact.getStudent().getId())
                        .name(name).dateTime(message.getDateTime().toString())
                        .picture("http://localhost:8080/api/v1/users/images/" + contact.getStudent().getPictureLocation())
                        .content(message.getContent()).isRead(contact.isRead())
                        .date(message.getDate()).time(message.getTime()).recipientId(contact.getStudent().getId().toString())
                        .senderId(contact.getTeacher().getId().toString()).isFile(message.isFile()).build();
            } else {
                String name;
                if (contact.getTeacher().getFirstname() == null) {
                    name = String.valueOf(contact.getTeacher().getId());
                }
                else {
                    name = contact.getTeacher().getFirstname() + " " + contact.getTeacher().getLastname();
                }
                messageContactsResponse = MessageContactsResponse.builder().receiverId(contact.getTeacher().getId())
                        .name(name).dateTime(message.getDateTime().toString())
                        .picture("http://localhost:8080/api/v1/users/images/" + contact.getTeacher().getPictureLocation())
                        .content(message.getContent()).isRead(contact.isRead())
                        .date(message.getDate()).time(message.getTime()).recipientId(contact.getTeacher().getId().toString())
                        .senderId(contact.getStudent().getId().toString()).isFile(message.isFile()).build();
            }
            responses.add(messageContactsResponse);
        }
        return responses;
    }

    public List<MessageContactsResponse> getContactsWithName(String token, String searchTerm) throws CustomException {
        Student student = studentRepository.findStudentByTokens_token(token.substring(7));
        Teacher teacher;
        List<MessageContact> contacts;
        if (student == null) {
            teacher = teacherRepository.findTeacherByTokens_token(token.substring(7));
            if (teacher == null) throw new CustomException(HttpStatus.FORBIDDEN, "Моля логнете се отново");
            contacts = messageContactRepo.getMessageContactsByTeacher_IdAndSearch(teacher.getId(), searchTerm);
        } else {
            contacts = messageContactRepo.getMessageContactsByStudent_IdAndSearch(student.getId(), searchTerm);
        }
        List<MessageContactsResponse> responses = new ArrayList<>();
        for (MessageContact contact : contacts) {
            Message message = messageRepo.findTopByContact_MessageIDOrderByDateTimeDesc(contact.getMessageID());
            MessageContactsResponse messageContactsResponse;
            if (student == null) {
                //TODO Add dates as well if the message is not from today
                String name;
                if (contact.getStudent().getFirstname() == null) {
                    name = String.valueOf(contact.getStudent().getId());
                }
                else {
                    name = contact.getStudent().getFirstname() + " " + contact.getStudent().getLastname();
                }
                messageContactsResponse = MessageContactsResponse.builder().receiverId(contact.getStudent().getId())
                        .name(name).dateTime(message.getDateTime().toString())
                        .picture("http://localhost:8080/api/v1/users/images/" + contact.getStudent().getPictureLocation())
                        .content(message.getContent()).isRead(contact.isRead())
                        .date(message.getDate()).time(message.getTime()).recipientId(contact.getStudent().getId().toString())
                        .senderId(contact.getTeacher().getId().toString()).isFile(message.isFile()).build();
            } else {
                String name;
                if (contact.getTeacher().getFirstname() == null) {
                    name = String.valueOf(contact.getTeacher().getId());
                }
                else {
                    name = contact.getTeacher().getFirstname() + " " + contact.getTeacher().getLastname();
                }
                messageContactsResponse = MessageContactsResponse.builder().receiverId(contact.getTeacher().getId())
                        .name(name).dateTime(message.getDateTime().toString())
                        .picture("http://localhost:8080/api/v1/users/images/" + contact.getTeacher().getPictureLocation())
                        .content(message.getContent()).isRead(contact.isRead())
                        .date(message.getDate()).time(message.getTime()).recipientId(contact.getTeacher().getId().toString())
                        .senderId(contact.getStudent().getId().toString()).isFile(message.isFile()).build();
            }
            responses.add(messageContactsResponse);
        }
        return responses;
    }

    //TODO Find better option for single contact query
    public MessageContactsResponse getLastContact(String token) throws CustomException {
        Student student = studentRepository.findStudentByTokens_token(token.substring(7));
        Teacher teacher;
        List<MessageContact> contacts;
        if (student == null) {
            teacher = teacherRepository.findTeacherByTokens_token(token.substring(7));
            if (teacher == null) throw new CustomException(HttpStatus.FORBIDDEN, "Моля логнете се отново");
            contacts = messageContactRepo.getMessageContactsByTeacher_Id(teacher.getId());
        } else {
            contacts = messageContactRepo.getMessageContactsByStudent_Id(student.getId());
        }
        MessageContact contact = contacts.get(0);
            Message message = messageRepo.findTopByContact_MessageIDOrderByDateTimeDesc(contact.getMessageID());
            MessageContactsResponse messageContactsResponse;
            if (student == null) {
                //TODO Add dates as well if the message is not from today
                String name;
                if (contact.getStudent().getFirstname() == null) {
                    name = String.valueOf(contact.getStudent().getId());
                }
                else {
                    name = contact.getStudent().getFirstname() + " " + contact.getStudent().getLastname();
                }
                messageContactsResponse = MessageContactsResponse.builder().receiverId(contact.getStudent().getId())
                        .name(name).dateTime(message.getDateTime().toString())
                        .picture("http://localhost:8080/api/v1/users/images/" + contact.getStudent().getPictureLocation())
                        .content(message.getContent()).isRead(contact.isRead())
                        .date(message.getDate()).time(message.getTime()).recipientId(contact.getStudent().getId().toString())
                        .senderId(contact.getTeacher().getId().toString()).isFile(message.isFile()).build();
            } else {
                String name;
                if (contact.getTeacher().getFirstname() == null) {
                    name = String.valueOf(contact.getTeacher().getId());
                }
                else {
                    name = contact.getTeacher().getFirstname() + " " + contact.getTeacher().getLastname();
                }
                messageContactsResponse = MessageContactsResponse.builder().receiverId(contact.getTeacher().getId())
                        .name(name).dateTime(message.getDateTime().toString())
                        .picture("http://localhost:8080/api/v1/users/images/" + contact.getTeacher().getPictureLocation())
                        .content(message.getContent()).isRead(contact.isRead())
                        .date(message.getDate()).time(message.getTime()).recipientId(contact.getTeacher().getId().toString())
                        .senderId(contact.getStudent().getId().toString()).isFile(message.isFile()).build();
            }
        return messageContactsResponse;
    }

    public List<ChatNotification> getMessages(String token, int id, boolean isFromMessageTab) throws CustomException {
        //TODO Add check if the user has access to this messageContact
        Student student = studentRepository.findStudentByTokens_token(token.substring(7));
        Teacher teacher;
        if (student == null) {
            teacher = teacherRepository.findTeacherByTokens_token(token.substring(7));
            if (teacher == null) throw new CustomException(HttpStatus.FORBIDDEN, "Моля логнете се отново");
        }
        MessageContact contact;
        if (isFromMessageTab) {
            contact = messageContactRepo.getMessageContactByMessageID(id);
        } else if (student == null) {
            contact = messageContactRepo.getMessageContactByStudent_IdAndTeacher_Tokens_Token(id, token.substring(7));
        } else {
            contact = messageContactRepo.getMessageContactByTeacher_IdAndStudent_Tokens_Token(id, token.substring(7));
        }
        List<Message> messages = messageRepo.getAllMessagesByContact_MessageIDOrderByDateTimeAsc(contact.getMessageID());
        List<ChatNotification> chatNotifications = new ArrayList<>();
        for (Message message : messages) {
            ChatNotification chatNotification;
            if (message.isStudentTheSender()) {
                chatNotification = ChatNotification.builder().content(message.getContent())
                        .date(message.getDate()).time(message.getTime()).recipientId(contact.getTeacher().getId().toString())
                        .senderId(contact.getStudent().getId().toString()).isFile(message.isFile()).build();
            } else {
                chatNotification = ChatNotification.builder().content(message.getContent())
                        .date(message.getDate()).time(message.getTime()).recipientId(contact.getStudent().getId().toString())
                        .senderId(contact.getTeacher().getId().toString()).isFile(message.isFile()).build();
            }
            chatNotifications.add(chatNotification);
        }
        if (!contact.isRead()) {
            contact.setRead(true);
            messageContactRepo.save(contact);
        }
        return chatNotifications;
    }

    public void sendMessage(String token, String content, int receiverID, boolean isFile) throws CustomException {
        //TODO Check security
        // TODO Why not leave only one token and replace it every time on login
        var user = userRepository.findUserByTokens_token(token);
        if (user == null) {
            throw new CustomException(HttpStatus.FORBIDDEN, "Моля логнете се отново");
        }
        var user1 = userRepository.findUserById(receiverID);
        boolean foundContact = false;
        if (user.getRole().equals(Role.STUDENT)) {
            if (user1.getRole().equals(Role.STUDENT))
                throw new CustomException(HttpStatus.FORBIDDEN, "Can not send message to another student");
            Student student = studentRepository.findStudentById(user.getId());
            Teacher teacher = teacherRepository.findTeacherById(receiverID);
            List<MessageContact> messageContactList = messageContactRepo.getMessageContactsByStudent_Id(user.getId());
            for (MessageContact messageContact : messageContactList) {
                if (Objects.equals(messageContact.getTeacher().getUsername(), teacher.getUsername())) {
                    Message message = new Message();
                    message.setDateTime(new Timestamp(System.currentTimeMillis()));
                    message.setContent(content);
                    message.setStudentTheSender(true);
                    message.setContact(messageContact);
                    message.setFile(isFile);
                    messageRepo.save(message);
//                    messageContact.addNewMessage(message);
                    messageContact.setRead(false);
                    messageContactRepo.save(messageContact);
//                    student.saveMessage(messageContact);
//                    teacher.saveMessage(messageContact);
                    foundContact = true;
                    messagingTemplate.convertAndSendToUser(
                            userRepository.findLastToken(receiverID).get(0).getToken(), "/queue/messages",
                            NotificationResponse.builder().isChat(true).content(content).build()
                    );
                    break;
                }
            }
            if (!foundContact) {
                MessageContact messageContact = new MessageContact();
                messageContact.setStudent(student);
                messageContact.setTeacher(teacher);
                messageContact.setRead(false);
                messageContactRepo.save(messageContact);
//                List<Message> messages = new ArrayList<>();
                Message message = new Message();
                message.setDateTime(new Timestamp(System.currentTimeMillis()));
                message.setContent(content);
                message.setStudentTheSender(true);
//                messages.add(message);
                message.setContact(messageContact);
                message.setFile(isFile);
                messageRepo.save(message);
                messagingTemplate.convertAndSendToUser(
                        userRepository.findLastToken(receiverID).get(0).getToken(), "/queue/messages",
                        NotificationResponse.builder().isChat(true).content(content).build()
                );
//                messageContact.setMessages(messages);
//                student.saveMessage(messageContact);
//                teacher.saveMessage(messageContact);
            }
        } else {
            if (user1.getRole().equals(Role.TEACHER))
                throw new CustomException(HttpStatus.FORBIDDEN, "Can not send message to another teacher");
            Student student = studentRepository.findStudentById(receiverID);
            Teacher teacher = teacherRepository.findTeacherById(user.getId());
            List<MessageContact> messageContactList = messageContactRepo.getMessageContactsByTeacher_Id(user.getId());
            for (MessageContact messageContact : messageContactList) {
                if (Objects.equals(messageContact.getStudent().getUsername(), student.getUsername())) {
                    Message message = new Message();
                    message.setDateTime(new Timestamp(System.currentTimeMillis()));
                    message.setContent(content);
                    message.setStudentTheSender(false);
                    message.setContact(messageContact);
                    message.setFile(isFile);
                    messageRepo.save(message);
//                    messageContact.addNewMessage(message);
                    messageContact.setRead(false);
                    messageContactRepo.save(messageContact);
//                    student.saveMessage(messageContact);
//                    teacher.saveMessage(messageContact);
                    foundContact = true;
                    messagingTemplate.convertAndSendToUser(
                            userRepository.findLastToken(receiverID).get(0).getToken(), "/queue/messages",
                            NotificationResponse.builder().isChat(true).content(content).build()
                    );
                    break;
                }
            }
            if (!foundContact) {
                MessageContact messageContact = new MessageContact();
                messageContact.setStudent(student);
                messageContact.setTeacher(teacher);
                messageContact.setRead(false);
                messageContactRepo.save(messageContact);
//                List<Message> messages = new ArrayList<>();
                Message message = new Message();
                message.setDateTime(new Timestamp(System.currentTimeMillis()));
                message.setContent(content);
                message.setStudentTheSender(false);
                message.setContact(messageContact);
                message.setFile(isFile);
//                messages.add(message);
                messageRepo.save(message);
                messagingTemplate.convertAndSendToUser(
                        userRepository.findLastToken(receiverID).get(0).getToken(), "/queue/messages",
                        NotificationResponse.builder().isChat(true).content(content).build()
                );
//                messageContact.setMessages(messages);

//                student.saveMessage(messageContact);
//                teacher.saveMessage(messageContact);
            }
        }
    }

    public int verifyTeacher(String token, String name, String surname, String picture, Gender gender, City city,
                             String description, String subjects, Degree degree, String school, String university,
                             String specialty, ExperienceRequest[] experience) throws IOException, CustomException {
        Teacher teacher = teacherRepository.findTeacherByTokens_token(token.substring(7));
        teacher.setFirstname(name);
        teacher.setLastname(surname);
        teacher.setGender(gender);
        teacher.setCity(city);
        if (picture == null) {
            teacher.setPictureLocation("Assignment_301947782_0_number_of_mesoscopic_papers.PNG");
        } else {
            teacher.setPictureLocation(picture);
        }
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

    //TODO Maybe unneeded (delete)
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
        Teacher teacher = teacherRepository.findTeacherById(teacherID);
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
                        teacher.getRole().toString(),
                        "http://localhost:8080/api/v1/users/images/" +  teacher.getPictureLocation(),
                        teacher.isVerified(), true);
            } else if (teacher.getTimeOfVerificationRequest() != null) {
                return new UserResponse(teacher.getId(), teacher.getFirstname(), teacher.getLastname(),
                        teacher.getRole().toString(),
                        "http://localhost:8080/api/v1/users/images/" +  teacher.getPictureLocation(),
                        false, true);
            } else {
                return new UserResponse(teacher.getId(), teacher.getFirstname(), teacher.getLastname(),
                        teacher.getRole().toString(),
                        "http://localhost:8080/api/v1/users/images/" +  teacher.getPictureLocation(),
                        false, false);
            }
        } else {
            User user = userRepository.findUserByTokens_token(token.substring(7));
            return new UserResponse(user.getId(), user.getFirstname(), user.getLastname(), user.getRole().toString(),
                    "http://localhost:8080/api/v1/users/images/" + user.getPictureLocation(),
                    true, true);
        }
    }

    //TODO Add check if student is not null and throw exception accordingly
    public void editStudentProfile(StudentProfileRequest request, String token) throws CustomException {
        Student student = studentRepository.findStudentByTokens_token(token.substring(7));
        student.setGender(Gender.valueOf(request.getGender()));
        student.setFirstname(request.getName());
        student.setLastname(request.getSurname());
        String notifications = request.isClientService() + "," + request.isMarketingService()
                + "," + request.isReminders() + "," + request.isChatNotifications() + ","
                + request.isSavedCoursesNotifications();
        student.setNotificationModev2(notifications);
        student.setPictureLocation(request.getImageLocation().replace("http://localhost:8080/api/v1/users/images/", ""));
        studentRepository.save(student);
    }

    public StudentProfileResponse getStudentProfile(String token) throws CustomException {
        Student student = studentRepository.findStudentByTokens_token(token.substring(7));
        String pictureLocation = "";
        String[] notifications = new String[]{"false", "false", "false", "false", "false"};
        String name = "";
        String surname = "";
        String gender = "";
        if (student.getPictureLocation() != null) pictureLocation = "http://localhost:8080/api/v1/users/images/"
                + student.getPictureLocation();
        else {
            pictureLocation = "http://localhost:8080/api/v1/users/images/src/main/resources/images/default_image.jpg";
        }
        if (student.getNotificationModev2() != null) notifications = student.getNotificationModev2().split(",");
        if (student.getGender() != null) gender = student.getGender().toString();
        if (student.getFirstname() != null) name = student.getFirstname();
        if (student.getLastname() != null) surname = student.getLastname();
        String[] pictures = new String[]{pictureLocation, "http://localhost:8080/api/v1/users/images/src/main/resources/images/Avatar_01.png",
                "http://localhost:8080/api/v1/users/images/src/main/resources/images/Avatar_02.png",
                "http://localhost:8080/api/v1/users/images/src/main/resources/images/Avatar_03.png",
                "http://localhost:8080/api/v1/users/images/src/main/resources/images/Avatar_04.png",
                "http://localhost:8080/api/v1/users/images/src/main/resources/images/Avatar_05.png",
                "http://localhost:8080/api/v1/users/images/src/main/resources/images/Avatar_06.png"};
        return new StudentProfileResponse(student.getId(), name, surname, gender, pictures,
                Boolean.parseBoolean(notifications[0]), Boolean.parseBoolean(notifications[1]),
                Boolean.parseBoolean(notifications[2]), Boolean.parseBoolean(notifications[3]),
                Boolean.parseBoolean(notifications[4]));
    }

    public void editTeacherProfile(TeacherProfileRequest request, String token) throws CustomException {
        Teacher teacher = teacherRepository.findTeacherByTokens_token(token.substring(7));
        teacher.setGender(Gender.valueOf(request.getGender()));
        teacher.setFirstname(request.getName());
        teacher.setLastname(request.getSurname());
        teacher.setDescription(request.getDescription());
        teacher.setSpecialties(request.getSubjects());
        String notifications = request.isClientService() + "," + request.isMarketingService()
                + "," + request.isReminders() + "," + request.isChatNotifications() + ","
                + request.isSavedCoursesNotifications();
        teacher.setNotificationModev2(notifications);
        teacher.setPictureLocation(request.getImageLocation().replace("http://localhost:8080/api/v1/users/images/", ""));
        teacherRepository.save(teacher);
    }

    public TeacherProfileResponse getTeacherProfile(String token) throws CustomException {
        Teacher teacher = teacherRepository.findTeacherByTokens_token(token.substring(7));
        String pictureLocation = "";
        String[] notifications = new String[]{"false", "false", "false", "false", "false"};
        String name = "";
        String surname = "";
        String gender = "";
        String description = "";
        String subjects = "";
        if (teacher.getPictureLocation() != null) pictureLocation = "http://localhost:8080/api/v1/users/images/"
                + teacher.getPictureLocation();
        else {
            pictureLocation = "http://localhost:8080/api/v1/users/images/src/main/resources/images/default_image.jpg";
        }
        if (teacher.getNotificationModev2() != null) notifications = teacher.getNotificationModev2().split(",");
        if (teacher.getGender() != null) gender = teacher.getGender().toString();
        if (teacher.getFirstname() != null) name = teacher.getFirstname();
        if (teacher.getLastname() != null) surname = teacher.getLastname();
        if (teacher.getDescription() != null) description = teacher.getDescription();
        if (teacher.getSpecialties() != null) subjects = teacher.getSpecialties();
        return new TeacherProfileResponse(teacher.getId(), name, surname, gender, subjects, description, pictureLocation,
                Boolean.parseBoolean(notifications[0]), Boolean.parseBoolean(notifications[1]),
                Boolean.parseBoolean(notifications[2]), Boolean.parseBoolean(notifications[3]),
                Boolean.parseBoolean(notifications[4]));
    }


    public List<CalendarResponse> getCalendarStudent(String token) throws CustomException {
        //TODO Add multiple dates for courses in the calendar
        Student student = studentRepository.findStudentByTokens_token(token.substring(7));
        List<CalendarResponse> responses = new ArrayList<>();
        List<CourseTermin> courseTermins = student.getCourses();
        List<LessonTermin> lessonTermins = student.getPrivateLessons();
        for (CourseTermin courseTermin : courseTermins) {
            Lesson lesson = courseTermin.getLesson();
            CalendarResponse calendarResponse = CalendarResponse.builder().title(lesson.getTitle()).className("course")
                    .start(courseTermin.getDateTime().toString().replace(" ", "T").replace(".0", ""))
                    .end(new Timestamp(courseTermin.getDateTime().getTime() + lesson.getLength() * 60000L).toString()
                            .replace(" ", "T").replace(".0", "")).build();
            responses.add(calendarResponse);
        }
        for (LessonTermin lessonTermin : lessonTermins) {
            Lesson lesson = lessonTermin.getLesson();
            CalendarResponse calendarResponse = CalendarResponse.builder().title(lesson.getTitle()).className("privateLesson")
                    .start(lessonTermin.getDateTime().toString().replace(" ", "T").replace(".0", ""))
                    .end(new Timestamp(lessonTermin.getDateTime().getTime() + lesson.getLength() * 60000L).toString()
                            .replace(" ", "T").replace(".0", "")).build();
            responses.add(calendarResponse);
        }
        for (Assignment assignment : student.getAssignments()) {
            CalendarResponse calendarResponse = CalendarResponse.builder().title(assignment.getTitle()).className("assignment")
                    .start(assignment.getDueDateTime().toString().replace(" ", "T").replace(".0", ""))
                    .build();
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
                                .start(courseTermin.getDateTime().toString().replace(" ", "T").replace(".0", ""))
                                .end(new Timestamp(courseTermin.getDateTime().getTime() + lesson.getLength() * 60000L).toString()
                                        .replace(" ", "T").replace(".0", "")).build();
                    }
                    if (!courseTermin.isEmpty()) {
                        dateHasStudents = "hasStudents";
                        if (calendarResponse != null) {
                            calendarResponse.setClassName(dateHasStudents);
                        }
                    }
                    if (counter == courseTermins.size()) {
                        calendarResponse = CalendarResponse.builder().title(lesson.getTitle()).className(dateHasStudents)
                                .start(courseTermin.getDateTime().toString().replace(" ", "T").replace(".0", ""))
                                .end(new Timestamp(courseTermin.getDateTime().getTime() + lesson.getLength() * 60000L).toString()
                                        .replace(" ", "T").replace(".0", "")).build();
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
                                .start(lessonTermin.getDateTime().toString().replace(" ", "T").replace(".0", ""))
                                .end(new Timestamp(lessonTermin.getDateTime().getTime() + lesson.getLength() * 60000L).toString()
                                        .replace(" ", "T").replace(".0", "")).build();
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
                                .start(lessonTermin.getDateTime().toString().replace(" ", "T").replace(".0", ""))
                                .end(new Timestamp(lessonTermin.getDateTime().getTime() + lesson.getLength() * 60000L).toString()
                                        .replace(" ", "T").replace(".0", "")).build();
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
