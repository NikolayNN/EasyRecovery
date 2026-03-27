# EasyRecovery Library

## Overview
**EasyRecovery** is a lightweight Java library for service state backup and restoration with optional periodic backups.

It works without framework dependencies and can be integrated into plain Java apps or Spring-based projects.

---

## Requirements
- Java 11+
- Maven/Gradle project (for dependency management)

---

## Features
- State restore on application startup.
- Periodic backups via scheduler (`ScheduledExecutorService`).
- Java serialization-based persistence (`ObjectOutputStream` / `ObjectInputStream`).
- Custom exception handling via `EasyRecoveryExceptionHandler`.
- Graceful shutdown with final backup on `SIGTERM` / JVM shutdown hook.

---

## Installation (Maven)
Add JitPack repository and dependency:

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>com.github.NikolayNN</groupId>
        <artifactId>EasyRecovery</artifactId>
        <version><!-- put Git tag/version here --></version>
    </dependency>
</dependencies>
```

JitPack page: https://jitpack.io/#NikolayNN/EasyRecovery

---

## How to Use

### 1) Define recoverable service
Implement `EasyRecoverable<S>` for every service you want to back up.

> Important: The object returned from `backup()` must be serializable.

```java
import by.aurorasoft.easy.recovery.EasyRecoverable;
import java.io.Serializable;
import java.time.Duration;

public class SampleService implements EasyRecoverable<MyState> {

    private MyState state;

    @Override
    public String backupPath() {
        return "sample_service_backup.ser";
    }

    @Override
    public MyState backup() {
        return state;
    }

    @Override
    public void restore(MyState state) {
        this.state = state;
    }

    @Override
    public Duration backupPeriod() {
        return Duration.ofMinutes(5); // Duration.ZERO disables periodic backups
    }

    public static class MyState implements Serializable {
        private static final long serialVersionUID = 1L;
        // fields...
    }
}
```

### 2) Configure and start (standalone)
`EasyRecoveryConfig` is **abstract**, so create a small subclass:

```java
import by.aurorasoft.easy.recovery.*;
import java.util.List;

public class Main {

    static class AppRecoveryConfig extends EasyRecoveryConfig {
        public AppRecoveryConfig(List<EasyRecoverable<?>> services) {
            super(services, 1); // scheduler thread pool size
        }
    }

    public static void main(String[] args) {
        List<EasyRecoverable<?>> services = List.of(new SampleService());
        EasyRecoveryService recoveryService = new AppRecoveryConfig(services).easyRecoveryService();

        // recoveryService.start() is called inside easyRecoveryService()
    }
}
```

### 3) Spring integration (optional)

```java
import by.aurorasoft.easy.recovery.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class EasyRecoverySpringConfig extends EasyRecoveryConfig {

    public EasyRecoverySpringConfig(List<EasyRecoverable<?>> services) {
        super(services, 4);
    }

    @Override
    protected EasyRecoveryExceptionHandler exceptionHandler() {
        return e -> System.err.println("EasyRecovery error: " + e.getMessage());
    }

    @Bean
    public EasyRecoveryService easyRecoveryService() {
        return super.easyRecoveryService();
    }
}
```

---

## API Overview

### Core components
- `EasyRecoverable<S>` — contract for backup/restore + backup file path + backup period.
- `EasyRecoveryConfig` — bootstrap/configuration entry point.
- `EasyRecoveryService` — orchestration (restore on start, backup on stop).
- `BackupService` / `RestoreService` / `SchedulerService` — internal services for persistence and scheduling.

### Exceptions
- `EasyRecoveryException`
- `EasyRecoveryBackupException`
- `EasyRecoveryRestoreException`
- `EasyRecoveryScheduleBackupException`

---

## Notes & limitations
- Uses Java serialization; keep serialized classes version-compatible (`serialVersionUID`) across releases.
- `backupPath()` should point to a writable location.
- If backup file does not exist, restore is skipped with warning logs.

---

## License
License file is currently not included in this repository. Add a `LICENSE` file before publishing to package registries.

---

## Contribution
Issues and pull requests are welcome.
