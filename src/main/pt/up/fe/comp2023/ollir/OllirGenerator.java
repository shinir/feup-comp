package pt.up.fe.comp2023.ollir;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class OllirGenerator extends AJmmVisitor<OllirType, String> {
    private final StringBuilder ollirCode;
    private final SymbolTable symbolTable;
    private int indentationLevel;
    private int tempVarNum;
    private int ifThenElseNum;
    private int whileNum;

    public OllirGenerator(SymbolTable symbolTable) {
        this.ollirCode = new StringBuilder();
        this.symbolTable = symbolTable;
        this.indentationLevel = 0;
        this.tempVarNum = 0;
        this.ifThenElseNum = 1;
        this.whileNum = 1;

        addVisit("Program", this::programVisit);
        addVisit("ClassDeclaration", this::classDeclVisit);
        addVisit("MethodDeclaration", this::methodDeclVisit);
        addVisit("MainMethodDeclaration", this::methodDeclVisit);
        addVisit("MethodCall", this::methodCallVisit);
        addVisit("MethodCall", this::methodCallVisit);
        addVisit("BinOp", this::binOpVisit);
        addVisit("Assignment", this::assignmentVisit);
        addVisit("MethodArguments", this::methodArgsVisit);
        addVisit("IntegerLiteral", this::intLiteralVisit);
        addVisit("BooleanLiteral", this::booleanVisit);
        addVisit("Id", this::idVisit);
        addVisit("Index", this::indexVisit);
        addVisit("Length", this::lengthVisit);
        addVisit("IfStatement", this::ifStatementVisit);
        addVisit("WhileStatement", this::whileStatementVisit);
        addVisit("ScopeStatement", this::scopeStatementVisit);
        addVisit("This", this::thisVisit);
        addVisit("Not", this::notVisit);
        addVisit("Return", this::returnVisit);

    }

    public String getCode() {
        return ollirCode.toString();
    }

    private String programVisit(JmmNode start, OllirType var) {

    }

    private String classDeclVisitVisit(JmmNode classDecl, OllirType var) {
    }


}
