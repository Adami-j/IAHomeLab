package fr.lab.iahomelab;

import fr.lab.iahomelab.config.PostgresTestConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import(PostgresTestConfiguration.class)
class IaHomeLabApplicationIT {

    @Test
    void contextLoads() {
    }
}