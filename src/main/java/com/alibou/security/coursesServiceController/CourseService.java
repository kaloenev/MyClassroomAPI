package com.alibou.security.coursesServiceController;

import com.alibou.security.exceptionHandling.CustomException;
import com.alibou.security.lessons.*;
import com.alibou.security.token.TokenRepository;
import com.alibou.security.user.*;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.EnumUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CourseService {
    private final UserRepository repository;
    private final TokenRepository tokenRepository;

    private final LessonRepository lessonRepository;

    private final TerminRepo terminRepo;

    private final LessonTerminRepo lessonTerminRepo;

    private final CourseTerminRepo courseTerminRepo;

    private final TeacherRepository teacherRepository;

    private final ReviewRepo reviewRepo;

    public static final String HOME_PAGE_COURSES_LINK = "";

    public static final String ASKED_QUESTIONS_FILE_LOCATION = "";

    public void enrollUserInCourse(String token, int courseID, int terminID) throws CustomException {
        var token1 = tokenRepository.findByToken(token);
        if (token1.isEmpty()) throw new CustomException(HttpStatus.FORBIDDEN, "Login link");
        Student student = (Student) token1.get().getUser();
        CourseTermin termin = courseTerminRepo.getCourseTerminByTerminID(terminID);
        if (termin.isFull()) {
            throw new CustomException(HttpStatus.FORBIDDEN, "Error course is full");
        }
        termin.enrollStudent(student);
        student.addCourseTermin(termin);
    }

    public void enrollUserInLesson(String token, int courseID, int terminID) throws CustomException {
        var token1 = tokenRepository.findByToken(token);
        if (token1.isEmpty()) throw new CustomException(HttpStatus.FORBIDDEN, "Login link");
        Student student = (Student) token1.get().getUser();
        LessonTermin termin = lessonTerminRepo.getLessonTerminByTerminID(terminID);
        if (termin.isFull()) {
            throw new CustomException(HttpStatus.FORBIDDEN, "Error course is full");
        }
        termin.enrollStudent(student);
        student.addLessonTermin(termin);
    }

    public void likeCourse(String token, int lessonID) throws CustomException {
        var token1 = tokenRepository.findByToken(token);
        if (token1.isEmpty()) throw new CustomException(HttpStatus.FORBIDDEN, "Login link");
        Student student = (Student) token1.get().getUser();
        student.saveLessonToLiked(lessonRepository.getLessonByLessonID(lessonID));
    }

    public void dislikeCourse(String token, int lessonID) throws CustomException {
        var token1 = tokenRepository.findByToken(token);
        if (token1.isEmpty()) throw new CustomException(HttpStatus.FORBIDDEN, "Login link");
        Student student = (Student) token1.get().getUser();
        student.removeLessonsFromLiked(lessonRepository.getLessonByLessonID(lessonID));
    }

    public void createCourse(String token, CreateCourseRequest courseRequest, boolean isDraft, boolean isPrivateLesson) throws CustomException {
        Teacher teacher = teacherRepository.findTeacherByTokens_token(token.substring(7));
        System.out.println(teacher.getId());
        Lesson lesson = Lesson.builder().teacher(teacher).build();
        lesson.setTitle(courseRequest.getTitle());
        lesson.setSubject(courseRequest.getSubject());
        lesson.setGrade(courseRequest.getGrade());
        lesson.setDescription(courseRequest.getDescription());
        lesson.setLength(courseRequest.getLength());
        lesson.setPrivateLesson(isPrivateLesson);
        lesson.setPrice(courseRequest.getPrice());
        lesson.setDraft(isDraft);
        if (!isPrivateLesson) {
            lesson.setThemas(courseRequest.getThemas());
            lesson.setStudentsUpperBound(courseRequest.getStudentsUpperBound());
            lessonRepository.save(lesson);
            for (CourseTerminRequestResponse courseTerminRequest : courseRequest.getCourseTerminRequests()) {
                CourseTermin courseTermin = CourseTermin.builder().dateTime(Timestamp.valueOf(courseTerminRequest.getStartDate()))
                        .courseDays(courseTerminRequest.getCourseDays()).courseHours(courseTerminRequest.getCourseHours())
                        .courseHoursNumber(Integer.parseInt(courseTerminRequest.getCourseHours().replace(":", "")))
                        .weekLength(courseTerminRequest.getWeekLength()).studentsUpperBound(courseRequest.getStudentsUpperBound())
                        .lesson(lesson).build();
                courseTerminRepo.save(courseTermin);
                lesson.addTermin(courseTermin);
            }
        } else {
            lesson.setStudentsUpperBound(1);
            lessonRepository.save(lesson);
            for (String privateLessonTermin : courseRequest.getPrivateLessonTermins()) {
                String hours = privateLessonTermin.substring(privateLessonTermin.length() - 8);
                LessonTermin lessonTermin = LessonTermin.builder().lessonHours(Integer.parseInt(hours.replace(":", "")))
                        .dateTime(Timestamp.valueOf(privateLessonTermin)).build();
                lessonTerminRepo.save(lessonTermin);
                lessonTermin.setLesson(lesson);
                lesson.addTermin(lessonTermin);
            }
        }
        lesson.getTermins().sort(Comparator.comparing(Termin::getDateTime));
        lessonRepository.save(lesson);
        System.out.println(teacher.getId());
        teacher.addLesson(lesson);
    }

    public void editCourse(int lessonID, CreateCourseRequest courseRequest, boolean isDraft, boolean isPrivateLesson) throws CustomException {
        Lesson lesson = lessonRepository.getLessonByLessonID(lessonID);
        lesson.setTitle(courseRequest.getTitle());
        if (!isDraft) {
            checkForUnallowedChanges(lesson, courseRequest);
        }
        lesson.setSubject(courseRequest.getSubject());
        lesson.setGrade(courseRequest.getGrade());
        lesson.setDescription(courseRequest.getDescription());
        lesson.setLength(courseRequest.getLength());
        lesson.setPrivateLesson(isPrivateLesson);
        lesson.setPrice(courseRequest.getPrice());
        lesson.setDraft(isDraft);
        if (!isPrivateLesson) {
            lesson.setThemas(courseRequest.getThemas());
            lesson.setStudentsUpperBound(courseRequest.getStudentsUpperBound());
            terminRepo.deleteAll(lesson.getTermins());
            lesson.removeAllTermins();
            for (CourseTerminRequestResponse courseTerminRequest : courseRequest.getCourseTerminRequests()) {
                CourseTermin courseTermin = CourseTermin.builder().dateTime(Timestamp.valueOf(courseTerminRequest.getStartDate()))
                        .courseDays(courseTerminRequest.getCourseDays()).courseHours(courseTerminRequest.getCourseHours())
                        .courseHoursNumber(Integer.parseInt(courseTerminRequest.getCourseHours().replace(":", "")))
                        .weekLength(courseTerminRequest.getWeekLength()).studentsUpperBound(courseRequest.getStudentsUpperBound())
                        .lesson(lesson).build();
                courseTerminRepo.save(courseTermin);
                lesson.addTermin(courseTermin);
            }
        } else {
            lesson.setStudentsUpperBound(1);
            terminRepo.deleteAll(lesson.getTermins());
            lesson.removeAllTermins();
            for (String privateLessonTermin : courseRequest.getPrivateLessonTermins()) {
                String hours = privateLessonTermin.substring(privateLessonTermin.length() - 9);
                LessonTermin lessonTermin = LessonTermin.builder().lessonHours(Integer.parseInt(hours.replace(":", "")))
                        .dateTime(Timestamp.valueOf(privateLessonTermin)).build();
                lessonTerminRepo.save(lessonTermin);
                lessonTermin.setLesson(lesson);
                lesson.addTermin(lessonTermin);
            }
        }
        lesson.getTermins().sort(Comparator.comparing(Termin::getDateTime));
    }

    private void checkForUnallowedChanges(Lesson lesson, CreateCourseRequest courseRequest) throws CustomException {
        boolean violations = courseRequest.getCourseTerminRequests().isEmpty() && lesson.getTermins().isEmpty()
                || lesson.getTitle().isEmpty() && courseRequest.getTitle().isEmpty()
                || !EnumUtils.isValidEnum(Subject.class, lesson.getSubject()) && !EnumUtils.isValidEnum(Subject.class, courseRequest.getSubject())
                || lesson.getDescription().isEmpty() && courseRequest.getDescription().isEmpty()
                || lesson.getImageLocation().isEmpty() && courseRequest.getImageLocation().isEmpty()
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

    public HomePageResponse getHomePageInfo() {
        List<Lesson> lessons = lessonRepository.findFirst9ByOrderByPopularityDesc();
        // Add file reader and links to the courses
        HomePageResponse homePageResponse = new HomePageResponse();

        List<LessonResponse> lessonResponses = new ArrayList<>();
        for (Lesson lesson : lessons) {
            lessonResponses.add(new LessonResponse(lesson, lesson.getTermins().get(0).getDateTime().toString()));
        }

        List<ReviewResponse> reviewResponses = new ArrayList<>();
        for (Review review : reviewRepo.findTop3ByOrderByRatingDescMessageDesc()) {
            ReviewResponse reviewResponse = new ReviewResponse(review);
            reviewResponses.add(reviewResponse);
        }

        homePageResponse.setPopularLessonsResponse(lessonResponses);
        homePageResponse.setReviewsResponse(reviewResponses);
        return homePageResponse;
    }

    public Subject[] getFilters() {
        return Subject.values();
    }

    public List<LessonResponse> getFilteredLessons(FilterRequest request) throws IllegalArgumentException {
        List<LessonResponse> lessonResponses = new ArrayList<>();
        Pageable sortedAndPaged = PageRequest.of(request.getPageNumber() - 1, 12);
        String sort;
        switch (request.getSort()) {
            case "Най-евтини" ->
                    sort = "c.lesson.price";
            case "Най-висок рейтинг" ->
                    sort = "c.lesson.rating";
            case "Най-скоро започващи" ->
                    sort = "c.dateTime";
            default ->
                    sort = "c.lesson.popularity";
        }
        if (request.getPriceLowerBound() >= 0 && request.getPriceUpperBound() == 0) {
            request.setPriceUpperBound(10000);
            request.setPriceLowerBound(0);
        }
        Page<Lesson> lessons;
        if (request.isPrivateLesson()) {
            lessons = lessonTerminRepo.getFilteredLessonTermins(request.getSearchTerm(), request.getSearchTerm(), request.getSearchTerm(),
                    request.getSubject(), false, request.getGrade(), request.getPriceLowerBound(), request.getPriceUpperBound(),
                    request.getHoursLowerBound(), request.getHoursUpperBound(), Timestamp.valueOf(request.getLowerBound()),
                    Timestamp.valueOf(request.getUpperBound()), false, sortedAndPaged);
        } else {
            lessons = courseTerminRepo.getFilteredCourseTermins(request.getSearchTerm(), request.getSearchTerm(), request.getSearchTerm(),
                    request.getSubject(), false, request.getGrade(), request.getPriceLowerBound(), request.getPriceUpperBound(),
                    request.getHoursLowerBound(), request.getHoursUpperBound(), Timestamp.valueOf(request.getLowerBound()),
                    Timestamp.valueOf(request.getUpperBound()), false, sortedAndPaged);
        }
        for (Lesson lesson : lessons) {
            LessonResponse lessonResponse = new LessonResponse(lesson, lesson.getTermins().get(0).getDateTime().toString());
            lessonResponses.add(lessonResponse);
        }
        return lessonResponses;
    }

    public LessonResponse getLessonById(int id, String sort, int reviewPage) throws CustomException {
//TODO do lesson page for logged user   var token1= tokenRepository.findByToken(token);
        var lesson = lessonRepository.getLessonByLessonID(id);
        LessonResponse lessonResponse;
        Pageable sortedAndPaged;
        switch (sort) {
            case "Най-нови" -> sortedAndPaged = PageRequest.of(reviewPage - 1, 12, Sort.by("dateTime").descending());
            case "Най-стари" -> sortedAndPaged = PageRequest.of(reviewPage - 1, 12, Sort.by("dateTime").ascending());
            case "Най-висок" -> sortedAndPaged = PageRequest.of(reviewPage - 1, 12, Sort.by("rating").descending());
            case "Най-нисък" -> sortedAndPaged = PageRequest.of(reviewPage - 1, 12, Sort.by("rating").ascending());
            default -> sortedAndPaged = PageRequest.of(reviewPage - 1, 12, Sort.by("dateTime").descending());
        }
        List<Review> reviews = reviewRepo.findTopByLesson_lessonID(id, sortedAndPaged);
        if (lesson.isPrivateLesson()) {
            List<LessonTermin> lessonTermins = lesson.getLessonTermins();
            List<String> lessonTerminResponses = new ArrayList<>();
            for (LessonTermin lessonTermin : lessonTermins) {
                lessonTerminResponses.add(lessonTermin.getDateTime().toString());
            }
            lessonResponse = new LessonResponse(lesson, lessonTerminResponses, reviews, "url1", "url2");
        } else {
            lessonResponse = new LessonResponse(lesson, reviews, "url", "url");
        }
        return lessonResponse;
    }

    public List<LessonResponse> getStudentAll(String token, LessonStatus lessonStatus, String sort) throws ClassCastException, CustomException {
        //TODO add paging? or make default lessonstatus to active
        var token1 = tokenRepository.findByToken(token);
        if (token1.isEmpty()) throw new CustomException(HttpStatus.FORBIDDEN, "Login link");
        var user = token1.get().getUser();
        Student student = (Student) user;

        if (sort.equals("Частни уроци")) return getStudentPrivateLessons(lessonStatus, student);
        else if (sort.equals("Курсове")) return getStudentCourses(lessonStatus, student);

        List<LessonResponse> lessonResponses = new ArrayList<>();
        List<LessonTermin> lessonTermins = student.getPrivateLessons();
        int counter = 0;
        int lessonsLength = lessonTermins.size() - 1;
        for (CourseTermin courseTermin : student.getCourses()) {
            LessonResponse lessonResponse;
            if (counter > lessonsLength) {
                if (!courseTermin.getLessonStatus().equals(lessonStatus)) continue;
                CourseTerminRequestResponse courseTerminRequestResponse = new CourseTerminRequestResponse(courseTermin);
                Lesson lesson = courseTermin.getLesson();
                Teacher teacher = lesson.getTeacher();
                lessonResponse = new LessonResponse(lesson.getLessonID(), lesson.getTitle(), lesson.isPrivateLesson(),
                        teacher.getFirstname(), teacher.getLastname(), courseTermin.getLessonStatus().toString(), courseTerminRequestResponse, "url");
                lessonResponses.add(lessonResponse);
                continue;
            }
            LessonTermin lessonTermin = lessonTermins.get(counter);
            if (courseTermin.getDateTime().before(lessonTermin.getDateTime())) {
                if (!courseTermin.getLessonStatus().equals(lessonStatus)) continue;
                CourseTerminRequestResponse courseTerminRequestResponse = new CourseTerminRequestResponse(courseTermin);
                Lesson lesson = courseTermin.getLesson();
                Teacher teacher = lesson.getTeacher();
                lessonResponse = new LessonResponse(lesson.getLessonID(), lesson.getTitle(), lesson.isPrivateLesson(),
                        teacher.getFirstname(), teacher.getLastname(), courseTermin.getLessonStatus().toString(), courseTerminRequestResponse, "url1");
            } else {
                if (!lessonTermin.getLessonStatus().equals(lessonStatus)) continue;
                Lesson lesson = lessonTermin.getLesson();
                Teacher teacher = lesson.getTeacher();
                lessonResponse = new LessonResponse(lesson.getLessonID(), lesson.getTitle(), lesson.isPrivateLesson(),
                        teacher.getFirstname(), teacher.getLastname(), lessonTermin.getLessonStatus().toString(), lessonTermin.getDateTime());
                counter++;
            }
            lessonResponses.add(lessonResponse);
        }
        while (counter <= lessonsLength) {
            LessonResponse lessonResponse;
            LessonTermin  lessonTermin = lessonTermins.get(counter);
            if (!lessonTermin.getLessonStatus().equals(lessonStatus)) continue;
            Lesson lesson = lessonTermin.getLesson();
            Teacher teacher = lesson.getTeacher();
            lessonResponse = new LessonResponse(lesson.getLessonID(), lesson.getTitle(), lesson.isPrivateLesson(),
                    teacher.getFirstname(), teacher.getLastname(), lessonTermin.getLessonStatus().toString(), lessonTermin.getDateTime());
            lessonResponses.add(lessonResponse);
            counter++;
        }
        return lessonResponses;
    }

    private List<LessonResponse> getStudentPrivateLessons(LessonStatus lessonStatus, Student student) {
        List<LessonResponse> lessonResponses = new ArrayList<>();
        for (LessonTermin lessonTermin : student.getPrivateLessons()) {
            if (!lessonTermin.getLessonStatus().equals(lessonStatus)) continue;
            Lesson lesson = lessonTermin.getLesson();
            Teacher teacher = lesson.getTeacher();
            LessonResponse lessonResponse = new LessonResponse(lesson.getLessonID(), lesson.getTitle(), lesson.isPrivateLesson(),
                    teacher.getFirstname(), teacher.getLastname(), lessonTermin.getLessonStatus().toString(), lessonTermin.getDateTime());

            lessonResponses.add(lessonResponse);
        }
        return lessonResponses;
    }

    private List<LessonResponse> getStudentCourses(LessonStatus lessonStatus, Student student) {
        List<LessonResponse> lessonResponses = new ArrayList<>();
        for (CourseTermin courseTermin : student.getCourses()) {
            if (!courseTermin.getLessonStatus().equals(lessonStatus)) continue;
            CourseTerminRequestResponse courseTerminRequestResponse = new CourseTerminRequestResponse(courseTermin);
            Lesson lesson = courseTermin.getLesson();
            Teacher teacher = lesson.getTeacher();
            LessonResponse lessonResponse = new LessonResponse(lesson.getLessonID(), lesson.getTitle(), lesson.isPrivateLesson(),
                    teacher.getFirstname(), teacher.getLastname(), courseTermin.getLessonStatus().toString(), courseTerminRequestResponse, "url");
            lessonResponses.add(lessonResponse);
        }
        return lessonResponses;
    }

    public List<LessonResponse> getFavouriteCourses(String token, String sort, int pageNumber) throws ClassCastException, CustomException {
        var token1 = tokenRepository.findByToken(token);
        if (token1.isEmpty()) throw new CustomException(HttpStatus.FORBIDDEN, "Login link");
        var user = token1.get().getUser();
        Student student = (Student) user;
        List<LessonResponse> lessonResponses = new ArrayList<>();
        Pageable sortedAndPaged;
        switch (sort) {
            case "Най-популярни" ->
                    sortedAndPaged = PageRequest.of(pageNumber - 1, 12, Sort.by("popularity").descending());
            case "Най-скъпи" ->
                    sortedAndPaged = PageRequest.of(pageNumber - 1, 12, Sort.by("lesson_price").descending());
            case "Най-евтини" ->
                    sortedAndPaged = PageRequest.of(pageNumber - 1, 12, Sort.by("lesson_price").ascending());
            case "Най-висок рейтинг" ->
                    sortedAndPaged = PageRequest.of(pageNumber - 1, 12, Sort.by("lesson_rating").descending());
            case "Най-скоро започващи" ->
                    sortedAndPaged = PageRequest.of(pageNumber - 1, 12, Sort.by("dateTime").ascending());
            case "Най-нови" -> {
                List<Lesson> lessons = student.getFavouriteLessons();
                for (int i = (pageNumber - 1) * 12; i < pageNumber * 12; i++) {
                    Lesson lesson = lessons.get(i);
                    lessonResponses.add(new LessonResponse(lesson, lesson.getTermins().get(0).getDateTime().toString()));
                }
                return lessonResponses;
            }
            default ->
                    sortedAndPaged = PageRequest.of(pageNumber - 1, 12, Sort.by("dateTime").ascending());
        }
        List<Lesson> lessons = lessonRepository.getLessonByisLikedByStudent_id(student.getId(), sortedAndPaged);
        for (Lesson lesson : lessons) {
            LessonResponse lessonResponse = new LessonResponse(lesson, lesson.getTermins().get(0).getDateTime().toString());
            lessonResponses.add(lessonResponse);
        }
        return lessonResponses;
    }

    public List<TeacherResponse> getFavouriteTeachers(String token) throws ClassCastException, CustomException {
        var token1 = tokenRepository.findByToken(token);
        if (token1.isEmpty()) throw new CustomException(HttpStatus.FORBIDDEN, "Login link");
        var user = token1.get().getUser();
        Student student = (Student) user;
        List<TeacherResponse> teachers = new ArrayList<>();
        for (Teacher teacher : student.getFavouriteTeachers()) {
            TeacherResponse teacherResponse = new TeacherResponse(teacher, "url");
            teachers.add(teacherResponse);
        }
        return teachers;
    }

    public List<LessonResponse> getTeacherLessons(String token, String lessonStatus, boolean privateLessons, boolean upcoming) throws ClassCastException, CustomException {
        var token1 = tokenRepository.findByToken(token);
        if (token1.isEmpty()) throw new CustomException(HttpStatus.FORBIDDEN, "Login link");
        var user = token1.get().getUser();
        Teacher teacher = (Teacher) user;
        List<Lesson> lessons = teacher.getLessons();
        List<LessonResponse> lessonResponses = new ArrayList<>();
        for (Lesson lesson : lessons) {
            List<Termin> termins = lesson.getTermins();
            if (upcoming) {
                if (!termins.isEmpty()) {
                    LessonResponse lessonResponse = new LessonResponse(lesson, termins.get(0).getDateTime().toString());
                    lessonResponse.setNumberOfTermins(termins.size());
                    lessonResponse.setStatus("Активен");
                    lessonResponses.add(lessonResponse);
                }
            }
            else if (lesson.isPrivateLesson() == privateLessons) {
                if (lesson.isDraft() && lessonStatus.equals("Чернови")) {
                    if (termins.isEmpty()) {
                        LessonResponse lessonResponse = new LessonResponse(lesson, "Няма активни дати");
                        lessonResponse.setStatus("Чернова");
                        lessonResponses.add(lessonResponse);
                    } else {
                        LessonResponse lessonResponse = new LessonResponse(lesson, termins.get(0).getDateTime().toString());
                        lessonResponse.setNumberOfTermins(termins.size());
                        lessonResponse.setStatus("Чернова");
                        lessonResponses.add(lessonResponse);
                    }
                }
                else if (termins.isEmpty() && lessonStatus.equals("Неактивни")) {
                    LessonResponse lessonResponse = new LessonResponse(lesson, "Няма активни дати");
                    lessonResponse.setStatus("Неактивен");
                    lessonResponses.add(lessonResponse);
                }
                else if (!termins.isEmpty() && lessonStatus.equals("Активни")) {
                    LessonResponse lessonResponse = new LessonResponse(lesson, termins.get(0).getDateTime().toString());
                    lessonResponse.setNumberOfTermins(termins.size());
                    lessonResponse.setStatus("Активен");
                    lessonResponses.add(lessonResponse);
                }
                else throw new CustomException(HttpStatus.BAD_REQUEST, "Моля изберете някой от предложените статуси през интерфейса");
            }
        }
        return lessonResponses;
    }

    public List<CourseTerminRequestResponse> getCourseTerminsTeacher(String token, int lessonId)throws CustomException {
        var token1 = tokenRepository.findByToken(token);
        if (token1.isEmpty()) throw new CustomException(HttpStatus.FORBIDDEN, "Login link");
        var user = token1.get().getUser();
        if (!user.getRole().equals(Role.TEACHER)) throw new CustomException(HttpStatus.FORBIDDEN, "Error, this is a teacher function");
        List<CourseTermin> courseTermins = courseTerminRepo.getCourseTerminsByLessonID(lessonId);
        List<CourseTerminRequestResponse> courseTerminRequestResponses = new ArrayList<>();
        for (CourseTermin courseTermin : courseTermins) {
            courseTerminRequestResponses.add(new CourseTerminRequestResponse(courseTermin, courseTermin.getLessonStatus()));
        }
        return courseTerminRequestResponses;
    }

    public List<LessonTerminResponse> getLessonTerminsTeacher(String token, int lessonId) throws ClassCastException, CustomException {
        var token1 = tokenRepository.findByToken(token);
        if (token1.isEmpty()) throw new CustomException(HttpStatus.FORBIDDEN, "Login link");
        var user = token1.get().getUser();
        Teacher teacher = (Teacher) user;
        List<LessonTermin> lessonTermins = lessonTerminRepo.getLessonTerminsByLessonID(lessonId);
        List<LessonTerminResponse> lessonResponses = new ArrayList<>();
        int dayOfMonth = -1;
        int counter = 1;
        int lessonTerminsSize = lessonTermins.size();
        List<TimePair> timePairs = new ArrayList<>();
        for (LessonTermin lessonTermin : lessonTermins) {
            int currentDayOfMonth = lessonTermin.getDateTime().toLocalDateTime().getDayOfMonth();
            TimePair timePair = new TimePair(lessonTermin.getLessonHours(), lessonTermin.isFull());
            timePairs.add(timePair);
            if (dayOfMonth != -1 && currentDayOfMonth != dayOfMonth) {
                dayOfMonth = currentDayOfMonth;
            } else if (currentDayOfMonth != dayOfMonth || counter == lessonTerminsSize) {
                LessonTerminResponse lessonTerminResponse = new LessonTerminResponse(lessonTermin.getDateTime().toString().substring(0, 10),
                        timePairs, lessonTermin.getDateTime().toLocalDateTime().getDayOfWeek().toString(), lessonTermin.getLessonStatus().toString());
                timePairs = new ArrayList<>();
                dayOfMonth = currentDayOfMonth;
                lessonResponses.add(lessonTerminResponse);
            }
            counter++;
        }
        return lessonResponses;
    }

    public List<ReviewResponse> getLessonReviews(String token, int lessonId, int pageNumber) throws ClassCastException, CustomException {
        var token1 = tokenRepository.findByToken(token);
        if (token1.isEmpty()) throw new CustomException(HttpStatus.FORBIDDEN, "Login link");
        var user = token1.get().getUser();
        Teacher teacher = (Teacher) user;
        Pageable sortedAndPaged = PageRequest.of(pageNumber - 1, 12, Sort.by("lesson_rating").descending());
        List<Review> reviews = reviewRepo.findTopByLesson_lessonID(lessonId, sortedAndPaged);
        List<ReviewResponse> reviewResponses = new ArrayList<>();
        for (Review review : reviews) {
            reviewResponses.add(new ReviewResponse(review));
        }
        return reviewResponses;
    }

    public LessonResponse getCoursePage(int lessonId) throws CustomException {
        Lesson lesson = lessonRepository.getLessonByLessonID(lessonId);
        LessonResponse lessonResponse = new LessonResponse(lesson, lesson.getReviews(), "url1", "url2");
        Teacher teacher = lesson.getTeacher();
        TeacherResponse teacherResponse = new TeacherResponse(teacher, "url");
        teacherResponse.setDescription(teacher.getDescription());
        teacherResponse.setExperience(teacherResponse.getExperience());
        lessonResponse.setTeacherResponse(teacherResponse);
        return lessonResponse;
    }

    public LessonResponse getLessonPage(int lessonId) throws CustomException {
        Lesson lesson = lessonRepository.getLessonByLessonID(lessonId);
        List<String> termins = new ArrayList<>();
        for (Termin termin : lesson.getTermins()) {
            termins.add(termin.getDateTime().toString());
        }
        LessonResponse lessonResponse = new LessonResponse(lesson, termins, lesson.getReviews(),
                "url1", "url2");
        Teacher teacher = lesson.getTeacher();
        TeacherResponse teacherResponse = new TeacherResponse(teacher, "url");
        teacherResponse.setDescription(teacher.getDescription());
        teacherResponse.setExperience(teacherResponse.getExperience());
        lessonResponse.setTeacherResponse(teacherResponse);
        return lessonResponse;
    }
}