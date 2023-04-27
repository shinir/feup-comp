package pt.up.fe.comp2023.ollir;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;
import pt.up.fe.comp.jmm.ollir.OllirUtils;
import pt.up.fe.comp2023.ollir.OllirExpression;
import pt.up.fe.comp2023.MySymbolTable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class OllirGenerator extends AJmmVisitor<String, String> {
    private final StringBuilder ollirCode;
    private final SymbolTable symbolTable;
    private static int tempVarNum;

    public OllirGenerator(SymbolTable symbolTable) {
        this.ollirCode = new StringBuilder();
        this.symbolTable = symbolTable;
        this.tempVarNum = 0;
    }
    @Override
    protected void buildVisitor() {
        addVisit("Program", this::programVisit);
        addVisit("ClassDeclaration", this::classDeclVisit);
        addVisit("VariableDeclaration", this::varDeclVisit);
        addVisit("MethodDeclaration", this::methodDeclVisit);
        addVisit("MethodCall", this::methodCallVisit);
        addVisit("NewObj", this::newObjVisit);
        addVisit("BinOp", this::binOpVisit);
        addVisit("Assignment", this::assignmentVisit);
        addVisit("IntegerLiteral", this::intLiteralVisit);
        addVisit("BooleanLiteral", this::booleanVisit);
        addVisit("Id", this::idVisit);
        addVisit("Return", this::returnVisit);
        addVisit("ExpressionStatement", this::expressionStmtVisit);
        setDefaultVisit(this::defaultVisit);
    }

    private String defaultVisit(JmmNode jmmNode, String s) {
        return "";
    }

    public String getCode() {
        return ollirCode.toString();
    }

    private String getMethod(JmmNode jmmNode){
        JmmNode node = jmmNode;
        while(!(node.getKind().equals("MethodDeclaration"))){
            if(node.getKind().equals("MainMethodDeclaration")) {
                return node.get("value");
            }
            node = node.getJmmParent();
        }
        return node.getJmmChild(0).getJmmChild(0).get("value");
    }

        public static String nextTemp(){
            return "t" + (tempVarNum++);
        }

        public Type getVarType(String var, String method) {
        for(Symbol field: symbolTable.getFields()){
            if (field.getName().equals(var)) return field.getType();
        }
        for (Symbol param: symbolTable.getParameters(method)){
            if (param.getName().equals(var)) return param.getType();
        }
        for (Symbol localVar: symbolTable.getLocalVariables(method)){
            if (localVar.getName().equals(var)) return localVar.getType();
        }

        return null;
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
        ollirCode.append(symbolTable.getClassName() + (symbolTable.getSuper() != null ? " extends " + symbolTable.getSuper() : "") + '{' + '\n');

        /*var superClass = symbolTable.getSuper();

        if (superClass != null) {
            ollirCode.append(" extends ").append(superClass);
        }

        ollirCode.append(" {");*/

        for (var field : symbolTable.getFields()) {
            ollirCode.append(".field private " + field.getName() + (field.getType().isArray() ? ".array" : "") +  "." + ollirUtils.getOllirType(field.getType().getName()) + ";\n");
        }

        ollirCode.append(".construct" + symbolTable.getClassName() + "().V {\n invokespecial(this, \"<init>\").V;\n}\n");

        /*for (var child : classDecl.getChildren().subList(symbolTable.getFields().size(), classDecl.getNumChildren())) {
            System.out.println(classDecl.getChildren().subList(symbolTable.getFields().size(), classDecl.getNumChildren()));
            ollirCode.append("\n");
            visit(child);
        }*/
        for (var child: classDecl.getChildren()) {
            visit(child);
        }

        ollirCode.append("}");

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

        if (methodDecl.getJmmChild(0).getKind().equals("MainMethodDeclaration")){
            ollirCode.append("static main (args.array.String).V");
        }
        else {
            ollirCode.append(methodDecl.getJmmChild(0).get("funcName")).append("(");
            var parameters = symbolTable.getParameters(methodDecl.getJmmChild(0).get("funcName"));

            var parametersCode = parameters.stream().map(ollirUtils::getCode).collect(Collectors.joining(", "));

            ollirCode.append(parametersCode).append(").");

            ollirCode.append(ollirUtils.getOllirType(methodDecl.getJmmChild(0).getJmmChild(0).get("name")));

        }
        ollirCode.append(" {\n");

        for (JmmNode methodChild: methodDecl.getJmmChild(0).getChildren()) {
            visit(methodChild);
        }

        if (methodDecl.getJmmChild(0).getKind().equals("MainMethodDeclaration")) {
            ollirCode.append("ret.V;\n");
        }
        else {
           ollirCode.append("ret.").append(ollirUtils.getOllirType(methodDecl.getJmmChild(0).getJmmChild(0).get("name"))).append(";\n");
        }

        ollirCode.append("}\n\n");

        return "";
    }

    private String methodCallVisit(JmmNode methodCall, String var){
        return "";

    }

    private String newObjVisit(JmmNode newObj, String var){
        return "";

    }

    private String binOpVisit(JmmNode binOp, String var){
        OllirExpression lhs = new OllirExpression(visit(binOp.getJmmChild(0), var), var);
        OllirExpression rhs = new OllirExpression(visit(binOp.getJmmChild(1), var), var);
        StringBuilder code = new StringBuilder();
        String op = binOp.get("op") + ".i32";
        StringBuilder temporary = new StringBuilder(nextTemp()).append("i32");
                
        code.append(lhs.prefix);
        code.append(rhs.prefix);

        ollirCode.append(temporary).append(" :=.i32 ").append(lhs.value).append(" ").append(op).append(" ").append(rhs.value).append(";\n");
        return temporary.toString();
    }

    private String assignmentVisit(JmmNode assign, String var){
        ollirCode.append(assign.get("value")).append("." + ollirUtils.getCode(getVarType(assign.get("value"), getMethod(assign)), false));
        ;
        ollirCode.append(" :=");
        ollirCode.append(".").append(ollirUtils.getCode(getVarType(assign.get("value"), getMethod(assign)), false) + " ");
        if(assign.getJmmChild(0).getKind().equals("Variable")){
            ollirCode.append(assign.getJmmChild(0).get("value") + ";\n");
        }
        else{
            ollirCode.append(assign.getJmmChild(0).get("value")).append(".i32;\n");
        }
        return "";
    }

    private String intLiteralVisit(JmmNode intLiteral, String var){
        return intLiteral.get("value") + ".i32";
    }

    private String booleanVisit(JmmNode bool, String var){
        if(bool.get("value").equals("true")){
            return "1.bool";
        } else if(bool.get("value").equals("false")){
            return "0.bool";
        }
        return "";
    }

    private String idVisit(JmmNode id, String var){
        // Get type from identifier
        String type = ollirUtils.getCode(getVarType(id.get("name"), getMethod(id)), false);
        return id.get("name") + type;
    }

    private String returnVisit(JmmNode ret, String var){
        ollirCode.append(ret.get("value"));
        for (Symbol localVar : symbolTable.getLocalVariables(getMethod(ret))){
            if (ret.get("value").equals(localVar.getName())){
                ollirCode.append(ollirUtils.getCode(localVar.getType(), false));
            }
        }
        return "";
    }

    private String expressionStmtVisit(JmmNode exprStmt, String var){
        return "";
    }

}
