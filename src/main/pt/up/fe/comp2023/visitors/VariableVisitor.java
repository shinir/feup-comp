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
import java.util.List;
import java.util.Objects;

public class VariableVisitor extends PreorderJmmVisitor<MySymbolTable, Boolean> {
    private final AnalysisUtils utils = new AnalysisUtils();
    private final List<String> types = new ArrayList<>();
    List<Report> reports = new ArrayList<Report>();

    @Override
    protected void buildVisitor() {
        addVisit("VarDeclaration", this::dealWithVarDeclaration);
        addVisit("ComparisonOp", this::dealWithComparisonOp);
        addVisit("This", this::dealWithThis);
        this.setDefaultVisit(this::myVisitAllChildren);
    }

    private Boolean myVisitAllChildren(JmmNode jmmNode, MySymbolTable symbolTable) {
        return true;
    }

    private Boolean dealWithComparisonOp(JmmNode jmmNode, MySymbolTable symbolTable) {
        List<Report> reports = new ArrayList<>();
        Type lhsType = utils.getType(jmmNode.getJmmChild(0));
        Type rhsType = utils.getType(jmmNode.getJmmChild(1));
        if (lhsType.getName().equals("#UNKNOWN") || rhsType.getName().equals("#UNKNOWN")) {
            jmmNode.put("type", "UNKNOWN");
            //return reports;
        }

        return true;
    }

    private Boolean dealWithThis(JmmNode jmmNode, MySymbolTable symbolTable) {
        List<Report> reports = new ArrayList<Report>();
        if(jmmNode.getAncestor("MethodDeclaration").isEmpty()) {
            jmmNode.put("type", "UNKNOWN");
            reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, 0, "This expression cannot be used in a static method"));
        }
        utils.putType(jmmNode, new Type(symbolTable.getClassName(), false));
        return true;
    }

    private Boolean dealWithVarDeclaration(JmmNode jmmNode, MySymbolTable symbolTable) {
        List<Report> reports = new ArrayList<Report>();
        Type type = utils.getType(jmmNode.getJmmChild(0));
        if(!types.contains(type.getName())) reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, 0, "Class unknown/not instanced."));
        return true;
    }

    public List<Report> getReports() {
        return reports;
    }
}
