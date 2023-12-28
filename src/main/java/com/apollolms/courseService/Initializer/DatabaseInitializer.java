package com.apollolms.courseService.Initializer;

import com.apollolms.courseService.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DatabaseInitializer implements CommandLineRunner {

    @Autowired
    private final CategoryRepository categoryRepository;

    public DatabaseInitializer(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }


    @Override
    public void run(String... args) throws Exception {
        // Delete all documents from the collection
//        categoryRepository.deleteAll();

//        System.out.println("All documents deleted from the collection.");
    }
}
