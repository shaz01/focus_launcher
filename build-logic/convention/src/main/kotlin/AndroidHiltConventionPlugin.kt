import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType

class AndroidHiltConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        with(pluginManager) {
            apply("dagger.hilt.android.plugin")
            // KAPT must go last to avoid build warnings.
            // See: https://stackoverflow.com/questions/70550883/warning-the-following-options-were-not-recognized-by-any-processor-dagger-f
            apply("org.jetbrains.kotlin.kapt")
        }

        val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")
        dependencies {
            add(configurationName = "implementation", libs.findLibrary("hilt.android").get())
            add(configurationName = "kapt", libs.findLibrary("hilt.compiler").get())
            add(configurationName = "kapt", libs.findLibrary("hilt.android.compiler").get())
            add(configurationName = "kaptTest", libs.findLibrary("hilt.android.compiler").get())
            add(configurationName = "kaptAndroidTest", libs.findLibrary("hilt.android.compiler").get())
            add(configurationName = "implementation", libs.findLibrary("hilt.android.testing").get())
        }
    }
}
