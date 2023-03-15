grammar Javamm;

@header {
    package pt.up.fe.comp2023;
}

BOOL : 'true' | 'false' ;
INT : [-]*[0-9][0-9]* ;
ID : [a-zA-Z_$][a-zA-Z_$0-9]* ;
WS : [ \n\t\r\f]+ -> skip ;
SLC : '//' ~[\n]* -> skip;
MLC : '/*' .*? '*/' -> skip;

program
    :  importDeclaration* classDeclaration  EOF #ProgramDeclaration
    ;

importDeclaration
    : 'import' className+=ID ( '.' className+=ID )* ';' #Import
    ;

classDeclaration
    : 'class' className=ID ( 'extends' superClass=ID )? '{' ( varDeclaration )* ( methodDeclaration )* '}' #Class
    ;

varDeclaration
    : type var=ID ';' #Var
    ;

methodDeclaration
    : ('public')? type functName=ID '(' ( type var=ID ( ',' type var=ID )* )? ')' '{' ( varDeclaration )* ( statement )* 'return' expression ';' '}' #FunctionMethod
    | ('public')? 'static' 'void' 'main' '(' 'String' '[' ']' var=ID ')' '{' ( varDeclaration )* ( statement )* '}' #MainMethod
    ;

type
    : 'int' '[' ']' #Array
    | 'boolean' #Boolean
    | 'int' #Integer
    | 'char' #Character
    | 'String' #String
    | var=ID #Literal
    ;

statement
    : '{' ( statement )* '}' #Scope
    | 'if' '(' expression ')' statement 'else' statement #IfCondition
    | 'while' '(' expression ')' statement #WhileCondition
    | expression ';' #ExprStmt
    | ID '=' expression ';' #Assignment
    | ID '[' expression ']' '=' expression ';' #ArrayAssignment
    ;

expression
    : '!' expression #Not
    | '(' expression ')' #Parenthesis
    | expression ( op='*' | op='/' ) expression #BinaryOp
    | expression ( op='+' | op='-' ) expression #BinaryOp
    | expression ( op='<' | op='>' | op='<=' | op='>=' ) expression #BinaryOp
    | expression ( op='&&' | op='||' ) expression #BinaryOp
    | expression '[' expression ']' #ArrayAccess
    | expression '.' 'length' #GetLength
    | expression '.' functName=ID '(' ( expression ( ',' expression )* )? ')' #CallFunction
    | 'new' 'int' '[' expression ']' #NewArray
    | 'new' var=ID '(' ')' #NewVar
    | expression WS expression #NLExpression
    | value=BOOL #Bool
    | value=INT #Int
    | var=ID #Variable
    | 'this' #This
    ;