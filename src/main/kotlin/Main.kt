import parser.*
import tokenizer.*
import typechecker.*
import lsp.*
import java.io.File

fun main() {
    val parent = File(".").absolutePath.dropLast(1)
    val file = File(parent + File.separator + "program.upl")
    val s = file.readText()
    /*val defs = parse(tokenize(s))
    generateCallDependencyGraph(defs)*/
    /*var env = Environment()
    for (def in defs) {
        val (e) = generateProgramConstraints(env, def)
        printWithTypes(def)
        env = e
    }*/
}