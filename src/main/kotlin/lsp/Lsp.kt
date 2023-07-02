package lsp

import org.eclipse.lsp4j.*
import org.eclipse.lsp4j.jsonrpc.messages.Either
import org.eclipse.lsp4j.launch.LSPLauncher
import org.eclipse.lsp4j.services.*
import java.net.*
import java.util.concurrent.*
import kotlin.system.*

object CortecsServer: LanguageServer, LanguageClientAware {
    var hasConfigurationCapability = false
    var hasWorkspaceFolderCapability = false
    var hasDiagnosticRelatedInformationCapability = false
    var client: LanguageClient? = null

    var maxNumberOfProblems = 100.0
    override fun initialize(params: InitializeParams?): CompletableFuture<InitializeResult> {
        println("enter initialize")

        hasConfigurationCapability = params?.capabilities?.workspace?.configuration ?: false
        hasWorkspaceFolderCapability = params?.capabilities?.workspace?.configuration ?: false
        hasDiagnosticRelatedInformationCapability = params?.capabilities?.textDocument?.publishDiagnostics?.relatedInformation ?: false
        val capabilities = ServerCapabilities()
        capabilities.setTextDocumentSync(TextDocumentSyncKind.Incremental)
        capabilities.completionProvider = CompletionOptions(true, null)
        if(hasWorkspaceFolderCapability) {
            val options = WorkspaceFoldersOptions()
            options.supported = true
            capabilities.workspace = WorkspaceServerCapabilities(options)
        }

        println("exit initialize")

        return CompletableFuture.completedFuture(InitializeResult(capabilities))
    }

    override fun shutdown(): CompletableFuture<Any> {
        println("enter/exit shutdown")
        return CompletableFuture.completedFuture(null)
    }

    override fun exit() {
        println("enter/exit exit")
        exitProcess(0)
    }

    object TextDocumentServiceLsp : TextDocumentServiceImpl {
        val documents = mutableMapOf<String, CortecsFile>()

        override fun signatureHelp(params: SignatureHelpParams): CompletableFuture<SignatureHelp> {
            println("enter signatureHelp")
            val line = params.position?.line ?: return CompletableFuture.completedFuture(SignatureHelp())
            val char = params.position.character
            println("line: $line char: $char")
            println("exit signatureHelp")
            return CompletableFuture.completedFuture(SignatureHelp())
        }
        override fun didOpen(params: DidOpenTextDocumentParams?) {
            println("enter didOpen")
            val uri = params?.textDocument?.uri ?: return
            val cortecsFile = CortecsFile(params.textDocument.text)
            //cortecsFile.printInOrder()
            val diagnostics = mutableListOf<Diagnostic>()
            cortecsFile.publishErrors(diagnostics)
            client?.publishDiagnostics(PublishDiagnosticsParams(uri, diagnostics))
            documents[uri] = cortecsFile
            println("exit didOpen")
        }

        override fun completion(position: CompletionParams): CompletableFuture<Either<List<CompletionItem>, CompletionList>> {
            println("enter completion")
            val typescriptCompletionItem = CompletionItem()
            typescriptCompletionItem.label = "TypeScript"
            typescriptCompletionItem.kind = CompletionItemKind.Text
            typescriptCompletionItem.data = 1.0
            val javascriptCompletionItem = CompletionItem()
            javascriptCompletionItem.label = "JavaScript"
            javascriptCompletionItem.kind = CompletionItemKind.Text
            javascriptCompletionItem.data = 2.0
            val completions = listOf(typescriptCompletionItem, javascriptCompletionItem)
            println("exit completion")
            return CompletableFuture.completedFuture(Either.forLeft(completions))
        }

        override fun resolveCompletionItem(unresolved: CompletionItem): CompletableFuture<CompletionItem> {
            println("enter resolveCompletionItem")
            if (unresolved.data == 1.0) {
                unresolved.detail = "TypeScript details"
                unresolved.setDocumentation("TypeScript documentation")
            } else if (unresolved.data == 2.0) {
                unresolved.detail = "JavaScript details"
                unresolved.setDocumentation("JavaScript documentation")
            }
            println("exit resolveCompletionItem")
            return CompletableFuture.completedFuture(unresolved)
        }

        override fun didChange(params: DidChangeTextDocumentParams) {
            println("enter didChange")
            val uri = params.textDocument?.uri ?: return
            val doc = documents[uri] ?: return
            for(change in params.contentChanges) {
                if(change.range != null) documents[uri] = doc.contentChange(change)
                else documents[uri] = CortecsFile(change.text) //full document change
            }

            val diagnostics = mutableListOf<Diagnostic>()
            documents[uri]?.publishErrors(diagnostics)
            client?.publishDiagnostics(PublishDiagnosticsParams(uri, diagnostics))

            println("exit didChange")
        }

        override fun didClose(params: DidCloseTextDocumentParams) {
            println("enter didClose")
            //documents.remove(params?.textDocument?.uri ?: return)
            println("exit didClose")
        }

        override fun didSave(params: DidSaveTextDocumentParams) {
            println("enter didSave")
            val uri = params.textDocument?.uri ?: return
            documents[uri]?.file?.inOrder { println(it) }
            println("exit didSave")
        }
    }

    override fun getTextDocumentService() = TextDocumentServiceLsp

    object WorkspaceServiceLsp : WorkspaceService {
        override fun didChangeConfiguration(params: DidChangeConfigurationParams?) {
            println("enter didChangeConfiguration")
            val settings = params?.settings
            if(settings !is Map<*, *>) return
            val languageServerExample = settings["languageServerExample"] ?: return
            if(languageServerExample !is Map<*, *>) return
            maxNumberOfProblems = (languageServerExample["languageServerExample"] ?: languageServerExample) as Double
            println("exit didChangeConfiguration")
        }

        override fun didChangeWatchedFiles(params: DidChangeWatchedFilesParams?) {
            println("enter didChangeWatchedFiles")
            client?.logMessage(MessageParams(MessageType.Log, "We received an file change event"))
            println("exit didChangeWatchedFiles")
        }

    }
    override fun getWorkspaceService() = WorkspaceServiceLsp

    override fun connect(client: LanguageClient) {
        println("enter/exit connect")
        this.client = client
    }
}



fun main() {
    val socket = Socket("localhost", 52712)
    val server = LSPLauncher.createServerLauncher(CortecsServer, socket.inputStream, socket.outputStream)
    CortecsServer.connect(server.remoteProxy)
    server.startListening()
}
