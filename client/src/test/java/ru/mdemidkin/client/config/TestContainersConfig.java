package ru.mdemidkin.client.config;

import org.junit.jupiter.api.Tag;

@Tag("integration")
public class TestContainersConfig implements PostgresContainerConfig, RedisContainerConfig {
}
