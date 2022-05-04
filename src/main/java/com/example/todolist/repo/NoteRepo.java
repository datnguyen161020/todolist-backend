/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.todolist.repo;

import com.example.todolist.entity.Note;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/**
 *
 * @author hp
 */
public interface NoteRepo extends JpaRepository<Note, Long>{
    @Query("SELECT n FROM Note n WHERE n.title LIKE %?1% ")
    List<Note> findNoteByTitle(String title);
}
