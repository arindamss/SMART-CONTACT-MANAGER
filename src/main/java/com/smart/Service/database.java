package com.smart.Service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.smart.dao.ContactRepository;
import com.smart.dao.UserRepository;
import com.smart.entity.Contact;
import com.smart.entity.User;

@Service
public class database {
    
    @Autowired
    private UserRepository userRepo;

    @Autowired
    private ContactRepository contactRepo;

    public User userSave(User user){
        User u=userRepo.save(user);
        return u;
    }

    public User getUser(String email){
        return userRepo.findByEmail(email);
    }

    public Page<Contact> getContactsByUser(int userId, Pageable of){
        return contactRepo.getAllContactsByUser(userId,of);
    }

    public Optional<Contact> getContactDetails(int id){
        return contactRepo.findById(id);
    }

    public void deleteContact(Contact c){
        this.contactRepo.delete(c);
    }

    public void contactSave(Contact contact){
        this.contactRepo.save(contact);
    }

    public Contact findContact(int id){
        return contactRepo.findById(id).get();
    }

}
