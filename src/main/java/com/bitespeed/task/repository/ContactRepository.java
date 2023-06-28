package com.bitespeed.task.repository;

import com.bitespeed.task.model.Contact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface ContactRepository extends JpaRepository<Contact, Integer> {
    Contact findFirstByEmailAndLinkPrecedence(String email, String linkPrecedence);

    Contact findFirstByPhoneNumberAndLinkPrecedence(String phoneNumber, String linkPrecedence);

    List<Contact> findByEmailOrPhoneNumber(String email, String phoneNumber);

    List<Contact> findByLinkedId(Integer id);

    List<Contact> findByPhoneNumber(String phoneNumber);

    List<Contact> findByEmail(String email);

    Contact findPrimaryContactByPhoneNumberAndEmail(String phoneNumber, String email);

    Contact findPrimaryContactByPhoneNumberOrEmail(String phoneNumber, String email);

    Integer findLinkedIdByEmail(String email);

    List<Contact> findPrimaryContactsByEmailOrPhoneNumber(String email, String phoneNumber);

}
