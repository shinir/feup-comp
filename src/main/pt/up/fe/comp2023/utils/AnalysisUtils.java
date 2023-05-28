package pt.up.fe.comp2023.utils;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AnalysisUtils {
    public static Type getType(JmmNode jmmNode, SymbolTable symbolTable) {
        //boolean isArray = jmmNode.getAttributes().contains("isArray") && jmmNode.get("isArray").equals("true");
        //if(jmmNode.hasAttribute("isArray") && isArray == true) isArray = true;
        //if (jmmNode.getJmmParent().hasAttribute("name")) return new Type(jmmNode.getJmmParent().get("name"), isArray);
        //return new Type(jmmNode.getJmmParent().get("funcName"), isArray);

        if (jmmNode.getKind().equals("VarDeclaration")){
            boolean isArray = false;
            if (jmmNode.getJmmChild(0).hasAttribute("isArray") && jmmNode.getJmmChild(0).get("isArray").equals("true")) isArray = true;
             new Type(jmmNode.getJmmChild(0).get("name"), isArray);
        }

        return null;

    }

    public Symbol getSymbol(JmmNode jmmNode) {
        String symbol = jmmNode.get("name");
        Type type = new Type(jmmNode.getJmmChild(0).get("name"),Boolean.getBoolean(jmmNode.getJmmChild(0).get("isArray")));
        return new Symbol(type, symbol);
    }

    public void putType(JmmNode jmmNode, Type type) {
        jmmNode.put("type", type.getName());
        jmmNode.put("isArray", String.valueOf(type.isArray()));
    }
}
