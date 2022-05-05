/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.todolist.service;

/**
 *
 * @author hp
 */
public interface MailSender {
    void send(String from, String to,String subject,String content);
}
