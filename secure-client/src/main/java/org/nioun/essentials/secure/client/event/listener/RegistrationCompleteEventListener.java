package org.nioun.essentials.secure.client.event.listener;

import java.util.UUID;

import org.nioun.essentials.secure.client.entity.User;
import org.nioun.essentials.secure.client.event.RegistrationCompleteEvent;
import org.nioun.essentials.secure.client.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component 
@Slf4j
public class RegistrationCompleteEventListener implements ApplicationListener<RegistrationCompleteEvent> {

	@Autowired
	private UserService userService;
	
	@Override
	public void onApplicationEvent(RegistrationCompleteEvent event) {
	// create the verification Token for the user with link
		
		User user = event.getUser();
		String token = UUID.randomUUID().toString(); 
		userService.saveVerificationTokenForUser(token , user);

		// send mail to user 
		
		String url = 
				event.getApplicationUrl()
				+ "/verifyRegistration?token="
                + token;		

		//To Be Send To Verfification Mail
		log.info( "Click that link to verify account: {}",url);
	
	}

}
