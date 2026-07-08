package processing.gradle

object JextractUtils {
    fun findUserJextract(): String? {
        val jextractHome = System.getenv("JEXTRACT_HOME") ?: return null

        val isWindows = System.getProperty("os.name").lowercase().contains("windows")
        val path = if (isWindows) {
            "$jextractHome/bin/jextract.bat"
        } else {
            "$jextractHome/bin/jextract"
        }

        val file = java.io.File(path)
        if (file.exists()) {
            return path
        }

        return null
    }

    fun getExecutableName(): String {
        return if (System.getProperty("os.name").lowercase().contains("windows")) {
            "jextract.bat"
        } else {
            "jextract"
        }
    }
}
