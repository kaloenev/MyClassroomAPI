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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

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

    private final StudentRepository studentRepository;

    private final ReviewRepo reviewRepo;

    private final ThemaRepository themaRepository;

    public void enrollUserInCourse(String token, int courseID, int terminID) throws CustomException {
        Student student = studentRepository.findStudentByTokens_token(token.substring(7));
        CourseTermin termin = courseTerminRepo.getCourseTerminByTerminID(terminID);
        if (termin.isFull()) {
            throw new CustomException(HttpStatus.FORBIDDEN, "Error course is full");
        }
        termin.enrollStudent(student);
        student.addCourseTermin(termin);
    }

    public void enrollUserInLesson(String token, int courseID, int terminID) throws CustomException {
        Student student = studentRepository.findStudentByTokens_token(token.substring(7));
        LessonTermin termin = lessonTerminRepo.getLessonTerminByTerminID(terminID);
        if (termin.isFull()) {
            throw new CustomException(HttpStatus.FORBIDDEN, "Error course is full");
        }
        termin.enrollStudent(student);
        student.addLessonTermin(termin);
    }

    public void likeCourse(String token, int lessonID) throws CustomException {
        Student student = studentRepository.findStudentByTokens_token(token.substring(7));
        student.saveLessonToLiked(lessonRepository.getLessonByLessonID(lessonID));
    }

    public void dislikeCourse(String token, int lessonID) throws CustomException {
        Student student = studentRepository.findStudentByTokens_token(token.substring(7));
        student.removeLessonsFromLiked(lessonRepository.getLessonByLessonID(lessonID));
    }

    public void createCourse(String token, CreateCourseRequest courseRequest, boolean isDraft, boolean isPrivateLesson) throws CustomException {
        Teacher teacher = teacherRepository.findTeacherByTokens_token(token.substring(7));
        if (!teacher.isVerified()) throw new CustomException(HttpStatus.CONFLICT, "You must be verified to create a course");
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
            lesson.setStudentsUpperBound(courseRequest.getStudentsUpperBound());
            lessonRepository.save(lesson);
            for (CourseTerminRequestResponse courseTerminRequest : courseRequest.getCourseTerminRequests()) {
                List<Thema> themas = new ArrayList<>();
                for (ThemaSimpleResponse themaData : courseRequest.getThemas()) {
                    Thema thema = Thema.builder().description(themaData.getDescription()).title(themaData.getTitle()).build();
                    themas.add(thema);
                }
                CourseTermin courseTermin = CourseTermin.builder().dateTime(Timestamp.valueOf(courseTerminRequest.getStartDate()))
                        .courseDays(courseTerminRequest.getCourseDays())
                        .courseHoursNumber(Integer.parseInt(courseTerminRequest.getCourseHours().replace(":", "")))
                        .weekLength(courseTerminRequest.getWeekLength()).studentsUpperBound(courseRequest.getStudentsUpperBound())
                        .lesson(lesson).build();
                courseTerminRepo.save(courseTermin);
                for (Thema thema : themas) {
                    thema.setCourseTermin(courseTermin);
                    themaRepository.save(thema);
                    courseTermin.addThema(thema);
                }
                courseTerminRepo.save(courseTermin);
                lesson.addTermin(courseTermin);
            }
        } else {
            lesson.setStudentsUpperBound(1);
            lessonRepository.save(lesson);
            for (String privateLessonTermin : courseRequest.getPrivateLessonTermins()) {
                Thema thema = new Thema();
                thema.setDescription(courseRequest.getThemas()[0].getDescription());
                thema.setTitle(courseRequest.getThemas()[0].getTitle());
                themaRepository.save(thema);
                    String hours = privateLessonTermin.substring(privateLessonTermin.length() - 8);
                    LessonTermin lessonTermin = LessonTermin.builder().lessonHours(Integer.parseInt(hours.replace(":", "")))
                            .dateTime(Timestamp.valueOf(privateLessonTermin)).thema(thema).build();
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

    public void editCourse(String token, int lessonID, CreateCourseRequest courseRequest, boolean isDraft, boolean isPrivateLesson) throws CustomException {
        Teacher teacher = teacherRepository.findTeacherByTokens_token(token.substring(7));
        if (!teacher.isVerified()) throw new CustomException(HttpStatus.CONFLICT, "Трябва да се верифицирате първо");
        Lesson lesson = lessonRepository.getLessonByLessonID(lessonID);
        if (!Objects.equals(lesson.getTeacher().getId(), teacher.getId())) throw new CustomException(HttpStatus.FORBIDDEN, "Имате достъп само до Вашите курсове");
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
            //TODO Find alternative to removal
            lesson.setStudentsUpperBound(courseRequest.getStudentsUpperBound());
            for (CourseTermin termin : lesson.getCourseTermins()) {
                themaRepository.deleteAll(termin.getThemas());
            }
            terminRepo.deleteAll(lesson.getTermins());
            lesson.removeAllTermins();
            List<Thema> themas = new ArrayList<>();
            for (ThemaSimpleResponse themaData : courseRequest.getThemas()) {
                Thema thema = new Thema();
                thema.setDescription(themaData.getDescription());
                thema.setTitle(themaData.getTitle());
                themaRepository.save(thema);
                themas.add(thema);
            }
            for (CourseTerminRequestResponse courseTerminRequest : courseRequest.getCourseTerminRequests()) {
                CourseTermin courseTermin = CourseTermin.builder().dateTime(Timestamp.valueOf(courseTerminRequest.getStartDate()))
                        .courseDays(courseTerminRequest.getCourseDays())
                        .courseHoursNumber(Integer.parseInt(courseTerminRequest.getCourseHours().replace(":", "")))
                        .weekLength(courseTerminRequest.getWeekLength()).studentsUpperBound(courseRequest.getStudentsUpperBound())
                        .lesson(lesson).themas(themas).build();
                courseTerminRepo.save(courseTermin);
                lesson.addTermin(courseTermin);
            }
        } else {
            lesson.setStudentsUpperBound(1);
            terminRepo.deleteAll(lesson.getTermins());
            lesson.removeAllTermins();
            for (String privateLessonTermin : courseRequest.getPrivateLessonTermins()) {
                Thema thema = new Thema();
                thema.setDescription(courseRequest.getThemas()[0].getDescription());
                thema.setTitle(courseRequest.getThemas()[0].getTitle());
                themaRepository.save(thema);
                String hours = privateLessonTermin.substring(privateLessonTermin.length() - 8);
                LessonTermin lessonTermin = LessonTermin.builder().lessonHours(Integer.parseInt(hours.replace(":", "")))
                        .dateTime(Timestamp.valueOf(privateLessonTermin)).thema(thema).build();
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

    public HomePageResponse getHomePageInfo() throws CustomException {
        List<Lesson> lessons = lessonRepository.getDistinct9ByOrderByPopularityDesc();
        // Add file reader and links to the courses
        HomePageResponse homePageResponse = new HomePageResponse();

        List<LessonResponse> lessonResponses = new ArrayList<>();
        for (Lesson lesson : lessons) {
            LessonResponse lessonResponse = new LessonResponse(lesson, lesson.getTermins().get(0).getDate(), lesson.getTermins().get(0).getTime());
            if (!lesson.isPrivateLesson()) lessonResponse.setWeekLength(lesson.getCourseTermins().get(0).getWeekLength());
            lessonResponses.add(lessonResponse);
        }

        List<ReviewResponse> reviewResponses = new ArrayList<>();
        for (Review review : reviewRepo.getDistinct3ByOrderByRatingDescMessageDesc()) {
            ReviewResponse reviewResponse = new ReviewResponse(review);
            reviewResponses.add(reviewResponse);
        }

        homePageResponse.setPopularLessonsResponse(lessonResponses);
        homePageResponse.setReviewsResponse(reviewResponses);
        return homePageResponse;
    }

    public void leaveReview(String token, ReviewRequest reviewRequest) {
        //TODO Change from Teacher to Student!
        Lesson lesson = lessonRepository.getLessonByLessonID(reviewRequest.getLessonId());
        Teacher teacher = teacherRepository.findTeacherByTokens_token(token.substring(7));
        Review review = Review.builder().dateTime(Timestamp.valueOf(LocalDateTime.now())).lesson(lesson).message(reviewRequest.getMessage())
                .rating(reviewRequest.getRating()).studentName(teacher.getFirstname()).studentSurname(teacher.getLastname())
                .teacher(teacher).build();
        reviewRepo.save(review);
        lesson.leaveReview(review);
        lessonRepository.save(lesson);
        teacher.leaveReview(review);
        teacherRepository.save(teacher);
    }

    public FilterResponse getFilters() {
        List<String> subjects = lessonRepository.getAllSubjects();
        List<String> grades = lessonRepository.getAllGrades();
        int minPrice = lessonRepository.getMinPrice();
        int maxPrice = lessonRepository.getMaxPrice();
        return new FilterResponse(subjects, grades, minPrice, maxPrice);
    }

    public List<LessonResponse> getFilteredLessons(FilterRequest request) throws IllegalArgumentException, CustomException {
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
        int weekLength = -1;
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
            weekLength = 0;
        }
        for (Lesson lesson : lessons) {
            LessonResponse lessonResponse = new LessonResponse(lesson, lesson.getTermins().get(0).getDate(), lesson.getTermins().get(0).getTime());
            if (weekLength != -1) lessonResponse.setWeekLength(lesson.getCourseTermins().get(0).getWeekLength());
            lessonResponses.add(lessonResponse);
        }
        return lessonResponses;
    }

    public LessonResponse getLessonById(int id) throws CustomException {
//TODO do lesson page for logged user   Teacher teacher = teacherRepository.findTeacherByTokens_token(token.substring(7));
        var lesson = lessonRepository.getLessonByLessonID(id);
        LessonResponse lessonResponse;
        List<ReviewResponse> reviews = getLessonReviews(id, "", 1);
        if (lesson.isPrivateLesson()) {
            List<LessonTermin> lessonTermins = lesson.getLessonTermins();
            List<String> lessonTerminResponses = new ArrayList<>();
            ThemaSimpleResponse thema = new ThemaSimpleResponse(lessonTermins.get(0).getThema().getTitle(), lessonTermins.get(0).getThema().getDescription());
            for (LessonTermin lessonTermin : lessonTermins) {
                lessonTerminResponses.add(lessonTermin.getDateTime().toString());
            }
            lessonResponse = new LessonResponse(lesson, lessonTerminResponses, reviews, thema);
        } else {
            lessonResponse = new LessonResponse(lesson, reviews);
        }
        return lessonResponse;
    }

    public List<LessonResponse> getStudentAll(String token, LessonStatus lessonStatus, String sort) throws ClassCastException, CustomException {
        //TODO add paging? or make default lessonstatus to active
        Student student = studentRepository.findStudentByTokens_token(token.substring(7));

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
                        teacher.getFirstname(), teacher.getLastname(), courseTermin.getLessonStatus().toString(), courseTerminRequestResponse, teacher.getId());
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
                        teacher.getFirstname(), teacher.getLastname(), courseTermin.getLessonStatus().toString(), courseTerminRequestResponse, teacher.getId());
            } else {
                if (!lessonTermin.getLessonStatus().equals(lessonStatus)) continue;
                Lesson lesson = lessonTermin.getLesson();
                Teacher teacher = lesson.getTeacher();
                lessonResponse = new LessonResponse(lesson.getLessonID(), lesson.getTitle(), lesson.isPrivateLesson(),
                        teacher.getFirstname(), teacher.getLastname(), lessonTermin.getLessonStatus().toString(),
                        lessonTermin.getDate(), lessonTermin.getTime(), teacher.getId());
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
                    teacher.getFirstname(), teacher.getLastname(), lessonTermin.getLessonStatus().toString(),
                    lessonTermin.getDate(), lessonTermin.getTime(), teacher.getId());
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
                    teacher.getFirstname(), teacher.getLastname(), lessonTermin.getLessonStatus().toString(),
                    lessonTermin.getDate(), lessonTermin.getTime(), teacher.getId());

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
                    teacher.getFirstname(), teacher.getLastname(), courseTermin.getLessonStatus().toString(), courseTerminRequestResponse, teacher.getId());
            lessonResponses.add(lessonResponse);
        }
        return lessonResponses;
    }

    public List<LessonResponse> getFavouriteCourses(String token, String sort, int pageNumber) throws ClassCastException, CustomException {
        Student student = studentRepository.findStudentByTokens_token(token.substring(7));
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
                    lessonResponses.add(new LessonResponse(lesson, lesson.getTermins().get(0).getDate(), lesson.getTermins().get(0).getTime()));
                }
                return lessonResponses;
            }
            default ->
                    sortedAndPaged = PageRequest.of(pageNumber - 1, 12, Sort.by("dateTime").ascending());
        }
        List<Lesson> lessons = lessonRepository.getLessonByisLikedByStudent_id(student.getId(), sortedAndPaged);
        for (Lesson lesson : lessons) {
            LessonResponse lessonResponse = new LessonResponse(lesson, lesson.getTermins().get(0).getDate(), lesson.getTermins().get(0).getTime());
            lessonResponses.add(lessonResponse);
        }
        return lessonResponses;
    }

    public List<TeacherResponse> getFavouriteTeachers(String token) throws ClassCastException, CustomException {
        Student student = studentRepository.findStudentByTokens_token(token.substring(7));
        List<TeacherResponse> teachers = new ArrayList<>();
        for (Teacher teacher : student.getFavouriteTeachers()) {
            TeacherResponse teacherResponse = new TeacherResponse(teacher, "url");
            teachers.add(teacherResponse);
        }
        return teachers;
    }

    public List<LessonResponse> getTeacherLessons(String token, String lessonStatus, boolean privateLessons, boolean upcoming) throws ClassCastException, CustomException {
        Teacher teacher = teacherRepository.findTeacherByTokens_token(token.substring(7));
        List<Lesson> lessons = teacher.getLessons();
        List<LessonResponse> lessonResponses = new ArrayList<>();
        for (Lesson lesson : lessons) {
            List<Termin> termins = lesson.getTermins();
            if (upcoming) {
                if (!termins.isEmpty()) {
                    LessonResponse lessonResponse = new LessonResponse(lesson, lesson.getTermins().get(0).getDate(), lesson.getTermins().get(0).getTime());
                    lessonResponse.setNumberOfTermins(termins.size());
                    lessonResponse.setStatus("Активен");
                    lessonResponses.add(lessonResponse);
                }
            }
            else if (lesson.isPrivateLesson() == privateLessons) {
                if (lesson.isDraft() && lessonStatus.equals("Чернови")) {
                    if (termins.isEmpty()) {
                        LessonResponse lessonResponse = new LessonResponse(lesson, "", "");
                        lessonResponse.setStatus("Чернова");
                        lessonResponses.add(lessonResponse);
                    } else {
                        LessonResponse lessonResponse = new LessonResponse(lesson, lesson.getTermins().get(0).getDate(), lesson.getTermins().get(0).getTime());
                        lessonResponse.setNumberOfTermins(termins.size());
                        lessonResponse.setStatus("Чернова");
                        lessonResponses.add(lessonResponse);
                    }
                }
                else if (termins.isEmpty() && lessonStatus.equals("Неактивни")) {
                    LessonResponse lessonResponse = new LessonResponse(lesson, "", "");
                    lessonResponse.setStatus("Неактивен");
                    lessonResponses.add(lessonResponse);
                }
                else if (!termins.isEmpty() && lessonStatus.equals("Активни")) {
                    LessonResponse lessonResponse = new LessonResponse(lesson, lesson.getTermins().get(0).getDate(), lesson.getTermins().get(0).getTime());
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
        // TODO add to teacher exclusive functions
        List<CourseTermin> courseTermins = courseTerminRepo.getCourseTerminsByLessonID(lessonId);
        List<CourseTerminRequestResponse> courseTerminRequestResponses = new ArrayList<>();
        for (CourseTermin courseTermin : courseTermins) {
            courseTerminRequestResponses.add(new CourseTerminRequestResponse(courseTermin, courseTermin.getLessonStatus()));
        }
        return courseTerminRequestResponses;
    }

    public List<LessonTerminResponse> getLessonTerminsTeacher(String token, int lessonId) throws ClassCastException, CustomException {
        // TODO add to teacher exclusive functions
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

    public List<ReviewResponse> getLessonReviews(int lessonId, String sort, int pageNumber) throws ClassCastException {
        Pageable sortedAndPaged;
        switch (sort) {
            case "Най-нови" -> sortedAndPaged = PageRequest.of(pageNumber - 1, 12, Sort.by("dateTime").descending());
            case "Най-стари" -> sortedAndPaged = PageRequest.of(pageNumber - 1, 12, Sort.by("dateTime").ascending());
            case "Най-висок" -> sortedAndPaged = PageRequest.of(pageNumber - 1, 12, Sort.by("rating").descending());
            case "Най-нисък" -> sortedAndPaged = PageRequest.of(pageNumber - 1, 12, Sort.by("rating").ascending());
            default -> sortedAndPaged = PageRequest.of(pageNumber - 1, 12, Sort.by("dateTime").descending());
        }
        List<Review> reviews = reviewRepo.getByLesson_lessonID(lessonId, sortedAndPaged);
        List<ReviewResponse> reviewResponses = new ArrayList<>();
        for (Review review : reviews) {
            reviewResponses.add(new ReviewResponse(review));
        }
        return reviewResponses;
    }
}