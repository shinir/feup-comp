package pt.up.fe.comp2023.analysis.table;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;

import java.util.Objects;

public class AnalysisUtils {

    public Type getType(JmmNode jmmNode) {
        return new Type(jmmNode.getKind(), Objects.equals(jmmNode.getKind(), "Array"));
    }

    public Symbol getSymbol(JmmNode jmmNode) {
        String symbol = jmmNode.get("name");
        Type type = getType(jmmNode.getJmmChild(0));
        return new Symbol(type, symbol);
    }


}
