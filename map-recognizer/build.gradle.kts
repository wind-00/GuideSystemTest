plugins {
    id("com.android.library")
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.example.maprecognizer"
    compileSdk = 36

    defaultConfig {
        minSdk = 26
        targetSdk = 36
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation("com.google.code.gson:gson:2.10.1")
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.mockito:mockito-core:5.11.0")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}

// 添加地图生成任务
tasks.register<JavaExec>("generateMap") {
    group = "map"
    description = "Generate app automation map and save to file"
    
    // 使用Library模块的类路径，包含编译后的类和依赖
    classpath = files(
        // 编译后的Kotlin类
        files("$buildDir/tmp/kotlin-classes/debug"),
        // 编译后的Java类
        files("$buildDir/classes/java/debug"),
        // 依赖的jar文件 - 使用debugRuntimeClasspath替代runtimeClasspath
        configurations.getByName("debugRuntimeClasspath")
    )
    
    mainClass.set("com.example.maprecognizer.MainKt")
    
    // 设置输出目录和文件名
    val outputDir = project.rootDir
    val outputFile = File(outputDir, "app_automation_map_from_module.json")
    
    // 设置系统属性，用于传递输出路径
    systemProperty("map.output.file", outputFile.absolutePath)
    systemProperty("project.root.dir", project.rootDir.absolutePath)
    
    // 确保在执行JavaExec任务前先编译代码
    dependsOn("compileDebugKotlin")
    dependsOn("compileDebugJavaWithJavac")
}
