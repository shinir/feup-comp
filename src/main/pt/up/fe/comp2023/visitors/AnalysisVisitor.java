package pt.up.fe.comp2023.visitors;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.comp2023.MySymbolTable;
import pt.up.fe.comp2023.utils.AnalysisUtils;

import java.util.*;
import java.util.stream.Stream;

public class AnalysisVisitor extends PreorderJmmVisitor<MySymbolTable, Boolean> {
    private final AnalysisUtils utils = new AnalysisUtils();
    List<Report> reports = new ArrayList<Report>();
    private final List<String> types = new ArrayList<>();
    //private final String defaultMessage = "DEFAULT ERROR MESSAGE";

    @Override
    protected void buildVisitor() {
        //addVisit("ProgramDeclaration", this::dealWithProgramDeclaration);
        addVisit("ImportDeclaration", this::dealWithImports);
        addVisit("Class", this::dealWithClass);
        addVisit("FunctionMethodDeclaration", this::dealWithFunctionMethod);
        addVisit("MainMethodDeclaration", this::dealWithFunctionMethod);
        this.setDefaultVisit(this::myVisitAllChildren);
    }

    private Boolean myVisitAllChildren(JmmNode jmmNode, MySymbolTable symbolTable) {
        for (JmmNode child : jmmNode.getChildren()){
            //visit(child, symbolTable);
        }
        return true;
    }

    private Boolean dealWithProgramDeclaration(JmmNode jmmNode, MySymbolTable symbolTable) {
        for (JmmNode child : jmmNode.getChildren()){
            //visit(child, symbolTable);
        }
        return true;
    }


    private Boolean dealWithImports(JmmNode jmmNode, MySymbolTable symbolTable) {
        String importPath = jmmNode.get("importName");

        importPath = importPath.replaceAll("[\\[\\]]", "");
        importPath = String.join(".", importPath.split(", "));

        if (!symbolTable.getImports().contains(importPath)){
            String lastName = importPath.substring(importPath.lastIndexOf('.') + 1);
            Optional<String> match = symbolTable
                    .getImports()
                    .stream()
                    .filter(s -> s.substring(s.lastIndexOf('.') + 1).equals(lastName))
                    .findAny();
            if (match.isPresent()) {
                Report newReport = new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(jmmNode.get("lineStart")), Integer.parseInt(jmmNode.get("colStart")), "Import Related Error");
                reports.add(newReport);
                return false;
            }
            symbolTable.addImports(importPath);
        }


        return true;
    }

    private Boolean dealWithClass(JmmNode jmmNode, MySymbolTable symbolTable) {
        symbolTable.addClassName(jmmNode.get("name"));
        if (jmmNode.getAttributes().contains("superClass")){
            symbolTable.addSupers(jmmNode.get("superClass"));
        }

        for (JmmNode child : jmmNode.getChildren()){

            if (child.getKind().equals("VarDeclaration")){
                Symbol symbol = utils.getSymbol(child);

                if (symbolTable.getFields().stream().anyMatch(s -> s.getName().equals(symbol.getName()))){
                    Report newReport = new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(child.get("lineStart")), Integer.parseInt(child.get("colStart")), "VarDeclaration Related ERROR");
                    reports.add(newReport);
                }
                else {
                    symbolTable.addFields(symbol);
                }
            }
            else{
                //visit(child, symbolTable);
            }
        }
        return true;
    }

    private Boolean dealWithFunctionMethod(JmmNode jmmNode, MySymbolTable symbolTable) {
        List<Symbol> parameters = new ArrayList<>();
        List<Symbol> variables = new ArrayList<>();

        String funcName = jmmNode.get("funcName");
        Type returnType;
        if (funcName.equals("main")){
            returnType = new Type("void", false);
            Symbol defaultParameter = new Symbol(new Type("String", true), jmmNode.get("name"));
            parameters.add(defaultParameter);
        }
        else {
            returnType = new Type(jmmNode.getJmmChild(0).get("name"), Boolean.parseBoolean(jmmNode.getJmmChild(0).get("isArray")));
        }



        for (JmmNode child : jmmNode.getChildren()){
            if (child.getKind().equals("Parameter")){
                Symbol symbol = utils.getSymbol(child);

                if (parameters.stream().anyMatch(s -> s.getName().equals(symbol.getName()))){
                    Report newReport = new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(child.get("lineStart")), Integer.parseInt(child.get("colStart")), "Parameter Related ERROR");
                    reports.add(newReport);
                }
                else {
                    parameters.add(symbol);
                }
            }
            else if (child.getKind().equals("VarDeclaration")){
                Symbol symbol = utils.getSymbol(child);

                if (Stream.concat(variables.stream(), parameters.stream()).anyMatch(s -> s.getName().equals(symbol.getName()))){
                    Report newReport = new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(child.get("lineStart")), Integer.parseInt(child.get("colStart")), "VarDeclaration Related ERROR");
                    reports.add(newReport);
                }
                else {
                    variables.add(symbol);
                }
            }
        }

        StringBuilder signature = new StringBuilder();
        signature.append(funcName);
        for (Symbol s : parameters){
            signature.append("#");
            signature.append(s.getType().print());
        }

        if (symbolTable.getMethods().contains(signature.toString())){
            Report newReport = new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(jmmNode.get("lineStart")), Integer.parseInt(jmmNode.get("colStart")), "Signature Related ERROR");
            reports.add(newReport);
        }
        else {
            symbolTable.addMethods(signature.toString(), parameters, variables, returnType);
        }
        jmmNode.put("signature", signature.toString());

        return true;
    }

    public List<Report> getReports() {
        return reports;
    }
}
