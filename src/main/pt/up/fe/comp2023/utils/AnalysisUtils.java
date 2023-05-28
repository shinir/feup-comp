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

public class AnalysisUtils {

    static final List<String> ARITHMETIC_OP = Arrays.asList("+", "-", "*", "/");

    public Symbol getSymbol(JmmNode jmmNode) {
        String symbol = jmmNode.get("name");
        Type type = new Type(jmmNode.getJmmChild(0).get("name"),Boolean.parseBoolean(jmmNode.getJmmChild(0).get("isArray")));
        return new Symbol(type, symbol);
    }
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
        if (jmmNode.getKind().equals("Assignment")){
            JmmNode node = jmmNode;
            while (node.getJmmParent() != null){
                if (node.hasAttribute("funcName")) break;
                node = node.getJmmParent();
            }
            if (node.hasAttribute("funcName")){

                List<Symbol> parameters = new ArrayList<>();
                for (JmmNode child : node.getChildren()){
                    if (child.getKind().equals("Parameter")){
                        Symbol symbol = this.getSymbol(child);
                            parameters.add(symbol);
                        }
                    }
                
                StringBuilder signature = new StringBuilder();
                signature.append(node.get("funcName"));
                for (Symbol s : parameters){
                    signature.append("#");
                    signature.append(s.getType().print());
                }

                for (Symbol symbol : symbolTable.getParameters(signature.toString())){
                    if (jmmNode.get("value").equals(symbol.getName())){
                        return symbol.getType();
                    }
                }
                for (Symbol symbol : symbolTable.getLocalVariables(signature.toString())){
                    if (jmmNode.get("value").equals(symbol.getName())){
                        return symbol.getType();
                    }
                }
            }
            for (Symbol symbol : symbolTable.getFields()){
                if (jmmNode.get("value").equals(symbol.getName())){
                    return symbol.getType();
                }
            }

        }
        if (jmmNode.getKind().equals("Parameter")){
            JmmNode type = jmmNode.getJmmChild(0);
            return new Type(type.get("name"), type.hasAttribute("isArray") && type.get("isArray").equals("false"));

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
            JmmNode node = jmmNode;
            while (node.getJmmParent() != null){
                if (node.hasAttribute("funcName")) break;
                node = node.getJmmParent();
            }
            if (node.hasAttribute("funcName")){

                List<Symbol> parameters = new ArrayList<>();
                for (JmmNode child : jmmNode.getChildren()){
                    if (child.getKind().equals("Parameter")){
                        Symbol symbol = this.getSymbol(child);
                        parameters.add(symbol);
                    }
                }

                StringBuilder signature = new StringBuilder();
                signature.append(node.get("funcName"));
                for (Symbol s : parameters){
                    signature.append("#");
                    signature.append(s.getType().print());
                }

                for (Symbol symbol : symbolTable.getParameters(signature.toString())){
                    if (jmmNode.get("name").equals(symbol.getName())){
                        return symbol.getType();
                    }
                }
                for (Symbol symbol : symbolTable.getLocalVariables(signature.toString())){
                    if (jmmNode.get("name").equals(symbol.getName())){
                        return symbol.getType();
                    }
                }
            }
            for (Symbol symbol : symbolTable.getFields()){
                if (jmmNode.get("name").equals(symbol.getName())){
                    return symbol.getType();
                }
            }
        }
        if (jmmNode.getKind().equals("This")){
            //
        }
        return getType(jmmNode.getJmmChild(0), symbolTable);
    }


    public void putType(JmmNode jmmNode, Type type) {
        jmmNode.put("type", type.getName());
        jmmNode.put("isArray", String.valueOf(type.isArray()));
    }

    public static String getImportWord(String str) {
        int lastIndex = str.lastIndexOf('.');
        if (lastIndex != -1 && lastIndex < str.length() - 1) {
            return str.substring(lastIndex + 1);
        }
        return str;
    }
}
