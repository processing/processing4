package processing.app

import processing.app.ui.Editor
import java.io.File
import java.io.FileOutputStream
import java.net.URI
import java.net.URL
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.util.*


class Schema {
    companion object{
        private var base: Base? = null
        @JvmStatic
        fun handleSchema(input: String, base: Base): Editor?{
            this.base = base
            val uri = URI.create(input)
            return when (uri.host) {
                null -> handleLocalFile(uri.path)
                "sketch" -> handleSketch(uri)
                "preferences" -> handlePreferences(uri)
                else -> null
            }
        }
        private fun handleLocalFile(input: String): Editor?{
            return base?.handleOpen(input)
        }
        private fun handleSketch(uri: URI): Editor?{
            val paths = uri.path.split("/")
            return when(paths.getOrNull(1)){
                "new" -> handleSketchNew(uri)
                "base64" -> handleSketchBase64(uri)
                "url" -> handleSketchUrl(uri)
                else -> null
            }
        }
        private fun handleSketchNew(uri: URI): Editor?{
            base?.handleNew()
            return null
        }
        private fun handleSketchBase64(uri: URI): Editor?{
            val tempSketchFolder = SketchName.nextFolder(Base.untitledFolder);
            tempSketchFolder.mkdirs()
            val tempSketchFile = File(tempSketchFolder, "${tempSketchFolder.name}.pde")
            val sketchB64 = uri.path.replace("/base64/", "")
            val sketch = Base64.getDecoder().decode(sketchB64)
            tempSketchFile.writeBytes(sketch)
            handleSketchOptions(uri, tempSketchFolder)
            return base?.handleOpenUntitled(tempSketchFile.absolutePath)
        }
        private fun handleSketchUrl(uri: URI): Editor?{
            val url = File(uri.path.replace("/url/", ""))

            val tempSketchFolder = File(Base.untitledFolder, url.nameWithoutExtension)
            tempSketchFolder.mkdirs()
            val tempSketchFile = File(tempSketchFolder, "${tempSketchFolder.name}.pde")


            URL("https://$url").openStream().use { input ->
                FileOutputStream(tempSketchFile).use { output ->
                    input.copyTo(output)
                }
            }
            handleSketchOptions(uri, tempSketchFolder)
            return base?.handleOpenUntitled(tempSketchFile.absolutePath)
        }
        private fun handleSketchOptions(uri: URI, sketchFolder: File){
            val options = uri.query?.split("&")
                ?.map { it.split("=") }
                ?.associate {
                    URLDecoder.decode(it[0], StandardCharsets.UTF_8) to
                    URLDecoder.decode(it[1], StandardCharsets.UTF_8)
                }
                ?: emptyMap()
            options["data"]?.let{ data ->
                downloadFiles(uri, data, File(sketchFolder, "data"))
            }
            options["code"]?.let{ code ->
                downloadFiles(uri, code, File(sketchFolder, "code"))
            }
            options["pde"]?.let{ pde ->
                downloadFiles(uri, pde, sketchFolder)
            }
            options["mode"]?.let{ mode ->
                val modeFile = File(sketchFolder, "sketch.properties")
                modeFile.writeText("mode.id=$mode")
            }

        }
        private fun downloadFiles(uri: URI, urlList: String, targetFolder: File){
            Thread{
                targetFolder.mkdirs()

                val base = uri.path.split("/")
                    .drop(2) // drop the /sketch/base64/ or /sketch/url/ etc...
                    .dropLast(1) // drop the file name
                    .joinToString("/")

                val files = urlList.split(",")

                files.filter { it.isNotBlank() }
                    .map{ it.split(":", limit = 2) }
                    .map{ segments ->
                        if(segments.size == 2){
                            if(segments[0].isBlank()){
                                return@map listOf(null, segments[1])
                            }
                            return@map segments
                        }
                        return@map listOf(null, segments[0])
                    }
                    .forEach { (name, content) ->
                        try{
                            // Try to decode the content as base64
                            val file = Base64.getDecoder().decode(content)
                            if(name == null){
                                Messages.err("Base64 files needs to start with a file name followed by a colon")
                                return@forEach
                            }
                            File(targetFolder, name).writeBytes(file)
                        }catch(_: IllegalArgumentException){
                            // Assume it's a URL and download it
                            var url = URI.create(content)
                            if(url.host == null){
                                url = URI.create("https://$base/$content")
                            }
                            if(url.scheme == null){
                                url = URI.create("https://$content")
                            }

                            val target = File(targetFolder, name ?: url.path.split("/").last())
                            url.toURL().openStream().use { input ->
                                target.outputStream().use { output ->
                                    input.copyTo(output)
                                }
                            }
                        }

                    }
            }.start()
        }


        private fun handlePreferences(uri: URI): Editor?{
            val options = uri.query?.split("&")
                ?.map { it.split("=") }
                ?.associate {
                    URLDecoder.decode(it[0], StandardCharsets.UTF_8) to
                    URLDecoder.decode(it[1], StandardCharsets.UTF_8)
                }
                ?: emptyMap()
            for ((key, value) in options){
                Preferences.set(key, value)
            }
            Preferences.save()

            return null
        }
    }
}