package org.example.bookstore.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.io.Serializable;
@Setter
@Getter
@Entity
@Table(name = "orders")
public class Order implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Field(type = FieldType.Text, analyzer = "standard")
    private String bookId;
    @Field(type = FieldType.Text, analyzer = "standard")
    private Integer quantity;
    // getters/setters

    public Order() {
    }


    public Order(String bookId, Integer quantity, Long id) {
        this.bookId = bookId;
        this.quantity = quantity;
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBookId() {
        return bookId;
    }

    public void setBookId(String bookId) {
        this.bookId = bookId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
// 必须有无参构造函数
}