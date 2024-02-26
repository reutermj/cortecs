package errors

import kotlinx.serialization.*
import parser.*

@Serializable
data class CortecsError(val message: String, val offset: Span, val span: Span)
@Serializable
data class CortecsErrors(val errorSpan: Span?, val errors: List<CortecsError>) {
     companion object {
         val empty = CortecsErrors(null, emptyList())
     }
}