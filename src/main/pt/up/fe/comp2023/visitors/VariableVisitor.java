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
    static final List<String> COMPARISON_OP = List.of("<");
    static final List<String> LOGICAL_OP = List.of("&&");
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
        //Type lhsType = utils.getType(jmmNode.getJmmChild(0));
        //Type rhsType = utils.getType(jmmNode.getJmmChild(1));
        return false;
    }

    private Boolean dealWithFuncCall(JmmNode jmmNode, MySymbolTable symbolTable) {
        /*

        if(!jmmNode.getJmmChild(0).getAttributes().contains("type")){
            if (jmmNode.getJmmChild(0).get("name").equals(symbolTable.getClassName())){

                StringBuilder methodSignatureBuilder = new StringBuilder();
                methodSignatureBuilder.append(jmmNode.get("funcName"));
                for (JmmNode argument: jmmNode.getJmmChild(1).getChildren()) {
                    Type argType = utils.getType(argument);
                    methodSignatureBuilder.append("#");
                    methodSignatureBuilder.append(argType.print());
                }
                String signature = methodSignatureBuilder.toString();

                boolean exists = symbolTable
                        .getMethods()
                        .stream()
                        .anyMatch(f -> f.substring(0, methodSignatureBuilder.indexOf("#")).equals(jmmNode.get("funcName")));
                if (exists){
                    Report newReport = new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(jmmNode.get("lineStart")), Integer.parseInt(jmmNode.get("colStart")), "Method call caused an ERROR");
                    reports.add(newReport);
                }
                else if (symbolTable.getSuper() == null){
                    Report newReport = new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(jmmNode.get("lineStart")), Integer.parseInt(jmmNode.get("colStart")), "Method call caused an ERROR");
                    reports.add(newReport);
                }
            }
            jmmNode.put("type", "#UNKNOWN");
            return true;
        }

        Type operand = utils.getType(jmmNode.getJmmChild(0));
        if (PRIMITIVES.contains(operand.getName())){
            jmmNode.put("type", "#UNKNOWN");
            Report newReport = new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(jmmNode.get("lineStart")), Integer.parseInt(jmmNode.get("colStart")), "Function call ERROR");
            reports.add(newReport);
            return false;
        }

        if (operand.getName().equals("#UNKNOWN")
                || operand.isArray()
                || !symbolTable.getClassName().equals(operand.getName())){
            jmmNode.put("type", "#UNKNOWN");
            return false;
        }

        /*
        StringBuilder methodSignatureBuilder = new StringBuilder();
        methodSignatureBuilder.append(jmmNode.get("funcName"));
        for (JmmNode argument: jmmNode.getJmmChild(1).getChildren()) {
            Type argType = utils.getType(argument);
            methodSignatureBuilder.append("#");
            methodSignatureBuilder.append(argType.print());
        }
        String signature = methodSignatureBuilder.toString();
        Optional<String> foundSignature;
        if (symbolTable.getMethods().contains(signature)) {
            foundSignature = Optional.of(signature);
        } else {
            List<String> foundMethodSignatures = symbolTable
                    .getMethods()
                    .stream()
                    .filter(m -> signatureIsCompatibleWith(m, signature, symbolTable))
                    .collect(Collectors.toList());
            if (foundMethodSignatures.size() > 1) {
                jmmNode.put("type", "#UNKNOWN");
                Report newReport = new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(jmmNode.get("lineStart")), Integer.parseInt(jmmNode.get("colStart")), "Function call ERROR");
                reports.add(newReport);
                return false;
            } else {
                foundSignature = foundMethodSignatures.stream().findAny();
            }
        }
        */
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
        //Type lhsType = utils.getType(jmmNode.getJmmChild(0));
        //Type rhsType = utils.getType(jmmNode.getJmmChild(1));

        /*if (lhsType.getName().equals("#UNKNOWN") || rhsType.getName().equals("#UNKNOWN")) {
            jmmNode.put("type", "#UNKNOWN");
            return true;
        }*/

        return true;
    }
    private Boolean dealWithComparisonOp(JmmNode jmmNode, MySymbolTable symbolTable) {
        return true;
    }

    private Boolean dealWithThis(JmmNode jmmNode, MySymbolTable symbolTable) {
        if (jmmNode.getAncestor("functionMethodDeclaration").isEmpty()){
            //put unknown type
            Report newReport = new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(jmmNode.get("lineStart")), Integer.parseInt(jmmNode.get("colStart")), "This Keyword Related Error");
            reports.add(newReport);
            return false;
        }

        Type type = new Type(symbolTable.getClassName(), false);
        jmmNode.put("type", type.getName());
        jmmNode.put("isArray", String.valueOf(type.isArray()));

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
