package org.example.bookstore;

import org.example.bookstore.service.BookService;
import org.example.bookstore.service.zoo.ServiceRegistry;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

@SpringBootApplication
@EnableRedisHttpSession
@EnableAsync
public class BookstoreApplication implements CommandLineRunner {

    private final BookService bookService;
    private final ServiceRegistry serviceRegistry;

    public BookstoreApplication(BookService bookService, ServiceRegistry serviceRegistry) {
        this.bookService = bookService;
        this.serviceRegistry = serviceRegistry;
    }

    public static void main(String[] args) {
        SpringApplication.run(BookstoreApplication.class, args);
    }

    @Override
    public void run(String... args) {
        int size = bookService.getAllBooks().size();
        serviceRegistry.register("bookstore_instance_dev", "localhost:8080");
        System.out.println("Bookstore application started with " + size + " books.");
        serviceRegistry.discover("bookstore_instance_dev");
        System.out.println(size);


    }
}