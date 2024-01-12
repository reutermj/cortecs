package errors

import kotlinx.serialization.*
import parser.Span

@Serializable
data class CortecsError(val message: String, val offset: Span, val span: Span)