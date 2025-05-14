package ru.netology.cloudservice;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Testcontainers
class CloudServiceApplicationTests {

	@Container
	private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:13")
			.withDatabaseName("testdb")
			.withUsername("testuser")
			.withPassword("testpass");

	@Test
	void contextLoads() {
		assertTrue(postgres.isRunning());
	}
}