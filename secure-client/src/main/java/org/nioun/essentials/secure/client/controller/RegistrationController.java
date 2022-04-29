package org.nioun.essentials.secure.client.controller;

import java.util.Optional;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import org.nioun.essentials.secure.client.entity.User;
import org.nioun.essentials.secure.client.entity.VerificationToken;
import org.nioun.essentials.secure.client.event.RegistrationCompleteEvent;
import org.nioun.essentials.secure.client.model.PasswordModel;
import org.nioun.essentials.secure.client.model.UserModel;
import org.nioun.essentials.secure.client.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public class RegistrationController {
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private ApplicationEventPublisher publisher ;
	
	@PostMapping("/register")
	public String registerUser(@RequestBody UserModel userModel,final HttpServletRequest request  ) {
		 User user= userService.registerUser(userModel);
		 publisher.publishEvent(new RegistrationCompleteEvent(
				 user,
				 applicationUrl(request)
				 
				 ));
		 return "Success";
	}
	
	@GetMapping("/verifyRegistration")
	public String verifyRegistration(@RequestParam("token") String token ) {
		
		String result = userService.validateVerificationToken(token);
		if(result.equalsIgnoreCase("valid")) {
			return "User verifies successfully";
		}
		
		return "bad user ";
	}
	
	@GetMapping("/resendVerifyToken")
	public String resendVerificationToken(@RequestParam("token") String oldToken ,
											HttpServletRequest request) {
		
		VerificationToken verificationToken 
								= userService.generateNewVerificationToken(oldToken);
	
		User user = verificationToken.getUser(); 	
	
		resendVerificationTokenMail(user,applicationUrl(request) , verificationToken);	
	
		return "Verification Link Sent";
	}
	
	@PostMapping("/resetPassword")
	public String resetPassword(@RequestBody PasswordModel passwordModel , HttpServletRequest request) {
		
		User user = userService.findByEmail(passwordModel.getEmail());
	
		String url ="";
		
		if(user!=null) {
		String token = UUID.randomUUID().toString();
		userService.createPasswordResetTokenForUser(user,token);
		url= passwordResetTokenMail(user,applicationUrl(request),token);
		}
		
		
		return url;
	}

	@PostMapping("/savePassword")
	public String savePassword(@RequestParam("token") String token, @RequestBody PasswordModel passwordModel) {
		
		String result = userService.validatePasswordResetToken(token); 
		if(!result.equalsIgnoreCase("valid")) {
			
			return "invalid token";
		}
		Optional<User> user = userService.getUserByPasswordResetToken(token);
		
		if(user.isPresent()) {
			userService.changePassword(user.get() , passwordModel.getNewPassword());
		} else {
			return "invalid Token";
		}
		
		return "password saved "+user.get().getFirstName();
	}
	
	@PostMapping("/changePassword")
	public String changePassword(@RequestBody PasswordModel passwordModel ) {
		
		User user = userService.findByEmail(passwordModel.getEmail());
		
		if (!userService.checkIfValidOldPassword(user,passwordModel.getOldPassword())) {
			
			return "invalid old password";
		
		}
		
		// Save New Password 
		
		userService.changePassword(user, passwordModel.getNewPassword());
		return "Password changed successfully";
		
	}
	
	private String passwordResetTokenMail(User user, String applicationUrl , String token ) {
		
		
		String url = 
				applicationUrl
				+ "/savePassword?token="
                + token;		

		//To Be Send To Verfification Mail
		log.info( "Click that link to verify account: {}",url);
		
		return url;
	}

	private void resendVerificationTokenMail(User user, String applicationUrl , VerificationToken verificationToken) {
		// send mail to user 
		
				String url = 
						applicationUrl
						+ "/verifyRegistration?token="
		                + verificationToken.getToken();		

				//To Be Send To Verfification Mail
				log.info( "Click that link to verify account: {}",url);
		
	}
	
	private String applicationUrl(HttpServletRequest request) {
		return "http://"+
	            request.getServerName()+
	            ":"+
	            request.getServerPort() +
	            request.getContextPath() ;
	}

}
