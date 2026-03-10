import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/**
 * Convention plugin for base KMP library modules (no Compose).
 * Used by: :core:common, :core:model, :core:domain, :core:database, :core:network, :core:sync, :core:datastore
 *
 * Apply with: id("couplebase.kmp.library")
 */
class KmpLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        with(pluginManager) {
            apply("org.jetbrains.kotlin.multiplatform")
            apply("org.jetbrains.kotlin.plugin.serialization")
            apply("com.android.library")
        }

        configureKmpDefaults()
        configureAndroidLibrary()

        extensions.configure<KotlinMultiplatformExtension> {
            // Web target
            @Suppress("OPT_IN_USAGE")
            wasmJs {
                browser()
            }

            sourceSets.apply {
                commonMain.dependencies {
                    val catalog = versionCatalog()
                    implementation(catalog.findLibrary("kotlinx-coroutines-core").get())
                    implementation(catalog.findLibrary("kotlinx-serialization-json").get())
                    implementation(catalog.findLibrary("kotlinx-datetime").get())
                }

                commonTest.dependencies {
                    val catalog = versionCatalog()
                    implementation(catalog.findLibrary("kotlin-test").get())
                    implementation(catalog.findLibrary("kotlinx-coroutines-test").get())
                }
            }
        }
    }
}
