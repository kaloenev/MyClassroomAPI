package com.alibou.security;

import com.alibou.security.coursesServiceController.CourseService;
import com.alibou.security.lessons.LessonRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;

import java.util.List;

@SpringBootApplication
public class SecurityApplication extends SpringBootServletInitializer {


    public static void main(String[] args) {
        SpringApplication.run(SecurityApplication.class, args);
    }

    @Bean
    public CommandLineRunner commandLineRunner(
            LessonRepository lessonRepository,
            CourseService courseService
    ) {
        return args -> {
            List<Double> prices = lessonRepository.getPrivateLessonPrices();
            for (Double price : prices) {
                courseService.addNewPriceLesson(price);
            }
            List<Double> prices2 = lessonRepository.getCoursePrices();
            for (Double price : prices2) {
                courseService.addNewPriceCourse(price);
            }
        };
    }
}
