import org.gradle.external.javadoc.StandardJavadocDocletOptions


plugins {
    alias(libs.plugins.android.application)
    // FireBase
    id("com.google.gms.google-services")

}

android {
    namespace = "com.romerofernandez.meteoduo"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.romerofernandez.meteoduo"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
}

dependencies {
    implementation("androidx.fragment:fragment:1.7.1")

    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:34.8.0"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")

    // Para obtener provincias y municipios (GeoAPI con Volley)
    implementation("com.android.volley:volley:1.2.1")

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}


android {
    namespace = "com.romerofernandez.meteoduo"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.romerofernandez.meteoduo"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
}

dependencies {
    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:34.8.0"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")

    // Para obtener provincias y municipios (GeoAPI con Volley)
    implementation("com.android.volley:volley:1.2.1")

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}

/**
 * Tarea Gradle para generar JavaDoc en proyectos Android.
 * Salida: app/build/docs/javadoc/index.html
 */

        afterEvaluate {

            // 1) Nos apoyamos en la compilación debug (tiene el classpath correcto)
            val compileTask = tasks.named("compileDebugJavaWithJavac")

            tasks.register("androidJavadocs", Javadoc::class.java) {

                // 2) Primero compila, luego genera Javadoc
                dependsOn(compileTask)

                // 3) Fuentes Java
                setSource(android.sourceSets["main"].java.srcDirs)

                // 4) Boot classpath (android.jar)
                classpath = files(android.bootClasspath)

                // 5) Classpath real del compilador (AndroidX, Firebase, Volley, etc.)
                //    Se obtiene de la task de compileDebugJavaWithJavac
                val javac = compileTask.get() as JavaCompile
                classpath += javac.classpath

                // 6) Incluye también las clases ya compiladas del propio módulo
                classpath += files(javac.destinationDirectory)

                // 7) Salida
                setDestinationDir(file("$buildDir/docs/javadoc"))

                // 8) Evita doclint y no rompas por warnings
                (options as StandardJavadocDocletOptions).addStringOption("Xdoclint:none", "-quiet")
                isFailOnError = false
            }
        }


