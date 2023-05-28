package pt.up.fe.comp2023.ollir;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class OllirGenerator extends AJmmVisitor<String, OllirExpression> {
    private final StringBuilder ollirCode;
    private final SymbolTable symbolTable;
    private static int tempVarNum;
    private static int whileCounter;
    private static int ifCounter;

    public OllirGenerator(SymbolTable symbolTable) {
        this.ollirCode = new StringBuilder();
        this.symbolTable = symbolTable;
        this.tempVarNum = 0;
        this.whileCounter = 0;
        this.ifCounter = 0;
    }

    public static String nextTemp(){
        return "t" + (tempVarNum++);
    }

    public static String nextWhile(){
        return "" + (whileCounter++);
    }

    public static String nextIf(){
        return "" + (ifCounter++);
    }

    @Override
    protected void buildVisitor() {
        addVisit("Program", this::programVisit);
        addVisit("ClassDeclaration", this::classDeclVisit);
        addVisit("VariableDeclaration", this::varDeclVisit);
        addVisit("MethodDeclaration", this::methodDeclVisit);
        addVisit("CallFunction", this::methodCallVisit);
        addVisit("NewObj", this::newObjVisit);
        addVisit("BinaryOp", this::binOpVisit);
        addVisit("Assignment", this::assignmentVisit);
        addVisit("Integer", this::intLiteralVisit);
        addVisit("Bool", this::booleanVisit);
        addVisit("Variable", this::idVisit);
        addVisit("ReturnExpression", this::returnVisit);
        addVisit("ExprStmt", this::expressionStmtVisit);
        addVisit("ArrayAccess", this::arrayAccessVisit);
        addVisit("ArrayAssignment", this::arrayAssignVisit);
        addVisit("NewArray", this::newArrayVisit);
        addVisit("GetLength", this::arrayLengthVisit);
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
            //System.out.println("field" + field);
            //System.out.println("type" + field.getType());
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

            ollirCode.append(parametersCode).append(").");

            ollirCode.append((Boolean.parseBoolean(methodDecl.getJmmChild(0).getJmmChild(0).get("isArray")) ? "array." : "") + ollirUtils.getOllirType(methodDecl.getJmmChild(0).getJmmChild(0).get("name")));

        }
        ollirCode.append(" {\n");

        for (JmmNode methodChild: methodDecl.getJmmChild(0).getChildren()) {
            var stats = visit(methodChild);
            ollirCode.append(stats.prefix);
        }

        if (methodDecl.getJmmChild(0).getKind().equals("MainMethodDeclaration")) {
            ollirCode.append("ret.V;\n");
        }

        ollirCode.append("}\n\n");

        return new OllirExpression("", "");
    }

    private OllirExpression methodCallVisit(JmmNode methodCall, String var){
        StringBuilder code = new StringBuilder();
        String value = "";
        boolean isAssign = methodCall.getAncestor("Assignment").isPresent();

        List<String> params = new ArrayList<>();
        for (int i=1;i<methodCall.getNumChildren();i++){
            var paramCode = visit(methodCall.getJmmChild(1));
            code.append(paramCode.prefix);
            params.add(paramCode.value);
        }

        // Verify function caller - Static or Virtual
        if (methodCall.getJmmChild(0).getKind().equals("Variable") && (symbolTable.getImports().contains(methodCall.getJmmChild(0).get("name")) || (symbolTable.getSuper() != null && symbolTable.getSuper().equals(methodCall.getJmmChild(0).get("name"))))){
            // It is a static function
            code.append("invokestatic(").append(methodCall.getJmmChild(0).get("name")).append(", \"").append(methodCall.get("functName")).append("\"");
            for (String p: params) {
                code.append(", ").append(p);
            }
            code.append(").V;\n");
        } else {
            value = nextTemp() + ".i32"; String type = "";
            // It is a virtual function
            String temp = nextTemp();
            value = temp + ".i32";
            //code.append(value).append(" :=.i32 ");

            code.append("invokevirtual(").append(methodCall.getJmmChild(0).get("name")).append(", ").append("\"").append(methodCall.get("functName")).append("\"");
            for (String p: params) {
                code.append(", ").append(p);
            }
            code.append(").i32;\n");
        }

        return new OllirExpression(code.toString(), value);
    }

    private OllirExpression newObjVisit(JmmNode jmmNode, String var){
        StringBuilder newObject = new StringBuilder();
        Type objType = ollirUtils.getType(jmmNode);

        newObject.append("new(").append(jmmNode.get("type")).append(").").append(ollirUtils.getCode(new Symbol(objType, "objType")));

        Type call = ollirUtils.getType(jmmNode);
        String temp =  nextTemp();

        ollirCode.append(temp).append(" :=.").append(ollirUtils.getCode(new Symbol(objType, "objType"))).append(" ").append(newObject).append(";\n");

        ollirCode.append("invokespecial(").append(temp).append(",\"<init>\").V;\n");

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

        code.append(temporary).append(opType).append(" :=").append(opType).append(" ").append(lhs.value).append(" ").append(op).append(" ").append(rhs.value).append(";\n");

        return new OllirExpression(code.toString(), temporary.append(opType).toString());
    }

    private OllirExpression assignmentVisit(JmmNode assign, String var){
        StringBuilder code = new StringBuilder();
        // Get rhs prefix code first
        var rhsCode = visit(assign.getJmmChild(0));
        code.append(rhsCode.prefix);

        // Get lhs code
        code.append(assign.get("value")).append(".").append(ollirUtils.getCode(getVarType(assign.get("value"), getMethod(assign)), false));

        // Get assign type
        code.append(" :=");
        code.append(".").append(ollirUtils.getCode(getVarType(assign.get("value"), getMethod(assign)), false)).append(" ");

        // Get rhs code
        code.append(rhsCode.value).append(";\n");

        return new OllirExpression(code.toString(), "");
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

        ollirCode.append(retExpr.prefix);

        Type retType = ollirUtils.getReturnType(ret);
        ollirCode.append("ret.").append(ollirUtils.getCode(retType, false)).append(" ");

        ollirCode.append(retExpr.value).append(";\n");

        return new OllirExpression("", "");
    }

    private OllirExpression expressionStmtVisit(JmmNode exprStmt, String var){

        var expr = visit(exprStmt.getJmmChild(0));

        return new OllirExpression(expr.prefix, "");
    }

    private OllirExpression arrayAccessVisit(JmmNode arrayAccess, String var) {
        StringBuilder code = new StringBuilder();
        OllirExpression expr = visit(arrayAccess.getJmmChild(1));

        code.append(expr.prefix);

        // Create temp
        String temp = nextTemp();
        temp += ".i32";

        code.append(temp).append(" :=.i32 ");

        String child_node = visit(arrayAccess.getJmmChild(0)).value;

        code.append(child_node).append("[").append(expr.value).append("].i32;\n");

        return new OllirExpression(code.toString(), temp);
    }

    private OllirExpression ifElseVisit(JmmNode ifElse, String var){
        StringBuilder code = new StringBuilder();

        OllirExpression condition = visit(ifElse.getJmmChild(0));
        OllirExpression ifScope = visit(ifElse.getJmmChild(1).getJmmChild(0));
        OllirExpression elseScope = visit(ifElse.getJmmChild(2).getJmmChild(0));

        // Get if counter
        String ifCount = nextIf();

        code.append(condition.prefix);

        code.append("if (").append(condition.value).append(") goto ").append("if_then_").append(ifCount).append(";\n");

        code.append(ifScope.prefix);

        code.append("goto ").append("if_end_").append(ifCount).append(";\n").append("if_then_").append(ifCount).append(":\n");

        code.append(elseScope.prefix);

        code.append("if_end_").append(ifCount).append(":\n");

        return new OllirExpression(code.toString(), "");
    }

    private OllirExpression whileLoopVisit(JmmNode whileLoop, String var){
        StringBuilder code = new StringBuilder();
        OllirExpression condition = visit(whileLoop.getJmmChild(0));
        OllirExpression whileScope = visit(whileLoop.getJmmChild(1).getJmmChild(0));

        String whileCounter = nextWhile();
        code.append("goto while_cond_" + whileCounter + ";\n");
        code.append("while_body_" + whileCounter + ":\n");

        code.append(whileScope.prefix);

        code.append("while_cond_" + whileCounter + ":\n");

        code.append(condition.prefix);

        code.append("if (" + condition.value + ") goto while_body_" + whileCounter + ";\n");

        return new OllirExpression(code.toString(), "");
    }

    private OllirExpression arrayAssignVisit(JmmNode assign, String var){
        StringBuilder code = new StringBuilder();

        var lhsCode = visit(assign.getJmmChild(0));
        var rhsCode = visit(assign.getJmmChild(1));

        code.append(lhsCode.prefix);
        code.append(rhsCode.prefix);

        //String temp = nextTemp(); temp += ".i32";
        //code.append(temp).append(" :=.i32 ");
        code.append(assign.get("value")).append("[").append(lhsCode.value).append("]").append(".i32 :=.i32 ").append(rhsCode.value).append(";\n");

        return new OllirExpression(code.toString(),"");
    }

    private OllirExpression newArrayVisit(JmmNode newArray, String var){
        StringBuilder code = new StringBuilder();
        String temp = nextTemp(); temp = temp + ".i32";

        var expr = visit(newArray.getJmmChild(0));

        code.append(expr.prefix);
        code.append(temp).append(" :=.i32 ").append(expr.value).append(";\n");

        String temp2 = nextTemp() + ".array.i32";

        code.append(temp2).append(" :=.array.i32 new(array, ").append(temp).append(").array.i32;\n");

        return new OllirExpression(code.toString(), temp2);
    }

    private OllirExpression arrayLengthVisit(JmmNode arrayLength, String var){
        StringBuilder code = new StringBuilder();
        var expr = visit(arrayLength.getJmmChild(0));

        String temp = nextTemp(); temp += ".i32";

        code.append(expr.prefix);
        code.append(temp).append(" :=.i32 ").append("arraylength(").append(expr.value).append(").i32;\n");

        return new OllirExpression(code.toString(),temp);
    }

    // Auxiliar Functions
    public String getCode() {
        return ollirCode.toString();
    }

    private String getMethod(JmmNode jmmNode){
        JmmNode node = jmmNode;
        while(!(node.getKind().equals("MethodDeclaration"))){
            if(node.getKind().equals("MainMethodDeclaration")) {
                return "main#String[]";
            }
            node = node.getJmmParent();
        }

        return node.getJmmChild(0).get("signature");
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