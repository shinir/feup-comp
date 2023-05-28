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
        this.addVisit("BinaryOp", this::dealWithBinaryOp);
        this.addVisit("Assignment", this::dealWithAssignment);
        this.setDefaultVisit(this::myVisitAllChildren);
    }

    private Boolean dealWithAssignment(JmmNode jmmNode, MySymbolTable symbolTable) {
        Type left  = utils.getType(jmmNode,symbolTable);
        Type right = utils.getType(jmmNode.getJmmChild(0), symbolTable);
        return true;
    }

    private Boolean dealWithBinaryOp(JmmNode jmmNode, MySymbolTable symbolTable) {
        Type left = utils.getType(jmmNode.getJmmChild(0), symbolTable);
        Type right = utils.getType(jmmNode.getJmmChild(1), symbolTable);

        if (LOGICAL_OP.contains(jmmNode.get("op"))){
            if (left.isArray() || right.isArray() || !left.getName().equals("boolean") || !right.getName().equals("boolean")){
                Report newReport = new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(jmmNode.get("lineStart")), Integer.parseInt(jmmNode.get("colStart")), "Logical operators should be use with boolean expressions");
                reports.add(newReport);
                return false;
            }
        }
        if (left.isArray() || right.isArray() || !left.getName().equals("int") || !right.getName().equals("int")){
            Report newReport = new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(jmmNode.get("lineStart")), Integer.parseInt(jmmNode.get("colStart")), "Logical operators should be use with boolean expressions");
            reports.add(newReport);
            return false;
        }

        return true;

    }


    private Boolean myVisitAllChildren(JmmNode jmmNode, MySymbolTable symbolTable) {
        return true;
    }


    public List<Report> getReports() {
        return reports;
    }
    
}
