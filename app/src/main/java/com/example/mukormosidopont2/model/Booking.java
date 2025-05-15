package com.example.mukormosidopont2.model;

public class Booking {
    private String bookingDate;
    private String userId;

    public Booking() {
        // Üres konstruktor Firestore-hoz
    }

    public Booking(String bookingDate, String userId) {
        this.bookingDate = bookingDate;
        this.userId = userId;
    }

    public String getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(String bookingDate) {
        this.bookingDate = bookingDate;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
