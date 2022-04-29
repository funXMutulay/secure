package org.nioun.essentials.secure.client.service;

import java.util.Calendar;
import java.util.Optional;
import java.util.UUID;

import org.nioun.essentials.secure.client.entity.PasswordResetToken;
import org.nioun.essentials.secure.client.entity.User;
import org.nioun.essentials.secure.client.entity.VerificationToken;
import org.nioun.essentials.secure.client.model.UserModel;
import org.nioun.essentials.secure.client.repository.PasswordResetTokenRepository;
import org.nioun.essentials.secure.client.repository.UserRepository;
import org.nioun.essentials.secure.client.repository.VerificationTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Component
public class UserServiceImpl implements UserService {

	@Autowired
	UserRepository userRepository;


	@Autowired
	private PasswordEncoder passwordEncoder;
	
	@Autowired
	VerificationTokenRepository verificationTokenRepository;


	@Autowired
	private PasswordResetTokenRepository passwordResetTokenRepository;
	
	@Override
	public User registerUser(UserModel userModel) {
		User user = new User();
		user.setFirstName(userModel.getFirstName());
		user.setLastName(userModel.getLastName());
		user.setEmail(userModel.getEmail());
		user.setPassword(passwordEncoder.encode(userModel.getPassword()));
		user.setRole("USER");
		userRepository.save(user);
		return user;
	}

	@Override
	public void saveVerificationTokenForUser(String token, User user) {
		VerificationToken verificationToken = new VerificationToken(user,token);
		
		verificationTokenRepository.save(verificationToken);
	}

	@Override
	public String validateVerificationToken(String token) {
		VerificationToken verificationToken = verificationTokenRepository.findByToken(token); 
		if(verificationToken == null) {
			return "invalid user";
		}
		User user = verificationToken.getUser();
		Calendar cal = Calendar.getInstance();
		if(verificationToken.getExpirationTime().getTime() - cal.getTime().getTime() <= 0) {
			verificationTokenRepository.delete(verificationToken);
			return "expired";
		}
		
		user.setEnabled(true);
		userRepository.save(user);
		
		return "valid";
	}

	@Override
	public VerificationToken generateNewVerificationToken(String oldToken) {
		VerificationToken verificationToken= verificationTokenRepository.findByToken(oldToken);
		verificationToken.setToken(UUID.randomUUID().toString());
		verificationTokenRepository.save(verificationToken);
		return verificationToken;
	}

	
	public User findByEmail(String email) {
	
		return userRepository.findByEmail(email);
	}

	
	public void createPasswordResetTokenForUser(User user, String token) {
		PasswordResetToken passwordResetToken = new PasswordResetToken(user, token);
		 passwordResetTokenRepository.save(passwordResetToken);  
		
	}

	@Override
	public String validatePasswordResetToken(String token) {
		PasswordResetToken passwordResetToken = passwordResetTokenRepository.findByToken(token); 
		if(passwordResetToken == null) {
			return "invalid user";
		}
		
		User user = passwordResetToken.getUser();
		
		Calendar cal = Calendar.getInstance();
		
		if(passwordResetToken.getExpirationTime().getTime() - cal.getTime().getTime() <= 0) {
			passwordResetTokenRepository.delete(passwordResetToken);
			return "expired";
		}
				
		return "valid";
	}

	@Override
	public Optional<User> getUserByPasswordResetToken(String token) {
		
		return Optional.ofNullable(passwordResetTokenRepository.findByToken(token).getUser());
	}

	@Override
	public void changePassword(User user, String newPassword) {
		user.setPassword(passwordEncoder.encode(newPassword));
		userRepository.save(user);
		
	}

	@Override
	public boolean checkIfValidOldPassword(User user, String oldPassword) {
		// TODO Auto-generated method stub
		return passwordEncoder.matches(oldPassword, user.getPassword());
	}

}
