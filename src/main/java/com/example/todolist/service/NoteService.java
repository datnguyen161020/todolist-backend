/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.todolist.service;

import com.example.todolist.entity.Folder;
import com.example.todolist.entity.Note;
import java.util.List;

/**
 *
 * @author hp
 */
public interface NoteService {
    public Note getNote(Long id);
    public void saveNote(Note note);
    public void deleteNote(Long id);
    public List<Note> getNotes(String title);
    public void saveFolder(Folder folder);
    public void deleteFolder(Long id);
    public List<Folder> getFolders();
    public Folder getFolder(Long id);
    public void changeFolder(Long idNote, Long idFolder);
    
}
