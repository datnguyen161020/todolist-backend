/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.todolist.repo;

import com.example.todolist.entity.Folder;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 *
 * @author hp
 */
public interface FolderRepo extends JpaRepository<Folder, Long>{
    Folder findByName(String name);
}
