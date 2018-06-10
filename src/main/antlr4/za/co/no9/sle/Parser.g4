grammar Parser;

module
    : declaration*
    ;

declaration
    : LowerID+ '=' expression
    ;

expression
    : factor expression*
    | expression ('*' | '/') expression
    | expression ('+' | '-') expression
    | expression ('==' | '!=' | '<=' | '<' | '>=' | '>') expression
    | expression '&&' expression
    | expression '||' expression
    | LowerID+ '->' expression
    | 'if' expression 'then' expression 'else' expression
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