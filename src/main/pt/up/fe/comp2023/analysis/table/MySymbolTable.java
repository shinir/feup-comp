package pt.up.fe.comp2023.analysis.table;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MySymbolTable implements SymbolTable {
    List<String> imports = new ArrayList<>();
    String className = null, supers = null;
    List<Symbol> fields = new ArrayList<>();
    Map<String, Method> functMethods = new HashMap<>(), mainMethods = new HashMap<>();

    @Override
    public List<String> getImports() {
        return imports;
    }

    public void addImports(String lol) {
        this.imports.add(lol);
    }

    @Override
    public String getClassName() {
        return className;
    }

    public void addClassName(String className) {
        this.className = className;
    }

    @Override
    public String getSuper() {
        return supers;
    }

    public void addSupers(String supers) {
        this.supers = supers;
    }

    @Override
    public List<Symbol> getFields() {
        return fields;
    }

    public void addFields(Symbol fields) {
        this.fields.add(fields);
    }

    @Override
    public List<String> getMethods() {
        return new ArrayList<>(this.functMethods.keySet());
    }

    public void addMethods(String name, List<Symbol> parameters, List<Symbol> localVariables, Type type) {
        this.functMethods.put(name, new Method(type, parameters, localVariables));
    }

    @Override
    public Type getReturnType(String s) {
        return this.functMethods.get(s).getReturnType();
    }

    @Override
    public List<Symbol> getParameters(String s) {
        return this.functMethods.get(s).getParameters();
    }

    @Override
    public List<Symbol> getLocalVariables(String s) {
        return this.functMethods.get(s).getLocalVariables();
    }
}
