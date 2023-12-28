package com.apollolms.courseService.entity;

import lombok.Builder;
import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;

@Data
@Document(collection = "courses")
@Builder
public class Course {
    @Id
    public String id;
    public String userId;

    public String title;

    public String description;
    public String imageUrl;
    public Float price;
    public Boolean isPublished;
    public String categoryId;
    public Category category;
    public List<Chapter> chapters;
    public List<Attachment> attachments;
    public List<Purchase> purchases;

    @CreatedDate
    private Date createdAt;

    @LastModifiedDate
    private Date updatedAt;

    @Data
    public static class Attachment {
        @Id
        private String id;
        private String name;
        private String url;

        @CreatedDate
        private Date createdAt;

        @LastModifiedDate
        private Date updatedAt;

        public Attachment() {
            id = new ObjectId().toString();
        }
    }

    @Data
    public static class Chapter {
        @Id
        private String id;
        public String title;

        public String description;

        public String videoUrl;
        public Integer position;
        public Boolean isPublished;
        public Boolean isFree;
        public MuxData muxData;
        public String courseId;
        public Course course;
        public List<UserProgress> userProgress;

        @CreatedDate
        private Date createdAt;

        @LastModifiedDate
        private Date updatedAt;

        public Chapter() {
            id = new ObjectId().toString();
        }
    }

    @Data
    public static class MuxData {
        @Id
        private String id;
        private String assetId;
        private String playbackId;

        @CreatedDate
        private Date createdAt;

        @LastModifiedDate
        private Date updatedAt;

    }

    @Data
    public static class UserProgress {
        @Id
        private String id;
        private String userId;
        private Boolean isCompleted;

        @CreatedDate
        private Date createdAt;

        @LastModifiedDate
        private Date updatedAt;

        public UserProgress() {
            id = new ObjectId().toString();
        }
    }

    @Data
    public static class Purchase {
        @Id
        private String id;
        private String userId;

        @CreatedDate
        private Date createdAt;

        @LastModifiedDate
        private Date updatedAt;

        public Purchase() {
            id = new ObjectId().toString();
        }
    }

    @Data
    static class StripeCustomer {
        @Id
        private String id;
        @Indexed(unique = true)
        private String userId;
        @Indexed(unique = true)
        private String stripeCustomerId;

        @CreatedDate
        private Date createdAt;

        @LastModifiedDate
        private Date updatedAt;

    }
}

