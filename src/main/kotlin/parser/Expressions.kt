package parser

sealed interface Expression: Ast

sealed interface SingleExpression: Expression
class UnaryExpression(sequence: List<Ast>, val op: OperatorToken, val expression: Expression?): SequenceAst(sequence), SingleExpression
class GroupingExpression(sequence: List<Ast>, val expression: Expression?): SequenceAst(sequence), SingleExpression
class AtomicExpression(sequence: List<Ast>, val atom: AtomicExpressionToken): SequenceAst(sequence), SingleExpression

sealed interface CompoundExpression: Expression
class FunctionCallExpression(sequence: List<Ast>, val function: Expression, val arguments: StarAst<Argument>?): SequenceAst(sequence), CompoundExpression
class BinaryOpExpression(sequence: List<Ast>, val lhs: Expression, val op: OperatorToken, val rhs: Expression?): SequenceAst(sequence), CompoundExpression

class Argument(sequence: List<Ast>, val argument: Expression): SequenceAst(sequence)