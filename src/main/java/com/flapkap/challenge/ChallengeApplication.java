package com.flapkap.challenge;

import com.flapkap.challenge.entities.User;
import com.flapkap.challenge.entities.enums.UserRole;
import com.flapkap.challenge.services.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ChallengeApplication {

	@Autowired
	private UserService userService;
	@Value("${default.admin.username}")
	private String defaultAdminUsername;
	@Value("${default.admin.password}")
	private String defaultAdminPassword;

	public static void main(String[] args) {
		SpringApplication.run(ChallengeApplication.class, args);
	}

	@Bean
	public void init() {
		try {
			User admin = User.builder()
					.username(defaultAdminUsername)
					.password(defaultAdminPassword)
					.role(UserRole.ROLE_ADMIN)
					.build();
			userService.createUser(admin);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
