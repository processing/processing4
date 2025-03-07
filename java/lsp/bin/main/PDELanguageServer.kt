import org.eclipse.lsp4j.InitializeParams
import org.eclipse.lsp4j.InitializeResult
import org.eclipse.lsp4j.services.LanguageServer
import org.eclipse.lsp4j.services.TextDocumentService
import org.eclipse.lsp4j.services.WorkspaceService
import java.util.concurrent.CompletableFuture


class PDELanguageServer: LanguageServer {
    companion object{
        fun main( args: Array<String>){

        }
    }

    override fun initialize(params: InitializeParams?): CompletableFuture<InitializeResult> {
        TODO("Not yet implemented")
    }

    override fun shutdown(): CompletableFuture<Any> {
        TODO("Not yet implemented")
    }

    override fun exit() {
        TODO("Not yet implemented")
    }

    override fun getTextDocumentService(): TextDocumentService {
        return PDETextDocumentService()
    }

    override fun getWorkspaceService(): WorkspaceService {
        TODO("Not yet implemented")
    }
}