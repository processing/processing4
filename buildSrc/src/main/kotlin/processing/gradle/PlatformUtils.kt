package processing.gradle

import org.gradle.api.GradleException

object PlatformUtils {
    data class Platform(
        val os: String,
        val arch: String,
        val libExtension: String,
        val target: String
    ) {
        val libName: String
            get() = if (os == "windows") "processing.$libExtension" else "libprocessing.$libExtension"

        val jextractPlatform: String
            get() {
                val jextractArch = if (arch == "x86_64") "x64" else arch
                return "$os-$jextractArch"
            }
    }

    fun detect(): Platform {
        val osName = System.getProperty("os.name").lowercase()
        val osArch = System.getProperty("os.arch").lowercase()

        val os = when {
            osName.contains("mac") || osName.contains("darwin") -> "macos"
            osName.contains("win") -> "windows"
            osName.contains("linux") -> "linux"
            else -> throw GradleException("Unsupported OS: $osName")
        }

        val arch = when {
            osArch.contains("aarch64") || osArch.contains("arm") -> "aarch64"
            osArch.contains("x86_64") || osArch.contains("amd64") -> "x86_64"
            else -> throw GradleException("Unsupported architecture: $osArch")
        }

        val libExtension = when (os) {
            "macos" -> "dylib"
            "windows" -> "dll"
            "linux" -> "so"
            else -> throw GradleException("Unknown platform: $os")
        }

        return Platform(os, arch, libExtension, "$os-$arch")
    }

    fun getCargoPath(): String {
        return System.getenv("CARGO_HOME")?.let { "$it/bin/cargo" }
            ?: "${System.getProperty("user.home")}/.cargo/bin/cargo"
    }
}
