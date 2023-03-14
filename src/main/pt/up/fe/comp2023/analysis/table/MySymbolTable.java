package pt.up.fe.comp2023.analysis.table;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp2023.analysis.table.Method;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.lang.Class;

public class MySymbolTable implements SymbolTable {
    List<String> imports;
    String className, supers;
    List<Symbol> fields;
    Map<String, Method> methods;

    @Override
    public List<String> getImports() {
        return imports;
    }

    public void setImports(List<String> imports) {
        this.imports = imports;
    }

    @Override
    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    @Override
    public String getSuper() {
        return supers;
    }

    public void setSupers(String supers) {
        this.supers = supers;
    }

    @Override
    public List<Symbol> getFields() {
        return fields;
    }

    public void setFields(List<Symbol> fields) {
        this.fields = fields;
    }

    @Override
    public List<String> getMethods() {
        return new ArrayList<>(this.methods.keySet());
    }

    public void setMethods(String name, List<Symbol> parameters, List<Symbol> localVariables, Type type) {
        this.methods.put(name, new Method(type, parameters, localVariables));
    }

    @Override
    public Type getReturnType(String s) {
        return this.methods.get(s).getReturnType();
    }

    @Override
    public List<Symbol> getParameters(String s) {
        return this.methods.get(s).getParameters();
    }

    @Override
    public List<Symbol> getLocalVariables(String s) {
        return this.methods.get(s).getLocalVariables();
    }
}
