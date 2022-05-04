/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.todolist.service;

import com.example.todolist.entity.Folder;
import com.example.todolist.entity.Note;
import com.example.todolist.repo.FolderRepo;
import com.example.todolist.repo.NoteRepo;
import java.util.List;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 *
 * @author hp
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class NoteServiceImpl implements NoteService{
    
    private final NoteRepo noteRepo;
    private final FolderRepo folderRepo;
    
    @Override
    public Note getNote(Long id) {
        return noteRepo.getById(id);
    }

    @Override
    public void saveNote(Note note) {
        noteRepo.save(note);
    }

    @Override
    public void deleteNote(Long id) {
        noteRepo.delete(noteRepo.getById(id));
    }

    @Override
    public List<Note> getNotes(String title) {
        if(title.equals("")){
            return noteRepo.findAll();
        }
        else return noteRepo.findNoteByTitle(title);
    }

    @Override
    public void saveFolder(Folder folder) {
        folderRepo.save(folder);
    }

    @Override
    public void deleteFolder(Long id) {
        folderRepo.delete(folderRepo.getById(id));
    }

    @Override
    public List<Folder> getFolders() {
        return folderRepo.findAll();
    }

    @Override
    public void changeFolder(Long idNote, Long idFolder) {
        noteRepo.getById(idNote).setFolder(folderRepo.getById(idFolder));
    }

    @Override
    public Folder getFolder(Long id) {
        return folderRepo.getById(id);
    }


    
}
