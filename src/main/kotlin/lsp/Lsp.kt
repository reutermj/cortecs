package lsp

import org.eclipse.lsp4j.*
import org.eclipse.lsp4j.jsonrpc.messages.Either
import org.eclipse.lsp4j.launch.LSPLauncher
import org.eclipse.lsp4j.services.*
import parser_v2.*
import java.io.*
import java.net.*
import java.nio.file.*
import java.util.concurrent.*
import kotlin.system.*


object CortecsServer: LanguageServer, LanguageClientAware {
    var hasConfigurationCapability = false
    var hasWorkspaceFolderCapability = false
    var hasDiagnosticRelatedInformationCapability = false
    lateinit var client: LanguageClient
    lateinit var workspaceRoot: Path
    lateinit var dotCortecsRoot: Path
    lateinit var crashDumpRoot: Path

    var maxNumberOfProblems = 100.0
    override fun initialize(params: InitializeParams?): CompletableFuture<InitializeResult> {
        println("enter initialize")
        workspaceRoot = Paths.get(URI.create(params?.workspaceFolders?.first()?.uri!!))
        dotCortecsRoot = workspaceRoot.resolve(".cortecs")
        crashDumpRoot = dotCortecsRoot.resolve("crash-dumps")
        val crashDumpRootFile = crashDumpRoot.toFile()
        if(!crashDumpRootFile.exists()) crashDumpRootFile.mkdirs()

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
        val diagnostics = mutableListOf<Diagnostic>()
        val diagnostic = Diagnostic()
        diagnostic.severity = DiagnosticSeverity.Warning
        diagnostic.range = Range(Position(i, index), Position(i, index + 10))
        diagnostic.message = String.format("%s should be spelled TypeScript", line.substring(index, index + 10))
        diagnostic.source = "ex"
        diagnostics.add(diagnostic)
        client?.publishDiagnostics(PublishDiagnosticsParams(document.uri, diagnostics))
    }*/

    object TextDocumentServiceLsp : TextDocumentServiceImpl {
        val documents = mutableMapOf<String, Pair<ProgramAst, String>>()

        override fun signatureHelp(params: SignatureHelpParams): CompletableFuture<SignatureHelp> {
            println("enter signatureHelp")
            val line = params.position?.line ?: return CompletableFuture.completedFuture(SignatureHelp())
            val char = params.position.character
            println("line: $line char: $char")
            println("exit signatureHelp")
            return CompletableFuture.completedFuture(SignatureHelp())
        }

        fun reportErrors(uri: String, program: ProgramAst) {
//            val diagnostics = mutableListOf<Diagnostic>()
//            for(error in program.errors) {
//
//                val diagnostic = Diagnostic()
//                diagnostic.severity = DiagnosticSeverity.Error
//                diagnostic.range = Range(Position(error.offset.line, error.offset.column), Position(error.offset.line + error.span.line, error.offset.column + error.span.column))
//                diagnostic.message = error.message
//                diagnostic.source = "ex"
//                diagnostics.add(diagnostic)
//
//            }
//            client.publishDiagnostics(PublishDiagnosticsParams(uri, diagnostics))
        }

        override fun didOpen(params: DidOpenTextDocumentParams?) {
            println("enter didOpen")
            val uri = params?.textDocument?.uri ?: return
            val iter = ParserIterator()
            iter.add(params.textDocument.text)
            val program = parseProgram(iter)
            reportErrors(uri, program)
            crashDump.put(program)
            documents[uri] = Pair(program, params.textDocument.text)
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

        fun generateGoldText(inString: String, change: String, start: Span, end: Span): String {
            val lines = inString.lines()
            val withNewLines = lines.mapIndexed { i, s ->
                if(i == lines.size - 1) s
                else "$s\n"
            }

            val preStart = withNewLines.filterIndexed { i, _ -> i < start.line }.fold("") { acc, s -> acc + s }
            val startLine = withNewLines[start.line].substring(0, start.column)
            val endLine = withNewLines[end.line].substring(end.column)
            val postEnd = withNewLines.filterIndexed { i, _ -> i > end.line }.fold("") { acc, s -> acc + s }
            return preStart + startLine + change + endLine + postEnd
        }

        val crashDump = CrashDump(20)

        override fun didChange(params: DidChangeTextDocumentParams) {
            /* From the spec:
             * The actual content changes. The content changes describe single state
             * changes to the document. So if there are two content changes c1 (at
             * array index 0) and c2 (at array index 1) for a document in state S then
             * c1 moves the document from S to S' and c2 from S' to S''. So c1 is
             * computed on the state S and c2 is computed on the state S'.
             *
             * To mirror the content of a document using change events use the following
             * approach:
             * - start with the same initial content
             * - apply the 'textDocument/didChange' notifications in the order you
             *   receive them.
             * - apply the `TextDocumentContentChangeEvent`s in a single notification
             *   in the order you receive them.
             */

            println("enter didChange")
            val uri = params.textDocument?.uri ?: return
            val (doc, text) = documents[uri] ?: return
            for(contentChange in params.contentChanges) {
                if(contentChange.range != null) {
                    val start = Span(contentChange.range.start.line, contentChange.range.start.character)
                    val end = Span(contentChange.range.end.line, contentChange.range.end.character)
                    val change = Change(contentChange.text, start, end)
                    crashDump.put(change)
                    try {
                        val iter = doc.createChangeIterator(change)
                        val outProgram = parseProgram(iter)
                        reportErrors(uri, outProgram)

                        val gold = generateGoldText(text, contentChange.text, start, end)
                        val goldIterator = ParserIterator()
                        goldIterator.add(gold)
                        val goldProgram = parseProgram(goldIterator)


                        if(outProgram != goldProgram) {
                            crashDump.dump(crashDumpRoot)
                        }
                        crashDump.put(goldProgram)

                        documents[uri] = Pair(outProgram, gold)
                    } catch (e: Exception) {
                        crashDump.dump(crashDumpRoot)
                    }
                } else {//full document change
                    val iter = ParserIterator()
                    iter.add(contentChange.text)
                    documents[uri] = Pair(parseProgram(iter), contentChange.text)
                }
            }

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
            val builder = StringBuilder()
            documents[uri]?.first?.stringify(builder)
            println(builder)
            println("exit didSave")
        }

    }

    override fun getTextDocumentService() = TextDocumentServiceLsp

    object WorkspaceServiceLsp : WorkspaceService {
        override fun didChangeWorkspaceFolders(params: DidChangeWorkspaceFoldersParams) {
            println("enter/exit didChangeWorkspaceFolders")
        }

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

    /*val dump = File("/home/mark/.data/CodeProjects/test/.cortecs/crash-dumps/2024-01-16-05-50-30-041.dump").readLines()
    var program = astJsonFormat.decodeFromString<StarAst<TopLevelAst>>(dump.first())
    val changes = dump.drop(1).map { astJsonFormat.decodeFromString<Change>(it) }
    for(change in changes) {
        val iter = constructChangeIterator(program, change)
        program = parseProgram(iter)
    }*/
}