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
import com.example.todolist.dto.UserDto;
import com.example.todolist.service.MailSender;
import com.example.todolist.entity.User;
import com.example.todolist.entity.VerificationOtp;
import com.example.todolist.service.UserService;
import com.google.api.client.json.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

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
    private ModelMapper mapper = new ModelMapper();
    
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
    @GetMapping("/user/detail")
    public ResponseEntity<UserDto> getUserDetail(HttpServletRequest request){
        String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        String access_token = authorizationHeader.substring("Bearer ".length());
        Algorithm algorithm = Algorithm.HMAC256("secret".getBytes());
        JWTVerifier verifier = JWT.require(algorithm).build();
        DecodedJWT decodedJWT = verifier.verify(access_token);
        String username = decodedJWT.getSubject();
        User u = userService.getUser(username);
        UserDto udto = mapper.map(u, UserDto.class);
        return ResponseEntity.ok().body(udto);
    }
    @PutMapping("/user/detail/edit")
    public ResponseEntity<?> getUserDetail(@RequestBody UserDto userDto){
        User u = userService.getUserById(userDto.getId());
        u.setName(userDto.getName());
        u.setEmail(userDto.getEmail());
        u.setUsername(userDto.getUsername());
        
        List<User> usersByUsername = userService.getUsersByUsername(userDto.getUsername());
        List<User> usersByEmail = userService.getUsersByEmail(userDto.getEmail());
        if(usersByEmail.isEmpty() && usersByUsername.isEmpty()){
            userService.saveUser(u);
            return ResponseEntity.ok().build();
        }else return ResponseEntity.badRequest().body("Username or password invalid"); 
    }
    @PostMapping("/password/forget")
    public ResponseEntity<?> forgetPassword(@RequestBody String email) throws MalformedURLException{
        User user = userService.getUserbyEmail(email);
        String chars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        int string_length = 6;
        String randomstring = "";
        for (int i=0; i<string_length; i++) {
            int rnum = (int) Math.floor(Math.random() * chars.length());
            randomstring+= chars.charAt(rnum);
        }
        VerificationOtp otp = new VerificationOtp(null, randomstring, LocalDateTime.now(), LocalDateTime.now().plusMinutes(5), null, user);
        userService.saveOtp(otp);
        mailSender.send("todolistapp0305@gmail.com", user.getEmail(), "Confirm account", "New OTP code:"+otp.getOtp());
        Algorithm algorithm = Algorithm.HMAC256("secret".getBytes());
        JWTVerifier verifier = JWT.require(algorithm).build();
        String token = JWT.create()
                .withSubject((user.getUsername()))
                .withIssuer(URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/api/password/forget").toUriString()).toURL().toString())
                .withExpiresAt(new Date(System.currentTimeMillis()+10*60*1000))
                .sign(algorithm);     
        Map<String, String> tokens = new HashMap<>();
        tokens.put("verify_token", token);
        return ResponseEntity.ok().body(tokens);
    }
    @PutMapping("/password/confirm")
    public void confirmPassword(HttpServletRequest request, HttpServletResponse response) throws IOException{
        String test = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
        String[] line = test.split("[\\r?\\n\\:\"\\s+\\,{}]");
        line = Arrays.stream(line).filter(s->!s.isEmpty()).toArray(String[]::new);
        String otpCode = line[1];
        String token = request.getParameter("token");
        if(token != null){
            try{
                //String verify_token = authorizationHeader.substring("Bearer ".length());
                Algorithm algorithm = Algorithm.HMAC256("secret".getBytes());
                JWTVerifier verifier = JWT.require(algorithm).build();
                DecodedJWT decodedJWT = verifier.verify(token);
                String username = decodedJWT.getSubject();
//                Map<String,Claim> userMap = decodedJWT.getClaims();
                User user = userService.getUser(username);
                List<VerificationOtp> otps = userService.getOtps(otpCode);
                VerificationOtp otp = new VerificationOtp();
                otps.forEach(otpTmp->{
                    if(otpTmp.getUser().equals(user) && otpTmp.getUser().isEnable() && otp.getExpireAt()==null ? true :otpTmp.getExpireAt().isAfter(otp.getExpireAt())){
                        otp.setId(otpTmp.getId());
                        otp.setOtp(otpTmp.getOtp());
                        otp.setCreateAt(otpTmp.getCreateAt());
                        otp.setExpireAt(otpTmp.getExpireAt());
                        otp.setUser(otpTmp.getUser());
                    }
                });
                LocalDateTime ldt = LocalDateTime.now();
                if(ldt.isBefore(otp.getExpireAt())){
                    otp.setConfirmedAt(ldt);
                    userService.saveOtp(otp);
                    String access_token = JWT.create()
                        .withSubject(user.getUsername())
                        .withExpiresAt(new Date(System.currentTimeMillis()+1*24*60*60*1000))
                        .withIssuer(request.getRequestURL().toString())
                        .sign(algorithm);
                    String refresh_token = JWT.create()
                            .withSubject(user.getUsername())
                            .withExpiresAt(new Date(System.currentTimeMillis()+30*60*1000))
                            .withIssuer(request.getRequestURL().toString())
                            .sign(algorithm);
                    Map<String, String> tokens = new HashMap<>();
                    tokens.put("access_token", access_token);
                    tokens.put("refresh_token", refresh_token);
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    new ObjectMapper().writeValue(response.getOutputStream(), tokens);
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
    
    @PutMapping("/password/change")
    public void changePassword(HttpServletRequest request,HttpServletResponse response, @RequestBody String password) throws IOException{
        String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if(authorizationHeader != null && authorizationHeader.startsWith("Bearer ")){
            try{
                String token = authorizationHeader.substring("Bearer ".length());
                Algorithm algorithm = Algorithm.HMAC256("secret".getBytes());
                JWTVerifier verifier = JWT.require(algorithm).build();
                DecodedJWT decodedJWT = verifier.verify(token);
                String username = decodedJWT.getSubject();
                User u = userService.getUser(username);
                u.setPassword(password);
                userService.saveUser(u);
                new ObjectMapper().writeValue(response.getOutputStream(), "Confirm success");
                
            } catch(JWTVerificationException | IOException | IllegalArgumentException e){
                throw new RuntimeException("token is missing");
            }
        } else {
            throw new RuntimeException("Refresh token is missing");
        }
    }
    
    @PostMapping("/login/social/google")
    public void loginWithGoogle(HttpServletRequest request,HttpServletResponse response,@RequestParam String idTokenString) throws GeneralSecurityException, IOException{
        String CLIENT_ID ="658977310896-knrl3gka66fldh83dao2rhgbblmd4un9.apps.googleusercontent.com";
        HttpTransport transport = new NetHttpTransport();
        JsonFactory factory = new GsonFactory();

            
                GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(transport, factory)
                        .setAudience(Arrays.asList(CLIENT_ID)).build();
                log.error("token:{}",idTokenString);
                GoogleIdToken idToken = GoogleIdToken.parse(verifier.getJsonFactory(), idTokenString);
                log.error("token:{}",idToken);
                boolean tokenIsValid = (idToken != null) && verifier.verify(idToken);
                log.error("check: {}",verifier.verify(idToken));
//                if(tokenIsValid){
                    log.error("token :{}",idToken.toString());
                    Payload payload = idToken.getPayload();
                    log.error(payload.toString());
                    String userId = payload.getSubject();
                    String email = payload.getEmail();
                    String name = (String) payload.get("name");
                    String pictureUrl = (String) payload.get("picture");
                    String locale = (String) payload.get("locale");
                    String familyName = (String) payload.get("family_name");
                    String givenName = (String) payload.get("given_name");
                    User user = userService.getUserbyEmail(email);
                    if(user==null){
                        user = new User(null,name,email,"default",email,true);
                        userService.saveUser(user);
                    }
                    Algorithm algorithm = Algorithm.HMAC256("secret".getBytes());
                    String access_token = JWT.create()
                            .withSubject(user.getUsername())
                            .withExpiresAt(new Date(System.currentTimeMillis()+1*24*60*60*1000))
                            .withIssuer(request.getRequestURL().toString())
                            .sign(algorithm);
                    String refresh_token = JWT.create()
                            .withSubject(user.getUsername())
                            .withExpiresAt(new Date(System.currentTimeMillis()+30*60*1000))
                            .withIssuer(request.getRequestURL().toString())
                            .sign(algorithm);
                    Map<String, String> tokens = new HashMap<>();
                    tokens.put("access_token", access_token);
                    tokens.put("refresh_token", refresh_token);
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    new ObjectMapper().writeValue(response.getOutputStream(), tokens);
//                    new ObjectMapper().writeValue(response.getOutputStream(), email);
//                } else {
//                    new ObjectMapper().writeValue(response.getOutputStream(), "Token invalid");
//                }

    }
}
    
