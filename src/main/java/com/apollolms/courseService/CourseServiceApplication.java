package com.apollolms.courseService;

import com.apollolms.courseService.entity.Category;
import com.apollolms.courseService.service.CourseService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

@SpringBootApplication
@EnableMongoAuditing
public class CourseServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(CourseServiceApplication.class, args);
	}


//	@Bean
//	public CommandLineRunner commandLineRunner(
//			CourseService service) {
//		return args -> {
//			var category1 = Category.builder()
//					.name("Computer Science")
//					.build();
//			 service.saveCategory(category1);
//
//			var category2 = Category.builder()
//					.name("Music")
//					.build();
//			service.saveCategory(category2);
//
//			var category3 = Category.builder()
//					.name("Fitness")
//					.build();
//			service.saveCategory(category3);
//
//			var category4 = Category.builder()
//					.name("Photography")
//					.build();
//			service.saveCategory(category4);
//
//			var category5 = Category.builder()
//					.name("Accounting")
//					.build();
//			service.saveCategory(category5);
//
//			var category6 = Category.builder()
//					.name("Engineering")
//					.build();
//			service.saveCategory(category6);
//
//			var category7 = Category.builder()
//					.name("Filming")
//					.build();
//			service.saveCategory(category7);
//
//		};
//	}
}
