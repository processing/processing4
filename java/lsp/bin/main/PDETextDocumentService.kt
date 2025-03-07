import org.eclipse.lsp4j.DidChangeTextDocumentParams
import org.eclipse.lsp4j.DidCloseTextDocumentParams
import org.eclipse.lsp4j.DidOpenTextDocumentParams
import org.eclipse.lsp4j.DidSaveTextDocumentParams
import org.eclipse.lsp4j.services.TextDocumentService

class PDETextDocumentService: TextDocumentService {
    override fun didOpen(params: DidOpenTextDocumentParams?) {
        TODO("Not yet implemented")
    }

    override fun didChange(params: DidChangeTextDocumentParams?) {
        TODO("Not yet implemented")
    }

    override fun didClose(params: DidCloseTextDocumentParams?) {
        TODO("Not yet implemented")
    }

    override fun didSave(params: DidSaveTextDocumentParams?) {
        TODO("Not yet implemented")
    }
}