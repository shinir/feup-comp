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

    static final List<String> ARITHMETIC_OP = Arrays.asList("+", "-", "*", "/");
    public Type getType(JmmNode jmmNode, SymbolTable symbolTable) {

        if (jmmNode.getKind().equals("VarDeclaration")){
            //symbol table check
        }
        if (jmmNode.getKind().equals("MainMethodDeclaration")){
            return new Type("void", false);
        }
        if (jmmNode.getKind().equals("FunctionMethodDeclaration")){
            //symbol table
        }

        if (jmmNode.getKind().equals("Parameter")){
            //symbol table
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
            //type of func
        }
        if (jmmNode.getKind().equals("BinaryOp")){
            if (ARITHMETIC_OP.contains( jmmNode.get("op"))){
                return new Type("int", false);
            }
            else return new Type("boolean", false);
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
        if (jmmNode.getKind().equals("Variable")){
            //
        }
        if (jmmNode.getKind().equals("This")){
            //
        }
        return getType(jmmNode.getJmmChild(0), symbolTable);
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
