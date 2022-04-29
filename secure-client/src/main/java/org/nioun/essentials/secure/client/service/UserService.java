package org.nioun.essentials.secure.client.service;

import java.util.Optional;

import org.nioun.essentials.secure.client.entity.User;
import org.nioun.essentials.secure.client.entity.VerificationToken;
import org.nioun.essentials.secure.client.model.UserModel;
import org.springframework.stereotype.Service;

@Service
public interface UserService {

	User registerUser(UserModel userModel);

	void saveVerificationTokenForUser(String token, User user);

	String validateVerificationToken(String token);

	VerificationToken generateNewVerificationToken(String oldToken);

	User findByEmail(String email);

	void createPasswordResetTokenForUser(User user, String token);

	String validatePasswordResetToken(String token);

	Optional<User> getUserByPasswordResetToken(String token);

	void changePassword(User user, String newPassword);

	boolean checkIfValidOldPassword(User user, String oldPassword);

}
