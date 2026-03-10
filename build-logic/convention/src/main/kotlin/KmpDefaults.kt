import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

@OptIn(ExperimentalKotlinGradlePluginApi::class)
fun Project.configureKmpDefaults() {
    extensions.configure<KotlinMultiplatformExtension> {
        compilerOptions {
            freeCompilerArgs.addAll(
                "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
                "-opt-in=kotlinx.coroutines.FlowPreview",
            )
        }

        androidTarget {
            compilerOptions {
                jvmTarget.set(JvmTarget.JVM_17)
            }
        }

        listOf(
            iosX64(),
            iosArm64(),
            iosSimulatorArm64(),
        ).forEach { target ->
            target.binaries.framework {
                baseName = project.path.replace(":", "-").drop(1)
                isStatic = true
            }
        }
    }
}

fun Project.configureAndroidLibrary() {
    extensions.configure<com.android.build.gradle.LibraryExtension> {
        val catalog = project.versionCatalog()
        namespace = "com.couplebase.${project.path.replace(":", ".").drop(1)}"
        compileSdk = catalog.findVersion("android-compileSdk").get().requiredVersion.toInt()

        defaultConfig {
            minSdk = catalog.findVersion("android-minSdk").get().requiredVersion.toInt()
        }

        compileOptions {
            sourceCompatibility = org.gradle.api.JavaVersion.VERSION_17
            targetCompatibility = org.gradle.api.JavaVersion.VERSION_17
        }
    }
}

fun Project.versionCatalog(): org.gradle.api.artifacts.VersionCatalog {
    return extensions.getByType(org.gradle.api.artifacts.VersionCatalogsExtension::class.java)
        .named("libs")
}
