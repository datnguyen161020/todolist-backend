/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.todolist.repo;

import com.example.todolist.entity.VerificationOtp;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/**
 *
 * @author hp
 */
public interface VerificationOtpRepo extends JpaRepository<VerificationOtp, Long>{
    List<VerificationOtp> findByOtp(String otp);
}
