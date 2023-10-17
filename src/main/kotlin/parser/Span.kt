package parser

data class Span(val line: Int, val column: Int) {
    companion object {
        val zero = Span(0, 0)
    }

    operator fun compareTo(other: Span): Int {
        val l = line.compareTo(other.line)
        return when {
            l < 0 -> -1
            l > 0 -> 1
            else -> {
                val c = column.compareTo(other.column)
                when {
                    c < 0 -> -1
                    c > 0 -> 1
                    else -> 0
                }
            }
        }
    }

    operator fun plus(rhs: Span): Span =
        if(rhs.line > 0) Span(line + rhs.line, rhs.column)
        else Span(line, column + rhs.column)

    operator fun minus(rhs: Span): Span {
        val l = line - rhs.line
        val c =
            if(l == 0) column - rhs.column
            else if(l < 0) 0
            else column
        return Span(l, c)
    }
}
