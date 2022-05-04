/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.todolist.api;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.todolist.email.MailSender;
import com.example.todolist.entity.User;
import com.example.todolist.entity.VerificationOtp;
import com.example.todolist.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author hp
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class UserResource {
    private final UserService userService;
    private final MailSender mailSender;
    
    @PostMapping("/register")
    public void regiter(HttpServletRequest request, HttpServletResponse response) throws IOException{
        String test = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
        String[] line = test.split("[\\r?\\n\\:\"\\s+\\,{}]");
        line = Arrays.stream(line).filter(s->!s.isEmpty()).toArray(String[]::new);
        Map<String,String> regisReq = new HashMap<>();
        String key = null;
        for(int i=0;i<line.length;i++){
            if(line[i].equals("name")||line[i].equals("email")||line[i].equals("username")||line[i].equals("password")){
                key=line[i];
            }else {
                regisReq.put(key, regisReq.get(key)==null?line[i]:regisReq.get(key)+" "+line[i]);
            }
            
        }
        List<User> usersByUsername = userService.getUsersByUsername(regisReq.get("username"));
        if(usersByUsername.isEmpty()){
            List<User> users = userService.getUsersByEmail(regisReq.get("email"));
            if(users.isEmpty()){
                User u = new User(null, regisReq.get("name"),regisReq.get("username"),regisReq.get("password"),regisReq.get("email"),false);
                String chars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
                int string_length = 6;
                String randomstring = "";
                for (int i=0; i<string_length; i++) {
                    int rnum = (int) Math.floor(Math.random() * chars.length());
                    randomstring+= chars.charAt(rnum);
                }
                VerificationOtp otp = new VerificationOtp(null, randomstring,
                        LocalDateTime.now(), LocalDateTime.now().plusMinutes(5), null, u);              
                mailSender.send("todolistapp0305@gmail.com", u.getEmail(), "Confirm account", "OTP code:"+otp.getOtp());
                userService.saveUser(u);
                userService.saveOtp(otp);
                Algorithm algorithm = Algorithm.HMAC256("secret".getBytes());
                JWTVerifier verifier = JWT.require(algorithm).build();
                String verify_token = JWT.create()
                        .withSubject((u.getUsername()))
                        .withIssuer(request.getRequestURL().toString())
                        .withExpiresAt(new Date(System.currentTimeMillis()+10*60*1000))
                        .sign(algorithm);     
                Map<String, String> tokens = new HashMap<>();
                tokens.put("verify_token", verify_token);
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                new ObjectMapper().writeValue(response.getOutputStream(), tokens);
            }else {
                response.setHeader("error","Email invalid");
                response.setStatus(HttpStatus.FORBIDDEN.value());
    //                    response.sendError(HttpStatus.FORBIDDEN.value());
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                new ObjectMapper().writeValue(response.getOutputStream(), "Email invalid");
            } 
        }else {
            response.setHeader("error","Username invalid");
            response.setStatus(HttpStatus.FORBIDDEN.value());
//                    response.sendError(HttpStatus.FORBIDDEN.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            new ObjectMapper().writeValue(response.getOutputStream(), "Username invalid");
        }
        
    }
//    @GetMapping("/token/refresh")
//    public void
    @PutMapping("/register/confirm")
    public void registration(HttpServletRequest request, HttpServletResponse response) throws IOException{
        String test = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
        String[] line = test.split("[\\r?\\n\\:\"\\s+\\,{}]");
        line = Arrays.stream(line).filter(s->!s.isEmpty()).toArray(String[]::new);
        String otpCode = line[1];
        String verify_token = request.getParameter("token");
        if(verify_token != null){
            try{
                //String verify_token = authorizationHeader.substring("Bearer ".length());
                Algorithm algorithm = Algorithm.HMAC256("secret".getBytes());
                JWTVerifier verifier = JWT.require(algorithm).build();
                DecodedJWT decodedJWT = verifier.verify(verify_token);
                String username = decodedJWT.getSubject();
//                Map<String,Claim> userMap = decodedJWT.getClaims();
                User u = userService.getUser(username);
                List<VerificationOtp> otps = userService.getOtps(otpCode);
                VerificationOtp otp = new VerificationOtp();
                otps.forEach(otpTmp->{
                    if(otpTmp.getUser().equals(u) && !otpTmp.getUser().isEnable() && otp.getExpireAt()==null ? true :otpTmp.getExpireAt().isAfter(otp.getExpireAt())){
                        otp.setId(otpTmp.getId());
                        otp.setOtp(otpTmp.getOtp());
                        otp.setCreateAt(otpTmp.getCreateAt());
                        otp.setExpireAt(otpTmp.getExpireAt());
                        otp.setUser(otpTmp.getUser());
                    }
                    });
                LocalDateTime ldt = LocalDateTime.now();
                if(ldt.isBefore(otp.getExpireAt())){
//                    User user = new User(null, u.getName(),u.getUsername(),userMap.get("password").asString(),u.getEmail(),true);
//                    userService.deleteOtp(otp);
//                    userService.deleteUser(u);
//                    userService.saveUser(user);
                    u.setEnable(true);
                    userService.updateUser(u);
                    otp.setConfirmedAt(ldt);
                    userService.saveOtp(otp);
                    new ObjectMapper().writeValue(response.getOutputStream(), "Regiter success");
                }else{
                    response.setHeader("error","Otp is expired");
                    response.setStatus(HttpStatus.FORBIDDEN.value());
                    new ObjectMapper().writeValue(response.getOutputStream(), "Otp is expired");
                }
            } catch(JWTVerificationException | IOException | IllegalArgumentException e){
                throw new RuntimeException("Verify token is missing");
            }
        }
    }
    @PostMapping("/register/refreshotp")
    public void refreshOtp(HttpServletRequest request, HttpServletResponse response) throws IOException{
        String test = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
        String verify_token = request.getParameter("token");
        if(verify_token != null){
            try{
                //String verify_token = authorizationHeader.substring("Bearer ".length());
                Algorithm algorithm = Algorithm.HMAC256("secret".getBytes());
                JWTVerifier verifier = JWT.require(algorithm).build();
                DecodedJWT decodedJWT = verifier.verify(verify_token);
                String username = decodedJWT.getSubject();
                User u = userService.getUser(username);
                String chars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
                int string_length = 6;
                String randomstring = "";
                for (int i=0; i<string_length; i++) {
                    int rnum = (int) Math.floor(Math.random() * chars.length());
                    randomstring+= chars.charAt(rnum);
                }
                VerificationOtp otp = new VerificationOtp(null, randomstring, LocalDateTime.now(), LocalDateTime.now().plusMinutes(5), null, u);
                userService.saveOtp(otp);
                mailSender.send("todolistapp0305@gmail.com", u.getEmail(), "Confirm account", "New OTP code:"+otp.getOtp());
                String verify_refresh_token = JWT.create()
                        .withSubject((u.getUsername()))
                        .withIssuer(request.getRequestURL().toString())
                        .withExpiresAt(new Date(System.currentTimeMillis()+10*60*1000))
                        .sign(algorithm);     
                Map<String, String> tokens = new HashMap<>();
                tokens.put("verify_token", verify_refresh_token);
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                new ObjectMapper().writeValue(response.getOutputStream(), tokens);
            } catch(JWTVerificationException | IOException | IllegalArgumentException e){
                throw new RuntimeException("Verify token is missing");
            }
        }
    }
}
