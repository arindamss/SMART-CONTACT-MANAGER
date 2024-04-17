package com.smart.MainController;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.smart.Service.database;
import com.smart.entity.Contact;
import com.smart.entity.User;
import com.smart.helper.Message;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    private database db;

    @ModelAttribute
    public void addUserForEveryone(Model m, Principal u) {
        User user = db.getUser(u.getName());
        m.addAttribute("user", user);
    }

    @RequestMapping("/index")
    public String dashbord(Model m, Principal p) {
        m.addAttribute("title", "Dashbord");
        return "normal/user_dashbord";
    }

    @GetMapping("/addContact")
    public String addContact(Model m) {
        m.addAttribute("title", "Add Contact");
        m.addAttribute("contact", new Contact());

        return "normal/addContact";
    }

    @PostMapping("/contactProcess")
    public String contactProcess(@ModelAttribute Contact contact, @RequestParam("profile") MultipartFile file, Principal p, HttpSession session, Model m){

        try{

            if(file.isEmpty()){
                System.out.println("Image not get");
                contact.setImage("contact.png");
            }
            else{
                DateFormat format=new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                Date date=new Date();
                System.out.println(format.format(date));

                contact.setImage(file.getOriginalFilename());

                File saveFile=new ClassPathResource("static/image").getFile();
                

                Path path=Paths.get(saveFile.getAbsolutePath()+File.separator,file.getOriginalFilename());

                Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

                System.out.println("FILE : "+file.getInputStream());
                System.out.println("Save File : "+saveFile);
                System.out.println("PATH : "+path);


            }

            String u=p.getName();
            User user=db.getUser(u);

            contact.setUser(user);

            user.getContact().add(contact);

            db.userSave(user);

            session.setAttribute("message", new Message("Your Details Added Successfully || Add more...","success"));
            // m.addAttribute("message", new Message("Your Details Added Successfully || Add more...","success"));
        }
        catch(Exception e){
            e.printStackTrace();
            session.setAttribute("message", new Message("Something went wrong.. || Try Again","danger"));
            // m.addAttribute("message", new Message("Something went wrong.. || Try Again","danger"));
        }
        
        // System.out.println(contact);

        return "redirect:/user/addContact";
    }


    @GetMapping("/showContact/{page}")
    public String showContact(@PathVariable("page") Integer page, Model m,Principal p){
        String u=p.getName();
        User user=db.getUser(u);

        Pageable of=PageRequest.of(page, 5);

        Page<Contact> contacts=db.getContactsByUser(user.getId(), of);

        m.addAttribute("contacts", contacts);
        m.addAttribute("currentPage", page);
        m.addAttribute("totalPage", of.getPageSize());
        System.out.println("PAGE SIZE "+of.getPageNumber());
        System.out.println("Total Page "+of.getPageSize());
        m.addAttribute("title", "View Contact");
        

        return "normal/showContact";
    }

    @RequestMapping("/getContact/{cId}")
    public String showDetails(@PathVariable("cId") Integer id, Model m, Principal p){
        System.out.println("\n\n\n\nCID : "+id);

        Optional<Contact> c=db.getContactDetails(id);
        Contact contact=c.get();

        String u=p.getName();

        User user=db.getUser(u);

        if(user.getId() == contact.getUser().getId()){
            m.addAttribute("contact", contact);
            m.addAttribute("title", contact.getName());

        }


        return "normal/ShowDetals";
    }

    @GetMapping("/deleteContact/{currentPage}/{cId}")
    public String deletecontact(@PathVariable("currentPage") int currentPage ,@PathVariable("cId") int id, Principal p, HttpSession session){
        System.out.println("Page : "+currentPage);
        System.out.println("Id  : "+id);
        String u=p.getName();
        User user=db.getUser(u);

        Optional<Contact> c=db.getContactDetails(id);

        if(c.isPresent()){
            Contact contact=c.get();

            System.out.println("Contact : "+contact.getName());

            if(user.getId() == contact.getUser().getId()){

                User user2=contact.getUser();
                user2.getContact().remove(contact);
                db.userSave(user2);
                db.deleteContact(contact);

                // System.out.println("User Id : "+user.getId());
                // System.out.println("Contact User Id : "+contact.getUser().getId());
                // contact.setUser(null);
                // db.deleteContact(contact);
                session.setAttribute("message", new Message("Contact Deletes Successfully...","success"));
            }
            else{
                System.out.println("Id is not match");
            }
        }
        else{
            System.out.println("contact not found..");
        }

        return "redirect:/user/showContact/"+currentPage;
    }

    @GetMapping("/update-contact/{cId}")
    public String UpdateContact(@PathVariable("cId") int cID, Model m){
        System.out.println("I am on Rock \n CID : "+cID);

        Contact contact=db.getContactDetails(cID).get();

        m.addAttribute("contact", contact);

        return "normal/update_contact";
    }

    @PostMapping("/contactUpdateSave")
    public String contactUpdateSave(@ModelAttribute Contact contact, @RequestParam("profile") MultipartFile file , HttpSession session , Principal p){

        Contact oldContact=db.findContact(contact.getcId());
        try{

        
            if(!file.isEmpty()){
                System.out.print(file.getOriginalFilename());

                File deleteFile=new ClassPathResource("static/image").getFile();
                File file1=new File(deleteFile, oldContact.getImage());
                file1.delete();

                File savFile=new ClassPathResource("static/image").getFile();
                Path path= Paths.get(savFile.getAbsolutePath() +File.separator +file.getOriginalFilename());
                Files.copy(file.getInputStream(),path, StandardCopyOption.REPLACE_EXISTING);

                contact.setImage(file.getOriginalFilename());
                System.out.println("FILE : "+file.getOriginalFilename());


            }
            else{
                contact.setImage(oldContact.getImage());
            }

            User user=db.getUser(p.getName());

            contact.setUser(user);
    
            db.contactSave(contact);
        }
        catch(Exception e){
            e.printStackTrace();
        }


        
       

        return "redirect:/user/getContact/"+contact.getcId();
    }

    @GetMapping("/yourProfile")
    public String yourProfile(Model m){
        m.addAttribute("title", "Profile");
        return "normal/yourProfile";
    }
}
