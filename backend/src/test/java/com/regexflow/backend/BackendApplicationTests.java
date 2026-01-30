package com.regexflow.backend;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Integration test that loads the full Spring context.
 * Disabled by default as it requires database connection.
 * Enable when running with proper database configuration.
 */
@SpringBootTest
@Disabled("Requires database connection - run with proper database configuration")
class BackendApplicationTests {

	@Test
	void contextLoads() {
	}

}
