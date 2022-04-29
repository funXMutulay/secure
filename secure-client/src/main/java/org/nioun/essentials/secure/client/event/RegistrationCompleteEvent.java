package org.nioun.essentials.secure.client.event;

import org.nioun.essentials.secure.client.entity.User;
import org.springframework.context.ApplicationEvent;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegistrationCompleteEvent extends ApplicationEvent {
	
	private User user;
	private String applicationUrl;

	public RegistrationCompleteEvent(User user ,String applicationUrl) {
		super(user);
		this.user=user;
		this.applicationUrl=applicationUrl;
	}

}
