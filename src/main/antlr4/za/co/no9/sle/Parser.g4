grammar Parser;

module
    : declaration*
    ;

declaration
    : LowerID+ '=' expression
    ;

expression
    : 'if' expression 'then' expression 'else' expression
    | LowerID+ '->' expression
    | expression '||' expression
    | expression '&&' expression
    | expression ('==' | '!=' | '<=' | '<' | '>=' | '>') expression
    | expression ('+' | '-') expression
    | expression ('*' | '/') expression
    | factor expression*
    ;

factor
    : '(' expression ')'
    | ConstantInt
    | 'true'
    | 'false'
    | ConstantString
    | '!' expression
    | LowerID
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
    : '\\' ('"' | '\\') '\\'
    ;

WS
    : [ \t\n]+ -> skip
    ;