package pt.up.fe.comp2023.utils;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;

import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class AnalysisUtils {
    static final List<String> ARITHMETIC_OP = Arrays.asList("+", "-", "*", "/");

    static final List<String> LOGICAL_OP = List.of("&&");

    public Type getType(JmmNode jmmNode, SymbolTable symbolTable) {

        if (jmmNode.getKind().equals("IntegerType")){
            return new Type("int", false);
        }

        if (jmmNode.getKind().equals("VarDeclaration")
                || jmmNode.getKind().equals("Variable")
                || jmmNode.getKind().equals("ArrayAssignment")){

            String method;

            JmmNode parent = jmmNode.getJmmParent();
            while (!parent.getKind().equals("MethodDeclaration")){
                parent = parent.getJmmParent();
            }
            parent = parent.getJmmChild(0);
            method = parent.get("signature");


            //List<Symbol> vars = symbolTable.getLocalVariables(method);

            for (Symbol s : symbolTable.getLocalVariables(method)){
                System.out.println(s.getName());
                if (s.getName().equals(jmmNode.get("name"))){
                    return s.getType();
                }
            }
            for (Symbol s : symbolTable.getParameters(method)){
                System.out.println(s.getName());
                if (s.getName().equals(jmmNode.get("name"))){
                    return s.getType();
                }
            }
            for (Symbol s : symbolTable.getFields()){
                System.out.println(s.getName());
                if (s.getName().equals(jmmNode.get("name"))){
                    return s.getType();
                }
            }
            return null;

        }

        if (jmmNode.getKind().equals("ArrayAccess")){
            Type arrayType = new Type( getType(jmmNode.getJmmChild(0), symbolTable).getName(), false);
            return arrayType;
        }
        if (jmmNode.getKind().equals("CallFunction")){
            Type returnType = symbolTable.getReturnType(jmmNode.get("name"));

            if (returnType == null){
                return new Type("#UNKNOWN", false);
            }
            return returnType;
        }

        if (jmmNode.getKind().equals("This")){
            JmmNode parent = jmmNode;
            while (!parent.getKind().equals("Class")){
                parent = parent.getJmmParent();
            }
            return new Type(parent.get("value"), false);
        }

        if (jmmNode.getKind().equals("BinaryOp")){
            if (ARITHMETIC_OP.contains(jmmNode.get("op"))){
                return new Type("int", false);
            }
            else{
                return new Type("boolean", false);
            }
        }

        if (jmmNode.getKind().equals("Parenthesis")){
            return getType(jmmNode.getJmmChild(0), symbolTable);
        }

        if (jmmNode.getKind().equals("ArrayAccess")){
            Type arrayType = getType(jmmNode.getJmmChild(0), symbolTable);
            if (arrayType == null)
                return null;
            return new Type(arrayType.getName(), false);
        }

        if (jmmNode.getKind().equals("Integer")){
            return new Type("int", false);
        }
        if (jmmNode.getKind().equals("Boolean"))
            return new Type("boolean", false);

        boolean isArray = jmmNode.hasAttribute("isArray");
        return new Type(jmmNode.get("name"), isArray);
    }

    public Symbol getSymbol(JmmNode jmmNode, SymbolTable symbolTable) {
        String symbol = jmmNode.get("name");
        Type type = getType(jmmNode.getJmmChild(0), symbolTable);
        return new Symbol(type, symbol);
    }

    public void putType(JmmNode jmmNode, Type type) {
        jmmNode.put("type", type.getName());
        jmmNode.put("isArray", String.valueOf(type.isArray()));
    }

    public Type getVariableType(JmmNode jmmNode, SymbolTable symbolTable){


        String method;
        JmmNode parent = jmmNode.getJmmParent();
        while (!parent.getKind().equals("MethodDeclaration")){
            parent = parent.getJmmParent();
        }
        parent = parent.getJmmChild(0);
        method = parent.get("signature");

        for (Symbol s : symbolTable.getLocalVariables(method)){
            System.out.println(s.getName());
            if (s.getName().equals(jmmNode.get("name"))){
                return s.getType();
            }
        }
        for (Symbol s : symbolTable.getParameters(method)){
            System.out.println(s.getName());
            if (s.getName().equals(jmmNode.get("name"))){
                return s.getType();
            }
        }
        for (Symbol s : symbolTable.getFields()){
            System.out.println(s.getName());
            if (s.getName().equals(jmmNode.get("name"))){
                return s.getType();
            }
        }

        return null;
    }

    public Type getArrayAcessType(JmmNode jmmNode, SymbolTable symbolTable){
        //System.out.println(jmmNode);


        JmmNode node = jmmNode.getJmmChild(1);
        return this.getType(node, symbolTable);
    }
}
