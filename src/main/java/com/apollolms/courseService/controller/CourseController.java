package com.apollolms.courseService.controller;

import com.apollolms.courseService.entity.Category;
import com.apollolms.courseService.entity.Course;
import com.apollolms.courseService.model.CourseRequest;
import com.apollolms.courseService.model.PositionUpdateRequest;
import com.apollolms.courseService.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/course")
@RequiredArgsConstructor
public class CourseController {

    @Autowired
    private CourseService courseService;

    @PreAuthorize("hasRole('TEACHER')")
    @GetMapping
    public ResponseEntity<List<Course>> getAllCourses(
            @RequestParam String userId
    ) {
        List<Course> allCourses = courseService.getAll(userId);
        return new ResponseEntity<>(allCourses, HttpStatus.OK);
    }

    @GetMapping("/getPublishedCourses")
    public ResponseEntity<List<Course>> getAllPublishedCourses(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String categoryId
    ) {
        List<Course> allCourses = courseService.getAllPublishedCourses(title, categoryId);
        return new ResponseEntity<>(allCourses, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('TEACHER')")
    @GetMapping("/{courseId}")
    public ResponseEntity<Course> getCourseDetails(
            @PathVariable("courseId") String courseId
    ) {
       Course course = courseService.getCourseDetails(courseId);
       return new ResponseEntity<>(course, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('TEACHER')")
    @PostMapping("/{courseId}/chapter")
    public ResponseEntity<Course> addChapter(
            @PathVariable("courseId") String courseId,
            @RequestBody Course.Chapter chapterRequest
    ) {
        Course course = courseService.addChapter(courseId, chapterRequest);
        return new ResponseEntity<>(course, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('TEACHER')")
    @PutMapping("/{courseId}")
    public ResponseEntity<Course> updateCourse(
            @PathVariable("courseId") String courseId,
            @RequestBody Course courseUpdates
    ) throws IllegalAccessException {
        Course updatedCourse =  courseService.updateCourse(courseId, courseUpdates);
        return new ResponseEntity<>(updatedCourse, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('TEACHER')")
    @PutMapping("/{courseId}/chapter/{chapterId}")
    public ResponseEntity<Course> updateChapter(
            @PathVariable("courseId") String courseId,
            @PathVariable("chapterId") String chapterId,
            @RequestBody Course.Chapter courseUpdates
    ) throws IllegalAccessException {
        Course updatedCourse =  courseService.updateChapter(courseId, chapterId, courseUpdates);
        return new ResponseEntity<>(updatedCourse, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('TEACHER')")
    @PostMapping
    public ResponseEntity<Course> createCourse(@RequestBody CourseRequest courseRequest) {
        Course newCourse = courseService.addCourse(courseRequest);
        return new ResponseEntity<>(newCourse, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('TEACHER')")
    @DeleteMapping("/{documentId}/deleteSubDocumentItem/{documentItemName}/{itemId}")
    public ResponseEntity<Course> deleteItem(
            @PathVariable String documentId,
            @PathVariable String documentItemName,
            @PathVariable String itemId
    ) {

        Course course = courseService.removeNestedSubDocument(documentId, documentItemName, itemId);
        return new ResponseEntity<>(course, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('TEACHER')")
    @PostMapping("/{courseId}/addSubDocumentItem/{documentItemName}")
    public ResponseEntity<Course> addSubDocumentItem(
            @PathVariable String courseId,
            @PathVariable String documentItemName,
            @RequestBody Course.Attachment newItem) {
        Course course = courseService.addAttachment(courseId, newItem);
        return new ResponseEntity<>(course, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('TEACHER')")
    @DeleteMapping("/{courseId}")
    public ResponseEntity<String> deleteCourse(
            @PathVariable String courseId
    ) {
        courseService.removeCourse(courseId);
        return ResponseEntity.ok("Course deleted successfully.");
    }

    @GetMapping("/{courseId}/publishedChapters")
    public ResponseEntity<Course> findCourseByPublishedChapters(
            @PathVariable String courseId
    ) {
        Course course =  courseService.getCourseWithPublishedChapters(courseId);
        return new ResponseEntity<>(course, HttpStatus.OK);
    }

    @GetMapping("/{courseId}/publishedChapters/{userId}")
    public ResponseEntity<Course> findCourseByPublishedChapters(
            @PathVariable String courseId,
            @PathVariable String userId
    ) {
       Course course =  courseService.getCourseWithPublishedChapters(courseId, userId);
       return new ResponseEntity<>(course, HttpStatus.OK);
    }

    @PutMapping("/{courseId}/chapters/{chapterId}/user-progress")
    public ResponseEntity<Course> updateUserProgress(
            @PathVariable String courseId,
            @PathVariable String chapterId,
            @RequestParam String userId,
            @RequestParam boolean isCompleted) {
        Course course =  courseService.updateUserProgress(courseId, chapterId, userId, isCompleted);
        return new ResponseEntity<>(course, HttpStatus.OK);
    }


    @PreAuthorize("hasRole('USER') || hasRole('TEACHER')")
    @PostMapping("/{courseId}/addPurchase")
    public ResponseEntity<Course> addPurchase(
            @PathVariable String courseId,
            @RequestBody Course.Purchase purchaseRequest
    ) {
        Course newCourse = courseService.addPurchase(courseId,purchaseRequest);
        return new ResponseEntity<>(newCourse, HttpStatus.OK);
    }

    @GetMapping("/{userId}/getDashboard")
    public ResponseEntity<List<Course>> getDashboard(
            @PathVariable String userId
    ) {
        List<Course> dashboardCourses = courseService.getPublishedCoursesByUserId(userId);
        return new ResponseEntity<>(dashboardCourses, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('TEACHER')")
    @GetMapping("/{userId}/analyticsCalculation")
    public ResponseEntity<Map<String, Object>> getAnalyticsCalc(
            @PathVariable String userId
    ) {
        Map<String, Object> analyticCalculations = courseService.calculateTotalRevenueAndSales(userId);
        return new ResponseEntity<>(analyticCalculations, HttpStatus.OK);
    }

    @GetMapping("/categories")
    public ResponseEntity<List<Category>> getAllCategories() {
        List<Category> categories = courseService.getCategories();
        return new ResponseEntity<>(categories, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('TEACHER')")
    @PostMapping("/updateChapterPositions")
    public ResponseEntity<String> updateChapterPositions(
            @RequestParam String courseId,
            @RequestBody List<PositionUpdateRequest> chapters
    ) {
        courseService.rearrangeChapters(courseId, chapters);
        return ResponseEntity.ok("Chapter order updated successfully.");
    }


}
