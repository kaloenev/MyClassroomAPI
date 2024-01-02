package com.alibou.security.userFunctions;


import com.alibou.security.coursesServiceController.CreateCourseRequest;
import com.alibou.security.exceptionHandling.CustomException;
import com.alibou.security.exceptionHandling.CustomWarning;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@CrossOrigin
public class UserController {

    private final UserService userService;

    @GetMapping("/verifyTeacher/form")
    public ResponseEntity<Object> getVerificationForm() {
        return ResponseEntity.ok(userService.getVerificationForm());
    }

    @PostMapping("/verifyTeacher")
    public ResponseEntity<Object> verifyTeacher(@RequestBody VerifyTeacherRequest request,
                                                HttpServletRequest httpRequest) {
        int teacherId;
        try {
        teacherId = userService.verifyTeacher(httpRequest.getHeader("Authorization"), request.getName(), request.getSurname(),
                request.getGender(), request.getCity(), request.getDescription(), request.getSubjects(),
                request.getDegree(), request.getSchool(), request.getUniversity(), request.getSpecialty(), request.getExperience());
            } catch (IOException e) {
                CustomWarning warning = new CustomWarning(HttpStatus.BAD_REQUEST, e.getMessage());
                return new ResponseEntity<>(warning, new HttpHeaders(), warning.getStatus());
            } catch (CustomException e) {
            e.printStackTrace();
            CustomWarning warning = new CustomWarning(e.getStatus(), e.getMessage());
            return new ResponseEntity<>(warning, new HttpHeaders(), warning.getStatus());
        }

        return ResponseEntity.ok(teacherId);
    }

    @PostMapping("/setTeacherImage/{id}")
    public ResponseEntity<Object> setTeacherImage(@RequestParam("file") MultipartFile requestFile,
                                                  HttpServletRequest httpRequest, @PathVariable int id) {
        try {
            userService.saveTeacherImage(httpRequest.getHeader("Authorization"), id, requestFile);
        } catch (IOException e) {
            CustomWarning warning = new CustomWarning(HttpStatus.BAD_REQUEST, e.getMessage());
            return new ResponseEntity<>(warning, new HttpHeaders(), warning.getStatus());
        } catch (CustomException e) {
            e.printStackTrace();
            CustomWarning warning = new CustomWarning(e.getStatus(), e.getMessage());
            return new ResponseEntity<>(warning, new HttpHeaders(), warning.getStatus());
        }

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/setStudentImage/id={id}&image={imageId} ")
    public ResponseEntity<Object> setStudentImage(@RequestParam("file") MultipartFile requestFile, @PathVariable int imageId,
                                                  HttpServletRequest httpRequest, @PathVariable int id) {
        try {
            if (imageId > 0 && imageId < 5) {
                userService.saveStudentDefaultImage(httpRequest.getHeader("Authorization"), imageId);
            }
            else userService.saveStudentImage(httpRequest.getHeader("Authorization"), id, requestFile);
        } catch (IOException e) {
            CustomWarning warning = new CustomWarning(HttpStatus.BAD_REQUEST, e.getMessage());
            return new ResponseEntity<>(warning, new HttpHeaders(), warning.getStatus());
        } catch (CustomException e) {
            e.printStackTrace();
            CustomWarning warning = new CustomWarning(e.getStatus(), e.getMessage());
            return new ResponseEntity<>(warning, new HttpHeaders(), warning.getStatus());
        }

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/getTeacherProfile/{id}")
    public ResponseEntity<Object> getTeacherProfile(@PathVariable int id) {
        try {
            return ResponseEntity.ok(userService.getTeacherPage(id));
        }
        catch (CustomException e) {
            e.printStackTrace();
            CustomWarning warning = new CustomWarning(e.getStatus(), e.getMessage());
            return new ResponseEntity<>(warning, new HttpHeaders(), warning.getStatus());
        }
    }

    @GetMapping("/getUser")
    public ResponseEntity<Object> getTeacherProfile(HttpServletRequest httpServletRequest) {
        return ResponseEntity.ok(userService.getUser(httpServletRequest));
    }

    @GetMapping("/getLikedTeachers")
    public ResponseEntity<Object> getLikedTeachers(HttpServletRequest httpServletRequest) {
        return ResponseEntity.ok(userService.getFavouriteTeachers(httpServletRequest.getHeader("Authorization")));
    }

    @GetMapping("/likeTeacher/{id}")
    public ResponseEntity<Object> likeTeacher(@PathVariable int id, HttpServletRequest httpServletRequest) {
        userService.likeTeacher(httpServletRequest.getHeader("Authorization"), id);
        return new ResponseEntity<>(HttpStatus.OK);
    }





//    @PostMapping("/exclusive/description")
//    public ResponseEntity<Object> saveDescription(@RequestParam("file") MultipartFile[] requestFiles,
//                                                  HttpServletRequest httpRequest) {
//        List<File> files = new ArrayList<>();
//        for (MultipartFile requestFile : requestFiles) {
//            if (requestFile.isEmpty()) {
//                FallobWarning warning = new FallobWarning(HttpStatus.BAD_REQUEST, JOB_DESCRIPTION_EMPTY);
//                return new ResponseEntity<>(warning, new HttpHeaders(), warning.getStatus());
//            }
//            File newFile;
//            // for tests
//            if (configuration.getDescriptionsbasePath().isEmpty()) {
//                newFile = new File(FILE_NAME + filenameCounter + FILE_EXTENSION);
//            }
//            // real case
//            else {
//                newFile = new File(configuration.getDescriptionsbasePath() + DIRECTORY_SEPARATOR + FILE_NAME
//                        + filenameCounter + FILE_EXTENSION);
//            }
//            filenameCounter++;
//            try {
//                requestFile.transferTo(newFile);
//            } catch (IOException e) {
//                FallobWarning warning = new FallobWarning(HttpStatus.BAD_REQUEST, e.getMessage());
//                return new ResponseEntity<>(warning, new HttpHeaders(), warning.getStatus());
//            }
//            files.add(newFile);
//        }
//        String username = (String) httpRequest.getAttribute(USERNAME);
//        JobDescription jobDescription = new JobDescription(files, SubmitType.EXCLUSIVE);
//        int descriptionId;
//        try {
//            descriptionId = jobSubmitCommand.saveJobDescription(username, jobDescription);
//        } catch (FallobException exception) {
//            exception.printStackTrace();
//            FallobWarning warning = new FallobWarning(exception.getStatus(), exception.getMessage());
//            return new ResponseEntity<>(warning, new HttpHeaders(), warning.getStatus());
//        }
//        return ResponseEntity.ok(new SubmitDescriptionResponse(descriptionId));
//    }
}
