package errors

import kotlinx.serialization.*
import parser.Span

@Serializable
data class CortecsError(val message: String, val offset: Span, val span: Span)
@Serializable
data class CortecsErrorV2(val message: String, val offset: parser_v2.Span, val span: parser_v2.Span)
@Serializable
data class CortecsErrors(val errorSpan: parser_v2.Span?, val errors: List<CortecsErrorV2>)