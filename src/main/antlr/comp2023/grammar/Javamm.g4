grammar Javamm;

@header {
    package pt.up.fe.comp2023;
}

BOOL : 'true' | 'false' ;
INT : [0-9]+ ;
ID : [a-zA-Z_$][a-zA-Z_$0-9]* ;
WS : [ \n\t\r\f]+ -> skip ;
SLC : '//' ~[\n]* -> skip;
MLC : '/*' .*? '*/' -> skip;

program
    :  importDeclaration* classDeclaration EOF #ProgramDeclaration
    ;

importDeclaration
    : 'import' importName+=ID ( '.' importName+=ID )* ';' #Import
    ;

classDeclaration
    : 'class' name=ID ( 'extends' superClass=ID )? '{' ( varDeclaration )* ( methodDeclaration )* '}' #Class
    ;

varDeclaration
    : type name=ID ';' #VariableDeclaration
    ;

methodDeclaration
    : ('public')? type functName=ID '(' ( type name=ID ( ',' type name=ID )* )? ')' '{' ( varDeclaration )* ( statement )* 'return' expression ';' '}' #FunctionMethod
    | ('public')? 'static' 'void' 'main' '(' 'String' '[' ']' functName=ID ')' '{' ( varDeclaration )* ( statement )* '}' #MainMethod
    ;

type
    : 'int' '[' ']' #Array
    | 'boolean' #Boolean
    | 'int' #Integer
    | 'char' #Character // fixed testIdStartingChar2
    | 'String' #String
    | literal=ID #Literal
    ;

statement
    : '{' ( statement )* '}' #Scope
    | 'if' '(' expression ')' statement 'else' statement #IfCondition
    | 'while' '(' expression ')' statement #WhileCondition
    | 'return' expression ';' #Return
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
    | 'new' name=ID '(' ')' #NewVar
    | expression WS expression #NLExpression
    | value=BOOL #Bool
    | value=INT #Int
    | name=ID #Variable
    | 'this' #This
    ;