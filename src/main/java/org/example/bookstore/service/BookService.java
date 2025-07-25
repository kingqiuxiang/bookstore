package org.example.bookstore.service;

import org.example.bookstore.model.Book;
import org.example.bookstore.repository.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class BookService {
    private final BookRepository bookRepository;

    @Autowired
    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }
    public CompletableFuture<List<Book>> getAllBook() {
        return new CompletableFuture<>(){
            @Override
            public List<Book> get() {
                return bookRepository.findAll();
            }
        };
    }

    public Book saveBook(Book book) {
        return bookRepository.save(book);
    }

    public List<Book> getBooksByCategory(String category) {
        return bookRepository.findByCategory(category);
    }
}