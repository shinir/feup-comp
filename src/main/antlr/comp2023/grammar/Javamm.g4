grammar Javamm;

@header {
    package pt.up.fe.comp2023;
}

BOOL : 'true' | 'false' ;
INTEGER : [0] | [1-9][0-9]* ;
ID : [a-zA-Z][a-zA-Z_$0-9]* ;
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
    : type name=ID ';'
    ;

methodDeclaration
    : mainMethodDeclaration
    | functionMethodDeclaration
    ;

functionMethodDeclaration
    : ('public')? type funcName=ID '(' ( parameter ( ',' parameter )* )? ')' '{' ( varDeclaration )* ( statement )* 'return' expression ';' '}'
    ;

mainMethodDeclaration
     : ('public')? 'static' 'void' 'main' '(' 'String' '[' ']' name=ID ')' '{' ( varDeclaration )* ( statement )* '}'
     ;

parameter
  : type name=ID
  ;

type
    : name = 'int' '[' ']' #Array
    | name = 'boolean' #Boolean
    | name = 'int' #Integer
    | name = 'char' #Character
    | name = 'String' #String
    | name = ID #Literal
    | name = 'void' #Void
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
    | value=INTEGER #Int
    | name=ID #Variable
    | 'this' #This
    ;
