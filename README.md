# EasyRecovery Library

## Overview
**EasyRecovery** is a lightweight Java library for managing service state backup and restoration with built-in periodic backups. The library supports integration with frameworks like Spring but works perfectly without external dependencies.

---

## Features
- Automatic state restoration on service startup.
- Periodic backups using a scheduler.
- JSON-based state persistence (using Jackson).
- Supports custom backup strategies.
- Framework-agnostic: works standalone or with Spring Boot.

---

## Installation
Add the following dependency to your `pom.xml` if you are using Maven to include the core EasyRecovery library, which handles service state management and JSON-based persistence using Jackson:

```xml
<dependency>
    <groupId>by.aurorasoft.easy</groupId>
    <artifactId>easy-recovery</artifactId>
    <version>1.0.0</version>
</dependency>
```

For Gradle:

```groovy
implementation 'by.aurorasoft.easy:easy-recovery:1.0.0'
```

---

## How to Use

### 1. Define Your Service
Implement the `EasyRecoverable<S>` interface for any service that requires backup and restoration.

```java
import by.aurorasoft.easy.recovery.EasyRecoverable;
import java.time.Duration;

public class SampleService implements EasyRecoverable<MyState> {
    private MyState state;

    @Override
    public String backupPath() {
        return "sample_service_backup.json";
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
        return Duration.ofMinutes(5);
    }
}
```

### 2. Configure the Library (Standalone)

```java
import by.aurorasoft.easy.recovery.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        List<EasyRecoverable<?>> services = List.of(new SampleService());

        EasyRecoveryService recoveryService = new EasyRecoveryConfig(
                services, new ObjectMapper(), 4
        ).easyRecoveryService();

        
    }
}
```

### 3. Spring Integration (Optional)
Create a Spring Boot configuration:

```java
import by.aurorasoft.easy.recovery.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.List;

@Configuration
public class EasyRecoverySpringConfig extends EasyRecoveryConfig {

    public EasyRecoverySpringConfig(ObjectMapper objectMapper, List<EasyRecoverable<?>> services) {
        super(services, objectMapper, 4);
    }

    @Bean
    public EasyRecoveryService easyRecoveryService() {
        return super.easyRecoveryService();
    }
}
```

---

## API Overview

### Core Classes
- **`EasyRecoverable<S>`:** Interface for defining services.
- **`EasyRecoveryService:`** Main service managing backup and restoration.

### Exceptions
- **`EasyRecoveryException:`** Custom exception for handling recovery-related issues.

---

## License
This project is licensed under the [MIT License](LICENSE).

---

## Contribution
Feel free to submit issues, feature requests, and pull requests.

---

