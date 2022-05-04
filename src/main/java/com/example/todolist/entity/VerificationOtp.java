/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.todolist.entity;

import java.time.LocalDateTime;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 * @author hp
 */
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class VerificationOtp {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String otp;
    private LocalDateTime createAt;
    private LocalDateTime expireAt;
    private LocalDateTime confirmedAt;
    
    @ManyToOne
    private User user;

    
    
}
