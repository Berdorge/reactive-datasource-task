plugins {
    java
}

repositories {
    mavenCentral()
}

tasks.jar {
    manifest {
        attributes("Main-Class" to "Main")
    }
}
