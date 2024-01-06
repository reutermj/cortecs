package parser

import kotlinx.serialization.*
import kotlinx.serialization.json.*
class CrashDump(val capacity: Int) {
    val changes = Array<Change?>(capacity) { null }
    val asts = Array<StarAst<TopLevelAst>>(capacity) { StarLeaf }
    var tail = -1
    var head = 0
    fun modInc(i: Int) = (i + 1) % capacity
    fun any() = tail != -1

    fun put(item: Change) {
        if (tail != -1) {
            tail = modInc(tail)
            if(head == tail) head = modInc(head)
        } else tail = 0
        changes[tail] = item
    }

    fun put(program: StarAst<TopLevelAst>) {
        asts[modInc(tail)] = program
    }

    fun dumpString(): String {
        val builder = StringBuilder()
        builder.append(astJsonFormat.encodeToString(asts[head]))
        builder.append("\n")

        if(any()) {
            var index = head
            do {
                builder.append(Json.encodeToString(changes[index]))
                builder.append("\n")
                index = modInc(index)
            } while(index != tail)
            builder.append(Json.encodeToString(changes[index]))
            builder.append("\n")
        }
        return builder.toString()
    }
}