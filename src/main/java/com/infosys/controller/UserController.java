package com.infosys.controller;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.infosys.model.User;
import com.infosys.service.JwtService;
import com.infosys.service.UserService;
import com.infosys.util.AuthenticationResponse;
import com.infosys.util.LoginRequest;
import com.infosys.util.UserDTO;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
public class UserController {

	@Autowired
	private UserService userService;

	@Autowired
	private JwtService jwtService;

	@Autowired
	private AuthenticationManager authenticationManager;

	@GetMapping("/admin/adminProfile")
	@PreAuthorize("hasAuthority('ROLE_ADMIN')")
	public String adminProfile() {
		return "Welcome to Admin Profile";
	}

	@PostMapping("/register")
	public ResponseEntity<String> registerUser(@Valid @RequestBody User user, BindingResult bindingResult) {

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

	@PostMapping("/login/generateToken")
	public ResponseEntity<?> authenticateAndGetToken(@RequestBody LoginRequest authRequest) {
		Authentication authentication = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(authRequest.getEmail(), authRequest.getPassword()));

		if (authentication.isAuthenticated()) {
			String email = authRequest.getEmail();
			Optional<User> userOptional = userService.isUserConfirmed(email);
			if (userOptional.isPresent()) {

				User user = userOptional.get();
				if (user.isConfirmed()) {
					String token = jwtService.generateToken(email);
					String roleName = user.getRole();
					String userName = user.getName();

					AuthenticationResponse response = new AuthenticationResponse(userName, roleName, token);

					return ResponseEntity.ok(response);
				} else {
					return ResponseEntity.badRequest().body("Please confirm your email.");
				}
			} else {
				return ResponseEntity.badRequest().body("User not found");
			}
		} else {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid user request!");
		}
	}

	@GetMapping("/admin/getAll")
	@PreAuthorize("hasAuthority('ADMIN')")
	public ResponseEntity<List<UserDTO>> getAllUsers() {
		List<User> users = userService.getAllUsers();
		List<UserDTO> userDTOs = users.stream().map(this::convertToDTO).collect(Collectors.toList());
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

	@GetMapping("/user/{userId}")
	@PreAuthorize("hasAuthority('USER')")
	public ResponseEntity<?> getUserById(@PathVariable Long userId) {
		try {
			User user = userService.getUserById(userId);
			return ResponseEntity.ok(user);
		} catch (RuntimeException e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed : " + e.getMessage());
		}
	}

	@PutMapping("/user/{userId}")
	@PreAuthorize("hasAuthority('USER')")
	public ResponseEntity<?> updateUser(@PathVariable Long userId, @RequestBody User updatedUser) {
		try {
			User user = userService.updateUser(userId, updatedUser);
			return ResponseEntity.ok(user);
		} catch (RuntimeException e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Failed to update user : " + e.getMessage());
		}
	}

	@DeleteMapping("/admin/deleteById")
	@PreAuthorize("hasAuthority('ADMIN')")
	public ResponseEntity<String> deleteById(@RequestParam("id") Long id) {
		return userService.deleteById(id);
	}

	@DeleteMapping("/admin/deleteAll")
	@PreAuthorize("hasAuthority('ADMIN')")
	public ResponseEntity<String> deleteAll() {
		return userService.deleteAll();
	}

}
