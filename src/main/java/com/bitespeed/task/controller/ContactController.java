package com.bitespeed.task.controller;

import com.bitespeed.task.model.ContactPayload;
import com.bitespeed.task.model.ContactRequest;
import com.bitespeed.task.services.ContactService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ContactController {
    private final ContactService contactService;

    @Autowired
    public ContactController(ContactService contactService) {
        this.contactService = contactService;
    }

    @PostMapping("/identify")
    public ResponseEntity<ContactPayload> identifyContact(@RequestBody ContactRequest contactRequest) {
        ContactPayload contactPayload = contactService.identifyContact(contactRequest.getEmail(), contactRequest.getPhoneNumber());

        if (contactPayload != null) {
            return ResponseEntity.ok(contactPayload);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
