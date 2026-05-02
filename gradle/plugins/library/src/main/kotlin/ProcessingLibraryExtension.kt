import org.gradle.api.Action
import org.gradle.api.model.ObjectFactory
import java.io.Serializable
import javax.inject.Inject

open class ProcessingLibraryExtension @Inject constructor(objects: ObjectFactory) {
    var version: String? = null
    val library = objects.newInstance(ProcessingLibraryConfiguration::class.java)
    fun library(action: Action<ProcessingLibraryConfiguration>) {
        action.execute(library)
    }
}

open class ProcessingLibraryConfiguration @Inject constructor() : Serializable {
    /**
     * Name of the library. If not set, the project name will be used.
     */
    var name: String? = null

    /**
     * Version number of the library.
     */
    var version: Int? = null

    /**
     * Pretty version string of the library.
     */
    var prettyVersion: String? = null

    /**
     * Map of author URLs to author names.
     */
    var authors: Map<String, String> = emptyMap()

    /**
     * URL of the library where more information can be found.
     */
    var url: String? = null

    /**
     * List of categories the library belongs to.
     */
    var categories: List<String> = emptyList()

    /**
     * A one-line sentence describing the library.
     */
    var sentence: String? = null

    /**
     * A longer paragraph describing the library.
     */
    var paragraph: String? = null

    /**
     * Minimum Processing revision required.
     */
    var minRevision: Int? = null

    /**
     * Maximum Processing revision supported.
     */
    var maxRevision: Int? = null
}