package errors

import kotlinx.serialization.*
import parser.Span

@Serializable
data class CortecsError(val message: String, val offset: Span, val span: Span)
@Serializable
data class CortecsErrorV2(val message: String, val offset: parser.Span, val span: parser.Span)
@Serializable
data class CortecsErrors(val errorSpan: parser.Span?, val errors: List<CortecsErrorV2>)