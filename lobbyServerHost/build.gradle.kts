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
    implementation(project(":lib"))
}

application {
    // Define the main class for the application.
    mainClass = "org.albard.dubito.app.DubitoApp"
}
