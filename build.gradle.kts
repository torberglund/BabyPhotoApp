plugins {
    id("com.android.application") version "8.8.0" apply false
    kotlin("android") version "1.8.0" apply false
}
task("clean", Delete::class) {
    delete(rootProject.buildDir)
}
