package parser

import kotlinx.serialization.*
import kotlinx.serialization.json.*
import kotlinx.serialization.modules.*

//TODO jesus fuck is do I need to override the default @SerialName for pretty much everything
//the serialization is absolutely massive
val astJsonFormat: Json by lazy {
    val module = SerializersModule {
        polymorphic(Ast::class) {
            subclass(FnAst::class)
            subclass(ParameterAst::class)
            subclass(ParameterAst::class)
            subclass(LetAst::class)
            subclass(ReturnAst::class)
            subclass(IfAst::class)
            subclass(TopGarbageAst::class)
            subclass(BodyGarbageAst::class)
            subclass(UnaryExpression::class)
            subclass(GroupingExpression::class)
            subclass(AtomicExpression::class)
            subclass(FnCallExpression::class)
            subclass(BinaryExpression::class)
            subclass(Argument::class)
            subclass(StarLeaf::class)
            subclass(StarNode.serializer(PolymorphicSerializer(Ast::class)))
        }
        polymorphic(AtomicExpressionToken::class) {
            subclass(NameToken::class)
            subclass(StringToken::class)
            subclass(BadStringToken::class)
            subclass(CharToken::class)
            subclass(BadCharToken::class)
            subclass(IntToken::class)
            subclass(FloatToken::class)
            subclass(BadNumberToken::class)
        }
        polymorphic(TypeAnnotationToken::class) {
            subclass(TypeToken::class)
            subclass(NameToken::class)
        }
    }
    Json { allowStructuredMapKeys = true; serializersModule = module }
}
//
//val bindableTokenModule = SerializersModule {
//    polymorphic(BindableToken::class) {
//        subclass(ReturnTypeToken::class)
//        subclass(OperatorToken::class)
//        subclass(NameToken::class)
//    }
//}
//
//val keywordModule = SerializersModule {
//    polymorphic(Keyword::class) {
//        subclass(LetToken::class)
//        subclass(IfToken::class)
//        subclass(ReturnToken::class)
//        subclass(FnToken::class)
//    }
//}
//
//val bodyKeywordModule = SerializersModule {
//    polymorphic(BodyKeyword::class) {
//        subclass(LetToken::class)
//        subclass(IfToken::class)
//        subclass(ReturnToken::class)
//    }
//}
//
//val typeAnnotationTokenModule = SerializersModule {
//    polymorphic(TypeAnnotationToken::class) {
//        subclass(TypeToken::class)
//        subclass(NameToken::class)
//    }
//}
//
//val atomicExpressionTokenModule = SerializersModule {

//}
//
//val topLevelKeywordModule = SerializersModule {
//    polymorphic(TopLevelKeyword::class) {
//        subclass(FnToken::class)
//    }
//}
//
