package com.bitespeed.task.services;

import com.bitespeed.task.model.Contact;
import com.bitespeed.task.model.ContactPayload;
import com.bitespeed.task.repository.ContactRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class ContactService {
    private final ContactRepository contactRepository;

    @Autowired
    public ContactService(ContactRepository contactRepository) {
        this.contactRepository = contactRepository;
    }

    public ContactPayload identifyContact(String email, String phoneNumber) {
        List<Contact> contacts = (List<Contact>) contactRepository.findByEmailOrPhoneNumber(email, phoneNumber);

        if (!contacts.isEmpty()) {
            ContactPayload contactPayload = new ContactPayload();
            List<Integer> secondaryContactIds = new ArrayList<>();
            Set<String> uniqueEmails = new HashSet<>();
            Set<String> uniquePhoneNumbers = new HashSet<>();

            Integer primaryContactId = null;

            for (Contact contact : contacts) {
                if (contact.getLinkPrecedence().equals("primary")) {
                    primaryContactId = contact.getId();
                    uniquePhoneNumbers.add(contact.getPhoneNumber());
                    if(uniquePhoneNumbers.size()>0){
                        phoneNumber=contact.getPhoneNumber();
                    }
                    if (contact.getEmail() != null) {
                        uniqueEmails.add(contact.getEmail());
                    }
                } else {
                    if (!secondaryContactIds.contains(contact.getId())) {
                        secondaryContactIds.add(contact.getId());
                    }
                }
            }

            // Check if primary contact is found based on phone number and email
            if (primaryContactId == null) {
                Contact primaryContact = contactRepository.findPrimaryContactByPhoneNumberOrEmail(phoneNumber, email);
                if (primaryContact != null) {
                    primaryContactId = primaryContact.getId();
                }
            }


            // Find all related emails and phone numbers recursively
            Set<String> processedEmails = new HashSet<>(uniqueEmails);
            Set<String> processedPhoneNumbers = new HashSet<>(uniquePhoneNumbers);

            primaryContactId = findRelatedEmailsAndPhoneNumbers(primaryContactId, email, phoneNumber, processedEmails, processedPhoneNumbers, secondaryContactIds);

            if (primaryContactId != null && secondaryContactIds.isEmpty()) {
                List<Contact> matchingPrimaryContacts = contactRepository.findPrimaryContactsByEmailOrPhoneNumber(email, phoneNumber);
                if (matchingPrimaryContacts.size() > 1) {
                    // Update one of the primary contacts to a secondary contact
                    Contact contactToUpdate = matchingPrimaryContacts.get(0);
                    contactToUpdate.setLinkPrecedence("secondary");
                    contactToUpdate.setLinkedId(primaryContactId);
                    contactRepository.save(contactToUpdate);
                    secondaryContactIds.add(contactToUpdate.getId());
                }
            }

            // Add the new email or phone number to the response if it is a secondary contact
            if (primaryContactId != null && !secondaryContactIds.isEmpty()) {
                if (email != null && !processedEmails.contains(email)) {
                    processedEmails.add(email);
                }
                if (phoneNumber != null && !processedPhoneNumbers.contains(phoneNumber)) {
                    processedPhoneNumbers.add(phoneNumber);
                }
            }

            contactPayload.setPrimaryContactId(primaryContactId);
            contactPayload.setEmails(new ArrayList<>(processedEmails));
            contactPayload.setPhoneNumbers(new ArrayList<>(processedPhoneNumbers));
            contactPayload.setSecondaryContactIds(new ArrayList<>(new HashSet<>(secondaryContactIds))); // Make secondary contact IDs unique

            return contactPayload;
        } else {
            // Create a new primary contact
            Contact newContact = new Contact();
            newContact.setEmail(email);
            newContact.setPhoneNumber(phoneNumber);
            newContact.setLinkPrecedence("primary");

            Contact savedContact = contactRepository.save(newContact);

            ContactPayload contactPayload = new ContactPayload();
            contactPayload.setPrimaryContactId(savedContact.getId());

            if (email != null) {
                contactPayload.getEmails().add(email);
            }
            if (phoneNumber != null) {
                contactPayload.getPhoneNumbers().add(phoneNumber);
            }

            return contactPayload;
        }
    }

    private Integer findRelatedEmailsAndPhoneNumbers(Integer primaryContactId, String email, String phoneNumber, Set<String> processedEmails, Set<String> processedPhoneNumbers, List<Integer> secondaryContactIds) {
        // Find emails related to the provided phone number
        if (phoneNumber != null) {
            List<Contact> relatedContactsByPhone = contactRepository.findByPhoneNumber(phoneNumber);
            if (relatedContactsByPhone.isEmpty()) {
                // Create a new secondary contact with the provided phone number
                Contact newSecondaryContact = new Contact();
                newSecondaryContact.setEmail(email);
                newSecondaryContact.setPhoneNumber(phoneNumber);
                newSecondaryContact.setLinkPrecedence("secondary");
                newSecondaryContact.setLinkedId(primaryContactId);

                Contact savedSecondaryContact = contactRepository.save(newSecondaryContact);
                secondaryContactIds.add(savedSecondaryContact.getId());

                processedEmails.add(email); // Add new email to processedEmails
            } else {
                for (Contact relatedContact : relatedContactsByPhone) {
                    if (relatedContact.getEmail() != null && !processedEmails.contains(relatedContact.getEmail())) {
                        processedEmails.add(relatedContact.getEmail());
                        if (relatedContact.getLinkPrecedence().equals("primary")) {
                            primaryContactId = relatedContact.getId();
                        } else {
                            secondaryContactIds.add(relatedContact.getId());
                        }
                        primaryContactId = findRelatedEmailsAndPhoneNumbers(primaryContactId, relatedContact.getEmail(), null, processedEmails, processedPhoneNumbers, secondaryContactIds);
                    }
                }
            }
        }

        // Find phone numbers related to the provided email
        if (email != null) {
            List<Contact> relatedContactsByEmail = contactRepository.findByEmail(email);
            if (relatedContactsByEmail.isEmpty()) {
                // Create a new secondary contact with the provided email
                Contact newSecondaryContact = new Contact();
                newSecondaryContact.setEmail(email);
                newSecondaryContact.setPhoneNumber(phoneNumber);
                newSecondaryContact.setLinkPrecedence("secondary");
                newSecondaryContact.setLinkedId(primaryContactId);

                Contact savedSecondaryContact = contactRepository.save(newSecondaryContact);
                secondaryContactIds.add(savedSecondaryContact.getId());

                processedPhoneNumbers.add(phoneNumber); // Add new phone number to processedPhoneNumbers
            } else {
                for (Contact relatedContact : relatedContactsByEmail) {
                    if (relatedContact.getPhoneNumber() != null && !processedPhoneNumbers.contains(relatedContact.getPhoneNumber())) {
                        processedPhoneNumbers.add(relatedContact.getPhoneNumber());
                        if (relatedContact.getLinkPrecedence().equals("primary")) {
                            primaryContactId = relatedContact.getId();
                        } else {
                            secondaryContactIds.add(relatedContact.getId());
                        }
                        primaryContactId = findRelatedEmailsAndPhoneNumbers(primaryContactId, null, relatedContact.getPhoneNumber(), processedEmails, processedPhoneNumbers, secondaryContactIds);
                    }
                }
            }
        }
        return primaryContactId;
    }
}
