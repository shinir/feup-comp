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

public class OllirGenerator extends AJmmVisitor<String, OllirExpression> {
    private final StringBuilder ollirCode;
    private final SymbolTable symbolTable;
    private static int tempVarNum;
    private static int whileCounter;

    public OllirGenerator(SymbolTable symbolTable) {
        this.ollirCode = new StringBuilder();
        this.symbolTable = symbolTable;
        this.tempVarNum = 0;
        this.whileCounter = 0;
    }

    public static String nextTemp(){
        return "t" + (tempVarNum++);
    }

    public static String nextWhile(){
        return "" + (whileCounter++);
    }

    @Override
    protected void buildVisitor() {
        addVisit("Program", this::programVisit);
        addVisit("ClassDeclaration", this::classDeclVisit);
        addVisit("VariableDeclaration", this::varDeclVisit);
        addVisit("MethodDeclaration", this::methodDeclVisit);
        addVisit("MethodCall", this::methodCallVisit);
        addVisit("NewObj", this::newObjVisit);
        addVisit("BinaryOp", this::binOpVisit);
        addVisit("Assignment", this::assignmentVisit);
        addVisit("Integer", this::intLiteralVisit);
        addVisit("Boolean", this::booleanVisit);
        addVisit("Variable", this::idVisit);
        addVisit("ReturnExpression", this::returnVisit);
        addVisit("ExpressionStatement", this::expressionStmtVisit);
        addVisit("ArrayAccess", this::arrayAccessVisit);
        addVisit("IfCondition", this::ifElseVisit);
        addVisit("WhileCondition", this::whileLoopVisit);
        setDefaultVisit(this::defaultVisit);
    }

    private OllirExpression defaultVisit(JmmNode jmmNode, String s) {
        return new OllirExpression("", "");
    }

    private OllirExpression programVisit(JmmNode start, String var) {
        for (String importString : symbolTable.getImports()) {
            ollirCode.append("import ").append(importString).append(";\n");
        }

        for (JmmNode child : start.getChildren()) {
            visit(child);
        }
        return new OllirExpression("","");
    }

    private OllirExpression classDeclVisit(JmmNode classDecl, String var) {
        ollirCode.append(symbolTable.getClassName() + (symbolTable.getSuper() != null ? " extends " + symbolTable.getSuper() : "") + '{' + '\n');

        /*var superClass = symbolTable.getSuper();

        if (superClass != null) {
            ollirCode.append(" extends ").append(superClass);
        }

        ollirCode.append(" {");*/

        for (var field : symbolTable.getFields()) {
            System.out.println("field" + field);
            System.out.println("type" + field.getType());
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

        return new OllirExpression("", "");
    }

    private OllirExpression varDeclVisit(JmmNode varDecl, String var){
        if(!varDecl.getJmmParent().getKind().equals("Class")){
            return new OllirExpression("", "");
        }
        Type type = ollirUtils.getType(varDecl.getJmmChild(0));

        ollirCode.append(".field private ").append(ollirUtils.getCode(new Symbol(type, varDecl.get("value")))).append(";\n");
        return new OllirExpression("", "");
    }

    private OllirExpression methodDeclVisit(JmmNode methodDecl, String var){
        ollirCode.append(".method public ");

        if (methodDecl.getJmmChild(0).getKind().equals("MainMethodDeclaration")){
            ollirCode.append("static main (args.array.String).V");
        }
        else {
            ollirCode.append(methodDecl.getJmmChild(0).get("funcName")).append("(");
            var parameters = symbolTable.getParameters(methodDecl.getJmmChild(0).get("funcName"));
            var parametersCode = parameters.stream().map(ollirUtils::getCode).collect(Collectors.joining(", "));
            // int.a --> a.array.i32
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

        ollirCode.append("}\n\n");

        return new OllirExpression("", "");
    }

    private OllirExpression methodCallVisit(JmmNode methodCall, String var){
        return new OllirExpression("", "");

    }

    private OllirExpression newObjVisit(JmmNode newObj, String var){
        return new OllirExpression("", "");

    }

    private OllirExpression binOpVisit(JmmNode binOp, String var){
        OllirExpression lhs = visit(binOp.getJmmChild(0));
        OllirExpression rhs = visit(binOp.getJmmChild(1));
        StringBuilder code = new StringBuilder();

        String opType = ollirUtils.getOllirOp(binOp.get("op"));
        String op = binOp.get("op") + opType;
        StringBuilder temporary = new StringBuilder(nextTemp());
                
        code.append(lhs.prefix);
        code.append(rhs.prefix);

        ollirCode.append(temporary).append(opType).append(" :=").append(opType).append(" ").append(lhs.value).append(" ").append(op).append(" ").append(rhs.value).append(";\n");

        return new OllirExpression(code.toString(), temporary.append(opType).toString());
    }

    private OllirExpression assignmentVisit(JmmNode assign, String var){
        // Get rhs prefix code first
        var rhsCode = visit(assign.getJmmChild(0));
        ollirCode.append(rhsCode.prefix);

        // Get lhs code
        ollirCode.append(assign.get("value")).append(".").append(ollirUtils.getCode(getVarType(assign.get("value"), getMethod(assign)), false));

        // Get assign type
        ollirCode.append(" :=");
        ollirCode.append(".").append(ollirUtils.getCode(getVarType(assign.get("value"), getMethod(assign)), false)).append(" ");

        // Get rhs code
        //System.out.println(rhsCode.value);
        /*String nodeType = assign.getJmmChild(0).getKind();
        if(nodeType.equals("Variable")){
            Type varType = getVarType(assign.getJmmChild(0).get("name"), getMethod(assign));
            ollirCode.append(assign.getJmmChild(0).get("name") + "." + ollirUtils.getCode(varType, false) + ";\n");
        } else if(nodeType.equals("Array")) {
            ollirCode.append(assign.getJmmChild(0).get("name") + ".i32;\n");
        } else if(nodeType.equals("Integer")){
            ollirCode.append(assign.getJmmChild(0).get("value")).append(".i32;\n");
        }*/

        ollirCode.append(rhsCode.value).append(";\n");

        return new OllirExpression("", "");
    }

    private OllirExpression intLiteralVisit(JmmNode intLiteral, String var){
        return new OllirExpression("", intLiteral.get("value") + ".i32");
    }

    private OllirExpression booleanVisit(JmmNode bool, String var){
        if(bool.get("value").equals("true")){
            return new OllirExpression("", "1.bool");
        } else if(bool.get("value").equals("false")){
            return new OllirExpression("", "0.bool");
        }
        return new OllirExpression("", "");
    }

    private OllirExpression idVisit(JmmNode id, String var){
        // Get type from identifier
        String type = "." + ollirUtils.getCode(getVarType(id.get("name"), getMethod(id)), false);
        return new OllirExpression("", id.get("name") + type);
    }

    private OllirExpression returnVisit(JmmNode ret, String var){

        var retExpr = visit(ret.getJmmChild(0));
        System.out.println(ret.getJmmChild(0));
        ollirCode.append(retExpr.prefix);

        Type retType = ollirUtils.getReturnType(ret);
        ollirCode.append("ret.").append(ollirUtils.getCode(retType, false)).append(" ");

        ollirCode.append(retExpr.value).append(";\n");

        return new OllirExpression("", "");
    }

    private OllirExpression expressionStmtVisit(JmmNode exprStmt, String var){
        return new OllirExpression("", "");
    }

    private OllirExpression arrayAccessVisit(JmmNode arrayAccess, String var) {
        String child_node = arrayAccess.getJmmChild(0).get("name");
        OllirExpression expr = visit(arrayAccess.getJmmChild(0));

        ollirCode.append(expr.prefix).append(child_node).append("[").append(expr.value).append("].i32;\n");

        return new OllirExpression("", "");
    }

    private OllirExpression ifElseVisit(JmmNode ifElse, String var){
        String condition = visit(ifElse.getJmmChild(0)).value;
        String ifScope = visit(ifElse.getJmmChild(1)).value;
        String elseScope = visit(ifElse.getJmmChild(2)).value;

        JmmNode ifNode = ifElse.getJmmChild(1);
        JmmNode elseNode = ifElse.getJmmChild(2);

        ollirCode.append("if (").append(condition).append(") goto ").append(ifScope).append(";\n");

        visit(elseNode, var);

        ollirCode.append("goto ").append(ifScope).append(";\n").append(elseScope).append(":\n");

        ollirCode.append(ifScope).append(":\n");

        visit(elseNode, var);

        return new OllirExpression("", "");
    }

    private OllirExpression whileLoopVisit(JmmNode whileLoop, String var){
        OllirExpression condition = visit(whileLoop.getJmmChild(0));
        OllirExpression whileScope = visit(whileLoop.getJmmChild(1));

        String whileCounter = nextWhile();
        ollirCode.append("goto while_cond_" + whileCounter + ";\n");
        ollirCode.append("while_body_" + whileCounter + ":\n");

        ollirCode.append(whileScope.prefix);

        ollirCode.append("while_cond_" + whileCounter + ":\n");

        ollirCode.append(condition.prefix);

        ollirCode.append("if (" + condition.value + "goto while_body_" + whileCounter + ";\n");

        return new OllirExpression("", "");
    }

    // Auxiliar Functions
    public String getCode() {
        return ollirCode.toString();
    }

    private String getMethod(JmmNode jmmNode){
        JmmNode node = jmmNode;
        while(!(node.getKind().equals("MethodDeclaration"))){
            if(node.getKind().equals("MainMethodDeclaration")) {
                return "main";
            }
            node = node.getJmmParent();
        }
        return node.getJmmChild(0).get("funcName");
    }

    public Type getVarType(String var, String method) {

        for (Symbol field : symbolTable.getFields()) {
            if (field.getName().equals(var)) return field.getType();
        }
        for (Symbol param : symbolTable.getParameters(method)) {
            if (param.getName().equals(var)) return param.getType();
        }
        for (Symbol localVar : symbolTable.getLocalVariables(method)) {
            if (localVar.getName().equals(var)) return localVar.getType();
        }

        return null;
    }

}