package pt.up.fe.comp2023.utils;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class AnalysisUtils {

    static final List<String> PRIMITIVES = Arrays.asList("int", "void", "boolean");
    static final List<String> ARITHMETIC_OP = Arrays.asList("+", "-", "*", "/");
    static final List<String> COMPARISON_OP = List.of("<", ">", ">=", "<=", "==", "!=");
    static final List<String> LOGICAL_OP = List.of("&&", "||");

    public Type getType(JmmNode jmmNode, SymbolTable symbolTable) {
        if (jmmNode.getKind().equals("VarDeclaration")){
            return new Type(jmmNode.getJmmChild(0).get("name"), Boolean.parseBoolean(jmmNode.getJmmChild(0).get("isArray")));
        }
        if (jmmNode.getKind().equals("MethodDeclaration")){
            return getType(jmmNode.getJmmChild(0), symbolTable);
        }
        if (jmmNode.getKind().equals("MainMethodDeclaration")){
            return new Type("void", false);
        }
        if (jmmNode.getKind().equals("FunctionMethodDeclaration")){
            return new Type(jmmNode.getJmmChild(0).get("name"), Boolean.parseBoolean(jmmNode.getJmmChild(0).get("isArray")));
        }
        if (jmmNode.getKind().equals("Parameter")){
            return new Type(jmmNode.getJmmChild(0).get("name"), Boolean.parseBoolean(jmmNode.getJmmChild(0).get("isArray")));
        }
        if (jmmNode.getKind().equals("ReturnExpression")){
            return getType(jmmNode.getJmmChild(0), symbolTable);
        }
        if (jmmNode.getKind().equals("Not")){
            return new Type("boolean", false);
        }
        if (jmmNode.getKind().equals("Parenthesis")){
            return getType(jmmNode.getJmmChild(0), symbolTable);
        }
        if (jmmNode.getKind().equals("ArrayAccess")){
            return new Type("int", false);
        }
        if (jmmNode.getKind().equals("GetLength")){
            return new Type("int", false);
        }
        if (jmmNode.getKind().equals("CallFunction")){

            //return symbolTable.getReturnType(signature.toString());
        }
        if (jmmNode.getKind().equals("BinaryOp")){
            if (ARITHMETIC_OP.contains(jmmNode.get("op"))) return new Type("int", false);
            if (COMPARISON_OP.contains(jmmNode.get("op"))) return new Type("boolean", false);
            if (LOGICAL_OP.contains(jmmNode.get("op"))) return new Type("boolean", false);
        }
        if (jmmNode.getKind().equals("NewArray")){
            return new Type("int", true);
        }
        if (jmmNode.getKind().equals("NewVar")){
            return new Type(jmmNode.get("name"), false);
        }
        if (jmmNode.getKind().equals("Bool")){
            return new Type("boolean", false);
        }
        if (jmmNode.getKind().equals("Integer")){
            return new Type("int", false);
        }
        if (jmmNode.getKind().equals("This")){
            return new Type(symbolTable.getClassName(), false);
        }
        if (jmmNode.getKind().equals("Variable")){

        }
        return null;
    }


    public Symbol getSymbol(JmmNode jmmNode) {
        String symbol = jmmNode.get("name");
        Type type = new Type(jmmNode.getJmmChild(0).get("name"),Boolean.parseBoolean(jmmNode.getJmmChild(0).get("isArray")));
        return new Symbol(type, symbol);
    }

    public void putType(JmmNode jmmNode, Type type) {
        jmmNode.put("type", type.getName());
        jmmNode.put("isArray", String.valueOf(type.isArray()));
    }
}
