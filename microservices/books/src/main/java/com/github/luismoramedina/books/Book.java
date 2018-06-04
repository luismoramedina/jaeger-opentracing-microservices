package com.github.luismoramedina.books;

@lombok.Data
public class Book {
    int id;
    String title;
    String year;
    String author;
    int stars;
    public String[] covers;
}
