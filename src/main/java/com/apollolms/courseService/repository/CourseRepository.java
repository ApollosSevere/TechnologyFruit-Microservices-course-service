package com.apollolms.courseService.repository;

import com.apollolms.courseService.entity.Course;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseRepository extends MongoRepository<Course,String> {
    List<Course> findCourseByUserId(String userId);

    @Query("{" +
            "'isPublished': true, " +
            "'purchases.userId': ?0, " +
            "'chapters.isPublished': true," +
            "}")
    List<Course> findPublishedCoursesByUserId(String userId);

}
