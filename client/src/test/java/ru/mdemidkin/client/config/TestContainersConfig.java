package ru.mdemidkin.client.config;

import org.junit.jupiter.api.Tag;
import org.springframework.test.annotation.DirtiesContext;

@Tag("integration")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class TestContainersConfig implements PostgresContainerConfig, RedisContainerConfig {
}
