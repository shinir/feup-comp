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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class VariableVisitor extends PreorderJmmVisitor<MySymbolTable, Boolean> {
    private final AnalysisUtils utils = new AnalysisUtils();

    static final List<String> PRIMITIVES = Arrays.asList("int", "void", "boolean");
    static final List<String> ARITHMETIC_OP = Arrays.asList("+", "-", "*", "/");
    static final List<String> COMPARISON_OP = List.of("<");
    static final List<String> LOGICAL_OP = List.of("&&");

    private final String defaultMessage = "DEFAULT ERROR MESSAGE";
    private final List<String> nodeKinds = Arrays.asList("varDeclaration", "Import", "Class", "BinaryOp","returnExpression", "ComparisonOp");


    private final List<String> types = new ArrayList<>();
    List<Report> reports = new ArrayList<Report>();

    @Override
    protected void buildVisitor() {
        addVisit("Class", this::dealWithClass);
        addVisit("varDeclaration", this::dealWithVarDeclaration);
        addVisit("BinaryOp", this::dealWithBinaryOp);
        addVisit("ComparisonOp", this::dealWithComparisonOp);
        addVisit("This", this::dealWithThis);
        addVisit("returnExpression", this::dealWithReturn);
        this.setDefaultVisit(this::myVisitAllChildren);
    }

    private Boolean dealWithClass(JmmNode jmmNode, MySymbolTable symbolTable) {
        if (!jmmNode.getAttributes().contains("superClass")){
            System.out.println("here");
            return true;
        }

        String extend = jmmNode.get("superClass");
        if (jmmNode.get("name").equals(extend)){
            Report newReport = new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(jmmNode.get("lineStart")), Integer.parseInt(jmmNode.get("colStart")), defaultMessage);
            reports.add(newReport);
            return true;
        }

        boolean findImport = symbolTable
                .getImports()
                .stream()
                .anyMatch(imp -> imp.substring(imp.lastIndexOf(".") +1)
                        .equals(extend));

        if (findImport){
            return true;
        }

        Report newReport = new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(jmmNode.get("lineStart")), Integer.parseInt(jmmNode.get("colStart")), defaultMessage);
        reports.add(newReport);

        return true;
    }

    private Boolean dealWithReturn(JmmNode jmmNode, MySymbolTable symbolTable) {
        return true;
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
