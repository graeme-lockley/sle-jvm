grammar Parser;


module
    : declaration*
    ;

declaration
    : 'typealias' UpperID '=' schema
        # TypeAliasDeclaration
    | 'let' LowerID+ (':' schema)? '=' expression
        # LetDeclaration
    ;

expression
    : factor expression*
        # CallExpression
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
    ;

factor
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
    | type '|' type
        # BarType
    | <assoc=right> type '->' type
        # ArrowType
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