package lsp

import org.eclipse.lsp4j.*
import org.eclipse.lsp4j.jsonrpc.messages.*
import org.eclipse.lsp4j.services.*
import java.util.concurrent.*

interface TextDocumentServiceImpl: TextDocumentService {
    override fun completion(position: CompletionParams): CompletableFuture<Either<List<CompletionItem>, CompletionList>> {
        println("Called unimplemented completion")
        throw UnsupportedOperationException()
    }

    override fun resolveCompletionItem(unresolved: CompletionItem): CompletableFuture<CompletionItem> {
        println("Called unimplemented resolveCompletionItem")
        throw UnsupportedOperationException()
    }

    override fun hover(params: HoverParams): CompletableFuture<Hover> {
        println("Called unimplemented hover")
        throw UnsupportedOperationException()
    }

    override fun signatureHelp(params: SignatureHelpParams): CompletableFuture<SignatureHelp> {
        println("Called unimplemented signatureHelp")
        throw UnsupportedOperationException()
    }

    override fun declaration(params: DeclarationParams): CompletableFuture<Either<List<Location>, List<LocationLink>>> {
        println("Called unimplemented declaration")
        throw UnsupportedOperationException()
    }

    override fun definition(params: DefinitionParams): CompletableFuture<Either<List<Location>, List<LocationLink>>> {
        println("Called unimplemented definition")
        throw UnsupportedOperationException()
    }

    override fun typeDefinition(params: TypeDefinitionParams): CompletableFuture<Either<List<Location>, List<LocationLink>>> {
        println("Called unimplemented typeDefinition")
        throw UnsupportedOperationException()
    }

    override fun implementation(params: ImplementationParams): CompletableFuture<Either<List<Location>, List<LocationLink>>> {
        println("Called unimplemented implementation")
        throw UnsupportedOperationException()
    }

    override fun references(params: ReferenceParams): CompletableFuture<List<Location>> {
        println("Called unimplemented references")
        throw UnsupportedOperationException()
    }

    override fun documentHighlight(params: DocumentHighlightParams): CompletableFuture<List<DocumentHighlight>> {
        println("Called unimplemented documentHighlight")
        throw UnsupportedOperationException()
    }

    override fun documentSymbol(params: DocumentSymbolParams): CompletableFuture<List<Either<SymbolInformation, DocumentSymbol>>> {
        println("Called unimplemented documentSymbol")
        throw UnsupportedOperationException()
    }

    override fun codeAction(params: CodeActionParams): CompletableFuture<List<Either<Command, CodeAction>>> {
        println("Called unimplemented codeAction")
        throw UnsupportedOperationException()
    }

    override fun resolveCodeAction(unresolved: CodeAction): CompletableFuture<CodeAction> {
        println("Called unimplemented resolveCodeAction")
        throw UnsupportedOperationException()
    }

    override fun codeLens(params: CodeLensParams): CompletableFuture<List<CodeLens>> {
        println("Called unimplemented codeLens")
        throw UnsupportedOperationException()
    }

    override fun resolveCodeLens(unresolved: CodeLens): CompletableFuture<CodeLens> {
        println("Called unimplemented resolveCodeLens")
        throw UnsupportedOperationException()
    }

    override fun formatting(params: DocumentFormattingParams): CompletableFuture<List<TextEdit>> {
        println("Called unimplemented formatting")
        throw UnsupportedOperationException()
    }

    override fun rangeFormatting(params: DocumentRangeFormattingParams): CompletableFuture<List<TextEdit>> {
        println("Called unimplemented rangeFormatting")
        throw UnsupportedOperationException()
    }

    override fun onTypeFormatting(params: DocumentOnTypeFormattingParams): CompletableFuture<List<TextEdit>> {
        println("Called unimplemented onTypeFormatting")
        throw UnsupportedOperationException()
    }

    override fun rename(params: RenameParams): CompletableFuture<WorkspaceEdit> {
        println("Called unimplemented rename")
        throw UnsupportedOperationException()
    }

    override fun linkedEditingRange(params: LinkedEditingRangeParams): CompletableFuture<LinkedEditingRanges> {
        println("Called unimplemented linkedEditingRange")
        throw UnsupportedOperationException()
    }

    override fun willSave(params: WillSaveTextDocumentParams) {
        println("Called unimplemented willSave")
    }

    override fun willSaveWaitUntil(params: WillSaveTextDocumentParams): CompletableFuture<List<TextEdit>> {
        println("Called unimplemented willSaveWaitUntil")
        throw UnsupportedOperationException()
    }

    override fun documentLink(params: DocumentLinkParams): CompletableFuture<List<DocumentLink>> {
        println("Called unimplemented documentLink")
        throw UnsupportedOperationException()
    }

    override fun documentLinkResolve(params: DocumentLink): CompletableFuture<DocumentLink> {
        println("Called unimplemented documentLinkResolve")
        throw UnsupportedOperationException()
    }

    override fun documentColor(params: DocumentColorParams): CompletableFuture<List<ColorInformation>> {
        println("Called unimplemented documentColor")
        throw UnsupportedOperationException()
    }

    override fun colorPresentation(params: ColorPresentationParams): CompletableFuture<List<ColorPresentation>> {
        println("Called unimplemented colorPresentation")
        throw UnsupportedOperationException()
    }

    override fun foldingRange(params: FoldingRangeRequestParams): CompletableFuture<List<FoldingRange>> {
        println("Called unimplemented foldingRange")
        throw UnsupportedOperationException()
    }

    override fun prepareRename(params: PrepareRenameParams): CompletableFuture<Either3<Range, PrepareRenameResult, PrepareRenameDefaultBehavior>> {
        println("Called unimplemented prepareRename")
        throw UnsupportedOperationException()
    }

    override fun prepareTypeHierarchy(params: TypeHierarchyPrepareParams): CompletableFuture<List<TypeHierarchyItem>> {
        println("Called unimplemented prepareTypeHierarchy")
        throw UnsupportedOperationException()
    }

    override fun typeHierarchySupertypes(params: TypeHierarchySupertypesParams): CompletableFuture<List<TypeHierarchyItem>> {
        println("Called unimplemented typeHierarchySupertypes")
        throw UnsupportedOperationException()
    }

    override fun typeHierarchySubtypes(params: TypeHierarchySubtypesParams): CompletableFuture<List<TypeHierarchyItem>> {
        println("Called unimplemented typeHierarchySubtypes")
        throw UnsupportedOperationException()
    }

    override fun prepareCallHierarchy(params: CallHierarchyPrepareParams): CompletableFuture<List<CallHierarchyItem>> {
        println("Called unimplemented prepareCallHierarchy")
        throw UnsupportedOperationException()
    }

    override fun callHierarchyIncomingCalls(params: CallHierarchyIncomingCallsParams): CompletableFuture<List<CallHierarchyIncomingCall>> {
        println("Called unimplemented callHierarchyIncomingCalls")
        throw UnsupportedOperationException()
    }

    override fun callHierarchyOutgoingCalls(params: CallHierarchyOutgoingCallsParams): CompletableFuture<List<CallHierarchyOutgoingCall>> {
        println("Called unimplemented callHierarchyOutgoingCalls")
        throw UnsupportedOperationException()
    }

    override fun selectionRange(params: SelectionRangeParams): CompletableFuture<List<SelectionRange>> {
        println("Called unimplemented selectionRange")
        throw UnsupportedOperationException()
    }

    override fun semanticTokensFull(params: SemanticTokensParams): CompletableFuture<SemanticTokens> {
        println("Called unimplemented semanticTokensFull")
        throw UnsupportedOperationException()
    }

    override fun semanticTokensFullDelta(params: SemanticTokensDeltaParams): CompletableFuture<Either<SemanticTokens, SemanticTokensDelta>> {
        println("Called unimplemented semanticTokensFullDelta")
        throw UnsupportedOperationException()
    }

    override fun semanticTokensRange(params: SemanticTokensRangeParams): CompletableFuture<SemanticTokens> {
        println("Called unimplemented semanticTokensRange")
        throw UnsupportedOperationException()
    }

    override fun moniker(params: MonikerParams): CompletableFuture<List<Moniker>> {
        println("Called unimplemented moniker")
        throw UnsupportedOperationException()
    }

    override fun inlayHint(params: InlayHintParams): CompletableFuture<List<InlayHint>> {
        println("Called unimplemented inlayHint")
        throw UnsupportedOperationException()
    }

    override fun resolveInlayHint(unresolved: InlayHint): CompletableFuture<InlayHint> {
        println("Called unimplemented resolveInlayHint")
        throw UnsupportedOperationException()
    }

    override fun inlineValue(params: InlineValueParams): CompletableFuture<List<InlineValue>> {
        println("Called unimplemented inlineValue")
        throw UnsupportedOperationException()
    }

    override fun diagnostic(params: DocumentDiagnosticParams): CompletableFuture<DocumentDiagnosticReport> {
        println("Called unimplemented diagnostic")
        throw UnsupportedOperationException()
    }
}