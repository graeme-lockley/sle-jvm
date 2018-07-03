grammar Parser;


module
    : declaration*
    ;

declaration
    : 'let' LowerID+ '=' expression
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

LowerID
    : [a-z][a-zA-Z0-0_']*
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