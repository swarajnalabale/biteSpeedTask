package com.bitespeed.task.model;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "Contact")
public class Contact {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "phone_Number")
    private String phoneNumber;

    @Column(name = "email")
    private String email;

    @Column(name = "linked_Id")
    private Integer linkedId;

    @Column(name = "link_Precedence")
    private String linkPrecedence;

    @Column(name = "created_At")
    private String createdAt;

    @Column(name = "updated_At")
    private String updatedAt;

    @Column(name = "deleted_At")
    private String deletedAt;

    // Getters and Setters
    // ...
}
