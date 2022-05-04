/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.todolist.request;

import java.util.Date;
import lombok.Data;

/**
 *
 * @author hp
 */
@Data
public class NoteRequest {
    private String title;
    private String description;
    private Date dateSet;
    private Long idFolder;
}
