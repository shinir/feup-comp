package pt.up.fe.comp2023.visitors;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class VariableVisitor extends PreorderJmmVisitor<MySymbolTable, Boolean> {
    private final AnalysisUtils utils = new AnalysisUtils();
    private final List<String> types = new ArrayList<>();
    List<Report> reports = new ArrayList<Report>();

    static final List<String> PRIMITIVES = Arrays.asList("int", "void", "boolean");
    static final List<String> ARITHMETIC_OP = Arrays.asList("+", "-", "*", "/");
    static final List<String> COMPARISON_OP = List.of("<");
    static final List<String> LOGICAL_OP = List.of("&&");

    @Override
    protected void buildVisitor() {
        addVisit("Class", this::dealWithClass);
        addVisit("VarDeclaration", this::dealWithVarDeclaration);
        addVisit("CallFunction", this::dealWithCall);
        addVisit("BinaryOp", this::dealWithBinaryOp);
        addVisit("Variable", this::dealWithVariable);
        addVisit("ComparisonOp", this::dealWithComparisonOp);
        addVisit("Assignment", this::dealWithAssignment);
        addVisit("ArrayAccess", this::dealWithArrayAccess);
        addVisit("This", this::dealWithThis);
        this.setDefaultVisit(this::myVisitAllChildren);
    }

    private Boolean dealWithCall(JmmNode jmmNode, MySymbolTable symbolTable) {
        System.out.println(jmmNode);

        jmmNode.getJmmChild(0);

        for (String s : symbolTable.getImports()){
            if (s.substring(s.lastIndexOf(".") + 1).equals(jmmNode.getJmmChild(0).get("name"))) return true;
        }
        Report newReport = new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(jmmNode.get("lineStart")), Integer.parseInt(jmmNode.get("colStart")), "Not imported");
        reports.add(newReport);
        return true;
    }

    private Boolean dealWithVariable(JmmNode jmmNode, MySymbolTable symbolTable) {
        //if (symbolTable.getImports())
        //System.out.println(symbolTable.getImports());
        if (jmmNode.getJmmParent().getKind().equals("CallFunction")) return true;

        if (utils.getVariableType(jmmNode, symbolTable) == null){
            Report newReport = new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(jmmNode.get("lineStart")), Integer.parseInt(jmmNode.get("colStart")), "Variable not declared");
            reports.add(newReport);
            return true;
        }
        return true;
    }

    private Boolean dealWithBinaryOp(JmmNode jmmNode, MySymbolTable symbolTable) {

        JmmNode left = jmmNode.getJmmChild(0);
        Type leftType = utils.getType(left, symbolTable);
        if (leftType == null){
            Report newReport = new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(jmmNode.get("lineStart")), Integer.parseInt(jmmNode.get("colStart")), "Cannot find symbol");
            reports.add(newReport);
            return true;
        }

        JmmNode right = jmmNode.getJmmChild(1);
        Type rightType = utils.getType(right, symbolTable);
        if (rightType == null){
            Report newReport = new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(jmmNode.get("lineStart")), Integer.parseInt(jmmNode.get("colStart")), "Cannot find symbol");
            reports.add(newReport);
            return true;
        }


        if (leftType.getName().equals("#UNKNOWN")
                || rightType.getName().equals("#UNKNOWN")){
            jmmNode.put("type", "#UNKNOWN");
            return true;
        }

        //System.out.println(leftType);
        //System.out.println(rightType);

        if (!leftType.getName().equals(rightType.getName())){
            jmmNode.put("type", "#UNKNOWN");
            Report newReport = new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(jmmNode.get("lineStart")), Integer.parseInt(jmmNode.get("colStart")), "Types dont match");
            reports.add(newReport);
        }

        if (ARITHMETIC_OP.contains(jmmNode.get("op"))
                || LOGICAL_OP.contains(jmmNode.get("op"))
                || COMPARISON_OP.contains(jmmNode.get("op"))){
            if (leftType.isArray()){
                jmmNode.put("type", "#UNKNOWN");
                Report newReport = new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(jmmNode.get("lineStart")), Integer.parseInt(jmmNode.get("colStart")), "Cannot be applied");
                reports.add(newReport);
                return true;
            }
        }

        if (ARITHMETIC_OP.contains(jmmNode.get("op"))
                || COMPARISON_OP.contains(jmmNode.get("op"))){
            if (!leftType.getName().equals("int")) {
                jmmNode.put("type", "#UNKNOWN");
                Report newReport = new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(jmmNode.get("lineStart")), Integer.parseInt(jmmNode.get("colStart")), "Operation cannot be applied");
                reports.add(newReport);
                return true;
            }
        }

        if (LOGICAL_OP.contains(jmmNode.get("op"))){
            if (!leftType.getName().equals("boolean")){
                jmmNode.put("type", "#UNKNOWN");
                Report newReport = new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(jmmNode.get("lineStart")), Integer.parseInt(jmmNode.get("colStart")), "Types dont match");
                reports.add(newReport);
                return true;
            }
        }

        if (!PRIMITIVES.contains(leftType.getName())) {
            jmmNode.put("type", "#UNKNOWN");
            Report newReport = new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(jmmNode.get("lineStart")), Integer.parseInt(jmmNode.get("colStart")), "Types dont match");
            reports.add(newReport);
            return true;
        }

        if (COMPARISON_OP.contains(jmmNode.get("op"))) {
            jmmNode.put("type", new Type("boolean", false).getName());
        } else {
            jmmNode.put("type", leftType.getName());
        }
        return true;
    }

    private Boolean dealWithClass(JmmNode jmmNode, MySymbolTable symbolTable) {

        if (!jmmNode.getAttributes().contains("extends")){
            return true;
        }

        String superName = jmmNode.get("superClass");
        if (jmmNode.get("name").equals(superName)){
            Report newReport = new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(jmmNode.get("lineStart")), Integer.parseInt(jmmNode.get("colStart")), "Class cant extend herself");
            reports.add(newReport);
            return true;
        }

        boolean foundImport = symbolTable
                .getImports()
                .stream()
                .anyMatch(imp -> imp.substring(imp.lastIndexOf(".") + 1).equals(superName));

        if (foundImport) {
            return true;
        }

        Report newReport = new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(jmmNode.get("lineStart")), Integer.parseInt(jmmNode.get("colStart")), "Cant found that class on any import");
        reports.add(newReport);

        for(Symbol field : symbolTable.getFields()) {
            //System.out.println(field);
        }

        for(Symbol field : symbolTable.getFields()) {
            //System.out.println(field);
        }

        return true;
    }

    private Boolean myVisitAllChildren(JmmNode jmmNode, MySymbolTable symbolTable) {
        return true;
    }

    private Boolean dealWithComparisonOp(JmmNode jmmNode, MySymbolTable symbolTable) {
        return true;
    }

    private Boolean dealWithThis(JmmNode jmmNode, MySymbolTable symbolTable) {
        if(jmmNode.getAncestor("methodDeclaration").isPresent()) {
            jmmNode.put("type", "#UNKNOWN");
            Report newReport = new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(jmmNode.get("lineStart")), Integer.parseInt(jmmNode.get("colStart")), "Non static cannot be referenced from a static context.");
            reports.add(newReport);
        }
        return true;
    }

    private Boolean dealWithVarDeclaration(JmmNode jmmNode, MySymbolTable symbolTable) {

        return true;
    }

    private Boolean dealWithAssignment(JmmNode jmmNode, MySymbolTable symbolTable) {

        //for(jmmNode.getHierarchy())

        //System.out.println(jmmNode.getJmmParent().getChildren());
        return true;
    }

    private Boolean dealWithArrayAccess(JmmNode jmmNode, MySymbolTable symbolTable) {
        //System.out.println(jmmNode);
        Type integer = new Type("int", false);
        Type array = new Type("int", true);

        if(!utils.getType(jmmNode.getJmmChild(1),symbolTable).equals(integer)) {
            Report newReport = new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(jmmNode.get("lineStart")), Integer.parseInt(jmmNode.get("colStart")), "Array access not integer.");
            reports.add(newReport);
        }

        if (!utils.getVariableType(jmmNode.getJmmChild(0), symbolTable).equals(array)){
            Report newReport = new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(jmmNode.get("lineStart")), Integer.parseInt(jmmNode.get("colStart")), "Not an array.");
            reports.add(newReport);
        }

        return true;
    }

    public List<Report> getReports() {
        return reports;
    }
}
