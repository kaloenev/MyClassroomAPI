package com.alibou.security;

import com.alibou.security.auth.AuthenticationService;
import com.alibou.security.auth.RegisterRequest;
import com.alibou.security.coursesServiceController.CourseService;
import com.alibou.security.lessons.LessonRepository;
import com.alibou.security.user.Role;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.List;

import static com.alibou.security.user.Role.ADMIN;
import static com.alibou.security.user.Role.MANAGER;

@SpringBootApplication
public class SecurityApplication {


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
