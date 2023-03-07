grammar Javamm;

@header {
    package pt.up.fe.comp2023;
}

INT : [0-9]+ ;
ID : [a-zA-Z_][a-zA-Z_0-9]* ;

WS : [ \t\n\r\f]+ -> skip ;

program
    : (importDeclaration)* classDeclaration EOF
    ;

importDeclaration
    : 'import' var=ID ( '.' var=ID )* ';'
    ;

classDeclaration
    : 'class' var=ID ( 'extends' var=ID )? '{' ( varDeclaration )* ( methodDeclaration )* '}'
    ;

varDeclaration
    : type var=ID ';'
    ;

methodDeclaration
    : ('public')? type var=ID '(' ( type var=ID ( ',' type var=ID )* )? ')' '{' ( varDeclaration )* ( statement )* 'return' expression ';' '}' #FunctionMethod
    | ('public')? 'static' 'void' 'main' '(' 'String' '[' ']' var=ID ')' '{' ( varDeclaration )* ( statement )* '}' #MainMethod
    ;

type
    : 'int' '[' ']' #Array
    | 'boolean' #Boolean
    | 'int' #Integer
    | 'String' #String
    | var=ID #Identifier
    ;

statement
    : '{' ( statement )* '}'
    | 'if' '(' expression ')' statement 'else' statement
    | 'while' '(' expression ')' statement
    | expression ';'
    | ID '=' expression ';'
    | ID '[' expression ']' '=' expression ';'
    ;

SLC
    : '//' ~[\n]*
    ;

MLC
    : '/*' .*? '*/'
    ;

expression
    : '!' expression #Negative
    | '(' expression ')' #Parenthesis
    | expression ( op='&&' | op='||' ) expression #BinaryOp
    | expression ( op='<' | op='>' | op='<=' | op='>=' ) expression #BinaryOp
    | expression ( op='*' | op='/' ) expression #BinaryOp
    | expression ( op='+' | op='-' ) expression #BinaryOp
    | expression '[' expression ']' #GetArray
    | expression '.' 'length' #GetLength
    | expression '.' var=ID '(' ( expression ( ',' expression )* )? ')' #CallFunction
    | 'new' 'int' '[' expression ']' #NewArray
    | 'new' var=ID '(' ')' #NewVar
    | value=INT #Int
    | 'true' #True
    | 'false' #False
    | var=ID #Id
    | 'this' #This
    ;