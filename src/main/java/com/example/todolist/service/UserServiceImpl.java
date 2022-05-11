/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.todolist.service;

import com.example.todolist.entity.User;
import com.example.todolist.entity.VerificationOtp;
import com.example.todolist.repo.UserRepo;
import java.util.ArrayList;
import java.util.List;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.example.todolist.repo.VerificationOtpRepo;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.stream.Collectors;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

/**
 *
 * @author hp
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UserServiceImpl implements UserService,UserDetailsService{
    private final UserRepo userRepo;
    private final VerificationOtpRepo verificationOtpRepo;
    private final PasswordEncoder passwordEncoder;
    
    
    @Override
    public User getUserById(Long id) {
        return userRepo.getById(id);
    }
    @Override
    public User getUser(String username) {
        return userRepo.findUserByUsername(username);
    }

    @Override
    public User getUserbyEmail(String email) {
        return userRepo.findUserByEmail(email);
    }

    @Override
    public void saveUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepo.save(user);
    }

    @Override
    public boolean getState(String username) {
        return userRepo.findUserByUsername(username).isEnable();
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepo.findUserByUsername(username);
        if(user == null){
            log.error("User not found");
            throw new UsernameNotFoundException("User not found");
        }else {
            log.error("user found in databae: {}+{}",user.getUsername(),user.getPassword());
            Collection<GrantedAuthority> authoritys = new ArrayList<>();
            authoritys.add(new SimpleGrantedAuthority("USER"));
            log.error("check user:{}",user.toString());
            return new org.springframework.security.core.userdetails.User(user.getUsername(), user.getPassword(), user.isEnable(), true, true, true, authoritys);
        }
        
    }


    @Override
    public List<User> getUsersByEmail(String email) {
        return userRepo.findByEmail(email);
    }

    @Override
    public List<User> getUsersByUsername(String username) {
        return userRepo.findByUsername(username);
    }

    @Override
    public List<VerificationOtp> getOtps(String otp) {
        return verificationOtpRepo.findByOtp(otp);
    }

    @Override
    public void saveOtp(VerificationOtp otp) {
        verificationOtpRepo.save(otp);
    }

    @Override
    public void deleteUser(User user) {
        userRepo.delete(user);
    }

    @Override
    public void deleteOtp(VerificationOtp otp) {
        verificationOtpRepo.delete(otp);
    }

    @Override
    public void updateUser(User user) {
        userRepo.save(user);
    }

    @Override
    public void cleanData() {
        LocalDateTime dateTime = LocalDateTime.now();
        List<VerificationOtp> otps = verificationOtpRepo.findAll();
        otps = otps.stream().filter(otp->otp.getExpireAt().isBefore(dateTime) && otp.getConfirmedAt()==null).collect(Collectors.toList());
        otps.forEach(otp->{
            if(!otp.getUser().isEnable()){
                User u = otp.getUser();
                verificationOtpRepo.delete(otp);
                userRepo.delete(u);
            }else verificationOtpRepo.delete(otp);
        });
    }




    
    
}
