package com.bitespeed.task.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ContactPayload {
    private Integer primaryContactId;
    private List<String> emails = new ArrayList<>();
    private List<String> phoneNumbers = new ArrayList<>();
    private List<Integer> secondaryContactIds = new ArrayList<>();


    // Constructor, Getters and Setters

    // ...
}
