package com.infosys.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.infosys.model.Item;
import com.infosys.model.User;
import com.infosys.repository.UserRepository;
import com.infosys.util.AuthenticationResponse;

@Service
public class UserService  {
	
	

	@Autowired
    private UserRepository userRepository;
	
	
	 
	 @Autowired
	 private EmailService emailService;

//    @Autowired
//    private PasswordEncoder passwordEncoder;

    public String registerUser(User user) {
        // Check if user already exists
        if (userRepository.findByEmail(user.getEmail())!=null) {
            return "Email already exists.";
        }

        // Generate confirmation code (6-digit OTP)
        String confirmationCode = emailService.generateRandomOTP();
        user.setConfirmationCode(confirmationCode);
        user.setConfirmed(false);
        
        

        userRepository.save(user);

        // Send confirmation email
         //emailService.sendConfirmationEmail(user.getEmail(), confirmationCode);

        return "User registered successfully. Please check your email to confirm registration.";
    }

    public String confirmRegistration(String email, String confirmationCode) {
    	 User user = userRepository.findByEmail(email);
         if (user == null) {
             return "Invalid email address";
         }
         if(user.isConfirmed()) {
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

//    @Override
//    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
//        User user = userRepository.findByEmail(email);
//        
//                //.orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
//
//        return org.springframework.security.core.userdetails.User.builder()
//                .username(user.getEmail())
//                .password(user.getPassword())
//                .roles(user.getRole()) // Add roles/permissions as needed
//                .build();
//    }

    public User updateUser(Long userId, User updatedUser) {
        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (existingUser.getEmail()!=updatedUser.getEmail()) {
        	
        	if (userRepository.findByEmail(updatedUser.getEmail())!=null) {
                throw new RuntimeException( "Email already exists");
            }

            // Generate confirmation code (6-digit OTP)
            String confirmationCode = emailService.generateRandomOTP();
            updatedUser.setConfirmationCode(confirmationCode);
            updatedUser.setConfirmed(false);
        }

        // Update the existing user with the new data
        existingUser.setEmail(updatedUser.getEmail());
        existingUser.setName(updatedUser.getName());
        existingUser.setPhoneNumber(updatedUser.getPhoneNumber());
        existingUser.setRole(updatedUser.getRole());
        existingUser.setPassword(updatedUser.getPassword());
        existingUser.setCoins(updatedUser.getCoins());
       

        // Save the updated user
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
}
