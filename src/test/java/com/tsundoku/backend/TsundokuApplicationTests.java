package com.tsundoku.backend;

import com.tsundoku.backend.auth.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;

import javax.sql.DataSource;

@SpringBootTest
@ActiveProfiles("test")
class TsundokuApplicationTests {

    @MockBean
    private DataSource dataSource;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private StringRedisTemplate redisTemplate;

    @Test
    void contextLoads() {
        // Context loads successfully without requiring live DB connection in offline unit test run
    }
}
