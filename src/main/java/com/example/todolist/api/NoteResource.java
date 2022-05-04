/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.todolist.api;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.todolist.dto.FolderDto;
import com.example.todolist.dto.NoteDto;
import com.example.todolist.entity.Folder;
import com.example.todolist.entity.Note;
import com.example.todolist.entity.User;
import com.example.todolist.request.NoteRequest;
import com.example.todolist.service.NoteService;
import com.example.todolist.service.UserService;
import java.net.http.HttpRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author hp
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class NoteResource {
    private final NoteService noteService;
    private final UserService userService;
    private ModelMapper mapper = new ModelMapper();
    
    @GetMapping("/note")
    public ResponseEntity<List<NoteDto>> getNotes(@RequestParam(required = false,defaultValue = "") String title,HttpServletRequest request){
        String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        String token = authorizationHeader.substring("Bearer ".length());
        Algorithm algorithm = Algorithm.HMAC256("secret".getBytes());
        JWTVerifier verifier = JWT.require(algorithm).build();
        DecodedJWT decodedJWT = verifier.verify(token);
        String username = decodedJWT.getSubject();
        User u = userService.getUser(username);
        List<Note> notes = noteService.getNotes(title);
        notes =  notes.stream().filter(note -> note.getUser().getId() == u.getId()).collect(Collectors.toList());
        List<NoteDto> noteDtos = new ArrayList<>();
        notes.forEach((Note note)->{
            NoteDto noteDto = mapper.map(note, NoteDto.class); 
            noteDtos.add(noteDto);
        });
        return ResponseEntity.ok().body(noteDtos);
    }
    
    @PostMapping("/note/add")
    public ResponseEntity<?> addNote(@RequestBody NoteRequest n, HttpServletRequest request){
        String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        String token = authorizationHeader.substring("Bearer ".length());
        Algorithm algorithm = Algorithm.HMAC256("secret".getBytes());
        JWTVerifier verifier = JWT.require(algorithm).build();
        DecodedJWT decodedJWT = verifier.verify(token);
        String username = decodedJWT.getSubject();
        User u = userService.getUser(username);
        log.error(n.toString());
        Folder folder = null ;
        if(n.getIdFolder()!=null){
            folder = noteService.getFolder(n.getIdFolder());
        }
        Note note = new Note(null,n.getTitle(), n.getDescription(), n.getDateSet(), null,folder, u);
        log.error(note.toString());
        noteService.saveNote(note);
        return ResponseEntity.ok().build();
    }
    @PutMapping("/note/edit")
    public ResponseEntity<?> editNote(@RequestBody NoteDto n){
        Note note = noteService.getNote(n.getId());
        note.setTitle(n.getTitle());
        note.setDescription(n.getDescription());
        note.setDateSet(n.getDateSet());
        Folder folder = null;
        if(n.getFolder() != null){
            folder = noteService.getFolder(n.getFolder().getId());
        } 
        
        note.setFolder(folder);
        noteService.saveNote(note);
        return ResponseEntity.ok().build();
    }
    @DeleteMapping("/note/delete")
    public ResponseEntity<?> deleteNote(@RequestParam Long id){
        noteService.deleteNote(id);
        return ResponseEntity.ok().build();
    }
    @GetMapping("/folder")
    public ResponseEntity<List<FolderDto>> getFolder(HttpServletRequest request){
        String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        String token = authorizationHeader.substring("Bearer ".length());
        Algorithm algorithm = Algorithm.HMAC256("secret".getBytes());
        JWTVerifier verifier = JWT.require(algorithm).build();
        DecodedJWT decodedJWT = verifier.verify(token);
        String username = decodedJWT.getSubject();
        User u = userService.getUser(username);
        List<Folder> folders = noteService.getFolders();
        folders =  folders.stream().filter(folder -> folder.getUser().getId() == u.getId()).collect(Collectors.toList());
        log.error(u.getId().toString());
        List<FolderDto> folderDtos = new ArrayList<>();
        folders.forEach((Folder folder)->{
            
            FolderDto folderDto = mapper.map(folder, FolderDto.class); 
            folderDtos.add(folderDto);
        });
        return ResponseEntity.ok().body(folderDtos);
    }
    
    @PostMapping("/folder/add")
    public ResponseEntity<?> addFolder(@RequestBody Folder f,HttpServletRequest request){
        String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        String token = authorizationHeader.substring("Bearer ".length());
        Algorithm algorithm = Algorithm.HMAC256("secret".getBytes());
        JWTVerifier verifier = JWT.require(algorithm).build();
        DecodedJWT decodedJWT = verifier.verify(token);
        String username = decodedJWT.getSubject();
        User u = userService.getUser(username);
        
        Folder folder = new Folder(null, f.getName(), u);
        noteService.saveFolder(folder);
        return ResponseEntity.ok().build();
    }
    @PutMapping("/folder/edit")
    public ResponseEntity<?> editFolder(@RequestBody FolderDto f){
        Folder folder = noteService.getFolder(f.getId());
        folder.setName(f.getName());
        noteService.saveFolder(folder);
        return ResponseEntity.ok().build();
    }
    @DeleteMapping("/folder/delete")
    public ResponseEntity<?> deleteFolder(@RequestParam Long id){
        noteService.deleteFolder(id);
        return ResponseEntity.ok().build();
    }
    
    @PutMapping("/note/changefolder")
    public ResponseEntity<?> changeFolder(@RequestBody Long idNote,@RequestBody Long idFolder){
        noteService.changeFolder(idNote, idFolder);
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/folder/note")
    public ResponseEntity<List<NoteDto> > getNoteofFolder(@RequestParam Long id){
        List<Note> notes = noteService.getNotes("");
        notes =  notes.stream().filter(note -> note.getFolder().getId() == id).collect(Collectors.toList());
        List<NoteDto> noteDtos = new ArrayList<>();
        notes.forEach((Note note)->{
            NoteDto noteDto = mapper.map(note, NoteDto.class); 
            noteDtos.add(noteDto);
        });
        return ResponseEntity.ok().body(noteDtos);
    }
}
