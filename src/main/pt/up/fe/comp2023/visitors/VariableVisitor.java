package pt.up.fe.comp2023.visitors;

import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.comp2023.MySymbolTable;
import pt.up.fe.comp2023.utils.AnalysisUtils;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;

import java.util.*;
import java.util.stream.Collectors;

public class VariableVisitor extends PreorderJmmVisitor<MySymbolTable, Boolean> {
    private final AnalysisUtils utils = new AnalysisUtils();

    static final List<String> PRIMITIVES = Arrays.asList("int", "void", "boolean");
    static final List<String> ARITHMETIC_OP = Arrays.asList("+", "-", "*", "/");
    static final List<String> COMPARISON_OP = List.of("<", ">", ">=", "<=", "==", "!=");
    static final List<String> LOGICAL_OP = List.of("&&", "||");
    private final List<String> types = new ArrayList<>();
    List<Report> reports = new ArrayList<Report>();

    @Override
    protected void buildVisitor() {
        addVisit("Class", this::dealWithClass);
        addVisit("VarDeclaration", this::dealWithVarDeclaration);
        addVisit("BinaryOp", this::dealWithBinaryOp);
        addVisit("ComparisonOp", this::dealWithComparisonOp);
        addVisit("This", this::dealWithThis);
        addVisit("CallFunction", this::dealWithFuncCall);
        addVisit("Assignment", this::dealWithAssigment);
        this.setDefaultVisit(this::myVisitAllChildren);
    }

    private Boolean dealWithAssigment(JmmNode jmmNode, MySymbolTable symbolTable) {
        return false;
    }

    private Boolean dealWithFuncCall(JmmNode jmmNode, MySymbolTable symbolTable) {
        return false;
    }

    private Boolean dealWithClass(JmmNode jmmNode, MySymbolTable symbolTable) {
        return false;
    }

    private Boolean myVisitAllChildren(JmmNode jmmNode, MySymbolTable symbolTable) {
        return true;
    }

    private boolean dealWithBinaryOp(JmmNode jmmNode, MySymbolTable symbolTable) {
        return true;
    }

    private Boolean dealWithComparisonOp(JmmNode jmmNode, MySymbolTable symbolTable) {
        return true;
    }

    private Boolean dealWithThis(JmmNode jmmNode, MySymbolTable symbolTable) {
        return true;
    }

    private Boolean dealWithVarDeclaration(JmmNode jmmNode, MySymbolTable symbolTable) {
        return true;
    }

    public List<Report> getReports() {
        return reports;
    }
    
}
