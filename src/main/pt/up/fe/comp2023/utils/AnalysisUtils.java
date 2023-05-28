package pt.up.fe.comp2023.utils;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

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
            StringBuilder signature = new StringBuilder();
            signature.append(jmmNode.get("funcName"));
            for (int idx = 1; idx < jmmNode.getChildren().size(); idx++){
                signature.append("#");
                signature.append(getType(jmmNode.getJmmChild(idx), symbolTable).getName());
            }
            return symbolTable.getReturnType(signature.toString());
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
            JmmNode node = jmmNode;
            while (node != null) {
                if (node.getKind().equals("MethodDeclaration")) break;
                if (node.getKind().equals("Class")) break;
                node = node.getJmmParent();
            }
            if (node == null) return null;
            if (node.getKind().equals("MethodDeclaration")){
                node = node.getJmmChild(0);
                String signature = node.get("signature");

                for (Symbol symbol : symbolTable.getParameters(signature.toString())){
                    if (jmmNode.get("name").equals(symbol.getName())) return symbol.getType();
                }
                for (Symbol symbol : symbolTable.getLocalVariables(signature.toString())){
                    if (jmmNode.get("name").equals(symbol.getName())) return symbol.getType();
                }
                while (node.getKind().equals("Class")) node = node.getJmmParent();
            }
            if (node.getKind().equals("Class")){
                for (Symbol symbol : symbolTable.getFields()){
                    if (jmmNode.get("name").equals(symbol.getName())) return symbol.getType();
                }
            }
        }
        if (jmmNode.getKind().equals("Assignment") || jmmNode.getKind().equals("ArrayAssignment")){
            JmmNode node = jmmNode;
            while (node != null) {
                if (node.getKind().equals("MethodDeclaration")) break;
                if (node.getKind().equals("Class")) break;
                node = node.getJmmParent();
            }
            if (node == null) return null;
            if (node.getKind().equals("MethodDeclaration")){
                node = node.getJmmChild(0);

                String signature = node.get("signature");

                for (Symbol symbol : symbolTable.getParameters(signature)){
                    if (jmmNode.get("value").equals(symbol.getName())) return symbol.getType();
                }
                for (Symbol symbol : symbolTable.getLocalVariables(signature)){
                    if (jmmNode.get("value").equals(symbol.getName())) return symbol.getType();
                }
                while (node.getKind().equals("Class")) node = node.getJmmParent();
            }
            if (node.getKind().equals("Class")){
                for (Symbol symbol : symbolTable.getFields()){
                    if (jmmNode.get("value").equals(symbol.getName())) return symbol.getType();
                }
            }
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
