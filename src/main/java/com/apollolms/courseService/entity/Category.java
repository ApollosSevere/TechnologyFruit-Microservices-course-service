package com.apollolms.courseService.entity;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "categories")
@Builder
public class Category {

        @Id
        private String id;
        @Indexed(unique = true)
        private String name;
}

