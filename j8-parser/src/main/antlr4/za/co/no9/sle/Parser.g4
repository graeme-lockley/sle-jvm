grammar Parser;


@parser::members {
    public boolean notStartOfLine() {
        return getCurrentToken().getCharPositionInLine() > 0;
    }
}


module
    : declaration* EOF
    ;


declaration
    : 'typealias' UpperID '=' type
    | 'type' UpperID LowerID* '=' typeConstructor ( '|' typeConstructor )*
    | LowerID ':' type
    | LowerID+ '=' expression
    | LowerID+ ( '|' expression '=' expression)+
    ;


typeConstructor
    : UpperID type*
    ;


expression
    : caseExpression
    ;

caseExpression
    : 'case' expression 'of' caseItem+
    | multiplicativeExpression
    ;

caseItem
    : pattern '->' expression
    ;

multiplicativeExpression
    : additiveExpression ('*' | '/') additiveExpression
    | additiveExpression
    ;

additiveExpression
    : relationOpExpression ('+' | '-') relationOpExpression
    | relationOpExpression
    ;

relationOpExpression
    : booleanAndExpression ('==' | '!=' | '<=' | '<' | '>=' | '>') booleanAndExpression
    | booleanAndExpression
    ;

booleanAndExpression
    : booleanOrExpression '&&' booleanOrExpression
    | booleanOrExpression
    ;

booleanOrExpression
    : lambdaExpression '||' lambdaExpression
    | lambdaExpression
    ;

lambdaExpression
    : '\\' LowerID+ '->' expression
    | ifExpression
    ;

ifExpression
    : 'if' expression 'then' expression 'else' expression
    | callExpression
    ;

callExpression
    : term+
    ;

term
    : '(' expression ')'
    | ConstantInt
    | 'True'
    | 'False'
    | ConstantString
    | '!' expression
    | LowerID
    | UpperID
    | '()'
    ;


pattern
    : constructorPattern
    ;

constructorPattern
    : UpperID constructorArgumentPattern*
    | termPattern
    ;

constructorArgumentPattern
    : UpperID
    | termPattern
    ;

termPattern
    : ConstantInt
    | 'True'
    | 'False'
    | ConstantString
    | '()'
    | LowerID
    | '(' pattern ')'
    ;


type
    : adtType ( '->' type )+
    ;

adtType
    : UpperID adtType*
    | termType
    ;

termType
    : LowerID
    | '(' type ')'
    | '(' ')'
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