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
    : 'typealias' UpperID '=' schema
        # TypeAliasDeclaration
    | LowerID+ (':' schema)? '=' expression
        # LetDeclaration
    ;

expression
    : expression op=('*' | '/') expression
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
    | '(' ')'
        # UnitValueExpression
    ;


schema
    : typeParameters? type
    ;


typeParameters
    : '<' typeParameter (',' typeParameter)* '>'
    ;


typeParameter
    : UpperID (':' type)?
    ;


type
    : UpperID
        # UpperIDType
    | '(' type ')'
        # NestedType
    | <assoc=right> type '->' type
        # ArrowType
    | '(' ')'
        # UnitType
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