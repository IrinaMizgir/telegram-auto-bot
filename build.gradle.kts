plugins {
    java
    id("org.springframework.boot") version "4.1.1"
    id("io.spring.dependency-management") version "1.1.7"
    id("io.freefair.lombok") version "9.5.0"
}

group = "org.mizgir"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    val telegrambotsSpringBootStarterVersion = "10.3.0"
    val hypersistenceUtilsHibernateVersion = "3.16.0"
    val guavaVersion = "33.7.2-jre"

    implementation("org.apache.commons:commons-lang3")
    implementation("com.google.guava:guava:$guavaVersion")
    implementation("org.springframework.boot:spring-boot-h2console")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.telegram:telegrambots-springboot-longpolling-starter:$telegrambotsSpringBootStarterVersion")
    implementation("org.telegram:telegrambots-client:$telegrambotsSpringBootStarterVersion")
    implementation("io.hypersistence:hypersistence-utils-hibernate-73:$hypersistenceUtilsHibernateVersion")


    developmentOnly("org.springframework.boot:spring-boot-devtools")

    runtimeOnly("com.h2database:h2")
    runtimeOnly("org.postgresql:postgresql")

    testImplementation("org.springframework.boot:spring-boot-starter-data-jpa-test")
    testImplementation("org.springframework.boot:spring-boot-starter-thymeleaf-test")
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")


    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

}

tasks.withType<Test> {
    useJUnitPlatform()
}
