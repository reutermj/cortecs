package typechecker

 import parser.*
 import kotlin.test.*

class ProgramSubstitutionTests {
    @Test
    fun test001() {
        val s = """function id(x) {
                  |return x
                  |}
                  |function foo(x) {
                  |return id(x)
                  |}""".trimMargin()
        val iterator = ParserIterator()
        iterator.add(s)
        val program = parseProgram(iterator)
        val environment = program.environment

        val fooScheme = environment.substitution.apply(environment.bindings[NameToken("foo")]!!)
        assertIs<TypeScheme>(fooScheme)
        assertEquals(1, fooScheme.boundVariables.size)
        val fooTypeVar = fooScheme.boundVariables.first()
        val fooType = fooScheme.type
        assertIs<ArrowType>(fooType)
        assertEquals(fooTypeVar, fooType.lhs)
        assertEquals(fooTypeVar, fooType.rhs)

        val idScheme = environment.substitution.apply(environment.bindings[NameToken("id")]!!)
        assertIs<TypeScheme>(idScheme)
        assertEquals(1, idScheme.boundVariables.size)
        val idTypeVar = idScheme.boundVariables.first()
        val idType = idScheme.type
        assertIs<ArrowType>(idType)
        assertEquals(idTypeVar, idType.lhs)
        assertEquals(idTypeVar, idType.rhs)

        assertTrue { environment.requirements.isEmpty() }
    }

    @Test
    fun test002() {
        val s = """function foo(x) {
                  |return id(x)
                  |}
                  |function id(x) {
                  |return x
                  |}""".trimMargin()
        val iterator = ParserIterator()
        iterator.add(s)
        val program = parseProgram(iterator)
        val environment = program.environment

        val fooScheme = environment.substitution.apply(environment.bindings[NameToken("foo")]!!)
        assertIs<TypeScheme>(fooScheme)
        assertEquals(1, fooScheme.boundVariables.size)
        val fooTypeVar = fooScheme.boundVariables.first()
        val fooType = fooScheme.type
        assertIs<ArrowType>(fooType)
        assertEquals(fooTypeVar, fooType.lhs)
        assertEquals(fooTypeVar, fooType.rhs)

        val idScheme = environment.substitution.apply(environment.bindings[NameToken("id")]!!)
        assertIs<TypeScheme>(idScheme)
        assertEquals(1, idScheme.boundVariables.size)
        val idTypeVar = idScheme.boundVariables.first()
        val idType = idScheme.type
        assertIs<ArrowType>(idType)
        assertEquals(idTypeVar, idType.lhs)
        assertEquals(idTypeVar, idType.rhs)

        assertTrue { environment.requirements.isEmpty() }
    }

    @Test
    fun test003() {
        val s = """function foo() {
                  |return id(1)
                  |}
                  |function id(x) {
                  |return x
                  |}""".trimMargin()
        val iterator = ParserIterator()
        iterator.add(s)
        val program = parseProgram(iterator)
        val environment = program.environment

        val fooType = environment.substitution.apply(environment.bindings[NameToken("foo")]!!)
        assertIs<ArrowType>(fooType)
        assertIs<UnitType>(fooType.lhs)
        assertIs<I32Type>(fooType.rhs)

        val idScheme = environment.substitution.apply(environment.bindings[NameToken("id")]!!)
        assertIs<TypeScheme>(idScheme)
        assertEquals(1, idScheme.boundVariables.size)
        val idTypeVar = idScheme.boundVariables.first()
        val idType = idScheme.type
        assertIs<ArrowType>(idType)
        assertEquals(idTypeVar, idType.lhs)
        assertEquals(idTypeVar, idType.rhs)

        assertTrue { environment.requirements.isEmpty() }
    }

    @Test
    fun test004() {
        val s = """function foo() {
                  |return id(1)
                  |}
                  |function bar() {
                  |return id(1u)
                  |}
                  |function id(x) {
                  |return x
                  |}""".trimMargin()
        val iterator = ParserIterator()
        iterator.add(s)
        val program = parseProgram(iterator)
        val environment = program.environment

        val fooType = environment.substitution.apply(environment.bindings[NameToken("foo")]!!)
        assertIs<ArrowType>(fooType)
        assertIs<UnitType>(fooType.lhs)
        assertIs<I32Type>(fooType.rhs)

        val barType = environment.substitution.apply(environment.bindings[NameToken("bar")]!!)
        assertIs<ArrowType>(barType)
        assertIs<UnitType>(barType.lhs)
        assertIs<U32Type>(barType.rhs)

        val idScheme = environment.substitution.apply(environment.bindings[NameToken("id")]!!)
        assertIs<TypeScheme>(idScheme)
        assertEquals(1, idScheme.boundVariables.size)
        val idTypeVar = idScheme.boundVariables.first()
        val idType = idScheme.type
        assertIs<ArrowType>(idType)
        assertEquals(idTypeVar, idType.lhs)
        assertEquals(idTypeVar, idType.rhs)

        assertTrue { environment.requirements.isEmpty() }
    }

    @Test
    fun test005() {
        val s = """function foo() {
                  |return id(1)
                  |}
                  |function bar() {
                  |return id(1u)
                  |}
                  |function baz() {
                  |return id('c')
                  |}
                  |function id(x) {
                  |return x
                  |}""".trimMargin()
        val iterator = ParserIterator()
        iterator.add(s)
        val program = parseProgram(iterator)
        val environment = program.environment

        val fooType = environment.substitution.apply(environment.bindings[NameToken("foo")]!!)
        assertIs<ArrowType>(fooType)
        assertIs<UnitType>(fooType.lhs)
        assertIs<I32Type>(fooType.rhs)

        val barType = environment.substitution.apply(environment.bindings[NameToken("bar")]!!)
        assertIs<ArrowType>(barType)
        assertIs<UnitType>(barType.lhs)
        assertIs<U32Type>(barType.rhs)

        val bazType = environment.substitution.apply(environment.bindings[NameToken("baz")]!!)
        assertIs<ArrowType>(bazType)
        assertIs<UnitType>(bazType.lhs)
        assertIs<CharacterType>(bazType.rhs)

        val idScheme = environment.substitution.apply(environment.bindings[NameToken("id")]!!)
        assertIs<TypeScheme>(idScheme)
        assertEquals(1, idScheme.boundVariables.size)
        val idTypeVar = idScheme.boundVariables.first()
        val idType = idScheme.type
        assertIs<ArrowType>(idType)
        assertEquals(idTypeVar, idType.lhs)
        assertEquals(idTypeVar, idType.rhs)

        assertTrue { environment.requirements.isEmpty() }
    }

    @Test
    fun test006() {
        val s = """function isEven(x) {
                  |  if(x == 0) {
                  |    return 1
                  |  }
                  |  return isOdd(x - 1)
                  |}
                  |function isOdd(x) {
                  |  if(x == 0) {
                  |    return 0
                  |  }
                  |  return isEven(x - 1)
                  |}""".trimMargin()
        val iterator = ParserIterator()
        iterator.add(s)
        val program = parseProgram(iterator)
        val environment =  TopLevelEnvironment.base  + program.environment

        val isEvenType = environment.substitution.apply(environment.bindings[NameToken("isEven")]!!)
        assertIs<ArrowType>(isEvenType)
        assertIs<I32Type>(isEvenType.lhs)
        assertIs<I32Type>(isEvenType.rhs)

        val isOddType = environment.substitution.apply(environment.bindings[NameToken("isOdd")]!!)
        assertIs<ArrowType>(isOddType)
        assertIs<I32Type>(isOddType.lhs)
        assertIs<I32Type>(isOddType.rhs)
    }

    @Test
    fun test007() {
        val s = """function fib(x) {
                  |  if(x == 0) {
                  |    return 0
                  |  }
                  |  if(x == 1) {
                  |    return 1
                  |  }
                  |  return fibSub(x, 1) + fibSub(x, 2)
                  |}
                  |function fibSub(x, n) {
                  |  return fib(x - n)
                  |}""".trimMargin()
        val iterator = ParserIterator()
        iterator.add(s)
        val program = parseProgram(iterator)
        val environment =  TopLevelEnvironment.base  + program.environment

        val fibType = environment.substitution.apply(environment.bindings[NameToken("fib")]!!)
        assertIs<ArrowType>(fibType)
        assertIs<I32Type>(fibType.lhs)
        assertIs<I32Type>(fibType.rhs)

        val fibSubType = environment.substitution.apply(environment.bindings[NameToken("fibSub")]!!)
        assertIs<ArrowType>(fibSubType)
        val fibSubLhsType = fibSubType.lhs
        assertIs<ProductType>(fibSubLhsType)
        assertEquals(fibSubLhsType.types.size, 2)
        assertIs<I32Type>(fibSubLhsType.types[0])
        assertIs<I32Type>(fibSubLhsType.types[1])
        assertIs<I32Type>(fibSubType.rhs)
    }

    @Test
    fun test008() {
        val s = """function id(x) {
                  |  return x
                  |}
                  |function foo(x) {
                  |  let a = id(x)
                  |  return a
                  |}""".trimMargin()
        val iterator = ParserIterator()
        iterator.add(s)
        val program = parseProgram(iterator)
        val environment =  TopLevelEnvironment.base  + program.environment

        val idScheme = environment.substitution.apply(environment.bindings[NameToken("id")]!!)
        assertIs<TypeScheme>(idScheme)
        assertEquals(1, idScheme.boundVariables.size)
        val idTypeVar = idScheme.boundVariables.first()
        val idType = idScheme.type
        assertIs<ArrowType>(idType)
        assertEquals(idTypeVar, idType.lhs)
        assertEquals(idTypeVar, idType.rhs)

        val fooScheme = environment.substitution.apply(environment.bindings[NameToken("foo")]!!)
        assertIs<TypeScheme>(fooScheme)
        assertEquals(1, fooScheme.boundVariables.size)
        val fooTypeVar = fooScheme.boundVariables.first()
        val fooType = fooScheme.type
        assertIs<ArrowType>(fooType)
        assertEquals(fooTypeVar, fooType.lhs)
        assertEquals(fooTypeVar, fooType.rhs)
    }

    @Test
    fun test009() {
        val s = """function foo(x) {
                  |  let a = id(x)
                  |  let b = id(1)
                  |  let c = id(1.1)
                  |  return a
                  |}
                  |function id(x) {
                  |  return x
                  |}""".trimMargin()
        val iterator = ParserIterator()
        iterator.add(s)
        val program = parseProgram(iterator)
        val environment =  TopLevelEnvironment.base  + program.environment

        val idType = environment.substitution.apply(environment.bindings[NameToken("id")]!!)
        assertIs<TypeScheme>(idType)
        assertEquals(idType.boundVariables.size, 1)
        val idTypeVar = idType.boundVariables.first()
        assertEquals(ArrowType(idTypeVar, idTypeVar), idType.type)

        val fooType = environment.substitution.apply(environment.bindings[NameToken("foo")]!!)
        assertIs<TypeScheme>(fooType)
        assertEquals(fooType.boundVariables.size, 1)
        val fooTypeVar = fooType.boundVariables.first()
        assertEquals(ArrowType(fooTypeVar, fooTypeVar), fooType.type)
    }

    @Test
    fun test010() {
        val s = """function id(x) {
                  |  return x
                  |}
                  |function foo(x) {
                  |  let a = id(x)
                  |  let b = id(1)
                  |  let c = id(1.1)
                  |  return b
                  |}""".trimMargin()
        val iterator = ParserIterator()
        iterator.add(s)
        val program = parseProgram(iterator)
        val environment =  TopLevelEnvironment.base  + program.environment

        val idType = environment.substitution.apply(environment.bindings[NameToken("id")]!!)
        assertIs<TypeScheme>(idType)
        assertEquals(idType.boundVariables.size, 1)
        val idTypeVar = idType.boundVariables.first()
        assertEquals(ArrowType(idTypeVar, idTypeVar), idType.type)

        val fooType = environment.substitution.apply(environment.bindings[NameToken("foo")]!!)
        assertIs<TypeScheme>(fooType)
        assertEquals(fooType.boundVariables.size, 1)
        val fooTypeVar = fooType.boundVariables.first()
        assertEquals(ArrowType(fooTypeVar, I32Type), fooType.type)
    }

    @Test
    fun test011() {
        val s = """function b() {
                  |  return id(1)
                  |}
                  |function id(x) {
                  |  let y = d(1)
                  |  return x
                  |}
                  |function c() {
                  |  return id(1.1)
                  |}
                  |function d(x) {
                  |  return x
                  |}""".trimMargin()
        val iterator = ParserIterator()
        iterator.add(s)
        val program = parseProgram(iterator)
        val environment =  TopLevelEnvironment.base  + program.environment

        val idType = environment.substitution.apply(environment.bindings[NameToken("id")]!!)
        assertIs<TypeScheme>(idType)
        assertEquals(idType.boundVariables.size, 1)
        val idTypeVar = idType.boundVariables.first()
        assertEquals(ArrowType(idTypeVar, idTypeVar), idType.type)

        val bType = environment.substitution.apply(environment.bindings[NameToken("b")]!!)
        assertIs<ArrowType>(bType)
        assertIs<UnitType>(bType.lhs)
        assertIs<I32Type>(bType.rhs)

        val cType = environment.substitution.apply(environment.bindings[NameToken("c")]!!)
        assertIs<ArrowType>(cType)
        assertIs<UnitType>(cType.lhs)
        assertIs<F32Type>(cType.rhs)

        val dType = environment.substitution.apply(environment.bindings[NameToken("d")]!!)
        assertIs<TypeScheme>(dType)
        assertEquals(dType.boundVariables.size, 1)
        val dTypeVar = dType.boundVariables.first()
        assertEquals(ArrowType(dTypeVar, dTypeVar), dType.type)
    }

    @Test
    fun test012() {
        val s = """function foo() {
                  |let x = id(1)
                  |let y = id(1.1)
                  |return x
                  |}
                  |function id(x) {
                  |return x
                  |}""".trimMargin()
        val iterator = ParserIterator()
        iterator.add(s)
        val program = parseProgram(iterator)
        val environment = program.environment

        val fooType = environment.substitution.apply(environment.bindings[NameToken("foo")]!!)
        assertIs<ArrowType>(fooType)
        assertIs<UnitType>(fooType.lhs)
        assertIs<I32Type>(fooType.rhs)
    }

    @Test
    fun test014() {
        val s = """function foo() {
                  |let x = id(1)
                  |let y = id(1.1)
                  |return y
                  |}
                  |function id(x) {
                  |return x
                  |}""".trimMargin()
        val iterator = ParserIterator()
        iterator.add(s)
        val program = parseProgram(iterator)
        val environment = program.environment

        val fooType = environment.substitution.apply(environment.bindings[NameToken("foo")]!!)
        assertIs<ArrowType>(fooType)
        assertIs<UnitType>(fooType.lhs)
        assertIs<F32Type>(fooType.rhs)
    }

    @Test
    fun test015() {
        val s = """function foo() {
                  |let x = id(1)
                  |let y = id(x)
                  |return y
                  |}
                  |function id(x) {
                  |return x
                  |}""".trimMargin()
        val iterator = ParserIterator()
        iterator.add(s)
        val program = parseProgram(iterator)
        val environment = program.environment

        val fooType = environment.substitution.apply(environment.bindings[NameToken("foo")]!!)
        assertIs<ArrowType>(fooType)
        assertIs<UnitType>(fooType.lhs)
        assertIs<I32Type>(fooType.rhs)
    }

    @Test
    fun test016() {
        val s = """function foo() {
                  |let x = id(1)
                  |let y = id(1.1)
                  |return x
                  |}
                  |function bar() {
                  |let x = id(1)
                  |let y = id(1.1)
                  |return y
                  |}
                  |function id(x: t) {
                  |return x
                  |}""".trimMargin()
        val iterator = ParserIterator()
        iterator.add(s)
        val program = parseProgram(iterator)
        val environment = program.environment

        val fooType = environment.substitution.apply(environment.bindings[NameToken("foo")]!!)
        assertIs<ArrowType>(fooType)
        assertIs<UnitType>(fooType.lhs)
        assertIs<I32Type>(fooType.rhs)

        val barType = environment.substitution.apply(environment.bindings[NameToken("bar")]!!)
        assertIs<ArrowType>(barType)
        assertIs<UnitType>(barType.lhs)
        assertIs<F32Type>(barType.rhs)
    }

    @Test
    fun test017() {
        val s = """function foo(x: s) {
                  |return id(x)
                  |}
                  |function id(x: t) {
                  |return x
                  |}""".trimMargin()
        val iterator = ParserIterator()
        iterator.add(s)
        val program = parseProgram(iterator)
        val environment = program.environment

        val fooType = environment.substitution.apply(environment.bindings[NameToken("foo")]!!)
        assertIs<TypeScheme>(fooType)
        assertEquals(1, fooType.boundVariables.size)
        val fooTypeVar = fooType.boundVariables.first()
        assertIs<UserDefinedTypeVariable>(fooTypeVar)
        assertEquals(ArrowType(fooTypeVar, fooTypeVar), fooType.type)

        val idType = environment.substitution.apply(environment.bindings[NameToken("id")]!!)
        assertIs<TypeScheme>(idType)
        assertEquals(1, idType.boundVariables.size)
        val idTypeVar = idType.boundVariables.first()
        assertNotEquals(idTypeVar, fooTypeVar)
    }

    @Test
    fun test018() {
        val s = """function foo(x): s {
                  |return id(x)
                  |}
                  |function id(x: t) {
                  |return x
                  |}""".trimMargin()
        val iterator = ParserIterator()
        iterator.add(s)
        val program = parseProgram(iterator)
        val environment = program.environment

        val fooType = environment.substitution.apply(environment.bindings[NameToken("foo")]!!)
        assertIs<TypeScheme>(fooType)
        assertEquals(1, fooType.boundVariables.size)
        val fooTypeVar = fooType.boundVariables.first()
        assertIs<UserDefinedTypeVariable>(fooTypeVar)
        assertEquals(ArrowType(fooTypeVar, fooTypeVar), fooType.type)

        val idType = environment.substitution.apply(environment.bindings[NameToken("id")]!!)
        assertIs<TypeScheme>(idType)
        assertEquals(1, idType.boundVariables.size)
        val idTypeVar = idType.boundVariables.first()
        assertNotEquals(idTypeVar, fooTypeVar)
    }

    @Test
    fun test019() {

    }

    @Test
    fun test020() {

    }
}