package parser_v2

import kotlinx.serialization.json.*
import kotlinx.serialization.modules.*

val astJsonFormat: Json by lazy {
    val module = SerializersModule {
        polymorphic(Ast::class) {
            subclass(NewLineToken::class)
            subclass(ReturnTypeToken::class)
            subclass(LetToken::class)
            subclass(IfToken::class)
            subclass(FunctionToken::class)
            subclass(ReturnToken::class)
            subclass(WhitespaceToken::class)
            subclass(OperatorToken::class)
            subclass(TypeToken::class)
            subclass(BadToken::class)
            subclass(NameToken::class)
            subclass(StringToken::class)
            subclass(BadStringToken::class)
            subclass(CharToken::class)
            subclass(BadCharToken::class)
            subclass(IntToken::class)
            subclass(FloatToken::class)
            subclass(OpenParenToken::class)
            subclass(CloseParenToken::class)
            subclass(OpenCurlyToken::class)
            subclass(CloseCurlyToken::class)
            subclass(CommaToken::class)
            subclass(DotToken::class)
            subclass(ColonToken::class)
            subclass(EqualSignToken::class)
            subclass(ProgramAst::class)
            subclass(FunctionAst::class)
            subclass(ParametersAst::class)
            subclass(ParameterAst::class)
            subclass(BlockAst::class)
            subclass(LetAst::class)
            subclass(ReturnAst::class)
            subclass(IfAst::class)
            subclass(ArgumentAst::class)
            subclass(ArgumentsAst::class)
            subclass(FunctionCallExpression::class)
            subclass(AtomicExpression::class)
            subclass(GroupingExpression::class)
            subclass(UnaryExpression::class)
            subclass(BinaryExpressionP1::class)
            subclass(BinaryExpressionP2::class)
            subclass(BinaryExpressionP3::class)
            subclass(BinaryExpressionP4::class)
            subclass(BinaryExpressionP5::class)
            subclass(BinaryExpressionP6::class)
            subclass(BinaryExpressionP7::class)
        }
    }
    Json { allowStructuredMapKeys = true; serializersModule = module }
}
