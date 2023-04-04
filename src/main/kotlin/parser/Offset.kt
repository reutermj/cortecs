package parser

data class Offset(val line: Int, val column: Int) {
    companion object {
        val zero = Offset(0, 0)
    }

    operator fun compareTo(other: Offset): Int {
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

    operator fun plus(rhs: Offset): Offset =
        if(rhs.line > 0) Offset(line + rhs.line, rhs.column)
        else Offset(line, column + rhs.column)

    operator fun minus(rhs: Offset): Offset {
        val l = line - rhs.line
        val c =
            if(l == 0) column - rhs.column
            else if(l < 0) 0
            else column
        return Offset(l, c)
    }
}
