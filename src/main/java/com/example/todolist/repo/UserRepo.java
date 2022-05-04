/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.todolist.repo;

import com.example.todolist.entity.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 *
 * @author hp
 */
public interface UserRepo extends JpaRepository<User, Long>{
    User findUserByUsername(String username);
    List<User> findByUsername(String username);
    List<User> findByEmail(String email);
}
