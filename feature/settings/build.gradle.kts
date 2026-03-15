plugins {
    id("couplebase.kmp.feature")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.common)
            implementation(projects.core.model)
            implementation(projects.core.domain)
            implementation(projects.core.datastore)
            implementation(projects.core.ui)
        }
    }
}
