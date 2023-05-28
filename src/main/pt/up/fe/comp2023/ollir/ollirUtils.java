package pt.up.fe.comp2023.ollir;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;

public class ollirUtils {

    public static String getOllirType(String type){
        switch (type){
            case "void":
                return "V";
            case "int":
                return "i32";
            case "boolean":
                return "bool";
            default:
                return type;
        }
    }

    public static String getCode(Symbol symbol) {
        return symbol.getName() + "." + getCode(symbol.getType(),false);
    }

    public static String getCode(Type type, Boolean indexed) {
        StringBuilder code = new StringBuilder();

        if (type.isArray()) {
            code.append("array.");
        }

        code.append(getOllirType(type.getName()));

        return code.toString();
    }

    public static Type getType(JmmNode node) {
        boolean isArray = node.getAttributes().contains("isArray") && node.get("isArray").equals("true");
        return new Type(node.get("value"), isArray);
    }

    // Get return type of a function
    public static Type getReturnType(JmmNode node){
        JmmNode auxNode = node.getAncestor("FunctionMethodDeclaration").get().getJmmChild(0);
        return new Type(auxNode.get("name"), Boolean.parseBoolean(auxNode.get("isArray")));
    }

    public static String getOllirOp(String op){
        return switch (op) {
            case "+", "-", "/", "*" -> ".i32";
            case "&&", "||", "<", ">", "<=", ">=", "==", "!=" -> ".bool";
            default -> "";
        };

    }
}
