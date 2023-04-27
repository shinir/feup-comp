package pt.up.fe.comp2023.utils;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;

import java.util.Objects;

public class AnalysisUtils {
    public Type getType(JmmNode jmmNode) {
        boolean isArray = false;
        if(jmmNode.hasAttribute("isArray")) isArray = true;
        return new Type(jmmNode.get("name"), isArray);
    }

    public Symbol getSymbol(JmmNode jmmNode) {
        String symbol = jmmNode.get("name");
        Type type = getType(jmmNode.getJmmChild(0));
        return new Symbol(type, symbol);
    }

    public void putType(JmmNode jmmNode, Type type) {
        jmmNode.put("type", type.getName());
        jmmNode.put("isArray", String.valueOf(type.isArray()));
    }
}
