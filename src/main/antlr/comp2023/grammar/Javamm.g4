grammar Javamm;

@header {
    package pt.up.fe.comp2023;
}

BOOL : 'true' | 'false' ;
INTEGER : [0] | [1-9][0-9]* ;
ID : [a-zA-Z_$][a-zA-Z_$0-9]* ;
WS : [ \n\t\r\f]+ -> skip ;
SLC : '//' ~[\n]* -> skip;
MLC : '/*' .*? '*/' -> skip;

program
    :  (importDeclaration)* classDeclaration EOF #ProgramDeclaration
    ;

importDeclaration
    : 'import' importName+=ID ( '.' importName+=ID )* ';' #Import
    ;

classDeclaration
    : 'class' name=ID ( 'extends' superClass=ID )? '{' ( varDeclaration )* ( methodDeclaration )* '}' #Class
    ;

varDeclaration
    : type name=ID ';'
    ;

methodDeclaration
    : mainMethodDeclaration
    | functionMethodDeclaration
    ;

functionMethodDeclaration
    : ('public')? type funcName=ID '(' ( parameter ( ',' parameter )* )? ')' '{' ( varDeclaration )* ( statement )* 'return' returnExpression ';' '}'
    ;

mainMethodDeclaration
     : ('public')? 'static' 'void' funcName='main' '(' 'String' '[' ']' name=ID ')' '{' ( varDeclaration )* ( statement )* '}'
     ;

parameter
    : type name=ID
    ;

returnExpression
    : expression
    ;

type locals[boolean isArray = false]
    : name = 'int' '[' ']' {$isArray = true;} #Array
    | name = 'boolean' #Boolean
    | name = 'int' #IntegerType
    | name = 'char' #Character
    | name = 'String' #String
    | name = ID #Literal
    | name = 'void' #Void
    ;

statement
    : '{' ( statement )* '}' #Scope
    | 'if' '(' expression ')' statement ('else' statement)? #IfCondition
    | 'while' '(' expression ')' statement #WhileCondition
    | expression ';' #ExprStmt
    | value = ID '=' expression ';' #Assignment
    | value = ID '[' expression ']' '=' expression ';' #ArrayAssignment
    ;

expression
    : '!' expression #Not
    | '(' expression ')' #Parenthesis
    | expression '[' expression ']' #ArrayAccess
    | expression '.' 'length' #GetLength
    | expression '.' functName=ID '(' ( expression ( ',' expression )* )? ')' #CallFunction
    | expression ( op='*' | op='/' ) expression #BinaryOp
    | expression ( op='+' | op='-' ) expression #BinaryOp
    | expression ( op='<' | op='>' | op='<=' | op='>=' | op='==' | op='!=' ) expression #BinaryOp
    | expression ( op='&&' | op='||' ) expression #BinaryOp
    | 'new' 'int' '[' expression ']' #NewArray
    | 'new' name=ID '(' ')' #NewVar
    | expression WS expression #NLExpression
    | value=BOOL #Bool
    | value=INTEGER #Integer
    | name=ID #Variable
    | name='this' #This
    ;
