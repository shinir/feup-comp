package pt.up.fe.comp2023;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;

import java.util.List;

public class Method {
    private Type type;
    private List<Symbol> parameters;
    private List<Symbol> localVariables;

    public Method(Type type, List<Symbol> parameters, List<Symbol> localVariables) {
        this.type = type;
        this.parameters = parameters;
        this.localVariables = localVariables;
    }

    public Type getReturnType() {
        return this.type;
    }

    public List<Symbol> getParameters() {
        return this.parameters;
    }

    public List<Symbol> getLocalVariables() {
        return this.localVariables;
    }
}
