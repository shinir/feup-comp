package pt.up.fe.comp2023.analysis.table;

import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;

public class Visitor extends PreorderJmmVisitor<MySymbolTable, Boolean> {
    private MySymbolTable symbolTable;

    public Visitor(MySymbolTable symbolTable) {
        this.symbolTable = symbolTable;
    }

    protected void buildVisitor() {
        //addVisit("ProgramDeclaration", this::dealWithProgram);
        addVisit("Import", this::dealWithImports);
        //addVisit("Class", this::dealWithClass);
        //addVisit("Assignment", this::dealWithAssignment);
        //addVisit("Integer", this::dealWithLiteral);
        //addVisit("Identifier", this::dealWithLiteral);
        //addVisit("ExprStmt", this::dealWithExprStmt);
        //addVisit("BinaryOp", this::dealWithBinaryOp);
        //add here other rules!
    }

    //private Boolean dealWithProgram(JmmNode jmmNode, MySymbolTable symbolTable) {}

    private Boolean dealWithImports(JmmNode jmmNode, MySymbolTable symbolTable) {
        symbolTable.setImports(jmmNode.get("name"));
        return true;
    }
/*
    private Boolean dealWithClass(JmmNode jmmNode, MySymbolTable symbolTable) {

    }

    private Boolean dealWithAssignment(JmmNode jmmNode, MySymbolTable symbolTable) {
    }

    private Boolean dealWithLiteral(JmmNode jmmNode, MySymbolTable symbolTable) {
    }

    private Boolean dealWithExprStmt(JmmNode jmmNode, MySymbolTable symbolTable) {
    }

    private Boolean dealWithBinaryOp(JmmNode jmmNode, MySymbolTable symbolTable) {
    }
*/
}
