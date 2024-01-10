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
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@RestController
@RequestMapping("/api/v1/lessons")
@RequiredArgsConstructor
@CrossOrigin
public class CourseController {

    private final CourseService courseService;
    private int filenameCounter = 0;

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

    @PostMapping("/editCourse/{id}")
    public ResponseEntity<Object> editCourse(@PathVariable int id,
            @RequestBody CreateCourseRequest request, HttpServletRequest httpRequest
    ) {
        try {
            courseService.editCourse(httpRequest.getHeader("Authorization"), id, request, false, false);
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

    @PostMapping("/editCourseDraft/{id}")
    public ResponseEntity<Object> editCourseDraft(@PathVariable int id,
            @RequestBody CreateCourseRequest request, HttpServletRequest httpRequest
    ) {
        try {
            courseService.editCourse(httpRequest.getHeader("Authorization"),id, request, true, false);
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

    @PostMapping("/editPrivateLessonDraft/{id}")
    public ResponseEntity<Object> editPrivateLessonDraft(@PathVariable int id,
                                                    @RequestBody CreateCourseRequest request, HttpServletRequest httpRequest
    ) {
        try {
            courseService.editCourse(httpRequest.getHeader("Authorization"), id, request, true,true);
        } catch (CustomException e) {
            CustomWarning warning = new CustomWarning(HttpStatus.BAD_REQUEST, e.getMessage());
            return new ResponseEntity<>(warning, new HttpHeaders(), warning.getStatus());
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/editPrivateLesson/{id}")
    public ResponseEntity<Object> editPrivateLesson(@PathVariable int id,
            @RequestBody CreateCourseRequest request, HttpServletRequest httpRequest
    ) {
        try {
            courseService.editCourse(httpRequest.getHeader("Authorization"), id, request, false,true);
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
        try {
            return ResponseEntity.ok(courseService.getHomePageInfo());
        } catch (CustomException e) {
            CustomWarning warning = new CustomWarning(HttpStatus.BAD_REQUEST, e.getMessage());
            return new ResponseEntity<>(warning, new HttpHeaders(), warning.getStatus());
        }
    }

    @PostMapping("/getFilteredClasses")
    public ResponseEntity<Object> getFilteredCourses(@RequestBody FilterRequest filterRequest) {
        try {
            return ResponseEntity.ok(courseService.getFilteredLessons(filterRequest));
        } catch (IllegalArgumentException | CustomException e) {
        CustomWarning warning = new CustomWarning(HttpStatus.BAD_REQUEST, e.getMessage());
        return new ResponseEntity<>(warning, new HttpHeaders(), warning.getStatus());
        }
    }

    @PostMapping("/editThemaDescription/{id}")
    public ResponseEntity<Object> editThemaDescription(@PathVariable int id, @RequestBody ThemaRequest themaRequest,
                                                       HttpServletRequest httpRequest) {
        try {
            courseService.editDescription(themaRequest.getDescription(), id, httpRequest.getHeader("Authorization"));
        } catch (CustomException e) {
            CustomWarning warning = new CustomWarning(e.getStatus(), e.getMessage());
            return new ResponseEntity<>(warning, new HttpHeaders(), warning.getStatus());
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/addLinkToRecording/{id}")
    public ResponseEntity<Object> addLinkToRecording(@PathVariable int id, @RequestBody ThemaRequest themaRequest,
                                                       HttpServletRequest httpRequest) {
        try {
            courseService.addLinkToRecording(themaRequest.getLinkToRecording(), id, httpRequest.getHeader("Authorization"));
        } catch (CustomException e) {
            CustomWarning warning = new CustomWarning(e.getStatus(), e.getMessage());
            return new ResponseEntity<>(warning, new HttpHeaders(), warning.getStatus());
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/getLinkToRecording/{id}")
    public ResponseEntity<Object> getLinkToRecording(@PathVariable int id, HttpServletRequest httpRequest) {
        try {
            return ResponseEntity.ok(courseService.getLinkToRecording(id, httpRequest.getHeader("Authorization")));
        } catch (CustomException e) {
            CustomWarning warning = new CustomWarning(HttpStatus.BAD_REQUEST, e.getMessage());
            return new ResponseEntity<>(warning, new HttpHeaders(), warning.getStatus());
        }
    }

    @GetMapping("/deleteCourse/{id}")
    public ResponseEntity<Object> deleteCourse(@PathVariable int id, HttpServletRequest httpRequest) {
        try {
            courseService.deleteCourse(id, httpRequest.getHeader("Authorization"));
        } catch (CustomException e) {
            CustomWarning warning = new CustomWarning(HttpStatus.BAD_REQUEST, e.getMessage());
            return new ResponseEntity<>(warning, new HttpHeaders(), warning.getStatus());
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/addResource/{id}")
    public ResponseEntity<Object> getResource(@PathVariable int id, @RequestParam("file") MultipartFile[] requestFiles,
                                                     HttpServletRequest httpRequest) {

        if (requestFiles.length > 1) {
            CustomWarning warning = new CustomWarning(HttpStatus.BAD_REQUEST, "Може да качите само един файл");
            return new ResponseEntity<>(warning, new HttpHeaders(), warning.getStatus());
        }
        try {
        if (requestFiles.length == 0) {
                String file = courseService.addResource(id, httpRequest.getHeader("Authorization"), null);
                File file1 = new File(file);
                file1.delete();
            }
        } catch (CustomException e) {
            CustomWarning warning = new CustomWarning(HttpStatus.BAD_REQUEST, e.getMessage());
            return new ResponseEntity<>(warning, new HttpHeaders(), warning.getStatus());
        }
            File newFile;
            Random random = new Random();
            newFile = new File("Assignment_" + random.nextInt(Integer.MAX_VALUE) + "_"
                    + filenameCounter + "_" + requestFiles[0].getOriginalFilename());
            filenameCounter++;
            try {
                requestFiles[0].transferTo(newFile);
                courseService.addResource(id, httpRequest.getHeader("Authorization"), newFile.getPath());
            } catch (IOException | CustomException e) {
                CustomWarning warning = new CustomWarning(HttpStatus.BAD_REQUEST, e.getMessage());
                return new ResponseEntity<>(warning, new HttpHeaders(), warning.getStatus());
            }
            return new ResponseEntity<>(HttpStatus.OK);
    }

//    @GetMapping("/getResource/{id}")
//    public ResponseEntity<Object> getResource(@PathVariable int id, HttpServletRequest httpRequest) {
//        try {
//            return ResponseEntity.ok(courseService.getLinkToRecording(id, httpRequest.getHeader("Authorization")));
//        } catch (CustomException e) {
//            CustomWarning warning = new CustomWarning(HttpStatus.BAD_REQUEST, e.getMessage());
//            return new ResponseEntity<>(warning, new HttpHeaders(), warning.getStatus());
//        }
//    }

    @PostMapping("/leaveReview")
    public ResponseEntity<Object> leaveReview(@RequestBody ReviewRequest reviewRequest, HttpServletRequest httpRequest) {
        try {
            courseService.leaveReview(httpRequest.getHeader("Authorization"), reviewRequest);
        } catch (IllegalArgumentException e) {
            CustomWarning warning = new CustomWarning(HttpStatus.BAD_REQUEST, e.getMessage());
            return new ResponseEntity<>(warning, new HttpHeaders(), warning.getStatus());
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/getReviews")
    public ResponseEntity<Object> getReviews(@RequestBody LessonRequest lessonRequest) {
        return ResponseEntity.ok(courseService.getLessonReviews(lessonRequest.getId(), lessonRequest.getSort(), lessonRequest.getPage()));
    }

    @GetMapping("/getCourseFilters")
    public ResponseEntity<Object> getCourseFilters() {
        return ResponseEntity.ok(courseService.getFilters(false));
    }

    @GetMapping("/getLessonFilters")
    public ResponseEntity<Object> getLessonFilters() {
        return ResponseEntity.ok(courseService.getFilters(true));
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

    @GetMapping("/getCoursePage/{id}")
    public ResponseEntity<Object> getCoursePage(@PathVariable int id) {
        try {
            return ResponseEntity.ok(courseService.getLessonById(id));
        } catch (CustomException e) {
            CustomWarning warning = new CustomWarning(e.getStatus(), e.getMessage());
            return new ResponseEntity<>(warning, new HttpHeaders(), warning.getStatus());
        }
    }

    @GetMapping("/getCourseInformation/{id}")
    public ResponseEntity<Object> getCourseInformation(@PathVariable int id, HttpServletRequest httpRequest) {
        try {
            return ResponseEntity.ok(courseService.getCourseInformation(id, httpRequest.getHeader("Authorization")));
        } catch (CustomException e) {
            CustomWarning warning = new CustomWarning(e.getStatus(), e.getMessage());
            return new ResponseEntity<>(warning, new HttpHeaders(), warning.getStatus());
        }
    }

    @GetMapping("/getLessonClassroomPage/{id}")
    public ResponseEntity<Object> getClassroomPage(HttpServletRequest httpServletRequest, @PathVariable int id) {
        try {
            return ResponseEntity.ok(courseService.getClassroomPage(httpServletRequest.getHeader("Authorization"), id, true));
        } catch (CustomException e) {
            CustomWarning warning = new CustomWarning(e.getStatus(), e.getMessage());
            return new ResponseEntity<>(warning, new HttpHeaders(), warning.getStatus());
        }
    }

    @GetMapping("/getCourseClassroomPage/{id}")
    public ResponseEntity<Object> getCourseClassroomPage(HttpServletRequest httpServletRequest, @PathVariable int id) {
        try {
            return ResponseEntity.ok(courseService.getClassroomPage(httpServletRequest.getHeader("Authorization"), id, false));
        } catch (CustomException e) {
            CustomWarning warning = new CustomWarning(e.getStatus(), e.getMessage());
            return new ResponseEntity<>(warning, new HttpHeaders(), warning.getStatus());
        }
    }

    @PostMapping("/getDashboard/student")
    public ResponseEntity<Object> getStudentDashboard(HttpServletRequest httpServletRequest, @RequestParam String lessonStatus,
                                                      @RequestParam String sort) {
        try {
            return ResponseEntity.ok(courseService.getStudentAll(httpServletRequest.getHeader("Authorization"),
                    LessonStatus.valueOf(lessonStatus), sort));
        } catch (CustomException e) {
            CustomWarning warning = new CustomWarning(e.getStatus(), e.getMessage());
            return new ResponseEntity<>(warning, new HttpHeaders(), warning.getStatus());
        } catch (IllegalArgumentException e) {
            CustomWarning warning = new CustomWarning(HttpStatus.BAD_REQUEST, e.getMessage());
            return new ResponseEntity<>(warning, new HttpHeaders(), warning.getStatus());
        }
    }

    @GetMapping("/likeCourse/{id}")
    public ResponseEntity<Object> likeCourse(@PathVariable int id, HttpServletRequest httpServletRequest) {
        try {
            courseService.likeCourse(httpServletRequest.getHeader("Authorization"), id);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (CustomException e) {
            CustomWarning warning = new CustomWarning(e.getStatus(), e.getMessage());
            return new ResponseEntity<>(warning, new HttpHeaders(), warning.getStatus());
        }
    }

    @GetMapping("/getTeacherCourses/{status}")
    public ResponseEntity<Object> getTeacherCourses(HttpServletRequest httpServletRequest, @PathVariable String status) {
        try {
            return ResponseEntity.ok(courseService.getTeacherLessons(httpServletRequest.getHeader("Authorization"),
                    status, false, false));
        } catch (IllegalArgumentException | CustomException e) {
            CustomWarning warning = new CustomWarning(HttpStatus.BAD_REQUEST, e.getMessage());
            return new ResponseEntity<>(warning, new HttpHeaders(), warning.getStatus());
        }
    }

    @GetMapping("/getTeacherLessons/{status}")
    public ResponseEntity<Object> getTeacherLessons(HttpServletRequest httpServletRequest, @PathVariable String status) {
        try {
            return ResponseEntity.ok(courseService.getTeacherLessons(httpServletRequest.getHeader("Authorization"),
                    status, true, false));
        } catch (IllegalArgumentException | CustomException e) {
            CustomWarning warning = new CustomWarning(HttpStatus.BAD_REQUEST, e.getMessage());
            return new ResponseEntity<>(warning, new HttpHeaders(), warning.getStatus());
        }
    }

    @GetMapping("/getLessonDates/{id}")
    public ResponseEntity<Object> getLessonDates(HttpServletRequest httpServletRequest, @PathVariable int id) {
        try {
            return ResponseEntity.ok(courseService.getLessonTerminsTeacher(httpServletRequest.getHeader("Authorization"), id));
        } catch (IllegalArgumentException | CustomException e) {
            CustomWarning warning = new CustomWarning(HttpStatus.BAD_REQUEST, e.getMessage());
            return new ResponseEntity<>(warning, new HttpHeaders(), warning.getStatus());
        }
    }

    @GetMapping("/publishDraft/{id}")
    public ResponseEntity<Object> publishLesson(HttpServletRequest httpServletRequest, @PathVariable int id) {
        try {
            courseService.publishDraft(httpServletRequest.getHeader("Authorization"), id);
            return ResponseEntity.ok("Успешно публикуване на урока");
        } catch (IllegalArgumentException | CustomException e) {
            CustomWarning warning = new CustomWarning(HttpStatus.BAD_REQUEST, e.getMessage());
            return new ResponseEntity<>(warning, new HttpHeaders(), warning.getStatus());
        }
    }

    @GetMapping("/getCourseDates/{id}")
    public ResponseEntity<Object> getCourseDates(HttpServletRequest httpServletRequest, @PathVariable int id) {
        try {
            return ResponseEntity.ok(courseService.getCourseTerminsTeacher(httpServletRequest.getHeader("Authorization"), id));
        } catch (IllegalArgumentException | CustomException e) {
            CustomWarning warning = new CustomWarning(HttpStatus.BAD_REQUEST, e.getMessage());
            return new ResponseEntity<>(warning, new HttpHeaders(), warning.getStatus());
        }
    }

    @PostMapping("/addDate/{id}")
    public ResponseEntity<Object> addDate(HttpServletRequest httpServletRequest, @PathVariable int id, @RequestBody CourseTerminRequestResponse courseRequest) {
        try {
            courseService.addDate(courseRequest, id, httpServletRequest.getHeader("Authorization"));
        } catch (CustomException e) {
            CustomWarning warning = new CustomWarning(e.getStatus(), e.getMessage());
            return new ResponseEntity<>(warning, new HttpHeaders(), warning.getStatus());
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/getTeacherUpcoming")
    public ResponseEntity<Object> getTeacherUpcoming(HttpServletRequest httpServletRequest) {
        try {
            return ResponseEntity.ok(courseService.getTeacherLessons(httpServletRequest.getHeader("Authorization"),
                    "", false, true));
        } catch (IllegalArgumentException | CustomException e) {
            CustomWarning warning = new CustomWarning(HttpStatus.BAD_REQUEST, e.getMessage());
            return new ResponseEntity<>(warning, new HttpHeaders(), warning.getStatus());
        }
    }

    @GetMapping("/getSubjectGrades")
    public ResponseEntity<Object> getSubjectGrades() {
        return ResponseEntity.ok(courseService.getSubjectGrade());
    }




//    @PostMapping("/getLikedCourses")
//    public ResponseEntity<Object> getLikedCourses(HttpServletRequest httpServletRequest) {
//        try {
//            return ResponseEntity.ok(courseService.getFavouriteCourses(httpServletRequest.getHeader("Authorization")));
//        } catch (CustomException e) {
//            CustomWarning warning = new CustomWarning(e.getStatus(), e.getMessage());
//            return new ResponseEntity<>(warning, new HttpHeaders(), warning.getStatus());
//        }
//    }


}
