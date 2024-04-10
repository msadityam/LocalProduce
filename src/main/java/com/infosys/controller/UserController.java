package com.infosys.controller;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import com.infosys.model.User;
import com.infosys.service.UserService;
import com.infosys.util.AuthenticationResponse;
import com.infosys.util.JwtTokenProvider;
import com.infosys.util.LoginRequest;
import com.infosys.util.UserDTO;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/users")
public class UserController {

	    @Autowired
	    private UserService userService;

	    @PostMapping("/register")
	    public ResponseEntity<String> registerUser(@Valid @RequestBody User user, BindingResult bindingResult) {
	        System.out.println("===========================");
	        System.out.println("Received user registration request: " + user);
	    	if (bindingResult.hasErrors()) {
	            StringBuilder errorMessage = new StringBuilder("Validation failed: ");
	            for (FieldError error : bindingResult.getFieldErrors()) {
	                errorMessage.append(error.getDefaultMessage()).append("; ");
	            }
	            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessage.toString());
	        }

	        String result = userService.registerUser(user);
	        return ResponseEntity.status(HttpStatus.CREATED).body(result);
	    }
	
    @GetMapping("/confirm")
    public String confirmRegistration(@RequestParam("email") String email, @RequestParam("otp") String otp) {
        return userService.confirmRegistration(email, otp);
    }
    
    @GetMapping("/getAll")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        List<UserDTO> userDTOs = users.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(userDTOs);
    }

    private UserDTO convertToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setPhoneNumber(user.getPhoneNumber());
        return dto;
    }
    
    @GetMapping("/{userId}")
    public ResponseEntity<User> getUserById(@PathVariable Long userId) {
        User user = userService.getUserById(userId);
        return ResponseEntity.ok(user);
    }
    
    @PutMapping("/{userId}")
    public ResponseEntity<?> updateUser(@PathVariable Long userId, @RequestBody User updatedUser) {
       try {
    	User user = userService.updateUser(userId, updatedUser);
    	return ResponseEntity.ok(user);
       }
       catch (RuntimeException e) {
           return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                   .body("Failed to update user : " + e.getMessage());
       }
        
    }
    
  
    
    @DeleteMapping("/deleteById")
    public ResponseEntity<String> deleteById(@RequestParam("id") Long id) {
        return userService.deleteById(id);
    }
    
    @DeleteMapping("/deleteAll")
    public ResponseEntity<String> deleteAll() {
        return userService.deleteAll();
    }
    
    
//    @Autowired
//    private AuthenticationManager authenticationManager;
//
//    @Autowired
//    private JwtTokenProvider jwtTokenProvider;

//    @PostMapping("/login")
//    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
//        try {
//            Authentication authentication = authenticationManager.authenticate(
//                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
//            );
//
//            SecurityContextHolder.getContext().setAuthentication(authentication);
//
//            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
//            String jwtToken = jwtTokenProvider.generateToken(authentication);
//
//            return ResponseEntity.ok(new AuthenticationResponse(userDetails.getUsername(), jwtToken));
//        } catch (AuthenticationException ex) {
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
//        }
//    }
}
