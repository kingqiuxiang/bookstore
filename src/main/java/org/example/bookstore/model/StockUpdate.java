package org.example.bookstore.model;

import java.io.Serializable;

public class StockUpdate implements Serializable {
    private String bookId;
    private Integer adjustment;
    // getters/setters


    public StockUpdate() {
    }

    public String getBookId() {
        return bookId;
    }

    public void setBookId(String bookId) {
        this.bookId = bookId;
    }

    public Integer getAdjustment() {
        return adjustment;
    }

    public void setAdjustment(Integer adjustment) {
        this.adjustment = adjustment;
    }
}