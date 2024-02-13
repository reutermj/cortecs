package parser_v2

import kotlinx.serialization.*
import kotlinx.serialization.json.*
import java.nio.file.*
import java.text.*
import java.util.*

class CrashDump(val capacity: Int) {
    val changes = Array<Change?>(capacity) { null }
    val asts = Array<ProgramAst>(capacity) { ProgramAst.empty }
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

    fun put(program: ProgramAst) {
        asts[modInc(tail)] = program
    }

    fun dump(crashDumpRoot: Path) {
        if(any()) {
            val dateTime = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS").format(Calendar.getInstance().time)
            val crashDump = crashDumpRoot.resolve("$dateTime.dump").toFile()

            crashDump.appendText(astJsonFormat.encodeToString(asts[head]))
            crashDump.appendText("\n")

            var index = head
            do {
                crashDump.appendText(Json.encodeToString(changes[index]))
                crashDump.appendText("\n")
                index = modInc(index)
            } while(index != tail)
            crashDump.appendText(Json.encodeToString(changes[index]))
            crashDump.appendText("\n")
        }
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