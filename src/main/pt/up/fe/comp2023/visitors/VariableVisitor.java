package pt.up.fe.comp2023.visitors;

import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.report.Report;

import java.util.ArrayList;
import java.util.List;

public class VariableVisitor extends AJmmVisitor<Integer, Type> {
    List<Report> reports = new ArrayList<>();
    SymbolTable symbolTable;

    @Override
    protected void buildVisitor() {

    }
}
