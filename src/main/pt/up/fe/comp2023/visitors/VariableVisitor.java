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
        addVisit("Not", this::dealWithNot);
        addVisit("ArrayAccess", this::dealWithArrayAccess);
        addVisit("GetLength", this::dealWithLength);
        addVisit("NewArray", this::dealWithNewArray);
        this.setDefaultVisit(this::myVisitAllChildren);
    }

    private Boolean dealWithNewArray(JmmNode jmmNode, MySymbolTable symbolTable) {
        Type type = utils.getType(jmmNode.getJmmChild(0),symbolTable);
        if (type.isArray() || !type.getName().equals("int")){
            Report newReport = new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(jmmNode.get("lineStart")), Integer.parseInt(jmmNode.get("colStart")), "Expression must be an Integer to apply to an Array");
            reports.add(newReport);
            return false;
        }
        return true;
    }

    private Boolean dealWithLength(JmmNode jmmNode, MySymbolTable symbolTable) {
        Type type = utils.getType(jmmNode.getJmmChild(0), symbolTable);
        if (!type.isArray()){
            Report newReport = new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(jmmNode.get("lineStart")), Integer.parseInt(jmmNode.get("colStart")), "Expression must be an Array to use length");
            reports.add(newReport);
            return false;
        }
        return true;
    }

    private Boolean dealWithArrayAccess(JmmNode jmmNode, MySymbolTable symbolTable) {
        Type left = utils.getType(jmmNode.getJmmChild(0), symbolTable);
        Type right = utils.getType(jmmNode.getJmmChild(1), symbolTable);

        if (!left.isArray()){
            Report newReport = new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(jmmNode.get("lineStart")), Integer.parseInt(jmmNode.get("colStart")), "Expression must be an Array");
            reports.add(newReport);
            return false;
        }
        if (!right.getName().equals("int") || right.isArray()){
            Report newReport = new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(jmmNode.get("lineStart")), Integer.parseInt(jmmNode.get("colStart")), "Array index should be integer");
            reports.add(newReport);
            return false;
        }
        return true;
    }

    private Boolean dealWithNot(JmmNode jmmNode, MySymbolTable symbolTable) {
        Type type = utils.getType(jmmNode.getJmmChild(0), symbolTable);
        if (!type.getName().equals("boolean") || type.isArray() ){
            Report newReport = new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(jmmNode.get("lineStart")), Integer.parseInt(jmmNode.get("colStart")), "Not (!) must me applied to boolean expression");
            reports.add(newReport);
            return false;
        }
        return true;
    }

    private Boolean dealWithAssigment(JmmNode jmmNode, MySymbolTable symbolTable) {
        Type lhsType = utils.getType(jmmNode, symbolTable);
        Type rhsType = utils.getType(jmmNode.getJmmChild(0), symbolTable );

        if (lhsType == null || rhsType == null){
            Report newReport = new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(jmmNode.get("lineStart")), Integer.parseInt(jmmNode.get("colStart")), "Assigment ERROR");
            reports.add(newReport);
            return false;
        }
        if (!lhsType.getName().equals(rhsType.getName()) || !lhsType.isArray() == rhsType.isArray()){
            if (rhsType.getName().equals(symbolTable.getClassName())){
                if (lhsType.getName().equals(symbolTable.getSuper())){
                    return true;
                }
            }

            boolean left_imported = false, right_imported = false;
            for (String imported : symbolTable.getImports()){
                if (utils.getImportWord(imported).equals(lhsType.getName())) left_imported = true;
                if (utils.getImportWord(imported).equals(rhsType.getName())) right_imported = true;
                if (left_imported && right_imported) return true;
            }


            Report newReport = new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(jmmNode.get("lineStart")), Integer.parseInt(jmmNode.get("colStart")), "Assigment Type ERROR");
            reports.add(newReport);
            return false;
        }
        return true;
    }

    private Boolean dealWithFuncCall(JmmNode jmmNode, MySymbolTable symbolTable) {
        return false;
    }

    private Boolean dealWithClass(JmmNode jmmNode, MySymbolTable symbolTable) {
        if (!jmmNode.getAttributes().contains("superClass")){
            return true;
        }

        String superClass = jmmNode.get("superClass");
        if (jmmNode.get("name").equals(superClass)) {
            Report newReport = new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(jmmNode.get("lineStart")), Integer.parseInt(jmmNode.get("colStart")), "Class and SuperClass have the same name");
            reports.add(newReport);
            return false;
        }

        boolean imported = symbolTable.getImports().stream().anyMatch(i -> i.substring(i.lastIndexOf(".")+1).equals(superClass));

        if (imported){
            return true;
        }
        Report newReport = new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(jmmNode.get("lineStart")), Integer.parseInt(jmmNode.get("colStart")), "SuperClass dont exist");
        reports.add(newReport);
        return false;
    }

    private Boolean myVisitAllChildren(JmmNode jmmNode, MySymbolTable symbolTable) {
        return true;
    }

    private boolean dealWithBinaryOp(JmmNode jmmNode, MySymbolTable symbolTable) {
        Type lhsType = utils.getType(jmmNode.getJmmChild(0), symbolTable);
        Type rhsType = utils.getType(jmmNode.getJmmChild(1), symbolTable);

        if (ARITHMETIC_OP.contains(jmmNode.get("op")) || COMPARISON_OP.contains(jmmNode.get("op"))){
            if (!lhsType.getName().equals("int")
                    || !rhsType.getName().equals("int")
                    || lhsType.isArray()
                    || rhsType.isArray()){
                Report newReport = new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(jmmNode.get("lineStart")), Integer.parseInt(jmmNode.get("colStart")), "Arithmetic operations can only be realized with integer expression");
                reports.add(newReport);
                return false;
            }
        }
        else {
            if (!lhsType.getName().equals("boolean")
                    || !rhsType.getName().equals("boolean")
                    || lhsType.isArray()
                    || rhsType.isArray()){
                Report newReport = new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(jmmNode.get("lineStart")), Integer.parseInt(jmmNode.get("colStart")), "Arithmetic operations can only be realized with integer expression");
                reports.add(newReport);
                return false;
            }
        }

        return true;
    }

    private Boolean dealWithComparisonOp(JmmNode jmmNode, MySymbolTable symbolTable) {
        return true;
    }

    private Boolean dealWithThis(JmmNode jmmNode, MySymbolTable symbolTable) {
        return true;
    }

    private Boolean dealWithVarDeclaration(JmmNode jmmNode, MySymbolTable symbolTable) {
        //Type type = utils.getType(jmmNode.getJmmChild(0));
        //if(!types.contains(type.getName())) reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, 0, "Class unknown/not instanced."));
        return true;
    }

    public List<Report> getReports() {
        return reports;
    }


}
