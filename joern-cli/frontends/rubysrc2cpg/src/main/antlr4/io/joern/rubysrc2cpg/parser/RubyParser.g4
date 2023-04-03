parser grammar RubyParser;

options {
    tokenVocab = RubyLexer;
}

// --------------------------------------------------------
// Program
// --------------------------------------------------------

program
    :   wsOrNl* compoundStatement EOF
    ;

compoundStatement
    :   statements? separators?
    ;

separators
    :   WS* separator (WS* separator)*
    ;

separator
    :   SEMI
    |   NL
    ;

// --------------------------------------------------------
// Statements
// --------------------------------------------------------

statements
    :   statement (separators WS* statement)*
    ;

statement
    :   ALIAS wsOrNl* definedMethodNameOrSymbol wsOrNl* definedMethodNameOrSymbol                                   # aliasStatement
    |   UNDEF wsOrNl* definedMethodNameOrSymbol (wsOrNl* COMMA wsOrNl* definedMethodNameOrSymbol)*                  # undefStatement
    |   statement WS* mod=(IF | UNLESS | WHILE | UNTIL | RESCUE) wsOrNl* statement                                  # modifierStatement
    |   BEGIN_ wsOrNl* LCURLY wsOrNl* statements? wsOrNl* RCURLY                                                    # beginStatement
    |   END_ wsOrNl* LCURLY wsOrNl* statements? wsOrNl* RCURLY                                                      # endStatement
    |   expressionOrCommand                                                                                         # expressionOrCommandStatement
    ;

// --------------------------------------------------------
// Expressions
// --------------------------------------------------------

expressionOrCommand
    :   (EMARK wsOrNl*)? invocationWithoutParentheses                                                               # invocationExpressionOrCommand
    |   NOT wsOrNl* expressionOrCommand                                                                             # notExpressionOrCommand
    |   <assoc=right> expressionOrCommand WS* op=(OR | AND) wsOrNl* expressionOrCommand                             # orAndExpressionOrCommand
    |   expression                                                                                                  # expressionExpressionOrCommand
    ;

expression
    :   primary                                                                                                     # primaryExpression
    |   op=(TILDE | PLUS | EMARK) wsOrNl* expression                                                                # unaryExpression
    |   <assoc=right> expression WS* STAR2 wsOrNl* expression                                                       # powerExpression
    |   MINUS wsOrNl* expression                                                                                    # unaryMinusExpression
    |   expression WS* op=(STAR | SLASH | PERCENT) wsOrNl* expression                                               # multiplicativeExpression
    |   expression WS* op=(PLUS | MINUS) wsOrNl* expression                                                         # additiveExpression
    |   expression WS* op=(LT2 | GT2) wsOrNl* expression                                                            # bitwiseShiftExpression
    |   expression WS* op=AMP wsOrNl* expression                                                                    # bitwiseAndExpression
    |   expression WS* op=(BAR | CARET) wsOrNl* expression                                                          # bitwiseOrExpression
    |   expression WS* op=(GT | GTEQ | LT | LTEQ) wsOrNl* expression                                                # relationalExpression
    |   expression WS* op=(LTEQGT | EQ2 | EQ3 | EMARKEQ | EQTILDE | EMARKTILDE) wsOrNl* expression?                 # equalityExpression
    |   expression WS* op=AMP2 wsOrNl* expression                                                                   # operatorAndExpression
    |   expression WS* op=BAR2 wsOrNl* expression                                                                   # operatorOrExpression
    |   expression WS* op=(DOT2 | DOT3) wsOrNl* expression?                                                         # rangeExpression
    |   expression WS* QMARK wsOrNl* expression WS* COLON wsOrNl* expression                                        # conditionalOperatorExpression
    |   <assoc=right> singleLeftHandSide WS* op=(EQ | ASSIGNMENT_OPERATOR) wsOrNl* multipleRightHandSide            # singleAssignmentExpression
    |   <assoc=right> multipleLeftHandSide WS* EQ wsOrNl* multipleRightHandSide                                     # multipleAssignmentExpression
    |   IS_DEFINED wsOrNl* expression                                                                               # isDefinedExpression
    ;

primary
    :   classDefinition                                                                                             # classDefinitionPrimary
    |   moduleDefinition                                                                                            # moduleDefinitionPrimary
    |   methodDefinition                                                                                            # methodDefinitionPrimary
    |   yieldWithOptionalArgument                                                                                   # yieldWithOptionalArgumentPrimary
    |   ifExpression                                                                                                # ifExpressionPrimary
    |   unlessExpression                                                                                            # unlessExpressionPrimary
    |   caseExpression                                                                                              # caseExpressionPrimary
    |   whileExpression                                                                                             # whileExpressionPrimary
    |   untilExpression                                                                                             # untilExpressionPrimary
    |   forExpression                                                                                               # forExpressionPrimary
    |   jumpExpression                                                                                              # jumpExpressionPrimary
    |   beginExpression                                                                                             # beginExpressionPrimary
    |   LPAREN wsOrNl* compoundStatement wsOrNl* RPAREN                                                             # groupingExpressionPrimary
    |   variableReference                                                                                           # variableReferencePrimary
    |   COLON2 CONSTANT_IDENTIFIER                                                                                  # simpleScopedConstantReferencePrimary
    |   primary COLON2 CONSTANT_IDENTIFIER                                                                          # chainedScopedConstantReferencePrimary
    |   arrayConstructor                                                                                            # arrayConstructorPrimary
    |   hashConstructor                                                                                             # hashConstructorPrimary
    |   literal                                                                                                     # literalPrimary
    |   IS_DEFINED LPAREN expressionOrCommand RPAREN                                                                # isDefinedPrimary
    |   SUPER argumentsWithParentheses? block?                                                                      # superExpressionPrimary
    |   primary LBRACK WS* indexingArguments? WS* RBRACK                                                            # indexingExpressionPrimary
    |   methodOnlyIdentifier                                                                                        # methodOnlyIdentifierPrimary
    |   methodIdentifier WS? block                                                                                  # invocationWithBlockOnlyPrimary
    |   methodIdentifier argumentsWithParentheses WS* block?                                                        # invocationWithParenthesesPrimary
    |   primary (DOT | COLON2) wsOrNl* methodName argumentsWithParentheses? WS? block?                              # chainedInvocationPrimary
    |   primary COLON2 methodName block?                                                                            # chainedInvocationWithoutArgumentsPrimary
    ;

// --------------------------------------------------------
// Assignments
// --------------------------------------------------------

singleLeftHandSide
    :   variableIdentifier
    |   primary LBRACK arguments? RBRACK
    |   primary (DOT | COLON2) (LOCAL_VARIABLE_IDENTIFIER | CONSTANT_IDENTIFIER)
    |   COLON2 CONSTANT_IDENTIFIER
    ;

multipleLeftHandSide
    :   (multipleLeftHandSideItem COMMA wsOrNl*)+ (multipleLeftHandSideItem | packingLeftHandSide)?
    |   packingLeftHandSide
    |   groupedLeftHandSide
    ;

multipleLeftHandSideItem
    :   singleLeftHandSide
    |   groupedLeftHandSide
    ;

packingLeftHandSide
    :   STAR singleLeftHandSide
    ;

groupedLeftHandSide
    :   LPAREN multipleLeftHandSide RPAREN
    ;

multipleRightHandSide
    :   expressionOrCommands (WS* COMMA wsOrNl* splattingArgument)?
    |   splattingArgument
    ;

expressionOrCommands
    :   expressionOrCommand (WS* COMMA wsOrNl* expressionOrCommand)*
    ;

// --------------------------------------------------------
// Invocation expressions
// --------------------------------------------------------

invocationWithoutParentheses
    :   command
    |   chainedCommandWithDoBlock
    |   chainedCommandWithDoBlock (DOT | COLON2) methodName argumentsWithoutParentheses
    |   RETURN WS arguments
    |   BREAK WS arguments
    |   NEXT WS arguments
    ;

command
    :   SUPER argumentsWithoutParentheses
    |   YIELD argumentsWithoutParentheses
    |   methodIdentifier argumentsWithoutParentheses
    |   primary WS* (DOT | COLON2) wsOrNl* methodName argumentsWithoutParentheses
    ;

chainedCommandWithDoBlock
    :   commandWithDoBlock ((DOT | COLON2) methodName argumentsWithParentheses?)*
    ;

commandWithDoBlock
    :   SUPER argumentsWithoutParentheses WS* doBlock
    |   methodIdentifier argumentsWithoutParentheses WS* doBlock
    |   primary WS* (DOT | COLON2) methodName argumentsWithoutParentheses WS* doBlock
    ;

argumentsWithoutParentheses
    :   WS+ arguments
    ;

arguments
    :   blockArgument
    |   splattingArgument (COMMA wsOrNl* blockArgument)?
    |   expressions WS* COMMA wsOrNl* associations (WS* COMMA wsOrNl* splattingArgument)? (WS* COMMA wsOrNl* blockArgument)?
    |   (expressions | associations) (WS* COMMA wsOrNl* blockArgument)?
    |   command
    ;

blockArgument
    :   AMP expression
    ;

// --------------------------------------------------------
// Arguments
// --------------------------------------------------------

splattingArgument
    :   STAR expressionOrCommand
    ;

indexingArguments
    :   command
    |   expressions (WS* COMMA wsOrNl*)?
    |   expressions WS* COMMA wsOrNl* splattingArgument
    |   associations (WS* COMMA wsOrNl*)?
    |   splattingArgument
    ;

argumentsWithParentheses
    :   LPAREN wsOrNl* RPAREN
    |   LPAREN arguments RPAREN
    |   LPAREN expressions WS* COMMA wsOrNl* chainedCommandWithDoBlock wsOrNl* RPAREN
    |   LPAREN chainedCommandWithDoBlock RPAREN
    ;

expressions
    :   expression (WS* COMMA wsOrNl* expression)*
    ;

// --------------------------------------------------------
// Blocks
// --------------------------------------------------------

block
    :   braceBlock
    |   doBlock
    ;

braceBlock
    :   LCURLY wsOrNl* blockParameter? wsOrNl* compoundStatement wsOrNl* RCURLY
    ;

doBlock
    :   DO wsOrNl* blockParameter? separators wsOrNl* compoundStatement wsOrNl* END
    ;

blockParameter
    :   BAR WS* blockParameters? WS* BAR
    ;

blockParameters
    :   singleLeftHandSide
    |   multipleLeftHandSide
    ;

// --------------------------------------------------------
// Arrays
// --------------------------------------------------------

arrayConstructor
    :   LBRACK wsOrNl* indexingArguments? wsOrNl* RBRACK
    ;

// --------------------------------------------------------
// Hashes
// --------------------------------------------------------

hashConstructor
    :   LCURLY wsOrNl* (associations WS* COMMA?)? wsOrNl* RCURLY
    ;

associations
    :   association (WS* COMMA wsOrNl* association)*
    ;

association
    :   expression WS* (EQGT|COLON) wsOrNl* expression
    ;

// --------------------------------------------------------
// Method definitions
// --------------------------------------------------------

methodDefinition
    :   DEF wsOrNl* definedMethodName WS* methodParameterPart wsOrNl* bodyStatement wsOrNl* END
    |   DEF wsOrNl* singletonObject wsOrNl* (DOT | COLON2) wsOrNl* definedMethodName WS* methodParameterPart wsOrNl* bodyStatement wsOrNl* END
    ;

singletonObject
    :   variableIdentifier
    |   pseudoVariableIdentifier
    |   LPAREN expressionOrCommand RPAREN
    ;

definedMethodName
    :   methodName
    |   assignmentLikeMethodIdentifier
    ;

assignmentLikeMethodIdentifier
    :   (CONSTANT_IDENTIFIER | LOCAL_VARIABLE_IDENTIFIER) EQ
    ;

methodName
    :   methodIdentifier
    |   operatorMethodName
    |   keyword
    ;

methodIdentifier
    :   LOCAL_VARIABLE_IDENTIFIER
    |   CONSTANT_IDENTIFIER
    |   methodOnlyIdentifier
    ;

methodOnlyIdentifier
    :   (LOCAL_VARIABLE_IDENTIFIER | CONSTANT_IDENTIFIER) (EMARK | QMARK)
    ;

methodParameterPart
    :   LPAREN parameters? RPAREN
    |   parameters? separator
    ;

parameters
    :   mandatoryParameters (COMMA wsOrNl* optionalParameters)? (COMMA arrayParameter)? (COMMA procParameter)?
    |   optionalParameters (COMMA wsOrNl* arrayParameter)? (COMMA wsOrNl* procParameter)?
    |   arrayParameter (COMMA wsOrNl* procParameter)?
    |   procParameter
    ;

mandatoryParameters
    :   LOCAL_VARIABLE_IDENTIFIER (COMMA wsOrNl* LOCAL_VARIABLE_IDENTIFIER)*
    ;

optionalParameters
    :   optionalParameter (COMMA wsOrNl* optionalParameter)*
    ;

optionalParameter
    :   LOCAL_VARIABLE_IDENTIFIER EQ wsOrNl* expression
    ;

arrayParameter
    :   STAR LOCAL_VARIABLE_IDENTIFIER?
    ;

procParameter
    :   AMP LOCAL_VARIABLE_IDENTIFIER
    ;

// --------------------------------------------------------
// Conditional expressions
// --------------------------------------------------------

ifExpression
    :   IF wsOrNl* expressionOrCommand WS* thenClause (wsOrNl* elsifClause)* (wsOrNl* elseClause)? wsOrNl* END
    ;

thenClause
    :   separator wsOrNl* compoundStatement
    |   separator? THEN wsOrNl* compoundStatement
    ;

elsifClause
    :   ELSIF wsOrNl* expressionOrCommand WS? thenClause
    ;

elseClause
    :   ELSE wsOrNl* compoundStatement
    ;

unlessExpression
    :   UNLESS wsOrNl* expressionOrCommand WS* thenClause elseClause? END
    ;

caseExpression
    :   CASE (wsOrNl* expressionOrCommand)? separators? whenClause+ elseClause? END
    ;

whenClause
    :   WHEN wsOrNl* whenArgument thenClause
    ;

whenArgument
    :   expressions (WS* COMMA splattingArgument)?
    |   splattingArgument
    ;

// --------------------------------------------------------
// Iteration expressions
// --------------------------------------------------------

whileExpression
    :   WHILE wsOrNl* expressionOrCommand doClause wsOrNl* END
    ;

doClause
    :   separator wsOrNl* compoundStatement
    |   WS? DO wsOrNl* compoundStatement
    ;

untilExpression
    :   UNTIL wsOrNl* expressionOrCommand doClause wsOrNl* END
    ;

forExpression
    :   FOR wsOrNl* forVariable WS* IN wsOrNl* expressionOrCommand doClause wsOrNl* END
    ;

forVariable
    :   singleLeftHandSide
    |   multipleLeftHandSide
    ;

// --------------------------------------------------------
// Begin expression
// --------------------------------------------------------

beginExpression
    :   BEGIN wsOrNl* bodyStatement wsOrNl* END
    ;

bodyStatement
    :   compoundStatement (wsOrNl* rescueClause)* (wsOrNl* elseClause)? ensureClause?
    ;

rescueClause
    :   RESCUE WS* exceptionClass? wsOrNl* exceptionVariableAssignment? thenClause
    ;

exceptionClass
    :   expression
    |   multipleRightHandSide
    ;

exceptionVariableAssignment
    :   EQGT WS* singleLeftHandSide
    ;

ensureClause
    :   ENSURE wsOrNl* compoundStatement
    ;

// --------------------------------------------------------
// Class definitions
// --------------------------------------------------------

classDefinition
    :   CLASS wsOrNl* classOrModuleReference WS* (LT wsOrNl* expressionOrCommand)? separators wsOrNl* bodyStatement wsOrNl* END
    |   CLASS wsOrNl* LT2 expressionOrCommand separator bodyStatement wsOrNl* END
    ;

classOrModuleReference
    :   scopedConstantReference
    |   CONSTANT_IDENTIFIER
    ;

// --------------------------------------------------------
// Module definitions
// --------------------------------------------------------

moduleDefinition
    :   MODULE wsOrNl* classOrModuleReference wsOrNl* bodyStatement wsOrNl* END
    ;

// --------------------------------------------------------
// Yield expressions
// --------------------------------------------------------

yieldWithOptionalArgument
    :   YIELD (LPAREN arguments? RPAREN)?
    ;

// --------------------------------------------------------
// Jump expressions
// --------------------------------------------------------

jumpExpression
    :   RETURN
    |   BREAK
    |   NEXT
    |   REDO
    |   RETRY
    ;

// --------------------------------------------------------
// Variable references
// --------------------------------------------------------

variableReference
    :   variableIdentifier
    |   pseudoVariableIdentifier
    ;

variableIdentifier
    :   LOCAL_VARIABLE_IDENTIFIER
    |   GLOBAL_VARIABLE_IDENTIFIER
    |   INSTANCE_VARIABLE_IDENTIFIER
    |   CLASS_VARIABLE_IDENTIFIER
    |   CONSTANT_IDENTIFIER
    ;

pseudoVariableIdentifier
    :   NIL
    |   TRUE
    |   FALSE
    |   SELF
    |   FILE__
    |   LINE__
    |   ENCODING__
    ;

scopedConstantReference
    :   COLON2 CONSTANT_IDENTIFIER
    |   primary COLON2 CONSTANT_IDENTIFIER
    ;

// --------------------------------------------------------
// Literals
// --------------------------------------------------------

literal
    :   numericLiteral
    |   symbol
    |   SINGLE_QUOTED_STRING_LITERAL
    ;

symbol
    :   SYMBOL_LITERAL
    |   COLON SINGLE_QUOTED_STRING_LITERAL
    ;

// --------------------------------------------------------
// Numerics
// --------------------------------------------------------

numericLiteral
    :   (PLUS | MINUS)? unsignedNumericLiteral
    ;

unsignedNumericLiteral
    :   DECIMAL_INTEGER_LITERAL
    |   BINARY_INTEGER_LITERAL
    |   OCTAL_INTEGER_LITERAL
    |   HEXADECIMAL_INTEGER_LITERAL
    |   FLOAT_LITERAL_WITHOUT_EXPONENT
    |   FLOAT_LITERAL_WITH_EXPONENT
    ;

// --------------------------------------------------------
// Helpers
// --------------------------------------------------------

definedMethodNameOrSymbol
    :   definedMethodName
    |   symbol
    ;

wsOrNl
    :   WS
    |   NL
    ;

keyword
    :   LINE__
    |   ENCODING__
    |   FILE__
    |   BEGIN_
    |   END_
    |   ALIAS
    |   AND
    |   BEGIN
    |   BREAK
    |   CASE
    |   CLASS
    |   DEF
    |   IS_DEFINED
    |   DO
    |   ELSE
    |   ELSIF
    |   END
    |   ENSURE
    |   FOR
    |   FALSE
    |   IF
    |   IN
    |   MODULE
    |   NEXT
    |   NIL
    |   NOT
    |   OR
    |   REDO
    |   RESCUE
    |   RETRY
    |   RETURN
    |   SELF
    |   SUPER
    |   THEN
    |   TRUE
    |   UNDEF
    |   UNLESS
    |   UNTIL
    |   WHEN
    |   WHILE
    |   YIELD
    ;

operatorMethodName
    :   CARET
    |   AMP
    |   BAR
    |   LTEQGT
    |   EQ2
    |   EQ3
    |   EQTILDE
    |   GT
    |   GTEQ
    |   LT
    |   LTEQ
    |   LT2
    |   GT2
    |   PLUS
    |   MINUS
    |   STAR
    |   SLASH
    |   PERCENT
    |   STAR2
    |   TILDE
    |   PLUSAT
    |   MINUSAT
    |   LBRACK RBRACK
    |   LBRACK RBRACK EQ
    ;