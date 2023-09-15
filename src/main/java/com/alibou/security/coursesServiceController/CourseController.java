package com.alibou.security.coursesServiceController;


import com.alibou.security.auth.AuthenticationResponse;
import com.alibou.security.auth.RegisterRequest;
import com.alibou.security.exceptionHandling.CustomException;
import com.alibou.security.exceptionHandling.CustomWarning;
import com.alibou.security.lessons.LessonStatus;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/api/v1/lessons")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    @PostMapping("/createCourse")
    public ResponseEntity<Object> createCourse(
            @RequestBody CreateCourseRequest request, HttpServletRequest httpRequest
    ) {
        try {
            courseService.createCourse(httpRequest.getHeader("Authorization"), request, false, false);
        }
        catch (CustomException e) {
                CustomWarning warning = new CustomWarning(e.getStatus(), e.getMessage());
                return new ResponseEntity<>(warning, new HttpHeaders(), warning.getStatus());
            }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/saveCourseDraft")
    public ResponseEntity<Object> saveCourseDraft(
            @RequestBody CreateCourseRequest request, HttpServletRequest httpRequest
    ) {
        try {
            courseService.createCourse(httpRequest.getHeader("Authorization"), request, true, false);
        } catch (CustomException e) {
            CustomWarning warning = new CustomWarning(e.getStatus(), e.getMessage());
            return new ResponseEntity<>(warning, new HttpHeaders(), warning.getStatus());
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/createPrivateLesson")
    public ResponseEntity<Object> createPrivateLesson(
            @RequestBody CreateCourseRequest request, HttpServletRequest httpRequest
    ) {
        try {
            courseService.createCourse(httpRequest.getHeader("Authorization"), request, false, true);
        } catch (CustomException e) {
            CustomWarning warning = new CustomWarning(HttpStatus.BAD_REQUEST, e.getMessage());
            return new ResponseEntity<>(warning, new HttpHeaders(), warning.getStatus());
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/savePrivateLessonDraft")
    public ResponseEntity<Object> savePrivateLessonDraft(
            @RequestBody CreateCourseRequest request, HttpServletRequest httpRequest
    ) {
        try {
            courseService.createCourse(httpRequest.getHeader("Authorization"), request, true, true);
        } catch (CustomException e) {
            CustomWarning warning = new CustomWarning(e.getStatus(), e.getMessage());
            return new ResponseEntity<>(warning, new HttpHeaders(), warning.getStatus());
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/getHomePage")
    public ResponseEntity<Object> getHomePage() {
        return ResponseEntity.ok(courseService.getHomePageInfo());
    }

    @PostMapping("/getFilteredCourses")
    public ResponseEntity<Object> getFilteredCourses(@RequestBody FilterRequest filterRequest) {
        try {
            return ResponseEntity.ok(courseService.getFilteredLessons(filterRequest));
        } catch (IllegalArgumentException e) {
        CustomWarning warning = new CustomWarning(HttpStatus.BAD_REQUEST, e.getMessage());
        return new ResponseEntity<>(warning, new HttpHeaders(), warning.getStatus());
        }
    }

    @PostMapping("/getFilteredLessons")
    public ResponseEntity<Object> getFilteredLessons(@RequestBody FilterRequest filterRequest) {
        List<LessonResponse> lessons;
        try {
            lessons = courseService.getFilteredLessons(filterRequest);
        } catch (IllegalArgumentException e) {
        CustomWarning warning = new CustomWarning(HttpStatus.BAD_REQUEST, e.getMessage());
        return new ResponseEntity<>(warning, new HttpHeaders(), warning.getStatus());
        }
        return ResponseEntity.ok(lessons);
    }

    @GetMapping("/getFilters")
    public ResponseEntity<Object> getFilters() {
        return ResponseEntity.ok(courseService.getFilters());
    }

    @GetMapping(value = "/image/{url}", consumes = MediaType.ALL_VALUE)
    public ResponseEntity<Object> getImage(@PathVariable("url")String url, HttpServletResponse response) {
        byte[] image;
        try {
            image = Files.readAllBytes(Paths.get(url));
        } catch (IOException e) {
            CustomWarning warning = new CustomWarning(HttpStatus.BAD_REQUEST, e.getMessage());
            return new ResponseEntity<>(warning, new HttpHeaders(), warning.getStatus());
        }
        response.setContentType(MediaType.IMAGE_JPEG_VALUE);
        return ResponseEntity.ok(image);
    }

    @PostMapping("/getCoursePage")
    public ResponseEntity<Object> getCoursePage(@RequestBody LessonRequest lessonRequest) {
        try {
            return ResponseEntity.ok(courseService.getLessonById(lessonRequest.getId(), lessonRequest.getSort(), lessonRequest.getPage()));
        } catch (CustomException e) {
            CustomWarning warning = new CustomWarning(e.getStatus(), e.getMessage());
            return new ResponseEntity<>(warning, new HttpHeaders(), warning.getStatus());
        }
    }

    @PostMapping("/getDashboard/student")
    public ResponseEntity<Object> getStudentDashboard(HttpServletRequest httpServletRequest, @RequestParam String lessonStatus,
                                                      @RequestParam String sort) {
        try {
            return ResponseEntity.ok(courseService.getStudentAll(httpServletRequest.getHeader("Authentication"),
                    LessonStatus.valueOf(lessonStatus), sort));
        } catch (CustomException e) {
            CustomWarning warning = new CustomWarning(e.getStatus(), e.getMessage());
            return new ResponseEntity<>(warning, new HttpHeaders(), warning.getStatus());
        } catch (IllegalArgumentException e) {
            CustomWarning warning = new CustomWarning(HttpStatus.BAD_REQUEST, e.getMessage());
            return new ResponseEntity<>(warning, new HttpHeaders(), warning.getStatus());
        }
    }


}
