package com.example.todolist;

import com.example.todolist.entity.User;
import com.example.todolist.service.UserService;
import java.util.ArrayList;
import java.util.Properties;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
@EnableScheduling
public class TodolistApplication {

    public static void main(String[] args) {
            SpringApplication.run(TodolistApplication.class, args);
    }
    
    @Bean
    PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    CommandLineRunner run(UserService userService){
        return args -> {
            userService.saveUser(new User(null, "ABC", "abc12", "12345","abc@email", true));
            userService.saveUser(new User(null, "QQrfqq", "qnwh123", "12345","abc@email", false));
            userService.saveUser(new User(null, "Fafqsq", "qwxs1224", "12345","abc@email", false));
            userService.saveUser(new User(null, "Gasagwqv", "aat112", "12345","abc@email", true));

        };
    }
    @Bean
    JavaMailSender getMailSender(){
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        
        mailSender.setHost("smtp.gmail.com");
        mailSender.setPort(587);
        mailSender.setUsername("todolistapp0305@gmail.com");
        mailSender.setPassword("todolist12345");
        
        Properties javaMailProperties = new Properties();
        javaMailProperties.put("mail.smtp.starttls.enable", "true");
        javaMailProperties.put("mail.smtp.auth", "true");
        javaMailProperties.put("mail.transport.protocol","smtp");
        javaMailProperties.put("mail.debug", "true");
        
        mailSender.setJavaMailProperties(javaMailProperties);
        return mailSender;
    }


}

