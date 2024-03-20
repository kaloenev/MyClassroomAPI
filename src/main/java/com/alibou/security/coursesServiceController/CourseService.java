package com.alibou.security.coursesServiceController;

import com.alibou.security.emailing.EmailDetails;
import com.alibou.security.emailing.EmailService;
import com.alibou.security.exceptionHandling.CustomException;
import com.alibou.security.lessons.*;
import com.alibou.security.schedulingtasks.NotificationSenderTask;
import com.alibou.security.token.TokenRepository;
import com.alibou.security.user.*;
import com.alibou.security.userFunctions.CalendarResponse;
import com.alibou.security.userFunctions.UserProfileResponse;
import lombok.RequiredArgsConstructor;
import org.aspectj.weaver.ast.Not;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.io.File;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class CourseService {
    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final SolutionRepo solutionRepo;
    public static double LOWERMEDIAN_PRICE_COURSE;
    public static double UPPERMEDIAN_PRICE_COURSE;
    public static double SUM_PRICE_COURSE;
    public static double COURSES_COUNT;
    public static double BOTTOM20PRICE_COURSE;

    public static double DEVIATION_COURSE;
    public static double BOTTOM40PRICE_COURSE;
    public static double BOTTOM60PRICE_COURSE;
    public static double BOTTOM80PRICE_COURSE;
    public static double LOWERMEDIAN_PRICE_LESSON;
    public static double UPPERMEDIAN_PRICE_LESSON;
    public static double SUM_PRICE_LESSON;
    public static double LESSONS_COUNT;
    public static double BOTTOM20PRICE_LESSON;
    public static double BOTTOM40PRICE_LESSON;
    public static double BOTTOM60PRICE_LESSON;
    public static double BOTTOM80PRICE_LESSON;
    public static double DEVIATION_LESSON;

    private final EmailService emailService;
    private final LessonRepository lessonRepository;

    private final TerminRepo terminRepo;

    private final CommentRepo commentRepo;

    private final LessonTerminRepo lessonTerminRepo;

    private final CourseTerminRepo courseTerminRepo;

    private final TeacherRepository teacherRepository;

    private final StudentRepository studentRepository;

    private final ReviewRepo reviewRepo;

    private final ThemaRepository themaRepository;

    private final AssignmentRepo assignmentRepo;

    private final SimpMessagingTemplate messagingTemplate;

    public void enrollUserInCourse(String token, int terminID) throws CustomException {
        Student student = studentRepository.findStudentByTokens_token(token.substring(7));
        CourseTermin termin = courseTerminRepo.getCourseTerminByTerminID(terminID);
        //TODO Check if payment is present
        if (termin.isFull()) {
            throw new CustomException(HttpStatus.FORBIDDEN, "Error course is full");
        }
        termin.enrollStudent(student);
        courseTerminRepo.save(termin);
        student.addCourseTermin(termin);
        studentRepository.save(student);
    }

    public void enrollUserInLesson(String token, int terminID) throws CustomException {
        Student student = studentRepository.findStudentByTokens_token(token.substring(7));
        LessonTermin termin = lessonTerminRepo.getLessonTerminByTerminID(terminID);
        //TODO Check if payment is present
        if (termin.isFull()) {
            throw new CustomException(HttpStatus.FORBIDDEN, "Error course is full");
        }
        termin.enrollStudent(student);
        lessonTerminRepo.save(termin);
        student.addLessonTermin(termin);
        studentRepository.save(student);
    }

    public void likeCourse(String token, int lessonID) throws CustomException {
        Student student = studentRepository.findStudentByTokens_token(token.substring(7));
        Lesson lesson = lessonRepository.getLessonByLessonID(lessonID);
        student.saveLessonToLiked(lesson);
        studentRepository.save(student);
        lesson.addToIsLiked(student);
        lessonRepository.save(lesson);
    }

    public void dislikeCourse(String token, int lessonID) throws CustomException {
        Student student = studentRepository.findStudentByTokens_token(token.substring(7));
        Lesson lesson = lessonRepository.getLessonByLessonID(lessonID);
        student.removeLessonsFromLiked(lesson);
        studentRepository.save(student);
        lesson.removeFromIsLiked(student);
        lessonRepository.save(lesson);
    }

    public void publishDraft(String token, int id) throws CustomException {
        Teacher teacher = teacherRepository.findTeacherByTokens_token(token.substring(7));
        Lesson lesson = lessonRepository.getLessonByLessonID(id);
        if (!Objects.equals(lesson.getTeacher().getId(), teacher.getId()))
            throw new CustomException(HttpStatus.CONFLICT,
                    "Не притежавате този курс");
        checkForUnallowedValues(lesson);
        lesson.setDraft(false);
        lessonRepository.save(lesson);
        if (lesson.isPrivateLesson()) addNewPriceLesson(lesson.getPrice());
        else addNewPriceCourse(lesson.getPrice());
    }

    //TODO remove themas check
    private void checkForUnallowedValues(Lesson lesson) throws CustomException {
        if (lesson.getTitle() == null || Objects.equals(lesson.getTitle(), ""))
            throw new CustomException(HttpStatus.CONFLICT,
                    "Не сте задали заглавие на курса");
        if (lesson.getLength() < 30) throw new CustomException(HttpStatus.CONFLICT, "Не сте задали дължина на урока");
        if (lesson.getDescription() == null || Objects.equals(lesson.getDescription(), ""))
            throw new CustomException(HttpStatus.CONFLICT, "Не сте задали описание на курса");
        if (lesson.getSubject() == null || Objects.equals(lesson.getSubject(), ""))
            throw new CustomException(HttpStatus.CONFLICT, "Не сте задали клас на курса");
        if (!lesson.isHasTermins()) throw new CustomException(HttpStatus.CONFLICT, "Не сте добавили дати на курса");
        if (lesson.getPrice() <= 0) throw new CustomException(HttpStatus.CONFLICT, "Не сте задали цена на курса");
        if (lesson.getGrade() == null || Objects.equals(lesson.getGrade(), ""))
            throw new CustomException(HttpStatus.CONFLICT, "Не сте задали описание на курса");
        if (lesson.getStudentsUpperBound() <= 0)
            throw new CustomException(HttpStatus.CONFLICT, "Не сте задали максимален брой ученици");
        if (lesson.getThemas() == null || lesson.getThemas().isEmpty())
            throw new CustomException(HttpStatus.CONFLICT, "Не сте задали теми");
    }
    //TODO Check why courses get full without anyone being enrolled in them
    //TODO Add check for the same dates and times
    public void createCourse(String token, CreateCourseRequest courseRequest, boolean isDraft, boolean isPrivateLesson) throws CustomException {
        //TODO Add grade check
        Teacher teacher = teacherRepository.findTeacherByTokens_token(token.substring(7));
        if (!teacher.isVerified())
            throw new CustomException(HttpStatus.CONFLICT, "You must be verified to create a course");
        System.out.println(teacher.getId());
        if (!isDraft) checkNonDraftRequirements(courseRequest, isPrivateLesson);
        Lesson lesson = Lesson.builder().teacher(teacher).build();
        lesson.setTitle(courseRequest.getTitle());
        lesson.setSubject(courseRequest.getSubject());
        lesson.setGrade(courseRequest.getGrade());
        if (courseRequest.getImageLocation() == null) {
            lesson.setImageLocation(null);
        } else {
            lesson.setImageLocation(courseRequest.getImageLocation().replace("http://localhost:8080/api/v1/users/images/", ""));
        }
        lesson.setDescription(courseRequest.getDescription());
        lesson.setLength(courseRequest.getLength());
        lesson.setPrivateLesson(isPrivateLesson);
        lesson.setPrice(courseRequest.getPrice());
        lesson.setDraft(isDraft);
        lesson.setHasTermins(false);
        if (!isPrivateLesson) {
            lesson.setStudentsUpperBound(courseRequest.getStudentsUpperBound());
            lessonRepository.save(lesson);
            List<Thema> themas = new ArrayList<>();
            if (courseRequest.getThemas() != null && courseRequest.getThemas().length > 0) {
                for (ThemaSimpleResponse themaData : courseRequest.getThemas()) {
                    Thema thema;
                    if (themaData.getDescription() == null) {
                        thema = Thema.builder().title(themaData.getTitle()).build();
                    } else
                        thema = Thema.builder().description(themaData.getDescription()).title(themaData.getTitle()).build();
                    themas.add(thema);
                    thema.setLesson(lesson);
                    themaRepository.save(thema);
                }
                lesson.setThemas(themas);
            }
            if (courseRequest.getCourseTerminRequests() != null && !courseRequest.getCourseTerminRequests().isEmpty()) {
                for (CourseTerminRequestResponse courseTerminRequest : courseRequest.getCourseTerminRequests()) {
                    List<Thema> themasForCourse = new ArrayList<>();
                    if (courseRequest.getThemas() != null && courseRequest.getThemas().length > 0) {
                        for (ThemaSimpleResponse themaData : courseRequest.getThemas()) {
                            Thema thema;
                            if (themaData.getDescription() == null) {
                                thema = Thema.builder().title(themaData.getTitle()).build();
                            } else
                                thema = Thema.builder().description(themaData.getDescription()).title(themaData.getTitle()).build();
                            themasForCourse.add(thema);
                        }
                    }
                    CourseTermin courseTermin = CourseTermin.builder().dateTime(Timestamp.valueOf(courseTerminRequest.getStartDate()
                                    + " " + courseTerminRequest.getCourseHours() + ":00"))
                            .courseDays(Arrays.toString(courseTerminRequest.getCourseDaysNumbers()))
                            .courseHoursNumber(Integer.parseInt(courseTerminRequest.getCourseHours().replace(":", "")))
                            .weekLength(courseTerminRequest.getWeekLength()).studentsUpperBound(courseRequest.getStudentsUpperBound())
                            .lesson(lesson).placesRemaining(courseRequest.getStudentsUpperBound()).lessonStatus(LessonStatus.NOT_STARTED).build();
                    courseTerminRepo.save(courseTermin);
                    for (Thema thema : themasForCourse) {
                        thema.setCourseTermin(courseTermin);
                        themaRepository.save(thema);
                        courseTermin.addThema(thema);
                    }
                    courseTerminRepo.save(courseTermin);
                    lesson.addTermin(courseTermin);
                    lesson.setHasTermins(true);
                }
                lesson.getTermins().sort(Comparator.comparing(Termin::getDateTime));
            }
        } else {
            lesson.setUpperGrade(courseRequest.getUpperGrade());
            lessonRepository.save(lesson);
            Thema thema = null;
            if (courseRequest.getThemas() != null && courseRequest.getThemas().length > 0 && courseRequest.getThemas()[0] != null) {
                if (courseRequest.getThemas()[0].getDescription() == null) {
                    thema = Thema.builder().title(courseRequest.getThemas()[0].getTitle()).build();
                } else {
                    thema = Thema.builder().description(courseRequest.getThemas()[0].getDescription())
                            .title(courseRequest.getThemas()[0].getTitle()).build();
                }
                thema.setLesson(lesson);
                themaRepository.save(thema);
            } else {
                thema = Thema.builder().title(lesson.getTitle()).build();
            }
            lesson.setStudentsUpperBound(1);
            List<Thema> themas = new ArrayList<>();
            themas.add(thema);
            lesson.setThemas(themas);
            lessonRepository.save(lesson);
            if (courseRequest.getPrivateLessonTermins() != null && !courseRequest.getPrivateLessonTermins().isEmpty()) {
                for (LessonTerminRequest privateLessonTermin : courseRequest.getPrivateLessonTermins()) {
                    for (TimePair timePair : privateLessonTermin.getLessonHours()) {
                        Thema thema1 = null;
                        if (courseRequest.getThemas() != null && courseRequest.getThemas().length > 0 && courseRequest.getThemas()[0] != null) {
                            if (courseRequest.getThemas()[0].getDescription() == null) {
                                thema1 = Thema.builder().title(courseRequest.getThemas()[0].getTitle()).build();
                            } else {
                                thema1 = Thema.builder().description(courseRequest.getThemas()[0].getDescription())
                                        .title(courseRequest.getThemas()[0].getTitle()).build();
                            }
                            themaRepository.save(thema1);
                        } else {
                            thema = Thema.builder().title(lesson.getTitle()).build();
                        }
                        String hours = timePair.getTime();
                        LessonTermin lessonTermin = LessonTermin.builder().lessonHours(Integer.parseInt(hours.replace(":", "")))
                                .dateTime(Timestamp.valueOf(privateLessonTermin.getDate() + " " + hours + ":00")).thema(thema1)
                                .lessonStatus(LessonStatus.NOT_STARTED).build();
                        lessonTerminRepo.save(lessonTermin);
                        lessonTermin.setLesson(lesson);
                        lesson.addTermin(lessonTermin);
                        lesson.setHasTermins(true);
                        if (thema != null) {
                            thema.setLessonTermin(lessonTermin);
                            themaRepository.save(thema);
                        }
                    }
                }
                lesson.getTermins().sort(Comparator.comparing(Termin::getDateTime));
            }
        }
        lessonRepository.save(lesson);
        System.out.println(teacher.getId());
        teacher.addLesson(lesson);

        if (!lesson.isDraft()) {
            if (lesson.isPrivateLesson()) addNewPriceLesson(lesson.getPrice());
            else addNewPriceCourse(lesson.getPrice());
        }
    }

    private void checkNonDraftRequirements(CreateCourseRequest courseRequest, boolean isPrivateLesson) throws CustomException {
        if (!isPrivateLesson && (courseRequest.getThemas() == null || courseRequest.getThemas().length <= 0))
            throw new CustomException(HttpStatus.CONFLICT, "Моля добавете теми или запазете курса като чернова");

        if ((courseRequest.getPrivateLessonTermins() == null || courseRequest.getPrivateLessonTermins().isEmpty())
                && (courseRequest.getCourseTerminRequests() == null || courseRequest.getCourseTerminRequests().isEmpty()))
            throw new CustomException(HttpStatus.CONFLICT, "Моля добавете дати или запазете курса като чернова");
    }

    public void editCourse(String token, int lessonID, CreateCourseRequest courseRequest, boolean isDraft, boolean isPrivateLesson) throws CustomException {
        // TODO Check if the teacher has access to the course with lessonID
        Teacher teacher = teacherRepository.findTeacherByTokens_token(token.substring(7));
        if (!teacher.isVerified()) throw new CustomException(HttpStatus.CONFLICT, "Трябва да се верифицирате първо");
        Lesson lesson = lessonRepository.getLessonByLessonID(lessonID);
        if (!Objects.equals(lesson.getTeacher().getId(), teacher.getId()))
            throw new CustomException(HttpStatus.FORBIDDEN, "Имате достъп само до Вашите курсове");

        if (!isDraft) checkForUnallowedChanges(lesson, courseRequest);

        lesson.setTitle(courseRequest.getTitle());
        lesson.setSubject(courseRequest.getSubject());
        lesson.setGrade(courseRequest.getGrade());
        lesson.setDescription(courseRequest.getDescription());
        if (courseRequest.getImageLocation() == null) {
            lesson.setImageLocation(null);
        } else {
            lesson.setImageLocation(courseRequest.getImageLocation().replace("http://localhost:8080/api/v1/users/images/", ""));
        }
        lesson.setLength(courseRequest.getLength());
        lesson.setPrivateLesson(isPrivateLesson);
        lesson.setPrice(courseRequest.getPrice());
        lesson.setDraft(isDraft);
        if (!isPrivateLesson) {
            //TODO Find alternative to removal
            lesson.setStudentsUpperBound(courseRequest.getStudentsUpperBound());
            List<CourseTermin> courseTermins = new ArrayList<>();
            if (lesson.isHasTermins()) {
                courseTermins = lesson.getCourseTermins();
                for (CourseTermin termin : courseTermins) {
                    if (termin.isEmpty()) {
                        themaRepository.deleteAll(termin.getThemas());
                    }
                }
            }
            themaRepository.deleteAll(lesson.getThemas());
            if (lesson.isDraft()) {
                terminRepo.deleteAll(lesson.getTermins());
                lesson.removeAllTermins();
                lesson.setHasTermins(false);
            }
            else {
                boolean hasTermins = false;
                for (CourseTermin termin : courseTermins) {
                    if (termin.isEmpty()) {
                        courseTerminRepo.delete(termin);
                        lesson.removeTermin(termin);
                    }
                    else {
                        courseRequest.getCourseTerminRequests().removeIf(courseTerminRequest ->
                                Objects.equals(courseTerminRequest.getStartDate(), termin.getDate())
                                        && Objects.equals(courseTerminRequest.getTime(), termin.getTime()));
                        hasTermins = true;
                    }
                }
                lesson.setHasTermins(hasTermins);
            }
            List<Thema> themas = new ArrayList<>();
            if (courseRequest.getThemas() != null && courseRequest.getThemas().length > 0) {
                for (ThemaSimpleResponse themaData : courseRequest.getThemas()) {
                    Thema thema;
                    if (themaData.getDescription() == null) {
                        thema = Thema.builder().title(themaData.getTitle()).build();
                    } else
                        thema = Thema.builder().description(themaData.getDescription()).title(themaData.getTitle()).build();
                    themas.add(thema);
                    thema.setLesson(lesson);
                    themaRepository.save(thema);
                }
            }
            lesson.setThemas(themas);
            if (courseRequest.getCourseTerminRequests() != null && !courseRequest.getCourseTerminRequests().isEmpty()) {
                for (CourseTerminRequestResponse courseTerminRequest : courseRequest.getCourseTerminRequests()) {
                    List<Thema> themas1 = new ArrayList<>();
                    if (courseRequest.getThemas() != null && courseRequest.getThemas().length > 0) {
                        for (ThemaSimpleResponse themaData : courseRequest.getThemas()) {
                            Thema thema;
                            if (themaData.getDescription() == null) {
                                thema = Thema.builder().title(themaData.getTitle()).build();
                            } else
                                thema = Thema.builder().description(themaData.getDescription()).title(themaData.getTitle()).build();
                            themas1.add(thema);
                            themaRepository.save(thema);
                        }
                    }
                    CourseTermin courseTermin = CourseTermin.builder().dateTime(Timestamp.valueOf(courseTerminRequest.getStartDate()
                                    + " " + courseTerminRequest.getCourseHours() + ":00"))
                            .courseDays(Arrays.toString(courseTerminRequest.getCourseDaysNumbers()))
                            .courseHoursNumber(Integer.parseInt(courseTerminRequest.getCourseHours().replace(":", "")))
                            .weekLength(courseTerminRequest.getWeekLength()).studentsUpperBound(courseRequest.getStudentsUpperBound())
                            .lesson(lesson).lessonStatus(LessonStatus.NOT_STARTED).build();
                    courseTerminRepo.save(courseTermin);
                    for (Thema thema : themas1) {
                        thema.setCourseTermin(courseTermin);
                        themaRepository.save(thema);
                        courseTermin.addThema(thema);
                    }
                    courseTerminRepo.save(courseTermin);
                    lesson.addTermin(courseTermin);
                    lesson.setHasTermins(true);
                }
                lesson.getTermins().sort(Comparator.comparing(Termin::getDateTime));
                lessonRepository.save(lesson);
            }
        } else {
            lesson.setUpperGrade(courseRequest.getUpperGrade());
            List<LessonTermin> lessonTermins = new ArrayList<>();
            if (lesson.isHasTermins()) {
                lessonTermins = lesson.getLessonTermins();
                for (LessonTermin termin : lessonTermins) {
                    if (termin.isEmpty()) themaRepository.delete(termin.getThema());
                }
            }
            lesson.setStudentsUpperBound(1);
            if (lesson.isDraft()) {
                terminRepo.deleteAll(lesson.getTermins());
                lesson.removeAllTermins();
                lesson.setHasTermins(false);
            }
            else {
                boolean hasTermins = false;
                for (LessonTermin termin : lessonTermins) {
                    if (termin.isEmpty()) {
                        lessonTerminRepo.delete(termin);
                        lesson.removeTermin(termin);
                    }
                    else {
                        for (LessonTerminRequest lessonTerminRequest : courseRequest.getPrivateLessonTermins()){
                            if (Objects.equals(lessonTerminRequest.getDate(), termin.getDate())) {
                                lessonTerminRequest.getLessonHours().removeIf(timepair -> Objects.equals(timepair.getTime(), termin.getTime()));
                                if (lessonTerminRequest.getLessonHours().isEmpty()) {
                                    courseRequest.getPrivateLessonTermins().remove(lessonTerminRequest);
                                }
                            }
                        }
                        hasTermins = true;
                    }
                }
                lesson.setHasTermins(hasTermins);
            }
            themaRepository.deleteAll(lesson.getThemas());
            Thema thema = null;
            if (courseRequest.getThemas() != null && courseRequest.getThemas().length > 0 && courseRequest.getThemas()[0] != null) {
                if (courseRequest.getThemas()[0].getDescription() == null) {
                    thema = Thema.builder().title(courseRequest.getThemas()[0].getTitle()).build();
                } else {
                    thema = Thema.builder().description(courseRequest.getThemas()[0].getDescription())
                            .title(courseRequest.getThemas()[0].getTitle()).build();
                }
                thema.setLesson(lesson);
                themaRepository.save(thema);
            }
            List<Thema> themas = new ArrayList<>();
            themas.add(thema);
            lesson.setThemas(themas);
            if (courseRequest.getPrivateLessonTermins() != null && !courseRequest.getPrivateLessonTermins().isEmpty()) {
                for (LessonTerminRequest privateLessonTermin : courseRequest.getPrivateLessonTermins()) {
                    for (TimePair timePair : privateLessonTermin.getLessonHours()) {
                        Thema thema1 = null;
                        if (courseRequest.getThemas() != null && courseRequest.getThemas().length > 0 && courseRequest.getThemas()[0] != null) {
                            if (courseRequest.getThemas()[0].getDescription() == null) {
                                thema1 = Thema.builder().title(courseRequest.getThemas()[0].getTitle()).build();
                            } else {
                                thema1 = Thema.builder().description(courseRequest.getThemas()[0].getDescription())
                                        .title(courseRequest.getThemas()[0].getTitle()).build();
                            }
                            themaRepository.save(thema1);
                        }
                        String hours = timePair.getTime();
                        LessonTermin lessonTermin = LessonTermin.builder().lessonHours(Integer.parseInt(hours.replace(":", "")))
                                .dateTime(Timestamp.valueOf(privateLessonTermin.getDate() + " " + hours + ":00")).thema(thema1)
                                .lessonStatus(LessonStatus.NOT_STARTED).build();
                        lessonTerminRepo.save(lessonTermin);
                        lessonTermin.setLesson(lesson);
                        lesson.addTermin(lessonTermin);
                        lesson.setHasTermins(true);
                        if (thema != null) {
                            thema.setLessonTermin(lessonTermin);
                            themaRepository.save(thema);
                        }
                    }
                }
                lesson.getTermins().sort(Comparator.comparing(Termin::getDateTime));
                lessonRepository.save(lesson);
            }
        }
        if (!isDraft) {
            if (lesson.isPrivateLesson()) addNewPriceLesson(lesson.getPrice());
            else addNewPriceCourse(lesson.getPrice());
        }
    }

    private String getDaysOfWeek(int[] days) throws CustomException {
        StringBuilder daysOfWeek = new StringBuilder();
        for (int i : days) {
            switch (i) {
                case 1 -> daysOfWeek.append("Ponedelnik, ");
                case 2 -> daysOfWeek.append("Vtornik, ");
                case 3 -> daysOfWeek.append("Srqda, ");
                case 4 -> daysOfWeek.append("Chetvurtuk, ");
                case 5 -> daysOfWeek.append("Petuk, ");
                case 6 -> daysOfWeek.append("Subota, ");
                case 7 -> daysOfWeek.append("Nedelq, ");
                default -> throw new CustomException(HttpStatus.BAD_REQUEST, "Days of the week are 1-7");
            }
        }
        return daysOfWeek.toString().substring(0, daysOfWeek.length() - 3);
    }

    //TODO Maybe add separate checks for private lessons and courses
    private void checkForUnallowedChanges(Lesson lesson, CreateCourseRequest courseRequest) throws CustomException {
        boolean violations = (courseRequest.getCourseTerminRequests() == null || courseRequest.getCourseTerminRequests().isEmpty())
                && (courseRequest.getPrivateLessonTermins() == null || courseRequest.getPrivateLessonTermins().isEmpty())
                && lesson.getTermins().isEmpty() || (lesson.getTitle() == null || lesson.getTitle().isEmpty())
                && (courseRequest.getTitle() == null || courseRequest.getTitle().isEmpty())
//                || !EnumUtils.isValidEnum(Subject.class, lesson.getSubject()) && !EnumUtils.isValidEnum(Subject.class, courseRequest.getSubject())
                || (lesson.getDescription() == null || lesson.getDescription().isEmpty())
                && (courseRequest.getDescription() == null || courseRequest.getDescription().isEmpty())
//                || lesson.getImageLocation().isEmpty() && courseRequest.getImageLocation().isEmpty()
                || lesson.getPrice() == 0 && courseRequest.getPrice() == 0;
        if (violations) throw new CustomException(HttpStatus.BAD_REQUEST, "Please use the interface to send requests");
    }

    public void removeDraft(int lessonID) {
        var lesson = lessonRepository.getLessonByLessonID(lessonID);
        terminRepo.deleteAll(lesson.getTermins());
        lesson.getTeacher().removeLesson(lesson);
        lessonRepository.delete(lesson);
    }

    public void removeTermin(int terminID, int lessonID) throws CustomException {
        var termin = terminRepo.getTerminByTerminID(terminID);
        if (!termin.isEmpty()) throw new CustomException(HttpStatus.FORBIDDEN, "Login link");
        var lesson = lessonRepository.getLessonByLessonID(lessonID);
        lesson.removeTermin(termin);
        terminRepo.deleteById(terminID);
    }

    public List<String> getImagesBySubject(String subject) throws CustomException {
        File[] files = new File("src/main/resources/images/" + subject).listFiles();
        if (files == null || files.length == 0) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "Нямаме снимки за този предмет все още");
        }
        List<String> fileNames = new ArrayList<>();
        for (File file : files) {
            String path = file.getPath();
            if (path.endsWith(".jpg") || path.endsWith(".png") || path.endsWith(".jpeg")) {
                fileNames.add("http://localhost:8080/api/v1/users/images/" + path.replace("\\", "/"));
            }
        }
        return fileNames;
    }

    //TODO Add draft publishing conditions and course creation conditions for the presence of an image

    public HomePageResponse getHomePageInfo(String token) throws CustomException {
        // TODO Maybe find fix for drafts not to be shown
        List<Lesson> privateLessons = lessonRepository.findTop12ByIsDraftFalseAndIsPrivateLessonTrueOrderByPopularityDesc();
        List<Lesson> courses = lessonRepository.findTop12ByIsDraftFalseAndIsPrivateLessonFalseOrderByPopularityDesc();
        // Add file reader and links to the courses
        HomePageResponse homePageResponse = new HomePageResponse();

        List<Lesson> likedLessons = new ArrayList<>();
        if (token != null) {
            Student student = studentRepository.findStudentByTokens_token(token.substring(7));
            if (student != null) {
                likedLessons = student.getFavouriteLessons();
            }
        }
        List<LessonResponse> lessonResponses = new ArrayList<>();
        List<LessonResponse> courseResponses = new ArrayList<>();
        // Courses
        for (Lesson lesson : courses) {
            List<CourseTermin> termins;
            LessonResponse lessonResponse;
            termins = lesson.getCourseTermins();
            lessonResponse = new LessonResponse(lesson, termins.get(0).getDate(), termins.get(0).getTime(),
                    termins.get(0).getStudentsUpperBound() - termins.get(0).getPlacesRemaining());
            int weekLength = lesson.getCourseTermins().get(0).getWeekLength();
            String[] days = termins.get(0).getCourseDays().split(",");
            lessonResponse.setWeekLength(weekLength);
            lessonResponse.setPricePerHour(Math.round(lesson.getPrice() / (days.length * weekLength) * 100.0) / 100.0);
            for (Lesson lesson1 : likedLessons) {
                if (Objects.equals(lesson1.getLessonID(), lesson.getLessonID())) {
                    lessonResponse.setLikedByStudent(true);
                    break;
                }
            }
            courseResponses.add(lessonResponse);
        }
        // Lessons
        for (Lesson lesson : privateLessons) {
            List<LessonTermin> termins2;
            LessonResponse lessonResponse;
                termins2 = lesson.getLessonTermins();
                lessonResponse = new LessonResponse(lesson, termins2.get(0).getDate(), termins2.get(0).getTime(), 0);
                lessonResponse.setPricePerHour(Math.round(lesson.getPrice() * 100.0) / 100.0);
            for (Lesson lesson1 : likedLessons) {
                if (Objects.equals(lesson1.getLessonID(), lesson.getLessonID())) {
                    lessonResponse.setLikedByStudent(true);
                    break;
                }
            }
            lessonResponses.add(lessonResponse);
        }

        List<ReviewResponse> reviewResponses = new ArrayList<>();
        for (Review review : reviewRepo.getDistinct3ByOrderByRatingDescMessageDesc()) {
            ReviewResponse reviewResponse = new ReviewResponse(review);
            reviewResponses.add(reviewResponse);
        }

        homePageResponse.setPopularCourseResponse(courseResponses);
        homePageResponse.setPopularLessonsResponse(lessonResponses);
        homePageResponse.setReviewsResponse(reviewResponses);
        return homePageResponse;
    }

    public void leaveReview(String token, ReviewRequest reviewRequest) throws CustomException {
        //TODO Change from Teacher to Student!
        Lesson lesson = lessonRepository.getLessonByLessonID(reviewRequest.getLessonId());
        Student student = studentRepository.findStudentByTokens_token(token.substring(7));
        if (reviewRequest.getRating() < 1 || reviewRequest.getRating() > 5) {
            throw new CustomException(HttpStatus.FORBIDDEN, "Рейтингът трябва да е между 1 и 5");
        }
        if (reviewRequest.getMessage() == null || reviewRequest.getMessage().isEmpty()) {
            throw new CustomException(HttpStatus.FORBIDDEN, "Моля напишете и отзив във формата на текст");
        }
        Teacher teacher = lesson.getTeacher();
        Review review = Review.builder().dateTime(Timestamp.valueOf(LocalDateTime.now())).lesson(lesson).message(reviewRequest.getMessage())
                .rating(reviewRequest.getRating()).studentName(student.getFirstname()).studentSurname(student.getLastname())
                .teacher(teacher).build();
        reviewRepo.save(review);
        lesson.leaveReview(review);
        lessonRepository.save(lesson);
        teacher.leaveReview(review);
        teacherRepository.save(teacher);
    }

    public FilterResponse getFilters(boolean isPrivateLesson) {
        List<String> subjects = lessonRepository.getAllSubjects();
        List<String> grades = lessonRepository.getAllGrades();
        subjects.remove(null);
        grades.remove(null);
        double[] prices;
        if (isPrivateLesson) {
            prices = new double[]{BOTTOM20PRICE_LESSON, BOTTOM40PRICE_LESSON,
                    (LOWERMEDIAN_PRICE_LESSON + UPPERMEDIAN_PRICE_LESSON) / 2, BOTTOM60PRICE_LESSON, BOTTOM80PRICE_LESSON};
        } else {
            prices = new double[]{BOTTOM20PRICE_COURSE,
                    BOTTOM40PRICE_COURSE, (LOWERMEDIAN_PRICE_COURSE + UPPERMEDIAN_PRICE_COURSE) / 2, BOTTOM60PRICE_COURSE, BOTTOM80PRICE_COURSE};
        }
        Arrays.sort(prices);
        return new FilterResponse(subjects, grades, prices);
    }

    public FilterResponse getSubjectGrade() {
        List<String> subjects = new ArrayList<>();
        for (Subject subject : Subject.values()) {
            subjects.add(subject.toString());
        }
        List<String> grades = new ArrayList<>();
        for (Grade grade : Grade.values()) {
            grades.add(grade.toString());
        }
        FilterResponse filterResponse = new FilterResponse();
        filterResponse.setGrades(grades);
        filterResponse.setSubjects(subjects);
        return filterResponse;
    }

    public String[] getPicturesByGrade(String token, String grade) {
        String[] pictures = new String[5];
        for (int i = 0; i < 5; i++) {
            pictures[i] = grade + "_" + i;
        }
        return pictures;
    }

    public PagedResponse getFilteredLessons(FilterRequest request, String token) throws IllegalArgumentException, CustomException {
        List<LessonResponse> lessonResponses = new ArrayList<>();
        Pageable sortedAndPaged;
        String sort = request.getSort();
        if (sort == null) sort = "";
        switch (sort) {
            case "Lowest price" ->
                    sortedAndPaged = PageRequest.of(request.getPageNumber() - 1, 12, Sort.by("lesson.price").descending());
            case "Highest rating" ->
                    sortedAndPaged = PageRequest.of(request.getPageNumber() - 1, 12, Sort.by("lesson.rating").descending());
            case "Starting soonest" ->
                    sortedAndPaged = PageRequest.of(request.getPageNumber() - 1, 12, Sort.by("dateTime").ascending());
            default ->
                    sortedAndPaged = PageRequest.of(request.getPageNumber() - 1, 12, Sort.by("lesson.popularity").descending());
        }
        if (request.getPriceLowerBound() >= 0 && request.getPriceUpperBound() == 0) {
            request.setPriceUpperBound(10000);
            request.setPriceLowerBound(0);
        }
        int hoursUpperBound;
        int hoursLowerBound;
        if (request.getHoursUpperBound() == null) hoursUpperBound = 2400;
        else {
            hoursUpperBound = Integer.parseInt(request.getHoursUpperBound().replace(":", ""));
        }
        if (request.getHoursLowerBound() == null) hoursLowerBound = 0;
        else {
            hoursLowerBound = Integer.parseInt(request.getHoursLowerBound().replace(":", ""));
        }
        if (request.getLowerBound() == null)
            request.setLowerBound(String.valueOf(Timestamp.valueOf(LocalDateTime.now())));
        else request.setLowerBound(request.getLowerBound() + " 00:00:00");
        if (request.getUpperBound() == null)
            request.setUpperBound(String.valueOf(new Timestamp(System.currentTimeMillis() + 31556926000L)));
        else request.setUpperBound(request.getUpperBound() + " 23:59:59");
        //TODO maybe add subject/grade recognition in searchTerm
        Page<Lesson> lessons;
        int weekLength = -1;
        if (request.isPrivateLesson()) {
            if (request.getSearchTerm() != null && request.getSearchTerm().contains(" ")) {
                String[] searchTerms = request.getSearchTerm().split("");
                lessons = lessonTerminRepo.getFilteredLessonTermins(request.getSearchTerm(), searchTerms[0], searchTerms[1],
                        request.getSubject(), false, request.getGrade(), request.getPriceLowerBound(), request.getPriceUpperBound(),
                        hoursLowerBound, hoursUpperBound, Timestamp.valueOf(request.getLowerBound()),
                        Timestamp.valueOf(request.getUpperBound()), false, true, sortedAndPaged);
            } else {
                lessons = lessonTerminRepo.getFilteredLessonTermins(request.getSearchTerm(), request.getSearchTerm(), request.getSearchTerm(),
                        request.getSubject(), false, request.getGrade(), request.getPriceLowerBound(), request.getPriceUpperBound(),
                        hoursLowerBound, hoursUpperBound, Timestamp.valueOf(request.getLowerBound()),
                        Timestamp.valueOf(request.getUpperBound()), false, true, sortedAndPaged);
            }
            if (lessons.isEmpty()) {
                lessons = lessonTerminRepo.getFilteredLessonTermins(null, null, null,
                        request.getSubject(), false, request.getGrade(), request.getPriceLowerBound(), request.getPriceUpperBound(),
                        hoursLowerBound, hoursUpperBound, Timestamp.valueOf(request.getLowerBound()),
                        Timestamp.valueOf(request.getUpperBound()), false, true, sortedAndPaged);
            }
        } else {
            if (request.getSearchTerm() != null && request.getSearchTerm().contains(" ")) {
                String[] searchTerms = request.getSearchTerm().split("");
                lessons = courseTerminRepo.getFilteredCourseTermins(request.getSearchTerm(), searchTerms[0], searchTerms[1],
                        request.getSubject(), false, request.getGrade(), request.getPriceLowerBound(), request.getPriceUpperBound(),
                        hoursLowerBound, hoursUpperBound, Timestamp.valueOf(request.getLowerBound()),
                        Timestamp.valueOf(request.getUpperBound()), false, false, sortedAndPaged);
            } else {
                lessons = courseTerminRepo.getFilteredCourseTermins(request.getSearchTerm(), request.getSearchTerm(), request.getSearchTerm(),
                        request.getSubject(), false, request.getGrade(), request.getPriceLowerBound(), request.getPriceUpperBound(),
                        hoursLowerBound, hoursUpperBound, Timestamp.valueOf(request.getLowerBound()),
                        Timestamp.valueOf(request.getUpperBound()), false, false, sortedAndPaged);
            }
            if (lessons.isEmpty()) {
                lessons = courseTerminRepo.getFilteredCourseTermins(null, null, null,
                        request.getSubject(), false, request.getGrade(), request.getPriceLowerBound(), request.getPriceUpperBound(),
                        hoursLowerBound, hoursUpperBound, Timestamp.valueOf(request.getLowerBound()),
                        Timestamp.valueOf(request.getUpperBound()), false, false, sortedAndPaged);

            }
            weekLength = 0;
        }

        List<Lesson> likedLessons = new ArrayList<>();
        if (token != null) {
            Student student = studentRepository.findStudentByTokens_token(token.substring(7));
            if (student != null) {
                likedLessons = student.getFavouriteLessons();
            }
        }
        for (Lesson lesson : lessons) {
            List<CourseTermin> termins;
            List<LessonTermin> termins2;
            LessonResponse lessonResponse;
            if (!lesson.isPrivateLesson()) {
                termins = lesson.getCourseTermins();
                lessonResponse = new LessonResponse(lesson, termins.get(0).getDate(), termins.get(0).getTime(),
                        termins.get(0).getStudentsUpperBound() - termins.get(0).getPlacesRemaining());
                lessonResponse.setWeekLength(lesson.getCourseTermins().get(0).getWeekLength());
                int weekLength1 = lesson.getCourseTermins().get(0).getWeekLength();
                String[] days = termins.get(0).getCourseDays().split(",");
                lessonResponse.setWeekLength(weekLength1);
                lessonResponse.setPricePerHour(Math.round(lesson.getPrice() / (days.length * weekLength1) * 100.0) / 100.0);
            } else {
                termins2 = lesson.getLessonTermins();
                lessonResponse = new LessonResponse(lesson, termins2.get(0).getDate(), termins2.get(0).getTime(), 0);
                lessonResponse.setPricePerHour(Math.round(lesson.getPrice() * 100.0) / 100.0);
            }
            for (Lesson lesson1 : likedLessons) {
                if (Objects.equals(lesson1.getLessonID(), lesson.getLessonID())) {
                    lessonResponse.setLikedByStudent(true);
                    break;
                }
            }
            lessonResponses.add(lessonResponse);
        }
        return new PagedResponse(lessons.getTotalElements(), 12, lessonResponses, null);
    }

    public List<LessonResponse> getLessonById(int id, String token) throws CustomException {
//TODO do lesson page for logged user   Teacher teacher = teacherRepository.findTeacherByTokens_token(token.substring(7));
        var lesson = lessonRepository.getLessonByLessonID(id);
        LessonResponse lessonResponse;
        PagedResponse reviews = getLessonReviews(id, "", 1);
        List<Lesson> likedLessons = new ArrayList<>();
        List<Teacher> likedTeachers = new ArrayList<>();
        if (token != null) {
            Student student = studentRepository.findStudentByTokens_token(token.substring(7));
            if (student != null) {
                likedLessons = lessonRepository.getLessonByIsLikedByStudent(student.getId());
                likedTeachers = teacherRepository.getTeacherByIsLikedByStudent(student.getId());
            }
        }
        if (lesson.isPrivateLesson()) {
            List<LessonTermin> lessonTermins = lesson.getLessonTermins();
            List<LessonTerminResponse> lessonTerminResponses = new ArrayList<>();
            for (LessonTermin lessonTermin : lessonTermins) {
                Timestamp timestamp = Timestamp.valueOf(Instant.ofEpochMilli(lessonTermin.getDateTime().getTime()
                        + lesson.getLength() * 60000L).atZone(ZoneId.systemDefault()).toLocalDateTime());
                lessonTerminResponses.add(new LessonTerminResponse(lessonTermin.getTerminID(), lessonTermin.getDate(),
                        lessonTermin.getTime() + " - " + timestamp.toString().substring(11, 16), lesson.getLength()));
            }
            lessonResponse = new LessonResponse(lesson, lessonTerminResponses, reviews.getReviewResponses());
            lessonResponse.setPricePerHour(Math.round(lessonResponse.getPrice() * 100.0) / 100.0);
        } else {
            lessonResponse = new LessonResponse(lesson, reviews.getReviewResponses());
        }
        //TODO Check if somewhere else the heart for an added to favourites teacher needs to be updated like here so
        for (Teacher teacher : likedTeachers) {
            if (Objects.equals(lesson.getTeacher().getId(), teacher.getId())) {
                lessonResponse.getTeacherResponse().setLikedByStudent(true);
                break;
            }
        }
        for (Lesson lesson2 : likedLessons) {
            if (Objects.equals(lesson2.getLessonID(), lesson.getLessonID())) lessonResponse.setLikedByStudent(true);
        }
        List<LessonResponse> lessonResponses = new ArrayList<>();
        lessonResponses.add(lessonResponse);
        for (Lesson lesson1 : lessonRepository.getSimilarLessons(lesson.getSubject(), lessonResponse.getGrade())) {
            if (Objects.equals(lesson1.getLessonID(), lesson.getLessonID())) continue;
            if (lesson1.isPrivateLesson()) {
                List<LessonTermin> lessonTermins = lesson1.getLessonTermins();
                lessonResponses.add(new LessonResponse(lesson1, lessonTermins.get(0).getDate(), lessonTermins.get(0).getTime(), 0));
            } else {
                List<CourseTermin> courseTermins = lesson1.getCourseTermins();
                lessonResponses.add(new LessonResponse(lesson1, courseTermins.get(0).getDate(), courseTermins.get(0).getTime(),
                        courseTermins.get(0).getStudentsUpperBound() - courseTermins.get(0).getPlacesRemaining()));
                lessonResponse.setWeekLength(courseTermins.get(0).getWeekLength());
            }
            for (Lesson lesson2 : likedLessons) {
                if (Objects.equals(lesson2.getLessonID(), lesson1.getLessonID())) {
                    lessonResponse.setLikedByStudent(true);
                    break;
                }
            }
            lessonResponses.add(lessonResponse);
        }
        return lessonResponses;
    }

    public LessonResponse getLessonByTerminId(int id) throws CustomException {
//TODO do lesson page for logged user   Teacher teacher = teacherRepository.findTeacherByTokens_token(token.substring(7));
        var lesson = lessonRepository.getLessonByTerminId(id);
        LessonResponse lessonResponse;
        if (lesson.isPrivateLesson()) {
            LessonTermin lessonTermin = lessonTerminRepo.getLessonTerminByTerminID(id);
            List<LessonTerminResponse> lessonTerminResponses = new ArrayList<>();
                Timestamp timestamp = Timestamp.valueOf(Instant.ofEpochMilli(lessonTermin.getDateTime().getTime()
                        + lesson.getLength() * 60000L).atZone(ZoneId.systemDefault()).toLocalDateTime());
                lessonTerminResponses.add(new LessonTerminResponse(lessonTermin.getTerminID(), lessonTermin.getDate(),
                        lessonTermin.getTime() + " - " + timestamp.toString().substring(11, 16), lesson.getLength()));
            lessonResponse = new LessonResponse(lesson, lessonTerminResponses, null);
            lessonResponse.setPricePerHour(Math.round(lessonResponse.getPrice() * 100.0) / 100.0);
        } else {
            CourseTermin courseTermin = courseTerminRepo.getCourseTerminByTerminID(id);
            lessonResponse = new LessonResponse(lesson, courseTermin);
        }
        return lessonResponse;
    }

    public List<CourseTerminRequestResponse> addDate(CourseTerminRequestResponse courseRequest, int id, String token) throws CustomException {
        //TODO Check if the teacher has access to the course with id
        Teacher teacher = teacherRepository.findTeacherByTokens_token(token.substring(7));
        if (teacher == null)
            throw new CustomException(HttpStatus.NOT_FOUND, "Няма намерен учител с този тоукън, моля логнете се");
        Lesson lesson = lessonRepository.getLessonByLessonID(id);
        if (lesson.isPrivateLesson()) {
            String hours = courseRequest.getCourseHours();
            LessonTermin lessonTermin = LessonTermin.builder().lessonHours(Integer.parseInt(hours.replace(":", "")))
                    .dateTime(Timestamp.valueOf(courseRequest.getStartDate() + " " + hours + ":00"))
                    .lessonStatus(LessonStatus.NOT_STARTED).build();
            lessonTerminRepo.save(lessonTermin);
            lesson.setHasTermins(true);
            lesson.addTermin(lessonTermin);
            if (!lesson.getThemas().isEmpty() && lesson.getThemas() != null) {
                Thema thema = lesson.getThemas().get(0);
                lessonTermin.setThema(thema);
                lessonTerminRepo.save(lessonTermin);
                thema.setLessonTermin(lessonTermin);
                themaRepository.save(thema);
            }
        } else {
            //TODO Decide about zones in datetimes
            ZonedDateTime zonedDateTime = ZonedDateTime.parse(courseRequest.getStartDate() + "T" + courseRequest.getCourseHours() + ":00.000+02:00[Europe/Paris]");
            Timestamp timestamp = Timestamp.from(zonedDateTime.toInstant());
            CourseTermin courseTermin = CourseTermin.builder().dateTime(timestamp)
                    .courseDays(Arrays.toString(courseRequest.getCourseDaysNumbers()))
                    .courseHoursNumber(Integer.parseInt(courseRequest.getCourseHours().replace(":", "")))
                    .weekLength(courseRequest.getWeekLength()).studentsUpperBound(courseRequest.getStudentsUpperBound())
                    .lesson(lesson).placesRemaining(courseRequest.getStudentsUpperBound()).lessonStatus(LessonStatus.NOT_STARTED).build();
            courseTerminRepo.save(courseTermin);
            List<Thema> themas = lesson.getThemas();
            for (Thema thema : themas) {
                Thema thema1 = new Thema();
                thema1.setDescription(thema.getDescription());
                thema1.setTitle(thema.getDescription());
                thema1.setCourseTermin(courseTermin);
                themaRepository.save(thema1);
                courseTermin.addThema(thema1);
            }
            courseTerminRepo.save(courseTermin);
            lesson.setHasTermins(true);
            lesson.addTermin(courseTermin);
        }
        lessonRepository.save(lesson);
        return getCourseTerminsTeacher(token, id);
    }

    //TODO Remove past courses from info (maybe active dates as well>)
    public LessonResponse getCourseInformation(int id, String token) throws CustomException {
        //TODO Check if the teacher has access to the course with id
        Teacher teacher = teacherRepository.findTeacherByTokens_token(token.substring(7));
        if (teacher == null)
            throw new CustomException(HttpStatus.NOT_FOUND, "Няма намерен учител с този тоукън, моля логнете се");
        var lesson = lessonRepository.getLessonByLessonID(id);
        LessonResponse lessonResponse;
        if (lesson.isPrivateLesson()) {
//            ThemaSimpleResponse thema = null;
//            if (lesson.getThemas() != null && !lesson.getThemas().isEmpty()) {
//                thema = new ThemaSimpleResponse(lesson.getThemas().get(0).getTitle(), lesson.getThemas().get(0).getDescription());
//            }
            if (lesson.isHasTermins()) {
                List<LessonTermin> lessonTermins = lesson.getLessonTermins();
                List<LessonTerminResponse> lessonTerminResponses = new ArrayList<>();
                int dayOfMonth = -1;
                int counter = 1;
                int lessonTerminsSize = lessonTermins.size();
                List<TimePair> timePairs = new ArrayList<>();
                String lessonDate = lessonTermins.get(0).getDate();
                for (LessonTermin lessonTermin : lessonTermins) {
                    //TODO Maybe add a month check as well
                    //TODO Maybe Iterator would be faster
                    //TODO Maybe filter this at database
                    if (lessonTermin.getDateTime().before(new Timestamp(System.currentTimeMillis()))) {
                        continue;
                    }
                    int currentDayOfMonth = lessonTermin.getDateTime().toLocalDateTime().getDayOfMonth();
                    Timestamp timestamp = Timestamp.valueOf(Instant.ofEpochMilli(lessonTermin.getDateTime().getTime()
                            + lesson.getLength() * 60000L).atZone(ZoneId.systemDefault()).toLocalDateTime());
                    TimePair timePair = new TimePair(lessonTermin.getTerminID(), lessonTermin.getTime(),
                            lessonTermin.getTime() + " - " + timestamp.toString().substring(11, 16), lessonTermin.isFull());

                    if ((dayOfMonth == -1 || currentDayOfMonth == dayOfMonth) && counter != lessonTerminsSize) {
                        timePairs.add(timePair);
                        dayOfMonth = currentDayOfMonth;
                    } else if (currentDayOfMonth != dayOfMonth) {
                        LessonTerminResponse lessonTerminResponse = LessonTerminResponse.builder().date(lessonDate)
                                .lessonHours(timePairs).build();
                        timePairs = new ArrayList<>();
                        dayOfMonth = currentDayOfMonth;
                        lessonTerminResponses.add(lessonTerminResponse);
                        timePairs.add(timePair);
                    }
                    lessonDate = lessonTermin.getDate();
                    if (counter == lessonTerminsSize) {
                        if (!timePairs.contains(timePair)) {
                            timePairs.add(timePair);
                        }
                        LessonTerminResponse lessonTerminResponse = LessonTerminResponse.builder().date(lessonDate)
                                .lessonHours(timePairs).build();
                        lessonTerminResponses.add(lessonTerminResponse);
                    }
                    counter++;
                }
                lessonResponse = new LessonResponse(lesson, lessonTerminResponses, null);
                lessonResponse.setTeacherResponse(null);
            } else {
                lessonResponse = new LessonResponse(lesson, null, null);
                lessonResponse.setTeacherResponse(null);
            }
            lessonResponse.setPricePerHour(Math.round(lessonResponse.getPrice() * 100.0) / 100.0);
        } else {
            lessonResponse = new LessonResponse(lesson, new ArrayList<>());
            lessonResponse.setTeacherResponse(null);
        }
        return lessonResponse;
    }

    public void reportCourse(int id, String title, String description, String token, boolean isPrivateLesson) throws CustomException {
        Student student = studentRepository.findStudentByTokens_token(token.substring(7));
        if (student == null)
            throw new CustomException(HttpStatus.NOT_FOUND, "Няма намерен ученик с този тоукън, моля логнете се");
        String email;
        if (isPrivateLesson) {
            LessonTermin lessonTermin = lessonTerminRepo.getLessonTerminByTerminID(id);
            if (lessonTermin == null)
                throw new CustomException(HttpStatus.NOT_FOUND, "Търсената инстанция на урока не е намерена");
            email = lessonTermin.getLesson().getTeacher().getEmail();
        } else {
            CourseTermin courseTermin = courseTerminRepo.getCourseTerminByTerminID(id);
            if (courseTermin == null)
                throw new CustomException(HttpStatus.NOT_FOUND, "Търсената инстанция на урока не е намерена");
            email = courseTermin.getLesson().getTeacher().getEmail();
        }
        EmailDetails emailDetails = new EmailDetails();
        emailDetails.setRecipient("kaloyan.enev@gmail.com");
        emailDetails.setSubject("Report for courseTermin: " + id + " from: " + student.getEmail());
        emailDetails.setMsgBody("Title: " + title + " \n Description: " + description + "\n Teacher email: " + email);
        emailService.sendSimpleMail(emailDetails);
    }

    public ClassroomPageResponse getClassroomPage(String token, int terminId, boolean isPrivateLesson, boolean isTeacher) throws CustomException {
        Teacher teacher = null;
        Student student = null;
        if (isTeacher) {
            teacher = teacherRepository.findTeacherByTokens_token(token.substring(7));
            if (teacher == null)
                throw new CustomException(HttpStatus.NOT_FOUND, "Няма намерен учител с този тоукън, моля логнете се");
        } else {
            student = studentRepository.findStudentByTokens_token(token.substring(7));
            if (student == null)
                throw new CustomException(HttpStatus.NOT_FOUND, "Няма намерен ученик с този тоукън, моля логнете се");
        }
        CourseTermin courseTermin;
        LessonTermin lessonTermin;
        Lesson lesson;
        List<ThemaResponse> themas = new ArrayList<>();
        List<UserProfileResponse> students = new ArrayList<>();
        ClassroomPageResponse classroomPageResponse;
        if (isPrivateLesson) {
            lessonTermin = lessonTerminRepo.getLessonTerminByTerminID(terminId);
            if (lessonTermin == null)
                throw new CustomException(HttpStatus.NOT_FOUND, "Търсената инстанция на урока не е намерена");
            lesson = lessonTermin.getLesson();
            if (isTeacher) {
                if (!Objects.equals(lesson.getTeacher().getId(), teacher.getId()))
                    throw new CustomException(HttpStatus.CONFLICT, "Имате достъп само до вашите уроци");
            } else {
                if (lessonTermin.getStudent() == null || !Objects.equals(lessonTermin.getStudent().getId(), student.getId()))
                    throw new CustomException(HttpStatus.CONFLICT, "Имате достъп само до вашите уроци");
            }
            Thema thema = lessonTermin.getThema();
            Assignment assignment = thema.getAssignment();
            ThemaResponse themaResponse;
            if (assignment != null) {
                themaResponse = new ThemaResponse(thema.getThemaID(), thema.getLinkToRecording(), thema.getLinkToRecording(),
                        thema.getPresentation(), assignment.getAssignmentID(), assignment.getStudents().size(), assignment.getSolutions().size(),
                        thema.getTitle(), thema.getDescription());
            } else {
                themaResponse = ThemaResponse.builder().themaID(thema.getThemaID()).linkToRecording(thema.getLinkToRecording())
                        .presentation(thema.getPresentation()).title(thema.getTitle()).description(thema.getDescription())
                        .build();
            }
            themas.add(themaResponse);
            //TODO Maybe add link to getPictureLocation
            //TODO fix all calls to database that happen more than once for the same stuff
            String teacherName = null;
            if (isTeacher && lessonTermin.getStudent() != null) {
                Student terminStudent = lessonTermin.getStudent();
                students.add(new UserProfileResponse(terminStudent.getId(), terminStudent.getFirstname(),
                        terminStudent.getLastname(), terminStudent.getPictureLocation()));
            } else {
                students = null;
                teacher = lesson.getTeacher();
                teacherName = teacher.getFirstname() + " " + teacher.getLastname();
            }
            //endTime
            Timestamp timestamp = Timestamp.valueOf(Instant.ofEpochMilli(lessonTermin.getDateTime().getTime()
                    + lesson.getLength() * 60000L).atZone(ZoneId.systemDefault()).toLocalDateTime());
            classroomPageResponse = ClassroomPageResponse.builder().lessonTitle(lesson.getTitle())
                    .lessonDescription(lesson.getDescription()).courseHours(lessonTermin.getTime() + " - " + timestamp.toString().substring(11, 16))
                    .startDate(lessonTermin.getDate()).teacherId(teacher.getId())
                    .themas(themas).courseTerminId(lessonTermin.getTerminID())
                    .enrolledStudents(1).students(students).teacherName(teacherName).build();
        } else {
            courseTermin = courseTerminRepo.getCourseTerminByTerminID(terminId);
            if (courseTermin == null)
                throw new CustomException(HttpStatus.NOT_FOUND, "Търсената инстанция на урока не е намерена");
            lesson = courseTermin.getLesson();
            String teacherName = null;
            if (isTeacher) {
                if (!Objects.equals(lesson.getTeacher().getId(), teacher.getId()))
                    throw new CustomException(HttpStatus.CONFLICT, "Имате достъп само до вашите уроци");
                for (Student terminStudent : courseTermin.getEnrolledStudents()) {
                    students.add(new UserProfileResponse(terminStudent.getId(), terminStudent.getFirstname(),
                            terminStudent.getLastname(), terminStudent.getPictureLocation()));
                }
            } else {
                boolean isInCourse = false;
                for (Student terminStudent : courseTermin.getEnrolledStudents()) {
                    if (Objects.equals(terminStudent.getId(), student.getId())) {
                        isInCourse = true;
                        break;
                    }
                }
                if (!isInCourse) throw new CustomException(HttpStatus.CONFLICT, "Имате достъп само до вашите уроци");
                students = null;
                teacher = lesson.getTeacher();
                teacherName = teacher.getFirstname() + " " + teacher.getLastname();
            }
            for (Thema thema : courseTermin.getThemas()) {
                Assignment assignment = thema.getAssignment();
                ThemaResponse themaResponse;
                if (assignment != null) {
                    themaResponse = new ThemaResponse(thema.getThemaID(), thema.getLinkToRecording(), thema.getLinkToRecording(),
                            thema.getPresentation(), assignment.getAssignmentID(), assignment.getStudents().size(), assignment.getSolutions().size(),
                            thema.getTitle(), thema.getDescription());
                } else {
                    themaResponse = ThemaResponse.builder().themaID(thema.getThemaID()).linkToRecording(thema.getLinkToRecording())
                            .presentation(thema.getPresentation()).title(thema.getTitle()).description(thema.getDescription())
                            .build();
                }
                themas.add(themaResponse);
            }
            String endDate = (new Timestamp(courseTermin.getDateTime().getTime() + (long) courseTermin.getWeekLength() * 7 * 86400000).toString()).substring(0, 10);
            //endTime
            Timestamp timestamp = Timestamp.valueOf(Instant.ofEpochMilli(courseTermin.getDateTime().getTime()
                    + lesson.getLength() * 60000L).atZone(ZoneId.systemDefault()).toLocalDateTime());
            String[] daysString = courseTermin.getCourseDays().replaceFirst("\\[", "").replaceFirst("]", "")
                    .replace(" ", "").split(",");
            int[] days = new int[daysString.length];
            for (int i = 0; i < daysString.length; i++) {
                days[i] = Integer.parseInt(String.valueOf(daysString[i]));
            }
            classroomPageResponse = ClassroomPageResponse.builder().lessonTitle(lesson.getTitle())
                    .lessonDescription(lesson.getDescription()).courseHours(courseTermin.getTime() + " - " + timestamp.toString().substring(11, 16))
                    .startDate(courseTermin.getDate()).courseDaysNumbers(days).teacherId(teacher.getId())
                    .enrolledStudents(courseTermin.getStudentsUpperBound() - courseTermin.getPlacesRemaining()).endDate(endDate)
                    .themas(themas).courseTerminId(courseTermin.getTerminID()).students(students).teacherName(teacherName).build();
        }
        return classroomPageResponse;
    }

    public PagedResponse getStudentAll(String token, LessonRequest lessonRequest, String sort) throws ClassCastException, CustomException {
        //TODO maybe find better implementation
        Student student = studentRepository.findStudentByTokens_token(token.substring(7));
        LessonStatus lessonStatus = null;
        if (Objects.equals(lessonRequest.getSort(), "Upcoming")) lessonStatus = LessonStatus.NOT_STARTED;
        else if (Objects.equals(lessonRequest.getSort(), "Active")) lessonStatus = LessonStatus.STARTED;
        else if (Objects.equals(lessonRequest.getSort(), "Inactive")) lessonStatus = LessonStatus.FINISHED;

        if (sort.equals("Lessons")) return getStudentPrivateLessons(lessonRequest, student, lessonStatus);
        else if (sort.equals("Courses")) return getStudentCourses(lessonRequest, student, lessonStatus);

        List<LessonResponse> lessonResponses = new ArrayList<>();
        List<LessonTermin> lessonTermins;
        List<CourseTermin> courseTermins;
        if (lessonRequest.getSort() == null || Objects.equals(lessonRequest.getSort(), "")
                || Objects.equals(lessonRequest.getSort(), "all")) {
            lessonTermins = lessonTerminRepo.getLessonTerminsByStudent_Id(student.getId());
            courseTermins = courseTerminRepo.getCourseTerminsByEnrolledStudents_id(student.getId());
        } else {
            lessonTermins = lessonTerminRepo.getLessonTerminsByStudent_IdAndLessonStatus(student.getId(), lessonStatus);
            courseTermins = courseTerminRepo.getCourseTerminsByEnrolledStudents_idAndLessonStatus(student.getId(), lessonStatus);
        }
        int lessonTerminCounter = 0;
        int lessonsLength = lessonTermins.size() - 1;
        int elementCounter = 0;
        boolean nextCourseTermin = true;
        CourseTermin courseTermin = null;
        for (Iterator<CourseTermin> iterator = courseTermins.iterator(); iterator.hasNext()
                && elementCounter < lessonRequest.getPage() * 12; ) {

            if (nextCourseTermin) {
                courseTermin = iterator.next();
            }
            nextCourseTermin = true;
            LessonResponse lessonResponse;
            if (lessonTerminCounter > lessonsLength) {
                if (elementCounter >= lessonRequest.getPage() * 12 - 12) {
                    CourseTerminRequestResponse courseTerminRequestResponse = new CourseTerminRequestResponse(courseTermin);
                    Lesson lesson = courseTermin.getLesson();
                    Teacher teacher = lesson.getTeacher();
                    lessonResponse = new LessonResponse(lesson.getLessonID(), lesson.getImageLocation(), lesson.getTitle(), lesson.isPrivateLesson(),
                            teacher.getFirstname(), teacher.getLastname(), courseTermin.getLessonStatus().toString(), courseTerminRequestResponse, teacher.getId());
                    lessonResponse.setLength(lesson.getLength());
                    lessonResponses.add(lessonResponse);
                }
                continue;
            }
            LessonTermin lessonTermin = lessonTermins.get(lessonTerminCounter);
            if (courseTermin.getDateTime().before(lessonTermin.getDateTime())) {
                if (elementCounter >= lessonRequest.getPage() * 12 - 12) {
                    CourseTerminRequestResponse courseTerminRequestResponse = new CourseTerminRequestResponse(courseTermin);
                    Lesson lesson = courseTermin.getLesson();
                    Teacher teacher = lesson.getTeacher();
                    lessonResponse = new LessonResponse(lesson.getLessonID(), lesson.getImageLocation(), lesson.getTitle(), lesson.isPrivateLesson(),
                            teacher.getFirstname(), teacher.getLastname(), courseTermin.getLessonStatus().toString(), courseTerminRequestResponse, teacher.getId());
                    lessonResponse.setLength(lesson.getLength());
                    lessonResponses.add(lessonResponse);
                }
            } else {
                if (elementCounter >= lessonRequest.getPage() * 12 - 12) {
                    Lesson lesson = lessonTermin.getLesson();
                    Teacher teacher = lesson.getTeacher();
                    lessonResponse = new LessonResponse(lesson.getLessonID(), lesson.getImageLocation(),
                            lesson.getTitle(), lesson.isPrivateLesson(),
                            teacher.getFirstname(), teacher.getLastname(), lessonTermin.getLessonStatus().toString(),
                            lessonTermin.getDate(), lessonTermin.getTime() + " - "
                            + new Timestamp(lessonTermin.getDateTime().getTime() + lesson.getLength() * 60000L).toString().substring(11, 16),
                            teacher.getId(), lessonTermin.getTerminID());
                    lessonTerminCounter++;
                    lessonResponse.setLength(lesson.getLength());
                    lessonResponses.add(lessonResponse);
                }
                nextCourseTermin = false;
            }
            elementCounter++;
        }
        while (lessonTerminCounter <= lessonsLength && elementCounter < lessonRequest.getPage() * 12) {
            if (elementCounter >= lessonRequest.getPage() * 12 - 12) {
                LessonResponse lessonResponse;
                LessonTermin lessonTermin = lessonTermins.get(lessonTerminCounter);
                Lesson lesson = lessonTermin.getLesson();
                Teacher teacher = lesson.getTeacher();
                lessonResponse = new LessonResponse(lesson.getLessonID(), lesson.getTitle(), lesson.getImageLocation(),
                        lesson.isPrivateLesson(),
                        teacher.getFirstname(), teacher.getLastname(), lessonTermin.getLessonStatus().toString(),
                        lessonTermin.getDate(), lessonTermin.getTime() + " - "
                        + new Timestamp(lessonTermin.getDateTime().getTime() + lesson.getLength() * 60000L).toString().substring(11, 16),
                        teacher.getId(), lessonTermin.getTerminID());
                lessonResponse.setLength(lesson.getLength());
                lessonResponses.add(lessonResponse);
            }
            lessonTerminCounter++;
            elementCounter++;
        }
        return new PagedResponse((long) lessonTermins.size() + courseTermins.size(), 12, lessonResponses, null);
    }

    private PagedResponse getStudentPrivateLessons(LessonRequest lessonRequest, Student student, LessonStatus lessonStatus) {
        List<LessonResponse> lessonResponses = new ArrayList<>();
        int elementCounter = 0;
        List<LessonTermin> lessonTermins;
        if (lessonRequest.getSort() == null || Objects.equals(lessonRequest.getSort(), "")
                || Objects.equals(lessonRequest.getSort(), "all")) {
            lessonTermins = lessonTerminRepo.getLessonTerminsByStudent_Id(student.getId());
        } else {
            lessonTermins = lessonTerminRepo.getLessonTerminsByStudent_IdAndLessonStatus(student.getId(), lessonStatus);
        }
        for (LessonTermin lessonTermin : lessonTermins) {
            if (elementCounter >= lessonRequest.getPage() * 12) {
                break;
            }
            if (elementCounter >= lessonRequest.getPage() * 12 - 12) {
                Lesson lesson = lessonTermin.getLesson();
                Teacher teacher = lesson.getTeacher();
                LessonResponse lessonResponse = new LessonResponse(lesson.getLessonID(), lesson.getImageLocation(),
                        lesson.getTitle(), lesson.isPrivateLesson(),
                        teacher.getFirstname(), teacher.getLastname(), lessonTermin.getLessonStatus().toString(),
                        lessonTermin.getDate(), lessonTermin.getTime(), teacher.getId(), lessonTermin.getTerminID());

                lessonResponses.add(lessonResponse);
            }
            elementCounter++;
        }
        return new PagedResponse(lessonTermins.size(), 12, lessonResponses, null);
    }

    private PagedResponse getStudentCourses(LessonRequest lessonRequest, Student student, LessonStatus lessonStatus) {
        List<LessonResponse> lessonResponses = new ArrayList<>();
        List<CourseTermin> courseTermins;
        int elementCounter = 0;
        if (lessonRequest.getSort() == null || Objects.equals(lessonRequest.getSort(), "")
                || Objects.equals(lessonRequest.getSort(), "all")) {
            courseTermins = courseTerminRepo.getCourseTerminsByEnrolledStudents_id(student.getId());
        } else {
            courseTermins = courseTerminRepo.getCourseTerminsByEnrolledStudents_idAndLessonStatus(student.getId(), lessonStatus);
        }
        for (CourseTermin courseTermin : courseTermins) {
            if (elementCounter >= lessonRequest.getPage() * 12) {
                break;
            }
            if (elementCounter >= lessonRequest.getPage() * 12 - 12) {
                CourseTerminRequestResponse courseTerminRequestResponse = new CourseTerminRequestResponse(courseTermin);
                Lesson lesson = courseTermin.getLesson();
                Teacher teacher = lesson.getTeacher();
                LessonResponse lessonResponse = new LessonResponse(lesson.getLessonID(), lesson.getImageLocation(),
                        lesson.getTitle(), lesson.isPrivateLesson(), teacher.getFirstname(), teacher.getLastname(),
                        courseTermin.getLessonStatus().toString(), courseTerminRequestResponse, teacher.getId());
                lessonResponses.add(lessonResponse);
            }
            elementCounter++;
        }
        return new PagedResponse(courseTermins.size(), 12, lessonResponses, null);
    }

    public PagedResponse getFavouriteCourses(String token, String sort, int pageNumber) throws ClassCastException, CustomException {
        Student student = studentRepository.findStudentByTokens_token(token.substring(7));
        List<LessonResponse> lessonResponses = new ArrayList<>();
        Pageable sortedAndPaged;
        boolean sortByCourse = false;
        switch (sort) {
            case "Most popular" ->
                    sortedAndPaged = PageRequest.of(pageNumber - 1, 8, Sort.by("popularity").descending());
            case "Most expensive" -> sortedAndPaged = PageRequest.of(pageNumber - 1, 8, Sort.by("price").descending());
            case "Cheapest" -> sortedAndPaged = PageRequest.of(pageNumber - 1, 8, Sort.by("price").ascending());
            case "Highest rating" -> sortedAndPaged = PageRequest.of(pageNumber - 1, 8, Sort.by("rating").descending());
//            case "Starting soonest" -> {
//                sortedAndPaged = PageRequest.of(pageNumber - 1, 8, Sort.by("dateTime").ascending());
//                sortByCourse = true;
//            }
            case "Newest" -> {
                List<Lesson> lessons = student.getFavouriteLessons();
                for (int i = (pageNumber - 1) * 8; i < pageNumber * 8 && i < lessons.size(); i++) {
                    Lesson lesson = lessons.get(i);
                    List<CourseTermin> termins;
                    List<LessonTermin> termins2;
                    LessonResponse lessonResponse;
                    if (!lesson.isPrivateLesson()) {
                        termins = lesson.getCourseTermins();
                        lessonResponse = new LessonResponse(lesson, termins.get(0).getDate(), termins.get(0).getTime(),
                                termins.get(0).getStudentsUpperBound() - termins.get(0).getPlacesRemaining());
                        lessonResponse.setWeekLength(lesson.getCourseTermins().get(0).getWeekLength());
                    } else {
                        termins2 = lesson.getLessonTermins();
                        lessonResponse = new LessonResponse(lesson, termins2.get(0).getDate(), termins2.get(0).getTime(), 0);
                    }
                    lessonResponse.setLikedByStudent(true);
                    lessonResponses.add(lessonResponse);
                }
                return new PagedResponse(lessons.size(), 8, lessonResponses, null);
            }
            default -> {
                sortedAndPaged = PageRequest.of(pageNumber - 1, 8);
                sortByCourse = true;
            }
        }
        Page<Lesson> lessons;
        if (sortByCourse) {
            lessons = lessonRepository.getLessonByIsLikedByStudentOrderByDateTime(student.getId(), sortedAndPaged);
        } else {
            lessons = lessonRepository.getLessonByisLikedByStudent_id(student.getId(), sortedAndPaged);
        }
        for (Lesson lesson : lessons) {
            List<CourseTermin> termins;
            List<LessonTermin> termins2;
            LessonResponse lessonResponse;
            if (!lesson.isPrivateLesson()) {
                termins = lesson.getCourseTermins();
                lessonResponse = new LessonResponse(lesson, termins.get(0).getDate(), termins.get(0).getTime(),
                        termins.get(0).getStudentsUpperBound() - termins.get(0).getPlacesRemaining());
                lessonResponse.setWeekLength(lesson.getCourseTermins().get(0).getWeekLength());
            } else {
                termins2 = lesson.getLessonTermins();
                lessonResponse = new LessonResponse(lesson, termins2.get(0).getDate(), termins2.get(0).getTime(), 0);
            }
            lessonResponse.setLikedByStudent(true);
            lessonResponses.add(lessonResponse);
        }
        return new PagedResponse(lessons.getTotalElements(), 8, lessonResponses, null);
    }

    public List<LessonResponse> getTeacherLessons(String token, String lessonStatus, boolean privateLessons, boolean upcoming) throws ClassCastException, CustomException {
        Teacher teacher = teacherRepository.findTeacherByTokens_token(token.substring(7));
        List<Lesson> lessons = teacher.getLessons();
        List<LessonResponse> lessonResponses = new ArrayList<>();
        for (Lesson lesson : lessons) {
            if (upcoming) {
                if (lesson.isHasTermins() && !lesson.isDraft()) {
                    fillLessonResponseList(lessonResponses, lesson, "Active");
                }
            } else if (lesson.isPrivateLesson() == privateLessons) {
                switch (lessonStatus) {
                    case "All":
                        if (lesson.isDraft()) {
                            if (lesson.isHasTermins()) {
                                fillLessonResponseList(lessonResponses, lesson, "Draft");
                            } else {
                                LessonResponse lessonResponse = new LessonResponse(lesson, "", "", 0);
                                lessonResponse.setStatus("Draft");
                                lessonResponses.add(lessonResponse);
                            }
                        } else if (lesson.isHasTermins()) fillLessonResponseList(lessonResponses, lesson, "Active");
                        else {
                            LessonResponse lessonResponse = new LessonResponse(lesson, "", "", 0);
                            lessonResponse.setStatus("Inactive");
                            lessonResponses.add(lessonResponse);
                        }
                        break;
                    case "Draft":
                        if (!lesson.isDraft()) continue;
                        if (!lesson.isHasTermins()) {
                            LessonResponse lessonResponse = new LessonResponse(lesson, "", "", 0);
                            lessonResponse.setStatus("Draft");
                            lessonResponses.add(lessonResponse);
                        } else {
                            fillLessonResponseList(lessonResponses, lesson, "Draft");
                        }
                        break;
                    case "Inactive":
                        if (lesson.isHasTermins() || lesson.isDraft()) continue;
                        LessonResponse lessonResponse = new LessonResponse(lesson, "", "", 0);
                        lessonResponse.setStatus("Inactive");
                        lessonResponses.add(lessonResponse);
                        break;
                    case "Active":
                        if (lesson.isHasTermins() && !lesson.isDraft()) fillLessonResponseList(lessonResponses, lesson, "Active");
                        break;
                    default:
                        throw new CustomException(HttpStatus.BAD_REQUEST, "Моля изберете някой от предложените статуси през интерфейса");
                }
            }
        }
        return lessonResponses;
    }

    public void editDescription(String description, int themaId, String token) throws CustomException {
        //TODO check if thema belongs to teacher
        Teacher teacher = teacherRepository.findTeacherByTokens_token(token.substring(7));
        if (teacher == null)
            throw new CustomException(HttpStatus.NOT_FOUND, "Няма намерен учител с този тоукън, моля логнете се");
        Thema thema = themaRepository.getThemaByThemaID(themaId);
        thema.setDescription(description);
        themaRepository.save(thema);
    }

    public void addLinkToRecording(String linkToRecording, int themaId, String token) throws CustomException {
        //TODO check if thema belongs to teacher
        Teacher teacher = teacherRepository.findTeacherByTokens_token(token.substring(7));
        if (teacher == null)
            throw new CustomException(HttpStatus.NOT_FOUND, "Няма намерен учител с този тоукън, моля логнете се");
        Thema thema = themaRepository.getThemaByThemaID(themaId);
        thema.setLinkToRecording(linkToRecording);
        themaRepository.save(thema);
    }

    public String getLinkToRecording(int themaId, String token) throws CustomException {
        //TODO check if thema belongs to teacher
        Teacher teacher = teacherRepository.findTeacherByTokens_token(token.substring(7));
        if (teacher == null)
            throw new CustomException(HttpStatus.NOT_FOUND, "Няма намерен учител с този тоукън, моля логнете се");
        Thema thema = themaRepository.getThemaByThemaID(themaId);
        return thema.getLinkToRecording();
    }

    public void deleteCourse(int id, String token) throws CustomException {
        Teacher teacher = teacherRepository.findTeacherByTokens_token(token.substring(7));
        if (teacher == null)
            throw new CustomException(HttpStatus.NOT_FOUND, "Няма намерен учител с този тоукън, моля логнете се");
        Lesson lesson = lessonRepository.getLessonByLessonID(id);
        if (lesson == null) throw new CustomException(HttpStatus.NOT_FOUND, "Няма намерен курс с това Id");
        if (!Objects.equals(lesson.getTeacher().getId(), teacher.getId()))
            throw new CustomException(HttpStatus.FORBIDDEN,
                    "Нямате достъп до този курс");
//        if (lesson.isPrivateLesson()) {
//            List<LessonTermin> lessonTermins = lesson.getLessonTermins();
//            for (LessonTermin lessonTermin : lessonTermins) {
//                if (!lessonTermin.isEmpty()) throw new CustomException(HttpStatus.CONFLICT,
//                        "За някоя инстанция от този курс вече има записани ученици, така че не може да го изтриете");
//            }
//        }
//        else {
//            List<CourseTermin> courseTermins = lesson.getCourseTermins();
//            for (CourseTermin courseTermin : courseTermins) {
//                if (!courseTermin.isEmpty()) throw new CustomException(HttpStatus.CONFLICT,
//                        "За някоя инстанция от този курс вече има записани ученици, така че не може да го изтриете");
//            }
//        }
//        teacher.removeLesson(lesson);
//        lesson.setDraft(true);
        if (!lesson.isDraft()) throw new CustomException(HttpStatus.CONFLICT,
                "Може да триете само чернови");
        List<Thema> themas = lesson.getThemas();
        boolean themasNotNull = false;
        if (themas != null && !themas.isEmpty()) {
            themaRepository.deleteAll(themas);
            themasNotNull = true;
        }
        if (lesson.isHasTermins()) {
            List<CourseTermin> courseTermins;
            List<LessonTermin> lessonTermins;
            if (lesson.isPrivateLesson()) {
                lessonTermins = lesson.getLessonTermins();
                if (themasNotNull) {
                    for (LessonTermin lessonTermin : lessonTermins) {
                        themaRepository.delete(lessonTermin.getThema());
                    }
                }
                lessonTerminRepo.deleteAll(lessonTermins);
            } else {
                courseTermins = lesson.getCourseTermins();
                if (themasNotNull) {
                    for (CourseTermin courseTermin : courseTermins) {
                        themaRepository.deleteAll(courseTermin.getThemas());
                    }
                }
                courseTerminRepo.deleteAll(courseTermins);
            }
        }
        lessonRepository.delete(lesson);
    }

    public String addResource(int id, String token, String filename) throws CustomException {
        Teacher teacher = teacherRepository.findTeacherByTokens_token(token.substring(7));
        if (teacher == null)
            throw new CustomException(HttpStatus.NOT_FOUND, "Няма намерен учител с този тоукън, моля логнете се");
        Thema thema = themaRepository.getThemaByThemaID(id);
        if (thema == null) throw new CustomException(HttpStatus.NOT_FOUND, "Няма намерена тема с това id");
        String oldFilename = thema.getPresentation();
        thema.setPresentation(filename);
        themaRepository.save(thema);
        return oldFilename;
    }

    private void fillLessonResponseList(List<LessonResponse> lessonResponses, Lesson lesson, String lessonStatus) throws CustomException {
        List<CourseTermin> termins;
        List<LessonTermin> termins2;
        LessonResponse lessonResponse;
        if (!lesson.isPrivateLesson()) {
            termins = lesson.getCourseTermins();
            lessonResponse = new LessonResponse(lesson, termins.get(0).getDate(), termins.get(0).getTime(),
                    termins.get(0).getStudentsUpperBound() - termins.get(0).getPlacesRemaining());
            lessonResponse.setWeekLength(lesson.getCourseTermins().get(0).getWeekLength());
            lessonResponse.setNumberOfTermins(termins.size());
        } else {
            termins2 = lesson.getLessonTermins();
            lessonResponse = new LessonResponse(lesson, termins2.get(0).getDate(), termins2.get(0).getTime(), 0);
            lessonResponse.setNumberOfTermins(termins2.size());
        }

        lessonResponse.setStatus(lessonStatus);
        lessonResponses.add(lessonResponse);
    }

    public List<CourseTerminRequestResponse> getCourseTerminsTeacher(String token, int lessonId) throws CustomException {
        Teacher teacher = teacherRepository.findTeacherByTokens_token(token.substring(7));
        if (teacher == null)
            throw new CustomException(HttpStatus.NOT_FOUND, "Няма намерен учител с този тоукън, моля логнете се");
        List<CourseTermin> courseTermins = courseTerminRepo.getCourseTerminsByLessonID(lessonId);
        List<CourseTerminRequestResponse> courseTerminRequestResponses = new ArrayList<>();
        for (CourseTermin courseTermin : courseTermins) {
            courseTerminRequestResponses.add(new CourseTerminRequestResponse(courseTermin, courseTermin.getLessonStatus(),
                    courseTermin.getLesson().getLength()));
        }
        return courseTerminRequestResponses;
    }

    public List<LessonTerminResponse> getLessonTerminsTeacher(String token, int lessonId) throws ClassCastException, CustomException {
        Teacher teacher = teacherRepository.findTeacherByTokens_token(token.substring(7));
        if (teacher == null)
            throw new CustomException(HttpStatus.NOT_FOUND, "Няма намерен учител с този тоукън, моля логнете се");
        List<LessonTermin> lessonTermins = lessonTerminRepo.getLessonTerminsByLessonID(lessonId);
        List<LessonTerminResponse> lessonTerminResponses = new ArrayList<>();
        if (lessonTermins.isEmpty()) return lessonTerminResponses;
        int dayOfMonth = -1;
        int counter = 1;
        int lessonTerminsSize = lessonTermins.size();
        List<TimePair> timePairs = new ArrayList<>();
        String lessonDate = lessonTermins.get(0).getDate();
        for (LessonTermin lessonTermin : lessonTermins) {
            //TODO Maybe add a month check as well
            int currentDayOfMonth = lessonTermin.getDateTime().toLocalDateTime().getDayOfMonth();
            TimePair timePair = new TimePair(lessonTermin.getTerminID(),
                    lessonTermin.getTime() + " - " + new Timestamp(lessonTermin.getDateTime().getTime()
                            + lessonTermin.getLesson().getLength() * 60000L).toString().substring(11, 16),
                    null, lessonTermin.isFull());
            if ((dayOfMonth == -1 || currentDayOfMonth == dayOfMonth) && counter != lessonTerminsSize) {
                timePairs.add(timePair);
                dayOfMonth = currentDayOfMonth;
            } else if (currentDayOfMonth != dayOfMonth) {
                LessonTerminResponse lessonTerminResponse = LessonTerminResponse.builder().date(lessonDate).lessonHours(timePairs)
                        .dayOfTheWeek(lessonTermin.getDateTime().toLocalDateTime().getDayOfWeek().toString())
                        .status(lessonTermin.getLessonStatus().toString()).build();
                timePairs = new ArrayList<>();
                dayOfMonth = currentDayOfMonth;
                lessonTerminResponses.add(lessonTerminResponse);
                timePairs.add(timePair);
            }
            lessonDate = lessonTermin.getDate();
            if (counter == lessonTerminsSize) {
                if (!timePairs.contains(timePair)) {
                    timePairs.add(timePair);
                }
                LessonTerminResponse lessonTerminResponse = LessonTerminResponse.builder().date(lessonDate)
                        .lessonHours(timePairs).build();
                lessonTerminResponses.add(lessonTerminResponse);
            }
            counter++;
        }
        return lessonTerminResponses;
    }

    public PagedResponse getLessonReviews(int lessonId, String sort, int pageNumber) throws ClassCastException {
        Pageable sortedAndPaged;
        switch (sort) {
            case "Newest" -> sortedAndPaged = PageRequest.of(pageNumber - 1, 12, Sort.by("dateTime").descending());
            case "Oldest" -> sortedAndPaged = PageRequest.of(pageNumber - 1, 12, Sort.by("dateTime").ascending());
            case "Highest rating" ->
                    sortedAndPaged = PageRequest.of(pageNumber - 1, 12, Sort.by("rating").descending());
            case "Lowest rating" -> sortedAndPaged = PageRequest.of(pageNumber - 1, 12, Sort.by("rating").ascending());
            default -> sortedAndPaged = PageRequest.of(pageNumber - 1, 12, Sort.by("dateTime").descending());
        }
        Page<Review> reviews = reviewRepo.getByLesson_lessonID(lessonId, sortedAndPaged);
        List<ReviewResponse> reviewResponses = new ArrayList<>();
        for (Review review : reviews) {
            reviewResponses.add(new ReviewResponse(review));
        }
        return new PagedResponse(reviews.getTotalElements(), 12, null, reviewResponses);
    }

    public Integer addAssignment(AssignmentRequest assignmentRequest, String token, int id) throws CustomException {
        Teacher teacher = teacherRepository.findTeacherByTokens_token(token.substring(7));
        if (teacher == null)
            throw new CustomException(HttpStatus.NOT_FOUND, "Няма намерен учител с този тоукън, моля логнете се");
        Thema thema = themaRepository.getThemaByThemaID(id);
        if (thema == null) throw new CustomException(HttpStatus.NOT_FOUND, "Няма намерена тема с това id");
        CourseTermin courseTermin = thema.getCourseTermin();
        if (thema.getAssignment() != null) {
            throw new CustomException(HttpStatus.CONFLICT, "Вече има качено домашно");
        }
        List<Student> students = new ArrayList<>();
        Assignment assignment;
        if (courseTermin != null) {
            students = courseTermin.getEnrolledStudents();
            assignment = Assignment.builder().students(students).title(assignmentRequest.getTitle()).assignmentLocation(assignmentRequest.getFileNames())
                    .description(assignmentRequest.getDescription()).dueDateTime(Timestamp.valueOf(assignmentRequest.getDate()
                            + " " + assignmentRequest.getTime() + ":00")).teacher(teacher).thema(thema).students(new ArrayList<>())
                    .altTime(assignmentRequest.getAltTime()).build();
            assignmentRepo.save(assignment);
            courseTerminRepo.save(courseTermin);
        } else {
            LessonTermin lessonTermin = thema.getLessonTermin();
            students.add(lessonTermin.getStudent());
            assignment = Assignment.builder().students(students).title(assignmentRequest.getTitle()).assignmentLocation(assignmentRequest.getFileNames())
                    .description(assignmentRequest.getDescription()).dueDateTime(Timestamp.valueOf(assignmentRequest.getDate()
                            + " " + assignmentRequest.getTime() + ":00")).teacher(teacher).thema(thema).students(new ArrayList<>())
                    .altTime(assignmentRequest.getAltTime()).build();
            assignmentRepo.save(assignment);
            lessonTerminRepo.save(lessonTermin);
        }
        teacher.addAssignment(assignment);
        teacherRepository.save(teacher);
        for (Student student : students) {
            student.addAssignment(assignment);
            studentRepository.save(student);
        }
        return assignment.getAssignmentID();
    }

    public void editAssignment(AssignmentRequest assignmentRequest, String token, int id) throws CustomException {
        Teacher teacher = teacherRepository.findTeacherByTokens_token(token.substring(7));
        if (teacher == null)
            throw new CustomException(HttpStatus.NOT_FOUND, "Няма намерен учител с този тоукън, моля логнете се");
        Assignment assignment = assignmentRepo.getAssignmentByAssignmentID(id);
        if (assignment == null) throw new CustomException(HttpStatus.NOT_FOUND, "Няма намерена задача с това id");
//        List<Student> students = assignment.getStudents();
        //TODO delete old file from assignment
        assignment.setTitle(assignmentRequest.getTitle());
        assignment.setDescription(assignmentRequest.getDescription());
        assignment.setDueDateTime(Timestamp.valueOf(assignmentRequest.getDate() + " " + assignmentRequest.getTime() + ":00"));
        assignment.setAssignmentLocation(assignmentRequest.getFileNames());
        assignment.setAltTime(assignmentRequest.getAltTime());
        assignmentRepo.save(assignment);
//        for (Student student : students) {
//            student.addAssignment(assignment);
//            studentRepository.save(student);
//        }
    }


    public String deleteAssignmentFiles(String token, int id, String paths) throws CustomException {
        System.out.println("Reached service");
        Teacher teacher = teacherRepository.findTeacherByTokens_token(token.substring(7));
        if (teacher == null)
            throw new CustomException(HttpStatus.NOT_FOUND, "Няма намерен учител с този тоукън, моля логнете се");
        Assignment assignment = assignmentRepo.getAssignmentByAssignmentID(id);
        if (assignment == null) throw new CustomException(HttpStatus.NOT_FOUND, "Няма намерена задача с това id");
        String unneededPaths = assignment.getAssignmentLocation();
        assignment.setAssignmentLocation(paths);
        assignmentRepo.save(assignment);
        return unneededPaths;
    }

    public String deleteAssignment(String token, int id) throws CustomException {
        System.out.println("Reached service");
        Teacher teacher = teacherRepository.findTeacherByTokens_token(token.substring(7));
        if (teacher == null)
            throw new CustomException(HttpStatus.NOT_FOUND, "Няма намерен учител с този тоукън, моля логнете се");
        Assignment assignment = assignmentRepo.getAssignmentByAssignmentID(id);
        if (assignment == null) throw new CustomException(HttpStatus.NOT_FOUND, "Няма намерена задача с това id");
        List<Solution> solutions = assignment.getSolutions();
        for (Solution solution : solutions) {
            commentRepo.deleteAll(solution.getComments());
        }
        solutionRepo.deleteAll(solutions);
        String assignmentFile = assignment.getAssignmentLocation();
        assignmentRepo.delete(assignment);
        return assignmentFile;
        //TODO Check if removing this from the students is needed
    }

    public AssignmentResponse getAssignment(String token, int id) throws CustomException {
        Teacher teacher = teacherRepository.findTeacherByTokens_token(token.substring(7));
        if (teacher == null)
            throw new CustomException(HttpStatus.NOT_FOUND, "Няма намерен учител с този тоукън, моля логнете се");
        Assignment assignment = assignmentRepo.getAssignmentByAssignmentID(id);
        if (assignment == null) throw new CustomException(HttpStatus.NOT_FOUND, "Няма намерена задача с това id");
        return AssignmentResponse.builder().title(assignment.getTitle()).id(assignment.getAssignmentID())
                .description(assignment.getDescription()).date(assignment.getDate()).time(assignment.getTime())
                .fileNames(assignment.getAssignmentLocation()).altTime(assignment.getAltTime()).build();
    }

    public AssignmentResponse getAssignmentStudent(String token, int id) throws CustomException {
        Student student = studentRepository.findStudentByTokens_token(token.substring(7));
        if (student == null) {
            throw new CustomException(HttpStatus.NOT_FOUND, "Няма намерен ученик с този тоукън, моля логнете се");
        }
        Assignment assignment = assignmentRepo.getAssignmentByAssignmentID(id);
        if (assignment == null) throw new CustomException(HttpStatus.NOT_FOUND, "Няма намерена задача с това id");
        List<Solution> solutions = assignment.getSolutions();
        Solution studentSolution = null;
        for (Solution solution : solutions) {
            if ((solution.getName() + solution.getSurname()).equals(student.getFirstname() + student.getLastname())) {
                studentSolution = solution;
            }
        }
        AssignmentResponse assignmentResponse = AssignmentResponse.builder().id(assignment.getAssignmentID())
                .title(assignment.getTitle()).description(assignment.getDescription()).date(assignment.getDate())
                .time(assignment.getTime()).fileNames(assignment.getAssignmentLocation()).build();
        if (studentSolution != null) {
            assignmentResponse.setSolutionId(studentSolution.getSolutionID());
            assignmentResponse.setSolutionFileNames(studentSolution.getSolutionFilesLocation());
            List<AssignmentResponse> comments = new ArrayList<>();
            for (Comment comment : studentSolution.getComments()) {
                Teacher teacher = assignment.getTeacher();
                comments.add(AssignmentResponse.builder().comment(comment.getActualComment())
                        .date(comment.getDate()).time(comment.getTime()).id(comment.getCommentID())
                        .teacherName(teacher.getFirstname() + " " + teacher.getLastname()).build());
            }
            assignmentResponse.setComments(comments);
            if (studentSolution.isOverdue()) {
                assignmentResponse.setStatus("Submitted");
            } else {
                assignmentResponse.setStatus("Late");
            }
        } else {
            assignmentResponse.setStatus("Not submitted");
        }
        return assignmentResponse;
    }

    public List<CalendarResponse> getEventsForTheDayTeacher(String token, String date) throws CustomException {
        //TODO Add multiple dates for courses in the calendar
        Teacher teacher = teacherRepository.findTeacherByTokens_token(token.substring(7));
        Timestamp lowerBound = Timestamp.valueOf(date + " 00:00:00");
        Timestamp upperBound = Timestamp.valueOf(date + " 23:59:59");
        List<CalendarResponse> responses = new ArrayList<>();
        for (Lesson lesson : teacher.getLessons()) {
            if (lesson.isDraft()) continue;
            if (!lesson.isPrivateLesson()) {
                for (CourseTermin courseTermin : courseTerminRepo.getLessonTerminsByDateAndTeacherID(teacher.getId(), lowerBound, upperBound)) {
                    CalendarResponse calendarResponse;
                    if (courseTermin.isEmpty()) {
                        calendarResponse = CalendarResponse.builder().title(lesson.getTitle())
                                .startDate(courseTermin.getDate()).startTime(courseTermin.getTime())
                                .start(courseTermin.getDateTime().toString())
                                .endTime(new Timestamp(courseTermin.getDateTime().getTime() + lesson.getLength() * 60000L)
                                        .toString().substring(11, 16)).type("курс")
                                .end(new Timestamp(courseTermin.getDateTime().getTime() + lesson.getLength() * 60000L).toString())
                                .dayOfTheWeek(courseTermin.getDateTime().toLocalDateTime().getDayOfWeek().name())
                                //TODO Change themas
                                .themaTitle(courseTermin.getThemas().get(0).getTitle()).enrolledStudents("0").build();
                    } else {
                        calendarResponse = CalendarResponse.builder().title(lesson.getTitle()).className("hasStudents")
                                .startDate(courseTermin.getDate()).startTime(courseTermin.getTime())
                                .start(courseTermin.getDateTime().toString())
                                .endTime(new Timestamp(courseTermin.getDateTime().getTime() + lesson.getLength() * 60000L)
                                        .toString().substring(11, 16)).type("курс")
                                .end(new Timestamp(courseTermin.getDateTime().getTime() + lesson.getLength() * 60000L).toString())
                                .dayOfTheWeek(courseTermin.getDateTime().toLocalDateTime().getDayOfWeek().name())
                                .enrolledStudents(String.valueOf(courseTermin.getStudentsUpperBound() - courseTermin.getPlacesRemaining()))
                                .themaTitle(courseTermin.getThemas().get(0).getTitle()).build();
                    }
                    responses.add(calendarResponse);
                }
            } else {
                for (LessonTermin lessonTermin : lessonTerminRepo.getLessonTerminsByDateAndTeacherID(teacher.getId(), lowerBound, upperBound)) {
                    CalendarResponse calendarResponse;
                    if (lessonTermin.isEmpty()) {
                        calendarResponse = CalendarResponse.builder().title(lesson.getTitle())
                                .startDate(lessonTermin.getDate()).startTime(lessonTermin.getTime()).start(lessonTermin.getDateTime().toString())
                                .endTime(new Timestamp(lessonTermin.getDateTime().getTime() + lesson.getLength() * 60000L).toString().substring(11, 16))
                                .end(new Timestamp(lessonTermin.getDateTime().getTime() + lesson.getLength() * 60000L).toString())
                                .dayOfTheWeek(lessonTermin.getDateTime().toLocalDateTime().getDayOfWeek().name())
                                .themaTitle(lessonTermin.getThema().getTitle()).type("урок").enrolledStudents("0").build();
                    } else {
                        calendarResponse = CalendarResponse.builder().title(lesson.getTitle()).className("hasStudents")
                                .startDate(lessonTermin.getDate()).startTime(lessonTermin.getTime()).start(lessonTermin.getDateTime().toString())
                                .endTime(new Timestamp(lessonTermin.getDateTime().getTime() + lesson.getLength() * 60000L).toString().substring(11, 16))
                                .end(new Timestamp(lessonTermin.getDateTime().getTime() + lesson.getLength() * 60000L).toString())
                                .dayOfTheWeek(lessonTermin.getDateTime().toLocalDateTime().getDayOfWeek().name())
                                .themaTitle(lessonTermin.getThema().getTitle()).type("урок").enrolledStudents("1").build();
                    }
                    responses.add(calendarResponse);
                }
            }
        }
        return responses;
    }

    public List<CalendarResponse> getEventsForTheDayStudent(String token, String date) throws CustomException {
        //TODO Add multiple dates for courses in the calendar
        Student student = studentRepository.findStudentByTokens_token(token.substring(7));
        Timestamp lowerBound = Timestamp.valueOf(date + " 00:00:00");
        Timestamp upperBound = Timestamp.valueOf(date + " 23:59:59");
        List<CalendarResponse> responses = new ArrayList<>();
        for (CourseTermin courseTermin : courseTerminRepo.getCourseTerminsByDateAndEnrolledStudentID(student.getId(), lowerBound, upperBound)) {
            Lesson lesson = courseTermin.getLesson();
            CalendarResponse calendarResponse = CalendarResponse.builder().title(lesson.getTitle()).className("course")
                    .startDate(courseTermin.getDate()).startTime(courseTermin.getTime()).start(courseTermin.getDateTime().toString())
                    .endTime(new Timestamp(courseTermin.getDateTime().getTime() + lesson.getLength() * 60000L).toString().substring(11, 16))
                    .end(new Timestamp(courseTermin.getDateTime().getTime() + lesson.getLength() * 60000L).toString())
                    .dayOfTheWeek(courseTermin.getDateTime().toLocalDateTime().getDayOfWeek().name()).type("курс")
                    .themaTitle(courseTermin.getThemas().get(0).getTitle()).build();
            responses.add(calendarResponse);
        }
        for (LessonTermin lessonTermin : lessonTerminRepo.getLessonTerminsByDateAndEnrolledStudentID(student.getId(), lowerBound, upperBound)) {
            Lesson lesson = lessonTermin.getLesson();
            CalendarResponse calendarResponse = CalendarResponse.builder().title(lesson.getTitle()).className("privateLesson")
                    .startDate(lessonTermin.getDate()).startTime(lessonTermin.getTime()).start(lessonTermin.getDateTime().toString())
                    .endTime(new Timestamp(lessonTermin.getDateTime().getTime() + lesson.getLength() * 60000L).toString().substring(11, 16))
                    .end(new Timestamp(lessonTermin.getDateTime().getTime() + lesson.getLength() * 60000L).toString())
                    .dayOfTheWeek(lessonTermin.getDateTime().toLocalDateTime().getDayOfWeek().name()).type("урок")
                    .themaTitle(lessonTermin.getThema().getTitle()).build();
            responses.add(calendarResponse);
        }
        for (Assignment assignment : assignmentRepo.getAssignmentsByDateAndEnrolledStudentID(student.getId(), lowerBound, upperBound)) {
            CalendarResponse calendarResponse = CalendarResponse.builder().title(assignment.getTitle()).className("assignment")
                    .startDate(assignment.getDate()).startTime(assignment.getTime()).start(assignment.getDueDateTime().toString())
                    .endTime(assignment.getTime()).end(assignment.getDueDateTime().toString())
                    .dayOfTheWeek(assignment.getDueDateTime().toLocalDateTime().getDayOfWeek().name()).type("домашно")
                    .themaTitle(assignment.getThema().getTitle()).build();
            responses.add(calendarResponse);
        }
        return responses;
    }

    public void getAssignmentFiles(String token, int id, String requestedFile) throws CustomException {
        //TODO Maybe add control if the user has access to the assignment
        Teacher teacher = teacherRepository.findTeacherByTokens_token(token.substring(7));
        if (teacher == null) {
            Student student = studentRepository.findStudentByTokens_token(token.substring(7));
            if (student == null) {
                throw new CustomException(HttpStatus.NOT_FOUND, "Няма намерен учител или ученик с този тоукън, моля логнете се");
            }
        }
        Assignment assignment = assignmentRepo.getAssignmentByAssignmentID(id);
        if (assignment == null) throw new CustomException(HttpStatus.NOT_FOUND, "Няма намерена задача с това id");
        if (assignment.getAssignmentLocation() == null) throw new CustomException(HttpStatus.NOT_FOUND, "Файлът не беше намерен");
    }

    public void uploadSolutionFiles(String token, int id, String paths) throws CustomException {
        System.out.println("Reached service");
        Student student = studentRepository.findStudentByTokens_token(token.substring(7));
        if (student == null)
            throw new CustomException(HttpStatus.NOT_FOUND, "Няма намерен ученик с този тоукън, моля логнете се");
        Assignment assignment = assignmentRepo.getAssignmentByAssignmentID(id);
        if (assignment == null) throw new CustomException(HttpStatus.NOT_FOUND, "Няма намерена задача с това id");
        boolean isOverDue = assignment.getDueDateTime().before(new Timestamp(System.currentTimeMillis()));
        Solution solution = Solution.builder().assignment(assignment).comments(new ArrayList<>())
                .dateTime(new Timestamp(System.currentTimeMillis())).isOverdue(isOverDue).name(student.getFirstname())
                .surname(student.getLastname()).teacherCommentCount(0).solutionFilesLocation(paths).build();
        solutionRepo.save(solution);
    }

    public String getSolutionFiles(String token, int id) throws CustomException {
        Teacher teacher = teacherRepository.findTeacherByTokens_token(token.substring(7));
        if (teacher == null) {
            Student student = studentRepository.findStudentByTokens_token(token.substring(7));
            if (student == null)
                throw new CustomException(HttpStatus.NOT_FOUND, "Няма намерен потребител с този тоукън, моля логнете се");
        }
        Solution solution = solutionRepo.getSolutionBySolutionID(id);
        if (solution == null) throw new CustomException(HttpStatus.NOT_FOUND, "Няма намерено решение с това id");
        String solutionFile = solution.getSolutionFilesLocation();
        if (solutionFile == null)
            throw new CustomException(HttpStatus.NOT_FOUND, "Няма намерени файлове към тази задача");
        return solutionFile;
    }

    public String deleteSolution(String token, int id) throws CustomException {
        Student student = studentRepository.findStudentByTokens_token(token.substring(7));
        if (student == null)
            throw new CustomException(HttpStatus.NOT_FOUND, "Няма намерен ученик с този тоукън, моля логнете се");
        Solution solution = solutionRepo.getSolutionBySolutionID(id);
        if (solution == null) throw new CustomException(HttpStatus.NOT_FOUND, "Няма намерено решение с това id");
        String solutionFile = solution.getSolutionFilesLocation();
        commentRepo.deleteAll(solution.getComments());
        solutionRepo.delete(solution);
        return solutionFile;
    }

        public String getResourceFile(int id) throws CustomException {
//        Teacher teacher = teacherRepository.findTeacherByTokens_token(token.substring(7));
//        if (teacher == null)
//            Student student = studentRepository.
//            throw new CustomException(HttpStatus.NOT_FOUND, "Няма намерен учител с този тоукън, моля логнете се");
        Thema thema = themaRepository.getThemaByThemaID(id);
        if (thema == null) throw new CustomException(HttpStatus.NOT_FOUND, "Няма намерено решение с това id");
        if (thema.getPresentation() == null)
            throw new CustomException(HttpStatus.NOT_FOUND, "Няма намерени ресурси към тази тема");
        return thema.getPresentation();
    }

    public List<AssignmentResponse> checkSolutions(String token, int id) throws CustomException {
        Teacher teacher = teacherRepository.findTeacherByTokens_token(token.substring(7));
        if (teacher == null)
            throw new CustomException(HttpStatus.NOT_FOUND, "Няма намерен учител с този тоукън, моля логнете се");
        Assignment assignment = assignmentRepo.getAssignmentByAssignmentID(id);
        if (assignment == null) throw new CustomException(HttpStatus.NOT_FOUND, "Няма намерена задача с това id");
        List<AssignmentResponse> solutions = new ArrayList<>();
        for (Solution solution : assignment.getSolutions()) {
            String status;
            if (solution.isOverdue()) status = "навреме";
            else status = "закъснял";
            //TODO Change architecture solution must have student reference
            Student searchedStudent = null;
            for (Student student : assignment.getStudents()) {
                if ((student.getFirstname() + student.getLastname()).equals(solution.getName() + solution.getSurname())) {
                    searchedStudent = student;
                }
            }
            AssignmentResponse assignmentResponse = AssignmentResponse.builder().solutionId(solution.getSolutionID())
                    .studentName(solution.getName() + " " + solution.getSurname()).time(solution.getTime())
                    .date(solution.getDate()).status(status).commentAmount(solution.getTeacherCommentCount())
                    .solutionFileNames(solution.getSolutionFilesLocation())
                    .image("http://localhost:8080/api/v1/users/images/" + searchedStudent.getPictureLocation()).build();
            solutions.add(assignmentResponse);
        }
        return solutions;
    }

    public void leaveComment(String token, int id, String actualComment) throws CustomException {
        Teacher teacher = teacherRepository.findTeacherByTokens_token(token.substring(7));
        if (teacher == null)
            throw new CustomException(HttpStatus.NOT_FOUND, "Няма намерен учител с този тоукън, моля логнете се");
        Solution solution = solutionRepo.getSolutionBySolutionID(id);
        if (solution == null) throw new CustomException(HttpStatus.NOT_FOUND, "Няма намерено решение с това id");
        Comment comment = Comment.builder().actualComment(actualComment).solution(solution)
                .dateTime(Timestamp.valueOf(LocalDateTime.now())).build();
        commentRepo.save(comment);
        solution.leaveComment(comment);
        solutionRepo.save(solution);
    }

    public List<AssignmentResponse> getComments(String token, int id) throws CustomException {
        Teacher teacher = teacherRepository.findTeacherByTokens_token(token.substring(7));
        if (teacher == null)
            throw new CustomException(HttpStatus.NOT_FOUND, "Няма намерен учител с този тоукън, моля логнете се");
        Solution solution = solutionRepo.getSolutionBySolutionID(id);
        if (solution == null) throw new CustomException(HttpStatus.NOT_FOUND, "Няма намерено решение с това id");
        List<Comment> comments = solution.getComments();
        List<AssignmentResponse> assignmentResponses = new ArrayList<>();
        for (Comment comment : comments) {
            AssignmentResponse assignmentResponse = AssignmentResponse.builder().date(comment.getDate())
                    .time(comment.getTime()).teacherName(teacher.getFirstname() + " " + teacher.getLastname())
                    .comment(comment.getActualComment()).id(comment.getCommentID())
                    .image("http://localhost:8080/api/v1/users/images/" + teacher.getPictureLocation()).build();
            assignmentResponses.add(assignmentResponse);
        }
        return assignmentResponses;
    }

    public static void addNewPriceCourse(double newPrice) {
        COURSES_COUNT++;
        SUM_PRICE_COURSE += newPrice;
        LOWERMEDIAN_PRICE_COURSE = (COURSES_COUNT % 2 == 1) ? newPrice : ((LOWERMEDIAN_PRICE_COURSE + UPPERMEDIAN_PRICE_COURSE) / 2);
        UPPERMEDIAN_PRICE_COURSE = newPrice;
        double average = SUM_PRICE_COURSE / COURSES_COUNT;
        DEVIATION_COURSE = (COURSES_COUNT == 1) ? Math.abs(average - newPrice) : (DEVIATION_COURSE + Math.abs(average - newPrice))
                / (COURSES_COUNT - 1);
        BOTTOM20PRICE_COURSE = DEVIATION_COURSE * (-0.67) + average;
        BOTTOM40PRICE_COURSE = DEVIATION_COURSE * (-0.26) + average;
        BOTTOM60PRICE_COURSE = DEVIATION_COURSE * (0.26) + average;
        BOTTOM80PRICE_COURSE = DEVIATION_COURSE * (0.67) + average;
    }

    public static void addNewPriceLesson(double newPrice) {
        LESSONS_COUNT++;
        SUM_PRICE_LESSON += newPrice;
        LOWERMEDIAN_PRICE_LESSON = (LESSONS_COUNT % 2 == 1) ? newPrice : ((LOWERMEDIAN_PRICE_LESSON + UPPERMEDIAN_PRICE_LESSON) / 2);
        UPPERMEDIAN_PRICE_LESSON = newPrice;
        double average = SUM_PRICE_LESSON / LESSONS_COUNT;
        DEVIATION_LESSON = (LESSONS_COUNT == 1) ? Math.abs(average - newPrice) : (DEVIATION_LESSON + Math.abs(average - newPrice))
                / (LESSONS_COUNT - 1);
        BOTTOM20PRICE_LESSON = DEVIATION_LESSON * (-0.67) + average;
        BOTTOM40PRICE_LESSON = DEVIATION_LESSON * (-0.26) + average;
        BOTTOM60PRICE_LESSON = DEVIATION_LESSON * (0.26) + average;
        BOTTOM80PRICE_LESSON = DEVIATION_LESSON * (0.67) + average;
    }

    public String generateMeeting(int id, String token) throws CustomException {
        Teacher teacher = teacherRepository.findTeacherByTokens_token(token.substring(7));
        if (teacher == null)
            throw new CustomException(HttpStatus.NOT_FOUND, "Няма намерен учител с този тоукън, моля логнете се");
        LessonTermin termin = lessonTerminRepo.getLessonTerminByTerminID(id);
        CourseTermin courseTermin;
        String meetingId = "meet.jit.si/" + UUID.randomUUID().toString();
        if  (termin == null) {
            courseTermin = courseTerminRepo.getCourseTerminByTerminID(id);
            if (courseTermin == null) {
                throw new CustomException(HttpStatus.NOT_FOUND, "Няма намерена тема с това id");
            }
            ScheduledExecutorService service = Executors.newScheduledThreadPool(1);
            List<String> studentToken = new ArrayList<>();
            courseTermin.setLinkToClassroom(meetingId);
            courseTerminRepo.save(courseTermin);
            for (Student student : courseTermin.getEnrolledStudents()) {
                studentToken.add(userRepository.findLastToken(student.getId()).get(0).getToken());
            }
            NotificationSenderTask notificationTask = new NotificationSenderTask(messagingTemplate,
                    "Вашият курс започва след 5 минути, учителят вече създаде мийтинга. \n" +
                            "Кликнете линка, за да се присъедините:" + meetingId, studentToken, courseTermin.getLesson().getTitle());
            service.schedule(notificationTask, courseTermin.getDateTime().getTime() - System.currentTimeMillis(),
                    TimeUnit.MILLISECONDS);
        }
        else {
            ScheduledExecutorService service = Executors.newScheduledThreadPool(1);
            List<String> studentToken = new ArrayList<>();
            termin.setLinkToClassroom(meetingId);
            lessonTerminRepo.save(termin);
            studentToken.add(userRepository.findLastToken(termin.getStudent().getId()).get(0).getToken());
            NotificationSenderTask notificationTask = new NotificationSenderTask(messagingTemplate,
                    "Вашият урок започва след 5 минути, учителят вече създаде мийтинга. \n" +
                            "Кликнете линка, за да се присъедините:" + meetingId, studentToken, termin.getLesson().getTitle());
            service.schedule(notificationTask, termin.getDateTime().getTime() - System.currentTimeMillis(),
                    TimeUnit.MILLISECONDS);
        }
        return meetingId;
    }

    public void deleteComment(String token, int id) throws CustomException {
        Teacher teacher = teacherRepository.findTeacherByTokens_token(token.substring(7));
        if (teacher == null)
            throw new CustomException(HttpStatus.NOT_FOUND, "Няма намерен учител с този тоукън, моля логнете се");
        commentRepo.deleteById(id);
    }

    public List<NotificationResponse> getNotifications(String token) throws CustomException {
        User user = userRepository.findUserByTokens_token(token);
        if (user == null) {
            throw new CustomException(HttpStatus.NOT_FOUND, "Няма намерен учител с този тоукън, моля логнете се");
        }
        List<NotificationResponse> notificationResponses = new ArrayList<>();
        for (Notification notification : user.getNotifications()) {
            NotificationResponse notificationResponse = NotificationResponse.builder().lesson(notification.getLesson())
                    .time(notification.getTime()).date(notification.getDate()).content(notification.getMessage())
                    .isChat(false).build();
            notificationResponses.add(notificationResponse);
        }
        return notificationResponses;
    }

    public List<PaymentResponse> getPayments(String token) throws CustomException {
        Teacher teacher = teacherRepository.findTeacherByTokens_token(token);
        if (teacher == null) {
            throw new CustomException(HttpStatus.NOT_FOUND, "Няма намерен учител с този тоукън, моля логнете се");
        }
        List<PaymentResponse> paymentResponses = new ArrayList<>();
        for (Payment payment : teacher.getPayments()) {
            PaymentResponse paymentResponse = PaymentResponse.builder().lesson(payment.getLesson())
                    .time(payment.getTime()).date(payment.getDate()).number(payment.getNumber())
                    .paymentStatus(payment.getPaymentStatus()).amount(payment.getAmount()).build();
            paymentResponses.add(paymentResponse);
        }
        return paymentResponses;
    }
}