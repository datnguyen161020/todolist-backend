/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.todolist.service;

import com.example.todolist.entity.User;
import com.example.todolist.entity.VerificationOtp;
import java.util.List;

/**
 *
 * @author hp
 */
public interface UserService{
    public User getUser(String username);
    public boolean getState(String username);
    public void saveUser(User user);
    public void updateUser(User user);
    public void deleteUser(User user);
    public List<User> getUsersByUsername(String username);
    public List<User> getUsersByEmail(String email);
    public List<VerificationOtp> getOtps(String otp);
    public void saveOtp(VerificationOtp otp);
    public void deleteOtp(VerificationOtp otp);
    public void cleanData();
}
