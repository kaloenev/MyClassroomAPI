package com.alibou.security.coursesServiceController;


import com.alibou.security.exceptionHandling.CustomException;
import com.alibou.security.exceptionHandling.CustomWarning;
import com.alibou.security.lessons.LessonStatus;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Random;

@RestController
@RequestMapping("/api/v1/lessons")
@RequiredArgsConstructor
@CrossOrigin
public class CourseController {

    private final CourseService courseService;
    private int filenameCounter = 0;

    private Random random = new Random();

    @PostMapping("/createCourse")
    public ResponseEntity<Object> createCourse(
            @RequestBody CreateCourseRequest request, HttpServletRequest httpRequest
    ) {
        try {
            courseService.createCourse(httpRequest.getHeader("Authorization"), request, false, false);
        } catch (CustomException e) {
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
        } catch (CustomException e) {
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
            courseService.editCourse(httpRequest.getHeader("Authorization"), id, request, true, false);
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
            courseService.editCourse(httpRequest.getHeader("Authorization"), id, request, true, true);
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
            courseService.editCourse(httpRequest.getHeader("Authorization"), id, request, false, true);
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
    public ResponseEntity<Object> getHomePage(HttpServletRequest httpRequest) {
        try {
            return ResponseEntity.ok(courseService.getHomePageInfo(httpRequest.getHeader("Authorization")));
        } catch (CustomException e) {
            CustomWarning warning = new CustomWarning(HttpStatus.BAD_REQUEST, e.getMessage());
            return new ResponseEntity<>(warning, new HttpHeaders(), warning.getStatus());
        }
    }

    @PostMapping("/getFilteredClasses")
    public ResponseEntity<Object> getFilteredCourses(@RequestBody FilterRequest filterRequest, HttpServletRequest httpRequest) {
        try {
            return ResponseEntity.ok(courseService.getFilteredLessons(filterRequest, httpRequest.getHeader("Authorization")));
        } catch (IllegalArgumentException | CustomException e) {
            CustomWarning warning = new CustomWarning(HttpStatus.BAD_REQUEST, e.getMessage());
            return new ResponseEntity<>(warning, new HttpHeaders(), warning.getStatus());
        }
    }

    @GetMapping("/getTeacherCalendarEvents/{date}")
    public ResponseEntity<Object> getTeacherCalendarEvents(HttpServletRequest httpRequest, @PathVariable String date) {
        try {
            return ResponseEntity.ok(courseService.getEventsForTheDayTeacher(httpRequest.getHeader("Authorization"), date));
        } catch (CustomException e) {
            e.printStackTrace();
            CustomWarning warning = new CustomWarning(e.getStatus(), e.getMessage());
            return new ResponseEntity<>(warning, new HttpHeaders(), warning.getStatus());
        }
    }

    @GetMapping("/getStudentCalendarEvents/{date}")
    public ResponseEntity<Object> getStudentCalendarEvents(HttpServletRequest httpRequest, @PathVariable String date) {
        try {
            return ResponseEntity.ok(courseService.getEventsForTheDayStudent(httpRequest.getHeader("Authorization"), date));
        } catch (CustomException e) {
            e.printStackTrace();
            CustomWarning warning = new CustomWarning(e.getStatus(), e.getMessage());
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

    @GetMapping("/dislikeCourse/{id}")
    public ResponseEntity<Object> dislikeCourse(@PathVariable int id, HttpServletRequest httpRequest) {
        try {
            courseService.dislikeCourse(httpRequest.getHeader("Authorization"), id);
        } catch (CustomException e) {
            CustomWarning warning = new CustomWarning(HttpStatus.BAD_REQUEST, e.getMessage());
            return new ResponseEntity<>(warning, new HttpHeaders(), warning.getStatus());
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/addResource/{id}")
    public ResponseEntity<Object> addResource(@PathVariable int id, @RequestParam("file") MultipartFile[] requestFiles,
                                              HttpServletRequest httpRequest) {
        if (requestFiles.length > 1) {
            CustomWarning warning = new CustomWarning(HttpStatus.BAD_REQUEST, "Може да качите само един файл");
            return new ResponseEntity<>(warning, new HttpHeaders(), warning.getStatus());
        }
        Path newFile;
        newFile = Paths.get("Resource_" + random.nextInt(Integer.MAX_VALUE) + "_"
                + filenameCounter + "_" + requestFiles[0].getOriginalFilename());
        filenameCounter++;
        try {
            Files.copy(requestFiles[0].getInputStream(), newFile);
            courseService.addResource(id, httpRequest.getHeader("Authorization"), newFile.toString());
        } catch (IOException | CustomException e) {
            CustomWarning warning = new CustomWarning(HttpStatus.BAD_REQUEST, e.getMessage());
            return new ResponseEntity<>(warning, new HttpHeaders(), warning.getStatus());
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/deleteResource/{id}")
    public ResponseEntity<Object> deleteResource(@PathVariable int id, HttpServletRequest httpRequest) {
        try {
            String file = courseService.addResource(id, httpRequest.getHeader("Authorization"), null);
            File file1 = new File(file);
            file1.delete();
        } catch (CustomException e) {
            CustomWarning warning = new CustomWarning(HttpStatus.BAD_REQUEST, e.getMessage());
            return new ResponseEntity<>(warning, new HttpHeaders(), warning.getStatus());
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * @param id                themaId
     * @param assignmentRequest
     * @param httpRequest
     * @return
     */
    @PostMapping("/addAssignment/{id}")
    public ResponseEntity<Object> addAssignment(@PathVariable int id, @RequestBody AssignmentRequest assignmentRequest,
                                                HttpServletRequest httpRequest) {
        try {
            return ResponseEntity.ok(courseService.addAssignment(assignmentRequest, httpRequest.getHeader("Authorization"), id));
        } catch (CustomException e) {
            CustomWarning warning = new CustomWarning(e.getStatus(), e.getMessage());
            return new ResponseEntity<>(warning, new HttpHeaders(), warning.getStatus());
        }
    }

    /**
     * @param id                assignmentId
     * @param assignmentRequest
     * @param httpRequest
     * @return
     */
    @PostMapping("/editAssignment/{id}")
    public ResponseEntity<Object> editAssignment(@PathVariable int id, @RequestBody AssignmentRequest assignmentRequest,
                                                 HttpServletRequest httpRequest) {
        try {
            courseService.editAssignment(assignmentRequest, httpRequest.getHeader("Authorization"), id);
        } catch (CustomException e) {
            CustomWarning warning = new CustomWarning(e.getStatus(), e.getMessage());
            return new ResponseEntity<>(warning, new HttpHeaders(), warning.getStatus());
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/getAssignment/{id}")
    public ResponseEntity<Object> getAssignment(@PathVariable int id, HttpServletRequest httpRequest) {
        try {
            return ResponseEntity.ok(courseService.getAssignment(httpRequest.getHeader("Authorization"), id));
        } catch (CustomException e) {
            CustomWarning warning = new CustomWarning(HttpStatus.BAD_REQUEST, e.getMessage());
            return new ResponseEntity<>(warning, new HttpHeaders(), warning.getStatus());
        }
    }

    @GetMapping("/checkSolutions/{id}")
    public ResponseEntity<Object> checkSolutions(@PathVariable int id, HttpServletRequest httpRequest) {
        try {
            return ResponseEntity.ok(courseService.checkSolutions(httpRequest.getHeader("Authorization"), id));
        } catch (CustomException e) {
            CustomWarning warning = new CustomWarning(HttpStatus.BAD_REQUEST, e.getMessage());
            return new ResponseEntity<>(warning, new HttpHeaders(), warning.getStatus());
        }
    }

    @GetMapping("/getComments/{id}")
    public ResponseEntity<Object> getComments(@PathVariable int id, HttpServletRequest httpRequest) {
        try {
            return ResponseEntity.ok(courseService.getComments(httpRequest.getHeader("Authorization"), id));
        } catch (CustomException e) {
            CustomWarning warning = new CustomWarning(HttpStatus.BAD_REQUEST, e.getMessage());
            return new ResponseEntity<>(warning, new HttpHeaders(), warning.getStatus());
        }
    }

    @PostMapping("/uploadAssignmentFiles/{id}")
    public ResponseEntity<Object> uploadAssignmentFiles(@PathVariable int id, @RequestParam("file") MultipartFile[] requestFiles,
                                                        HttpServletRequest httpRequest) {
        System.out.println("Reached controller");
        //TODO Check if the person is using this only for a new Assignment (more than 4 files risk)
        if (requestFiles.length > 4) {
            CustomWarning warning = new CustomWarning(HttpStatus.BAD_REQUEST, "Може да качите до 4 файла");
            return new ResponseEntity<>(warning, new HttpHeaders(), warning.getStatus());
        }
        String paths;
        StringBuilder pathBuilder = new StringBuilder();
        for (MultipartFile requestFile : requestFiles) {
            Path newFile;
            newFile = Paths.get("Assignment_" + random.nextInt(Integer.MAX_VALUE) + "_"
                    + filenameCounter + "_" + requestFile.getOriginalFilename());
            filenameCounter++;
            try {
                Files.copy(requestFile.getInputStream(), newFile);
            } catch (IOException e) {
                CustomWarning warning = new CustomWarning(HttpStatus.BAD_REQUEST, e.getMessage());
                return new ResponseEntity<>(warning, new HttpHeaders(), warning.getStatus());
            }
            pathBuilder.append(newFile).append(",");
        }
        System.out.println("Saved files");
        if (pathBuilder.isEmpty()) {
            CustomWarning warning = new CustomWarning(HttpStatus.BAD_REQUEST, "Не сте качили валидни файлове");
            return new ResponseEntity<>(warning, new HttpHeaders(), warning.getStatus());
        }
        paths = pathBuilder.substring(0, pathBuilder.length() - 1);
        try {
            courseService.uploadAssignmentFiles(httpRequest.getHeader("Authorization"), id, paths);
        } catch (CustomException e) {
            CustomWarning warning = new CustomWarning(HttpStatus.BAD_REQUEST, e.getMessage());
            return new ResponseEntity<>(warning, new HttpHeaders(), warning.getStatus());
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/getAssignmentStudent/{id}")
    public ResponseEntity<Object> getAssignmentStudent(@PathVariable int id, HttpServletRequest httpRequest) {
        try {
            return ResponseEntity.ok(courseService.getAssignmentStudent(httpRequest.getHeader("Authorization"), id));
        } catch (CustomException e) {
            CustomWarning warning = new CustomWarning(e.getStatus(), e.getMessage());
            return new ResponseEntity<>(warning, new HttpHeaders(), warning.getStatus());
        }
    }

    @PostMapping("/uploadSolutionFiles/{id}")
    public ResponseEntity<Object> uploadSolutionFiles(@PathVariable int id, @RequestParam("file") MultipartFile[] requestFiles,
                                                      HttpServletRequest httpRequest) {
        System.out.println("Reached controller");
        if (requestFiles.length > 4) {
            CustomWarning warning = new CustomWarning(HttpStatus.BAD_REQUEST, "Може да качите до 4 файла");
            return new ResponseEntity<>(warning, new HttpHeaders(), warning.getStatus());
        }
        String paths;
        StringBuilder pathBuilder = new StringBuilder();
        for (MultipartFile requestFile : requestFiles) {
            Path newFile;
            newFile = Paths.get("Solution" + random.nextInt(Integer.MAX_VALUE) + "_"
                    + filenameCounter + "_" + requestFile.getOriginalFilename());
            filenameCounter++;
            try {
                Files.copy(requestFile.getInputStream(), newFile);
            } catch (IOException e) {
                CustomWarning warning = new CustomWarning(HttpStatus.BAD_REQUEST, e.getMessage());
                return new ResponseEntity<>(warning, new HttpHeaders(), warning.getStatus());
            }
            pathBuilder.append(newFile).append(",");
        }
        System.out.println("Saved solution files");
        if (pathBuilder.isEmpty()) {
            CustomWarning warning = new CustomWarning(HttpStatus.BAD_REQUEST, "Не сте качили валидни файлове");
            return new ResponseEntity<>(warning, new HttpHeaders(), warning.getStatus());
        }
        paths = pathBuilder.substring(0, pathBuilder.length() - 1);
        try {
            courseService.uploadSolutionFiles(httpRequest.getHeader("Authorization"), id, paths);
        } catch (CustomException e) {
            CustomWarning warning = new CustomWarning(HttpStatus.BAD_REQUEST, e.getMessage());
            return new ResponseEntity<>(warning, new HttpHeaders(), warning.getStatus());
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * Removes all files from assignment except the one whose paths are sent
     *
     * @param id
     * @param httpRequest
     * @param filesToDelete
     * @return
     */
    @GetMapping("/deleteAssignmentFile/{id}")
    public ResponseEntity<Object> deleteAssignmentFile(@PathVariable int id, HttpServletRequest httpRequest,
                                                       @RequestBody Map<String, String[]> filesToDelete) {
        String paths;
        StringBuilder pathBuilder = new StringBuilder();
        for (String path : filesToDelete.get("fileNames")) {
            pathBuilder.append(path).append(",");
        }
        if (pathBuilder.isEmpty()) {
            CustomWarning warning = new CustomWarning(HttpStatus.BAD_REQUEST, "Не сте качили валидни файлове");
            return new ResponseEntity<>(warning, new HttpHeaders(), warning.getStatus());
        }
        paths = pathBuilder.substring(0, pathBuilder.length() - 1);
        try {
            String deleteFiles = courseService.uploadAssignmentFiles(httpRequest.getHeader("Authorization"), id, paths);
            for (String file : deleteFiles.split(",")) {
                File file1 = new File(file);
                file1.delete();
            }
        } catch (CustomException e) {
            CustomWarning warning = new CustomWarning(HttpStatus.BAD_REQUEST, e.getMessage());
            return new ResponseEntity<>(warning, new HttpHeaders(), warning.getStatus());
        }

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/getAssignmentFile/{path}&&{id}")
    public ResponseEntity<Object> getAssignmentFile(@PathVariable String path, @PathVariable int id, HttpServletRequest httpRequest) throws IOException {

        try {
            courseService.getAssignmentFiles(httpRequest.getHeader("Authorization"), id, path);
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

    @GetMapping("/getSolutionFile/{path}&&{id}")
    public ResponseEntity<Object> getSolutionFile(@PathVariable String path, @PathVariable int id, HttpServletRequest httpRequest) throws IOException {

        try {
            courseService.getSolutionFiles(httpRequest.getHeader("Authorization"), id, path);
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

    @PostMapping("/leaveComment")
    public ResponseEntity<Object> leaveComment(@RequestBody ReviewRequest reviewRequest, HttpServletRequest httpRequest) {
        try {
            courseService.leaveComment(httpRequest.getHeader("Authorization"), reviewRequest.getLessonId(), reviewRequest.getMessage());
        } catch (IllegalArgumentException | CustomException e) {
            CustomWarning warning = new CustomWarning(HttpStatus.BAD_REQUEST, e.getMessage());
            return new ResponseEntity<>(warning, new HttpHeaders(), warning.getStatus());
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/reportCourse")
    public ResponseEntity<Object> reportCourse(@RequestBody ReportRequest reportRequest, HttpServletRequest httpRequest) {
        try {
            courseService.reportCourse(reportRequest.getTerminId(), reportRequest.getTitle(), reportRequest.getDescription(),
                    httpRequest.getHeader("Authorization"), false);
        } catch (IllegalArgumentException | CustomException e) {
            CustomWarning warning = new CustomWarning(HttpStatus.BAD_REQUEST, e.getMessage());
            return new ResponseEntity<>(warning, new HttpHeaders(), warning.getStatus());
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/reportLesson")
    public ResponseEntity<Object> reportLesson(@RequestBody ReportRequest reportRequest, HttpServletRequest httpRequest) {
        try {
            courseService.reportCourse(reportRequest.getTerminId(), reportRequest.getTitle(), reportRequest.getDescription(),
                    httpRequest.getHeader("Authorization"), true);
        } catch (IllegalArgumentException | CustomException e) {
            CustomWarning warning = new CustomWarning(HttpStatus.BAD_REQUEST, e.getMessage());
            return new ResponseEntity<>(warning, new HttpHeaders(), warning.getStatus());
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/getResourceFile/{id}")
    public ResponseEntity<Object> getResourceFile(@PathVariable int id, HttpServletRequest httpRequest) throws IOException {
        String path;
        try {
            path = courseService.getResourceFile(httpRequest.getHeader("Authorization"), id);
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

//    @RequestMapping(value = URIConstansts.GET_FILE, produces = { "application/json" }, method = RequestMethod.GET)
//    public @ResponseBody ResponseEntity getFile(@RequestParam(value="fileName", required=false) String fileName,HttpServletRequest request) throws IOException{
//
//        ResponseEntity respEntity = null;
//
//        byte[] reportBytes = null;
//        File result=new File("/home/arpit/Documents/PCAP/dummyPath/"+fileName);
//
//        if(result.exists()){
//            InputStream inputStream = new FileInputStream("/home/arpit/Documents/PCAP/dummyPath/"+fileName);
//            String type=result.toURL().openConnection().guessContentTypeFromName(fileName);
//
//            byte[]out=org.apache.commons.io.IOUtils.toByteArray(inputStream);
//
//            HttpHeaders responseHeaders = new HttpHeaders();
//            responseHeaders.add("content-disposition", "attachment; filename=" + fileName);
//            responseHeaders.add("Content-Type",type);
//
//            respEntity = new ResponseEntity(out, responseHeaders,HttpStatus.OK);
//        }else{
//            respEntity = new ResponseEntity ("File Not Found", HttpStatus.OK);
//        }
//        return respEntity;
//    }


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
        } catch (IllegalArgumentException | CustomException e) {
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
    public ResponseEntity<Object> getImage(@PathVariable("url") String url, HttpServletResponse response) {
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
    public ResponseEntity<Object> getCoursePage(@PathVariable int id, HttpServletRequest httpRequest) {
        try {
            return ResponseEntity.ok(courseService.getLessonById(id, httpRequest.getHeader("Authorization")));
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
            return ResponseEntity.ok(courseService.getClassroomPage(httpServletRequest.getHeader("Authorization"), id, true, true));
        } catch (CustomException e) {
            CustomWarning warning = new CustomWarning(e.getStatus(), e.getMessage());
            return new ResponseEntity<>(warning, new HttpHeaders(), warning.getStatus());
        }
    }

    @GetMapping("/getCourseClassroomPage/{id}")
    public ResponseEntity<Object> getCourseClassroomPage(HttpServletRequest httpServletRequest, @PathVariable int id) {
        try {
            return ResponseEntity.ok(courseService.getClassroomPage(httpServletRequest.getHeader("Authorization"), id, false, true));
        } catch (CustomException e) {
            CustomWarning warning = new CustomWarning(e.getStatus(), e.getMessage());
            return new ResponseEntity<>(warning, new HttpHeaders(), warning.getStatus());
        }
    }

    @GetMapping("/getLessonClassroomStudent/{id}")
    public ResponseEntity<Object> getLessonClassroomStudent(HttpServletRequest httpServletRequest, @PathVariable int id) {
        try {
            return ResponseEntity.ok(courseService.getClassroomPage(httpServletRequest.getHeader("Authorization"), id, true, false));
        } catch (CustomException e) {
            CustomWarning warning = new CustomWarning(e.getStatus(), e.getMessage());
            return new ResponseEntity<>(warning, new HttpHeaders(), warning.getStatus());
        }
    }

    @GetMapping("/getCourseClassroomStudent/{id}")
    public ResponseEntity<Object> getCourseClassroomStudent(HttpServletRequest httpServletRequest, @PathVariable int id) {
        try {
            return ResponseEntity.ok(courseService.getClassroomPage(httpServletRequest.getHeader("Authorization"), id, false, false));
        } catch (CustomException e) {
            CustomWarning warning = new CustomWarning(e.getStatus(), e.getMessage());
            return new ResponseEntity<>(warning, new HttpHeaders(), warning.getStatus());
        }
    }

    @GetMapping("/enrollStudentInCourse/{id}")
    public ResponseEntity<Object> enrollStudentInCourse(HttpServletRequest httpServletRequest, @PathVariable int id) {
        try {
            courseService.enrollUserInCourse(httpServletRequest.getHeader("Authorization"), id);
        } catch (CustomException e) {
            CustomWarning warning = new CustomWarning(e.getStatus(), e.getMessage());
            return new ResponseEntity<>(warning, new HttpHeaders(), warning.getStatus());
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/enrollStudentInLesson/{id}")
    public ResponseEntity<Object> enrollStudentInLesson(HttpServletRequest httpServletRequest, @PathVariable int id) {
        try {
            courseService.enrollUserInLesson(httpServletRequest.getHeader("Authorization"), id);
        } catch (CustomException e) {
            CustomWarning warning = new CustomWarning(e.getStatus(), e.getMessage());
            return new ResponseEntity<>(warning, new HttpHeaders(), warning.getStatus());
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/getStudentDashboard/All")
    public ResponseEntity<Object> getStudentDashboard(HttpServletRequest httpServletRequest, @RequestBody LessonRequest lessonRequest) {
        try {
            return ResponseEntity.ok(courseService.getStudentAll(httpServletRequest.getHeader("Authorization"),
                    lessonRequest, ""));
        } catch (CustomException e) {
            CustomWarning warning = new CustomWarning(e.getStatus(), e.getMessage());
            return new ResponseEntity<>(warning, new HttpHeaders(), warning.getStatus());
        } catch (IllegalArgumentException e) {
            CustomWarning warning = new CustomWarning(HttpStatus.BAD_REQUEST, e.getMessage());
            return new ResponseEntity<>(warning, new HttpHeaders(), warning.getStatus());
        }
    }

    @PostMapping("/getFavouriteCourses")
    public ResponseEntity<Object> getFavouriteCourses(HttpServletRequest httpServletRequest, @RequestBody LessonRequest lessonRequest) {
        try {
            return ResponseEntity.ok(courseService.getFavouriteCourses(httpServletRequest.getHeader("Authorization"),
                    lessonRequest.getSort(), lessonRequest.getPage()));
        } catch (CustomException e) {
            CustomWarning warning = new CustomWarning(e.getStatus(), e.getMessage());
            return new ResponseEntity<>(warning, new HttpHeaders(), warning.getStatus());
        } catch (IllegalArgumentException e) {
            CustomWarning warning = new CustomWarning(HttpStatus.BAD_REQUEST, e.getMessage());
            return new ResponseEntity<>(warning, new HttpHeaders(), warning.getStatus());
        }
    }

    @PostMapping("/getStudentDashboard/Courses")
    public ResponseEntity<Object> getStudentCourses(HttpServletRequest httpServletRequest, @RequestBody LessonRequest lessonRequest) {
        try {
            return ResponseEntity.ok(courseService.getStudentAll(httpServletRequest.getHeader("Authorization"),
                    lessonRequest, "Courses"));
        } catch (CustomException e) {
            CustomWarning warning = new CustomWarning(e.getStatus(), e.getMessage());
            return new ResponseEntity<>(warning, new HttpHeaders(), warning.getStatus());
        } catch (IllegalArgumentException e) {
            CustomWarning warning = new CustomWarning(HttpStatus.BAD_REQUEST, e.getMessage());
            return new ResponseEntity<>(warning, new HttpHeaders(), warning.getStatus());
        }
    }

    @PostMapping("/getStudentDashboard/Lessons")
    public ResponseEntity<Object> getStudentLessons(HttpServletRequest httpServletRequest, @RequestBody LessonRequest lessonRequest) {
        try {
            return ResponseEntity.ok(courseService.getStudentAll(httpServletRequest.getHeader("Authorization"),
                    lessonRequest, "Lessons"));
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
    public ResponseEntity<Object> addDate(HttpServletRequest httpServletRequest, @PathVariable int id, @RequestBody CreateCourseRequest courseRequest) {
        try {
            return ResponseEntity.ok(courseService.addDate(courseRequest.getCourseTerminRequests().get(0), id, httpServletRequest.getHeader("Authorization")));
        } catch (CustomException e) {
            CustomWarning warning = new CustomWarning(e.getStatus(), e.getMessage());
            return new ResponseEntity<>(warning, new HttpHeaders(), warning.getStatus());
        }
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
