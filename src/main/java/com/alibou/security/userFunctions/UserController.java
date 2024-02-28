package com.alibou.security.userFunctions;


import com.alibou.security.coursesServiceController.CreateCourseRequest;
import com.alibou.security.exceptionHandling.CustomException;
import com.alibou.security.exceptionHandling.CustomWarning;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@CrossOrigin
public class UserController {

    private int filenameCounter = 0;

    private String defaultImagePath = "defaultImage_";

    private Random random = new Random();
    private final UserService userService;

    @MessageMapping("/user.addUser")
    public ChatUser addUser(
            @Payload ChatUser user
    ) {
        userService.saveUser(user);
        return user;
    }

    @MessageMapping("/user.disconnectUser")
    public ChatUser disconnectUser(
            @Payload ChatUser user
    ) {
        userService.disconnect(user);
        return user;
    }

    @GetMapping("/users")
    public ResponseEntity<List<ChatUser>> findConnectedUsers() {
        return ResponseEntity.ok(userService.findConnectedUsers());
    }

    @GetMapping("/getMessages")
    public ResponseEntity<Object> getContacts(HttpServletRequest httpRequest) {
        try {
            return ResponseEntity.ok(userService.getContacts(httpRequest.getHeader("Authorization")));
        } catch (CustomException e) {
            e.printStackTrace();
            CustomWarning warning = new CustomWarning(e.getStatus(), e.getMessage());
            return new ResponseEntity<>(warning, new HttpHeaders(), warning.getStatus());
        }
    }

    @GetMapping("/getMessageFromMessageTab/{messageid}")
    public ResponseEntity<Object> getMessagesFromMessageTab(HttpServletRequest httpRequest, @PathVariable int messageid) {
        try {
            return ResponseEntity.ok(userService.getMessages(httpRequest.getHeader("Authorization"), messageid, true));
        } catch (CustomException e) {
            e.printStackTrace();
            CustomWarning warning = new CustomWarning(e.getStatus(), e.getMessage());
            return new ResponseEntity<>(warning, new HttpHeaders(), warning.getStatus());
        }
    }
    @GetMapping("/getMessage/{userid}")
    public ResponseEntity<Object> getMessages(HttpServletRequest httpRequest, @PathVariable int userid) {
        try {
            return ResponseEntity.ok(userService.getMessages(httpRequest.getHeader("Authorization"), userid, false));
        } catch (CustomException e) {
            e.printStackTrace();
            CustomWarning warning = new CustomWarning(e.getStatus(), e.getMessage());
            return new ResponseEntity<>(warning, new HttpHeaders(), warning.getStatus());
        }
    }

    @GetMapping("/getTeacherCalendar")
    public ResponseEntity<Object> getTeacherCalendar(HttpServletRequest httpRequest) {
        try {
            return ResponseEntity.ok(userService.getCalendarTeacher(httpRequest.getHeader("Authorization")));
        } catch (CustomException e) {
            e.printStackTrace();
            CustomWarning warning = new CustomWarning(e.getStatus(), e.getMessage());
            return new ResponseEntity<>(warning, new HttpHeaders(), warning.getStatus());
        }
    }

    @GetMapping("/getStudentCalendar")
    public ResponseEntity<Object> getStudentCalendar(HttpServletRequest httpRequest) {
        try {
            return ResponseEntity.ok(userService.getCalendarStudent(httpRequest.getHeader("Authorization")));
        } catch (CustomException e) {
            e.printStackTrace();
            CustomWarning warning = new CustomWarning(e.getStatus(), e.getMessage());
            return new ResponseEntity<>(warning, new HttpHeaders(), warning.getStatus());
        }
    }

    @GetMapping("/getStudentProfile")
    public ResponseEntity<Object> getStudentProfile(HttpServletRequest httpRequest) {
        try {
            return ResponseEntity.ok(userService.getStudentProfile(httpRequest.getHeader("Authorization")));
        } catch (CustomException e) {
            e.printStackTrace();
            CustomWarning warning = new CustomWarning(e.getStatus(), e.getMessage());
            return new ResponseEntity<>(warning, new HttpHeaders(), warning.getStatus());
        }
    }

    @PostMapping("/editStudentProfile")
    public ResponseEntity<Object> editStudentProfile(@RequestBody StudentProfileRequest request,
                                                HttpServletRequest httpRequest) {
        try {
            userService.editStudentProfile(request, httpRequest.getHeader("Authorization"));
        } catch (CustomException e) {
            e.printStackTrace();
            CustomWarning warning = new CustomWarning(e.getStatus(), e.getMessage());
            return new ResponseEntity<>(warning, new HttpHeaders(), warning.getStatus());
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/getTeacherProfile")
    public ResponseEntity<Object> getTeacherProfile(HttpServletRequest httpRequest) {
        try {
            return ResponseEntity.ok(userService.getTeacherProfile(httpRequest.getHeader("Authorization")));
        } catch (CustomException e) {
            e.printStackTrace();
            CustomWarning warning = new CustomWarning(e.getStatus(), e.getMessage());
            return new ResponseEntity<>(warning, new HttpHeaders(), warning.getStatus());
        }
    }

    @PostMapping("/editTeacherProfile")
    public ResponseEntity<Object> editTeacherProfile(@RequestBody TeacherProfileRequest request,
                                                     HttpServletRequest httpRequest) {
        try {
            userService.editTeacherProfile(request, httpRequest.getHeader("Authorization"));
        } catch (CustomException e) {
            e.printStackTrace();
            CustomWarning warning = new CustomWarning(e.getStatus(), e.getMessage());
            return new ResponseEntity<>(warning, new HttpHeaders(), warning.getStatus());
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

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
                    request.getPicture(), request.getGender(), request.getCity(), request.getDescription(), request.getSubjects(),
                    request.getDegree(), request.getSchool(), request.getUniversity(), request.getSpecialty(), request.getExperienceRequests());
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
    //TODO Add image urls in all the endpoints

    @PostMapping("/uploadImageStudent/{imageId}")
    public ResponseEntity<Object> setStudentImage(@RequestParam("file") MultipartFile[] requestFiles, @PathVariable int imageId,
                                                  HttpServletRequest httpRequest) {
        try {
            if (imageId > 0 && imageId < 5) {
                userService.saveStudentImage(httpRequest.getHeader("Authorization"), defaultImagePath + imageId);
            } else {
                if (requestFiles.length > 1) {
                    CustomWarning warning = new CustomWarning(HttpStatus.BAD_REQUEST, "Може да качите само един файл");
                    return new ResponseEntity<>(warning, new HttpHeaders(), warning.getStatus());
                }
                Path newFile;
                newFile = Paths.get("Student_Image_" + UUID.randomUUID().toString() + "_" + requestFiles[0].getOriginalFilename().replace(" ", ""));
                filenameCounter++;
                Files.copy(requestFiles[0].getInputStream(), newFile);
                userService.saveStudentImage(httpRequest.getHeader("Authorization"), newFile.toString());
            }
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

    @GetMapping("/getTeacherImage/{id}")
    public ResponseEntity<Object> getTeacherImage(@PathVariable int id) throws IOException {
        String path;
        try {
            path = userService.getTeacherImage(id);
        } catch (CustomException e) {
            CustomWarning warning = new CustomWarning(e.getStatus(), e.getMessage());
            return new ResponseEntity<>(warning, new HttpHeaders(), warning.getStatus());
        }

        // The file to be downloaded.
        Path file = Paths.get(path);

        // Get the media type of the file
        String contentType = Files.probeContentType(file);
        if (contentType == null) {
            // Use the default media type
            contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }

        // Load file data into a byte array
        byte[] fileData = Files.readAllBytes(file);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", contentType);
        headers.add("Content-Disposition", "attachment; filename=\"%s\"".formatted(file.getFileName()));

        return new ResponseEntity<>(fileData, headers, HttpStatus.OK);
    }

    @GetMapping("/getStudentImage/{id}")
    public ResponseEntity<Object> getStudentImage(@PathVariable int id) throws IOException {
        String path;
        try {
            path = userService.getStudentImage(id);
        } catch (CustomException e) {
            CustomWarning warning = new CustomWarning(e.getStatus(), e.getMessage());
            return new ResponseEntity<>(warning, new HttpHeaders(), warning.getStatus());
        }

        // The file to be downloaded.
        Path file = Paths.get(path);

        // Get the media type of the file
        String contentType = Files.probeContentType(file);
        if (contentType == null) {
            // Use the default media type
            contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }

        // Load file data into a byte array
        byte[] fileData = Files.readAllBytes(file);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", contentType);
        headers.add("Content-Disposition", "attachment; filename=\"%s\"".formatted(file.getFileName()));

        return new ResponseEntity<>(fileData, headers, HttpStatus.OK);
    }

    @GetMapping("/getTeacherProfile/{id}")
    public ResponseEntity<Object> getTeacherProfile(@PathVariable int id, HttpServletRequest httpRequest) {
        try {
            return ResponseEntity.ok(userService.getTeacherPage(id, httpRequest.getHeader("Authorization")));
        } catch (CustomException e) {
            e.printStackTrace();
            CustomWarning warning = new CustomWarning(e.getStatus(), e.getMessage());
            return new ResponseEntity<>(warning, new HttpHeaders(), warning.getStatus());
        }
    }

    @GetMapping("/getUser")
    public ResponseEntity<Object> getUser(HttpServletRequest httpServletRequest) {
        return ResponseEntity.ok(userService.getUser(httpServletRequest));
    }

    @GetMapping("/getLikedTeachers/{page}")
    public ResponseEntity<Object> getLikedTeachers(HttpServletRequest httpServletRequest, @PathVariable int page) {
        return ResponseEntity.ok(userService.getFavouriteTeachers(httpServletRequest.getHeader("Authorization"), page));
    }

    @GetMapping("/dislikeTeacher/{id}")
    public ResponseEntity<Object> dislikeCourse(@PathVariable int id, HttpServletRequest httpRequest) {
        try {
            userService.dislikeTeacher(httpRequest.getHeader("Authorization"), id);
        } catch (CustomException e) {
            CustomWarning warning = new CustomWarning(HttpStatus.BAD_REQUEST, e.getMessage());
            return new ResponseEntity<>(warning, new HttpHeaders(), warning.getStatus());
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/uploadImageTeacher")
    public ResponseEntity<Object> uploadImage(@RequestParam("file") MultipartFile[] requestFiles,
                                              HttpServletRequest httpRequest) {
        if (requestFiles.length > 1) {
            CustomWarning warning = new CustomWarning(HttpStatus.BAD_REQUEST, "Може да качите само един файл");
            return new ResponseEntity<>(warning, new HttpHeaders(), warning.getStatus());
        }
        Path newFile;
        newFile = Paths.get("Resource_" + UUID.randomUUID().toString() + "_" + requestFiles[0].getOriginalFilename().replace(" ", ""));
        filenameCounter++;
        try {
            Files.copy(requestFiles[0].getInputStream(), newFile);
            userService.saveTeacherImage(httpRequest.getHeader("Authorization"), newFile.toString());
        } catch (IOException | CustomException e) {
            CustomWarning warning = new CustomWarning(HttpStatus.BAD_REQUEST, e.getMessage());
            return new ResponseEntity<>(warning, new HttpHeaders(), warning.getStatus());
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/images/{filename}")
    public Resource getImage(@PathVariable String filename) throws CustomException, MalformedURLException {
        Path imagePath = Paths.get(filename);
        Resource resource = new UrlResource(imagePath.toUri());

        if (resource.exists() && resource.isReadable()) {
            return resource;
        } else {
            throw new CustomException(HttpStatus.NOT_FOUND, "Failed to load image: " + filename);
        }
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
