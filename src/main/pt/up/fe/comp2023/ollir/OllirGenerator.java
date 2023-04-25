package pt.up.fe.comp2023.ollir;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;
import pt.up.fe.comp.jmm.ollir.OllirUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class OllirGenerator extends PreorderJmmVisitor<String, String> {
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

    }
    @Override
    protected void buildVisitor() {
        addVisit("Program", this::programVisit);
        addVisit("ClassDeclaration", this::classDeclVisit);
        addVisit("VariableDeclaration", this::varDeclVisit);
        addVisit("MethodDeclaration", this::methodDeclVisit);
        addVisit("MainMethodDeclaration", this::methodDeclVisit);
        //addVisit("MethodCall", this::methodCallVisit);
        //addVisit("NewObj", this::newObjVisit);
        //addVisit("BinOp", this::binOpVisit);
        //addVisit("Assignment", this::assignmentVisit);
        //addVisit("IntegerLiteral", this::intLiteralVisit);
        //addVisit("BooleanLiteral", this::booleanVisit);
        //addVisit("Id", this::idVisit);
        //addVisit("Return", this::returnVisit);
        //addVisit("ExpressionStatement", this::expressionStmtVisit);
        //setDefaultVisit(this::defaultVisit);
    }

    private String defaultVisit(JmmNode jmmNode, String s) {
        return "";
    }

    public String getCode() {
        return ollirCode.toString();
    }

    private String programVisit(JmmNode start, String var) {
        for (String importString : symbolTable.getImports()) {
            ollirCode.append("import ").append(importString).append(";\n");
        }

        for (JmmNode child : start.getChildren()) {
            visit(child);
        }
        return "";
    }

    private String classDeclVisit(JmmNode classDecl, String var) {
        ollirCode.append(symbolTable.getClassName() + '{' + '\n');

        var superClass = symbolTable.getSuper();

        if (superClass != null) {
            ollirCode.append(" extends ").append(superClass);
        }

        for (var field : symbolTable.getFields()) {
            ollirCode.append(".field private" + field.getName() + (field.getType().isArray() ? ".array" : "") +  "." + ollirUtils.getOllirType(field.getType().getName()));
        }

        ollirCode.append(".construct" + symbolTable.getClassName() + "().V {\n invokespecial(this, \"<init>\").V;\n}");

        for (JmmNode child : classDecl.getChildren()){
            visit(child);
        }

        ollirCode.append('}');

        return "";
    }

    private String varDeclVisit(JmmNode varDecl, String var){
        if(!varDecl.getJmmParent().getKind().equals("Class")){
            return "";
        }
        Type type = ollirUtils.getType(varDecl.getJmmChild(0));

        ollirCode.append(".field private ").append(ollirUtils.getCode(new Symbol(type, varDecl.get("value")))).append(";\n");
        return "";
    }

    private String methodDeclVisit(JmmNode methodDecl, String var){
        ollirCode.append(".method public ");

        if (methodDecl.getKind().equals("MainMethodDeclaration")){
            ollirCode.append("static ");
        }

        ollirCode.append(methodDecl.get("name")).append("(");

        var parameters = symbolTable.getParameters(methodDecl.get("name"));

        var parametersCode = parameters.stream().map(ollirUtils::getCode).collect(Collectors.joining(", "));

        //ollirCode.append(parametersCode).append(").").append(ollirUtils.getOllirType(symbolTable.getReturnType())));

        ollirCode.append(" {\n");

        for (JmmNode methodChild: methodDecl.getChildren()) {

        }

        if (methodDecl.getKind().equals("MainMethodDeclaration")) {
            ollirCode.append("ret.V;\n");
        }

        ollirCode.append("}\n\n");

        return "";
    }

}
