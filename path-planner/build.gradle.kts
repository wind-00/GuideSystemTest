plugins {
    kotlin("jvm")
    kotlin("plugin.serialization") version "1.9.22"
    application
}

group = "com.example"
version = "1.0"

dependencies {
    // Kotlin standard library
    implementation(kotlin("stdlib"))
    
    // HTTP client dependencies
    implementation("io.ktor:ktor-client-core:2.3.8")
    implementation("io.ktor:ktor-client-cio:2.3.8")
    implementation("io.ktor:ktor-client-content-negotiation:2.3.8")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.8")
    
    // JSON serialization dependency
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
    
    // Coroutines dependency
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    
    // Test dependencies
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    testImplementation(kotlin("test"))
    testImplementation("junit:junit:4.13.2")
}

tasks.test {
    useJUnit()
}

application {
    mainClass.set("com.example.pathplanner.InteractiveTestKt")
}

// 配置run任务，启用标准输入
tasks.named<JavaExec>("run") {
    standardInput = System.`in`
}