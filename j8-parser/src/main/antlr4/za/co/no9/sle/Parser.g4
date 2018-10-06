grammar Parser;


@parser::members {
    public boolean notStartOfLine() {
        return getCurrentToken().getCharPositionInLine() > 0;
    }
}


module
    : declaration*
    ;


declaration
    : 'typealias' UpperID '=' type
        # TypeAliasDeclaration
    | 'type' UpperID LowerID* '=' typeConstructor ( '|' typeConstructor )*
        # TypeDeclaration
    | LowerID ':' type
        # LetSignature
    | LowerID+ '=' expression
        # LetDeclaration
    | LowerID+ ( '|' expression '=' expression)+
        # LetGuardDeclaration
    ;


typeConstructor
    : UpperID ( {notStartOfLine()}? type) *
    ;


expression
    : 'case' expression 'of' caseItem+
        # CaseExpression
    | expression op=('*' | '/') expression
        # MultiplicativeExpression
    | expression op=('+' | '-') expression
        # AdditiveExpression
    | expression op=('==' | '!=' | '<=' | '<' | '>=' | '>') expression
        # RelationalOpExpression
    | expression op='&&' expression
        # BooleanAndExpression
    | expression op='||' expression
        # BooleanOrExpression
    | '\\' LowerID+ '->' expression
        # LambdaExpression
    | 'if' expression 'then' expression 'else' expression
        # IfExpression
    | term ( {notStartOfLine()}? term )*
        # CallExpression
    ;


caseItem
    : pattern '->' expression
    ;

term
    : '(' expression ')'
        # ParenExpression
    | ConstantInt
        # ConstantIntExpression
    | 'True'
        # TrueExpression
    | 'False'
        # FalseExpression
    | ConstantString
        # ConstantStringExpression
    | '!' expression
        # NotExpression
    | LowerID
        # LowerIDExpression
    | UpperID
        # UpperIDExpression
    | '(' ')'
        # UnitValueExpression
    ;

pattern
    : ConstantInt
        # ConstantIntPattern
    | 'True'
        # TruePattern
    | 'False'
        # FalsePattern
    | ConstantString
        # ConstantStringPattern
    | '(' ')'
        # UnitPattern
    | LowerID
        # LowerIDPattern
    | UpperID pattern*
        # UpperIDPattern
    | '(' pattern ')'
        # ParentPattern
    ;

type
    : LowerID
        # LowerIDType
    | UpperID
        # UpperIDType
    | '(' typeArgument ')'
        # TypeArgumentType
    | <assoc=right> type '->' type
        # ArrowType
    | '(' ')'
        # UnitType
    ;


typeArgument
    : UpperID ( {notStartOfLine()}? type )*
        # ParameterTypeArgument
    | type
        # RecursiveTypeArgument
    ;


LowerID
    : [a-z][a-zA-Z0-9_]*[']*
    ;

UpperID
    : [A-Z][a-zA-Z0-9_]*[']*
    ;

ConstantInt
    : [0-9]+
    ;

ConstantString
    : '"' ( ESC | ~[\\"] )*? '"'
    ;

fragment ESC
    : '\\' ('"' | '\\')
    ;

WS
    : [ \t\n]+ -> skip
    ;