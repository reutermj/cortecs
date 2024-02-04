package parser_v2

val whitespaceCombos: List<String> = run {
    val whitespaceCombos = mutableListOf("")
    val whitespaceCharacters = listOf(" ", "\n")
    whitespaceCombos.addAll(whitespaceCharacters)

    for(i in whitespaceCharacters)
        for(j in whitespaceCharacters) {
            if(i == " " && j == " ") continue
            whitespaceCombos.add("$i$j")
        }

    for(i in whitespaceCharacters)
        for(j in whitespaceCharacters)
            for(k in whitespaceCharacters) {
                if(i == " " && j == " ") continue
                if(j == " " && k == " ") continue
                whitespaceCombos.add("$i$j$k")
            }

    for(i in whitespaceCharacters)
        for(j in whitespaceCharacters)
            for(k in whitespaceCharacters)
                for(l in whitespaceCharacters) {
                    if(i == " " && j == " ") continue
                    if(j == " " && k == " ") continue
                    if(k == " " && l == " ") continue
                    whitespaceCombos.add("$i$j$k$l")
                }

    whitespaceCombos
}