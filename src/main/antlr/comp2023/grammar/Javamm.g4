grammar Javamm;

@header {
    package pt.up.fe.comp2023;
}

INT : [0-9]+ ;
ID : [a-zA-Z_][a-zA-Z_0-9]* ;
NL: '\n';
WS : [ \t\r\f]+ -> skip ;
SLC : '//' ~[\n]* ;
MLC : '/*' .*? '*/' ;
BOOL : 'true' | 'false' ;

program
    : ( declaration | statement )* EOF #ProgramDeclaration
    ;

declaration
    : importDeclaration | classDeclaration
    ;

importDeclaration
    : 'import' var=ID ( '.' var=ID )* ';' #Import
    ;

classDeclaration
    : 'class' var=ID ( 'extends' var=ID )? '{' ( varDeclaration )* ( methodDeclaration )* '}' #Class
    ;

varDeclaration
    : type var=ID ';' #Var
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
    : '{' ( statement )* '}' #Condition
    | 'if' '(' expression ')' statement 'else' statement #IfCondition
    | 'while' '(' expression ')' statement #WhileCondition
    | expression ';' #ExprCondition
    | ID '=' expression ';' #VarDeclare
    | ID '[' expression ']' '=' expression ';' #ArrayDeclare
    | NL #NewLine
    ;

expression
    : '!' expression #Negative
    | '(' expression ')' #Parenthesis
    | expression ( op='&&' | op='||' ) expression #BinaryOp
    | expression ( op='<' | op='>' | op='<=' | op='>=' ) expression #BinaryOp
    | expression ( op='*' | op='/' ) expression #BinaryOp
    | expression ( op='+' | op='-' ) expression #BinaryOp
    | expression '[' expression ']' #ExpressionRange
    | expression '.' 'length' #GetLength
    | expression '.' var=ID '(' ( expression ( ',' expression )* )? ')' #CallFunction
    | 'new' 'int' '[' expression ']' #NewArray
    | 'new' var=ID '(' ')' #NewVar
    | expression WS expression #NLExpression
    | value=INT #Int
    | 'true' #True
    | 'false' #False
    | var=ID #Id
    | 'this' #This
    ;