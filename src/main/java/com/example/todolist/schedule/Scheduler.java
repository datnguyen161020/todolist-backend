/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.todolist.schedule;

import com.example.todolist.entity.VerificationOtp;
import com.example.todolist.repo.UserRepo;
import com.example.todolist.repo.VerificationOtpRepo;
import com.example.todolist.service.UserService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author hp
 */
@Component
@Slf4j

public class Scheduler {
    @Autowired
    private UserService userService;
    
    @Scheduled(fixedRate = 600000)
    public void scheduleDeleteOtpExpire(){
        userService.cleanData();
        log.info("Clean data");
    }
}
