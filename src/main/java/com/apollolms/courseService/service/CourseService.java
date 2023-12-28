package com.apollolms.courseService.service;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.amazonaws.services.rds.model.ResourceNotFoundException;
import com.apollolms.courseService.entity.Category;
import com.apollolms.courseService.entity.Course;
import com.apollolms.courseService.model.AnalyticsResponse;
import com.apollolms.courseService.model.ChapterRequest;
import com.apollolms.courseService.model.CourseRequest;
import com.apollolms.courseService.model.PositionUpdateRequest;
import com.apollolms.courseService.repository.CategoryRepository;
import com.apollolms.courseService.repository.CourseRepository;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.result.UpdateResult;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import static org.springframework.data.mongodb.core.query.Query.query;
import static org.springframework.data.mongodb.core.query.Update.update;

@Service
public class CourseService {

    private final MongoTemplate mongoTemplate;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    public CourseService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }


    public List<Course> getAll(String userId) {
        return courseRepository.findCourseByUserId(userId);
    }

    public void rearrangeChapters(String courseId, List<PositionUpdateRequest> rearrangeRequests) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new EntityNotFoundException("Course not found with id: " + courseId));

        List<Course.Chapter> originalChapters = new ArrayList<>(course.getChapters());
        Map<String, Course.Chapter> chapterMap = originalChapters.stream()
                .collect(Collectors.toMap(Course.Chapter::getId, Function.identity()));

        for (PositionUpdateRequest request : rearrangeRequests) {
            Course.Chapter chapter = chapterMap.get(request.getId());
            originalChapters.remove(chapter);
            originalChapters.add(request.getPosition(), chapter);
        }

        course.setChapters(originalChapters);
        courseRepository.save(course);
    }

    public List<Category> getCategories() {
        return categoryRepository.findAll();
    }

    public List<Course> getAllPublishedCourses(String title, String categoryId) {
        Criteria criteria = Criteria.where("isPublished").is(true);

        if (title != null && !title.isEmpty()) {
            criteria.and("title").regex(title, "i");
        }

        if (categoryId != null && !categoryId.isEmpty()) {
            criteria.and("categoryId").is(categoryId);
        }

        Query query = new Query(criteria);

        return mongoTemplate.find(query, Course.class);
    }

    public Course addCourse(CourseRequest courseRequest) {
        Course newCourse = Course.builder()
                .userId(courseRequest.getUserId())
                .title(courseRequest.getTitle())
                .build();
        courseRepository.save(newCourse);

        return newCourse;
    }

    public void removeCourse(String courseId) {
        courseRepository.deleteById(courseId);
    }

    public Course addChapter(String courseId, Course.Chapter chapterRequest) {
        Optional<Course> potentialCourse = courseRepository.findById(courseId);

        if (potentialCourse.isPresent()) {
            List<Course.Chapter> chapters = potentialCourse.get().getChapters();

            if (chapters == null) {
                chapters = new ArrayList<>();
                chapters.add(chapterRequest);
                potentialCourse.get().setChapters(chapters);
            } else {
                potentialCourse.get().getChapters().add(chapterRequest);
            }

            return courseRepository.save(potentialCourse.get());
        } else {
            throw new RuntimeException("COURSE NOT FOUND");
        }
    }

    public Course addPurchase(String courseId, Course.Purchase purchaseRequest) {
        Optional<Course> potentialCourse = courseRepository.findById(courseId);

        if (potentialCourse.isPresent()) {
            List<Course.Purchase> purchases = potentialCourse.get().getPurchases();

            if (purchases == null) {
                purchases = new ArrayList<>();
                purchases.add(purchaseRequest);
                potentialCourse.get().setPurchases(purchases);
            } else {
                potentialCourse.get().getPurchases().add(purchaseRequest);
            }

            return courseRepository.save(potentialCourse.get());
        } else {
            throw new RuntimeException("COURSE NOT FOUND");
        }
    }

    public Course getCourseDetails(String courseId) {
        Optional<Course> potentialCourse = courseRepository.findById(courseId);

        if (potentialCourse.isPresent()) {
            return potentialCourse.get();
        } else {
            throw new RuntimeException("COURSE NOT FOUND");
        }
    }

    public Course updateCourse(String courseId, Course courseRequest) throws IllegalAccessException {
        Query query = new Query(Criteria.where("id").is(courseId));

        Field[] fields = courseRequest.getClass().getFields();
        Course result = null;

        for (Field f : fields) {
            f.setAccessible(true);
            Object value = f.get(courseRequest);

            if (value != null) {
                Update update = new Update().set(f.getName(), value);
                FindAndModifyOptions options = new FindAndModifyOptions().returnNew(true).upsert(true);

                result = mongoTemplate.findAndModify(query, update, options, Course.class);
            }
        }

        return result;
    }

    public Course removeNestedSubDocument(String documentId, String documentItemName, String itemId) {
        Query query = query(Criteria.where("id").is(documentId));

        Update update = new Update().pull(documentItemName, query(Criteria.where("id").is(itemId)));

        mongoTemplate.updateFirst(query, update, Course.class);
        return getCourseDetails(documentId);
    }

    public Course updateChapter(String courseId, String chapterId, Course.Chapter courseRequest) throws IllegalAccessException {

        Query query = query(Criteria.where("id").is(courseId)
                .and("chapters._id").is(chapterId));

        Field[] fields = courseRequest.getClass().getFields();

        Course result = null;

        for (Field f : fields) {
            f.setAccessible(true);
            Object value = f.get(courseRequest);

            if (value != null) {

                Update update = new Update().set("chapters.$." + f.getName(), value);
                System.out.println("UPDATE---->> " + update);
                FindAndModifyOptions options = new FindAndModifyOptions().returnNew(true).upsert(true);

                result = mongoTemplate.findAndModify(query, update, options, Course.class);
            }
        }

        return result;
    }

    public Course addAttachment(String courseId, Course.Attachment attachmentRequest) {
        Optional<Course> potentialCourse = courseRepository.findById(courseId);

        if (potentialCourse.isPresent()) {
            List<Course.Attachment> attachments = potentialCourse.get().getAttachments();

            if (attachments == null) {
                attachments = new ArrayList<>();
                attachments.add(attachmentRequest);
                potentialCourse.get().setAttachments(attachments);
            } else {
                potentialCourse.get().getAttachments().add(attachmentRequest);
            }

            return courseRepository.save(potentialCourse.get());
        } else {
            throw new RuntimeException("COURSE NOT FOUND");
        }
    }

    public Course getCourseWithPublishedChapters(String courseId) {
        // Match pipeline stage to filter by courseId and published chapters
        AggregationOperation match = Aggregation.match(
                Criteria.where("_id").is(courseId)
                        .and("chapters.isPublished").is(true)
        );

        // Unwind the chapters array to have one document per chapter
        AggregationOperation unwindChapters = Aggregation.unwind("$chapters");

        // Match pipeline stage to filter only published chapters
        AggregationOperation matchPublishedChapters = Aggregation.match(Criteria.where("chapters.isPublished").is(true));

        // Lookup pipeline stage to retrieve userProgress for each chapter
        LookupOperation lookupUserProgress = LookupOperation.newLookup()
                .from("courses.chapters.userProgress")
                .localField("chapters.userProgress")
                .foreignField("_id")
                .as("chapters.userProgress");

        // Group pipeline stage to group chapters back into a list
        AggregationOperation groupChapters = Aggregation.group("_id")
                .push("chapters").as("chapters");

        // Execute the aggregation
        List<Course> result = mongoTemplate.aggregate(
                Aggregation.newAggregation(match, unwindChapters, matchPublishedChapters, lookupUserProgress, groupChapters),
                "courses",
                Course.class
        ).getMappedResults();

        // Return the first result or null if not found
        return result.isEmpty() ? null : result.get(0);
    }

    public Course getCourseWithPublishedChapters(String courseId, String userId) {
        MatchOperation match = Aggregation.match(
                Criteria.where("_id").is(courseId)
                        .and("chapters.isPublished").is(true)
        );

        UnwindOperation unwindChapters = Aggregation.unwind("$chapters");

        MatchOperation matchPublishedChapters = Aggregation.match(Criteria.where("chapters.isPublished").is(true));

        GroupOperation groupChapters = Aggregation.group("_id")
                .push("chapters").as("chapters");

        Aggregation aggregation = Aggregation.newAggregation(match, unwindChapters, matchPublishedChapters, groupChapters);
        List<Course> result = mongoTemplate.aggregate(aggregation, "courses", Course.class).getMappedResults();

        if (!result.isEmpty()) {
            Optional<Course> courseOptional = courseRepository.findById(courseId);

            if (courseOptional.isPresent()) {
                Course officialCourseData = courseOptional.get();
                Course filteredCourse = result.get(0);

                List<Course.Chapter> chapters = filteredCourse.getChapters().stream()
                        .peek(chapter -> {
                            List<Course.UserProgress> userProgressList = chapter.getUserProgress();

                            if (userProgressList == null) {
                                userProgressList = new ArrayList<>();
                                chapter.setUserProgress(userProgressList);
                            }

                            Course.UserProgress userProgress = userProgressList.stream()
                                    .filter(up -> up.getUserId().equals(userId))
                                    .findFirst()
                                    .orElse(null);

                            if (userProgress == null) {
                                userProgress = new Course.UserProgress();
                                userProgress.setUserId(userId);
                                userProgressList.add(userProgress);
                            }

                        })
                        .collect(Collectors.toList());

                officialCourseData.setChapters(chapters);
                return officialCourseData;
            }
        }

        return null;
    }

    public List<Course> getPublishedCoursesByUserId(String userId) {
        return courseRepository.findPublishedCoursesByUserId(userId);
    }

    public Course updateUserProgress(String courseId, String chapterId, String userId, boolean isCompleted) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + courseId));

        Course.Chapter targetChapter = course.getChapters().stream()
                .filter(chapter -> chapter.getId().equals(chapterId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Chapter not found with id: " + chapterId));

        if (targetChapter.getUserProgress() == null) {
            targetChapter.setUserProgress(new ArrayList<>());
        }

        Course.UserProgress userProgress = targetChapter.getUserProgress().stream()
                .filter(up -> up.getUserId().equals(userId))
                .findFirst()
                .orElseGet(() -> {
                    Course.UserProgress newUserProgress = new Course.UserProgress();
                    newUserProgress.setUserId(userId);
                    targetChapter.getUserProgress().add(newUserProgress);
                    return newUserProgress;
                });

        userProgress.setIsCompleted(isCompleted);

       return courseRepository.save(course);
    }

    public Map<String, Object> calculateTotalRevenueAndSales(String userId) {
        Map<String, Object> result = new HashMap<>();

        List<Course> courses = courseRepository.findCourseByUserId(userId);

        int totalSales = 0;
        float totalRevenue = 0;

        Map<String, Integer> courseSalesMap = new HashMap<>();

        for (Course course : courses) {
            List<Course.Purchase> purchases = course.getPurchases();

            if (purchases != null) {
                for (Course.Purchase purchase : purchases) {
                    totalSales++;

                    totalRevenue += course.getPrice();

                    courseSalesMap.put(course.getTitle(), courseSalesMap.getOrDefault(course.getTitle(), 0) + course.getPrice().intValue());
                }
            }
        }

        result.put("totalRevenue", totalRevenue);
        result.put("totalSales", totalSales);
        result.put("data", courseSalesMap);

        return result;
    }

    public void saveCategory(Category category) {
        categoryRepository.save(category);
    }
}