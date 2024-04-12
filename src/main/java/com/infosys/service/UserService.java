package com.infosys.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.infosys.model.User;
import com.infosys.repository.UserRepository;

@Service
public class UserService {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private EmailService emailService;

	@Autowired
	private PasswordEncoder passwordEncoder;

	public String registerUser(User user) {
		// Check if user already exists
		Optional<User> existingUserOptional = userRepository.findByEmail(user.getEmail());
		if (existingUserOptional.isPresent()) {
			return "Email already exists.";
		}

		// Generate confirmation code (6-digit OTP)
		String confirmationCode = emailService.generateRandomOTP();
		user.setConfirmationCode(confirmationCode);
		user.setConfirmed(false); // User is not yet confirmed

		if (user.getRole().equalsIgnoreCase("admin")) {
			user.setRole("ADMIN,USER");
		}
		// Encode user password before saving
		String encodedPassword = passwordEncoder.encode(user.getPassword());
		user.setPassword(encodedPassword);

		// Save user to the repository
		userRepository.save(user);

		// Optionally, return a success message or user ID upon successful registration
		return "User registered successfully. Confirmation code sent to " + user.getEmail();

	}

	public String confirmRegistration(String email, String confirmationCode) {
		Optional<User> optionalUser = userRepository.findByEmail(email);
		if (optionalUser.isPresent()) {
			User user = optionalUser.get();

			if (user.isConfirmed()) {
				return "User email is already verified";
			}
			if (!confirmationCode.equals(user.getConfirmationCode())) {
				return "Invalid OTP";
			}

			// Mark user as confirmed
			user.setConfirmed(true);
			userRepository.save(user);

			return "User registration confirmed successfully.";
		}
		return "Invalid email address";
	}

	public User updateUser(Long userId, User updatedUser) {
		User existingUser = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

		if (!existingUser.getEmail().equalsIgnoreCase(updatedUser.getEmail())) {

			if ((userRepository.findByEmail(updatedUser.getEmail()).isPresent())) {
				throw new RuntimeException("Email already exists");
			}
			existingUser.setEmail(updatedUser.getEmail());
			String confirmationCode = emailService.generateRandomOTP();
			updatedUser.setConfirmationCode(confirmationCode);
			updatedUser.setConfirmed(false);
		}
		if (!passwordEncoder.matches(updatedUser.getPassword(), existingUser.getPassword())) {
			existingUser.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
		}

		existingUser.setName(updatedUser.getName());
		existingUser.setPhoneNumber(updatedUser.getPhoneNumber());
		existingUser.setCoins(updatedUser.getCoins());

		return userRepository.save(existingUser);
	}

	public List<User> getAllUsers() {
		// TODO Auto-generated method stub
		List<User> users = userRepository.findAll();
		// Fetch walletItems eagerly to avoid lazy-loading issues
		users.forEach(user -> user.getWalletItems().size());
		return users;
	}

	public User getUserById(Long userId) {
		return userRepository.findById(userId)
				.orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
	}

	public ResponseEntity<String> deleteById(Long id) {

		userRepository.deleteById(id);
		return ResponseEntity.status(HttpStatus.NO_CONTENT).body("User deleted successfully.");
	}

	public ResponseEntity<String> deleteAll() {
		// TODO Auto-generated method stub
		userRepository.deleteAll();
		return ResponseEntity.status(HttpStatus.NO_CONTENT).body("All Users deleted");
	}

	public Optional<User> isUserConfirmed(String email) {
		Optional<User> userOptional = userRepository.findByEmail(email);
		return userOptional;
	}
}
