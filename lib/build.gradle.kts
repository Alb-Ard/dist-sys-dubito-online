plugins {
    id("application")
}

repositories {
    mavenCentral()
}

dependencies {
    // https://mvnrepository.com/artifact/com.fasterxml.jackson.core/jackson-databind
    implementation("com.fasterxml.jackson.core:jackson-databind:2.18.2")
    // https://mvnrepository.com/artifact/com.jgoodies/jgoodies-binding
    implementation("com.jgoodies:jgoodies-binding:2.13.0")

    testImplementation("org.junit.jupiter:junit-jupiter:5.7.1")
    // https://search.maven.org/artifact/org.mockito/mockito-core/5.18.0/jar
    testImplementation("org.mockito:mockito-core:5.18.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}

application {
    // Define the main class for the application.
    mainClass = "org.abianchi.dubito.app.App"
}
