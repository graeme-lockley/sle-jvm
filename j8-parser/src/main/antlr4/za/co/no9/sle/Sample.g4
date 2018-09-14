grammar Sample;


@parser::members {
    public boolean notDefinitionThingy() {
        return getCurrentToken().getCharPositionInLine() > 0;
    }
}


module
    : declaration*
    ;

declaration
    : LowerID+ '=' expression
        # LetDeclaration
    ;

expression
    : expression op=('+' | '-') expression
        # AdditiveExpression
    | term ( {notDefinitionThingy()}? term ) *
        # CallExpression
    ;


term
    : '(' expression ')'
        # ParenExpression
    | ConstantInt
        # ConstantIntExpression
    | LowerID
        # LowerIDExpression
    | '(' ')'
        # UnitValueExpression
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