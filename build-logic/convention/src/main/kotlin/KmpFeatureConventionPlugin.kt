import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/**
 * Convention plugin for feature modules.
 * Includes: Compose + Koin + Decompose + Essenty
 *
 * Used by: all :feature:* modules
 * Apply with: id("couplebase.kmp.feature")
 */
class KmpFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        with(pluginManager) {
            apply("couplebase.kmp.compose")
        }

        extensions.configure<KotlinMultiplatformExtension> {
            sourceSets.apply {
                commonMain.dependencies {
                    val catalog = versionCatalog()

                    // DI
                    implementation(catalog.findLibrary("koin-core").get())
                    implementation(catalog.findLibrary("koin-compose").get())

                    // Navigation & Lifecycle
                    implementation(catalog.findLibrary("decompose-core").get())
                    implementation(catalog.findLibrary("decompose-compose").get())
                    implementation(catalog.findLibrary("essenty-lifecycle").get())
                    implementation(catalog.findLibrary("essenty-statekeeper").get())
                    implementation(catalog.findLibrary("essenty-instancekeeper").get())
                    implementation(catalog.findLibrary("essenty-backhandler").get())
                }

                commonTest.dependencies {
                    val catalog = versionCatalog()
                    implementation(catalog.findLibrary("turbine").get())
                }
            }
        }
    }
}
