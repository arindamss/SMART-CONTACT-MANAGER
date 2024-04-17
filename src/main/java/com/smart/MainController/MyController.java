package com.smart.MainController;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.Service.database;
import com.smart.dao.UserRepository;
import com.smart.entity.User;
import com.smart.helper.Message;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;



@Controller
public class MyController {
    
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private database db;

    @GetMapping("/")
    public String home(Model m){
        m.addAttribute("title", "Home");
        return "home";
    }

    @RequestMapping("/about")
    public String about(Model m){
        m.addAttribute("title", "About | Smart Contact Manager");
        return "about";
    }

    @RequestMapping("/signup")
    public String signup(Model m){
        m.addAttribute("title", "Register | Smart Contact Manager");
        m.addAttribute("user",new User());
        return "signup";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("user") User user,BindingResult error, @RequestParam(name = "agreement", defaultValue = "false") boolean aggrement, HttpSession session, Model m){
        
        try{
            if(!aggrement){
                System.out.println("\n You have to accept the conditions \n");
                throw new Exception("You must heave to accept the terms and conditions");
            }

            if(error.hasErrors()){
                System.out.println("\n \n Error : "+error);
                m.addAttribute("user", user);
                return "signup";
            }
            
            user.setRole("ROLE_USER");
            user.setEnabled(true);
            user.setImage("default.png");
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            User u=db.userSave(user);
            // session.setAttribute("message", new Message("Success", "alert-success"));
            m.addAttribute("message",new Message("Success", "alert-success"));
            return "signup";



        }
        catch(Exception e){
            // e.printStackTrace();
            System.out.println("Error : "+e);
            m.addAttribute("user", user);
            // session.setAttribute("message", new Message("Something went wrong !!"+e.getMessage(),"alert-error"));
            m.addAttribute("message", new Message("Something went wrong "+e.getMessage(), "alert-danger"));
            return "signup";
        }

        // System.out.println(user);
        // System.out.println("Aggrement : "+aggrement);
        
        
    }

    @RequestMapping("/login")
    public String login(Model m){
        m.addAttribute("title", "Login");
        return "login";
    }

    @RequestMapping("/welcome")
    public String welcomeUser(){
        return "home";
    }

}
