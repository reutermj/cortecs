package lsp

import org.eclipse.lsp4j.*
import org.eclipse.lsp4j.jsonrpc.messages.Either
import org.eclipse.lsp4j.launch.LSPLauncher
import org.eclipse.lsp4j.services.*
import java.lang.Exception
import java.net.Socket
import java.util.concurrent.CompletableFuture
import kotlin.system.exitProcess


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

    /*private fun validateDocument(document: String) {
        val diagnostics = mutableListOf<Diagnostic>()
        val lines = document.split("\\r?\\n".toRegex()).dropLastWhile { it.isEmpty() }
        var problems = 0
        var i = 0
        while(i < lines.size) {
            val line = lines[i]
            if(problems >= maxNumberOfProblems) break

            val index = line.indexOf("typescript")
            if (index >= 0) {
                problems++
                val diagnostic = Diagnostic()
                diagnostic.severity = DiagnosticSeverity.Warning
                diagnostic.range = Range(Position(i, index), Position(i, index + 10))
                diagnostic.message = String.format("%s should be spelled TypeScript", line.substring(index, index + 10))
                diagnostic.source = "ex"
                diagnostics.add(diagnostic)
            }
            i++
        }

        //client?.publishDiagnostics(PublishDiagnosticsParams(document.uri, diagnostics))
    }*/

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

            //validateDocument(documents[uri] ?: return)
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
            //for((_, doc) in TextDocumentServiceLsp.documents) validateDocument(doc)
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
    val bla = LSPLauncher.createServerLauncher(CortecsServer, socket.inputStream, socket.outputStream)
    CortecsServer.connect(bla.remoteProxy)
    bla.startListening()


    /*val numbers = List(i) { it.toString() }
    val tree = CortecsFileTree.bulkLoad(numbers)
    if(!tree.isValid()) {
        println("here")
    }
    var l = 0
    tree.inOrder {
        if(it.toInt() != l) {
            println("here")
        }
        l++
    }*/

    /*for(i in 1 until 100000) {
        for(j in 1 until i) {
            for(k in 1 until (i - j)) {
                test(i, j, k)
                test2(i, j, k)
            }
            test3(i, j)
            test4(i, j)
        }

    }*/
    //test2(5, 2, 1)
}

fun test(i: Int, j: Int, k: Int) {
    val numbers = List(i) {
        if(it < j) it.toString()
        else (it + k).toString()
    }
    val toAdd = List(k) { (it + j).toString() }
    val tree = CortecsFileTree.bulkLoad(numbers)
    val treep = tree.insert(j, toAdd)

    if(!treep.isValid()) {
        println("$i $j $k")
    }

    var l = 0
    treep.inOrder {
        if(it.toInt() != l) {
            val n = numbers
            val a = toAdd
            val t = tree
            val tp = treep
            println("$i $j $k")
        }
        l++
    }
}

fun test2(i: Int, j: Int, k: Int) {
    val numbers = List(i) {
        it.toString()
    }

    try {
        val tree = CortecsFileTree.bulkLoad(numbers)
        val treep = tree.delete(j, k)

        if(!treep.isValid()) {
            println("$i $j $k")
        }

        var l = 0
        treep.inOrder {
            while(l >= j && l < j + k) l++

            val m = it.toInt()
            if(m != l) {
                val n = numbers
                val t = tree
                val tp = treep
                println("$i $j $k")
            }
            l++
        }
    } catch(e: Exception) {
        println("$i $j $k")
    }
}

fun test3(i: Int, j: Int) {
    val numbers = List(i) { it.toString() }
    val tree = CortecsFileTree.bulkLoad(numbers)
    var treep = tree.drop(j, 0)
    while(treep.size > CortecsFileTree.minChildren) treep = CortecsFileTree.oneLayer(treep)
    val treepp =
        when(treep.size) {
            0 -> CortecsFileNone
            1 -> treep.first()
            else -> CortecsFileNode(treep)
        }

    if(!treepp.isValid()) {
        println("$i $j")
    }

    var l = j
    treepp.inOrder {
        if(it.toInt() != l) {
            val n = numbers
            val t = tree
            val tp = treep
            val tpp = treepp
            println("$i $j")
        }
        l++
    }
}

fun test4(i: Int, j: Int) {
    val numbers = List(i) { it.toString() }
    val tree = CortecsFileTree.bulkLoad(numbers)
    var treep = tree.take(j, 0)
    while(treep.size > CortecsFileTree.minChildren) treep = CortecsFileTree.oneLayer(treep)
    val treepp =
        when(treep.size) {
            0 -> CortecsFileNone
            1 -> treep.first()
            else -> CortecsFileNode(treep)
        }

    if(!treepp.isValid()) {
        println("$i $j")
    }

    var l = 0
    treepp.inOrder {
        if(it.toInt() != l) {
            val n = numbers
            val t = tree
            val tp = treep
            val tpp = treepp
            println("$i $j")
        }
        l++
    }

    if(l != j) {
        println("$i $j")
    }
}