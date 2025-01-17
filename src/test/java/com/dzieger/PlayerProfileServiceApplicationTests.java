package com.dzieger;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = PlayerServiceApplication.class)
@ActiveProfiles("test")
class PlayerProfileServiceApplicationTests {

	@Autowired
	private Environment env;

	@Test
	void contextLoads() {
	}

}
