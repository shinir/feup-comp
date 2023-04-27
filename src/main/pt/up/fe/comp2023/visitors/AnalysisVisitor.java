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

    @Override
    protected void buildVisitor() {
        addVisit("Program", this::dealWithProgram);
        addVisit("Import", this::dealWithImports);
        addVisit("Class", this::dealWithClass);
        addVisit("FunctionMethodDeclaration", this::dealWithFunctionMethod);
        addVisit("MainMethodDeclaration", this::dealWithMainMethod);
        this.setDefaultVisit(this::myVisitAllChildren);
    }

    private Boolean dealWithProgram(JmmNode jmmNode, MySymbolTable symbolTable) {
        return true;
    }

    private Boolean myVisitAllChildren(JmmNode jmmNode, MySymbolTable symbolTable) {
        return true;
    }

    private Boolean dealWithImports(JmmNode jmmNode, MySymbolTable symbolTable) {

        StringBuilder imported = new StringBuilder();
        String importString = jmmNode.get("importName");
        for (String path : importString.substring(1 , importString.length() -1 ).replaceAll("\\s+", "").split(",")){
            imported.append(path);
            imported.append(".");
        }

        imported.deleteCharAt(imported.length()-1);
        String importedPath = imported.toString();

        if (symbolTable.getImports().contains(importedPath)){
            String last = importedPath.substring(importedPath.lastIndexOf(".") +1);
            Optional<String> match = symbolTable
                    .getImports()
                    .stream()
                    .filter(s -> s.substring(s.lastIndexOf('.') + 1).equals(last))
                    .findAny();
            if (match.isPresent()) {
                Report newReport = new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(jmmNode.get("lineStart")), Integer.parseInt(jmmNode.get("colStart")), "Already imported");
                reports.add(newReport);
                return true;
            }
            symbolTable.addImports(importedPath);//must
        }

        return true;
    }

    private Boolean dealWithClass(JmmNode jmmNode, MySymbolTable symbolTable) {
        //System.out.println();
        //must
        symbolTable.addClassName(jmmNode.get("name"));
        if(jmmNode.hasAttribute("superClass")) {
            symbolTable.addSupers(jmmNode.get("superClass"));
        }
        for (var node : jmmNode.getChildren()){
            if (node.getKind().equals("VarDeclaration")) {
                Type type = utils.getType(node.getJmmChild(0), symbolTable);
                Symbol symbol = new Symbol(type, node.getJmmChild(0).get("name"));

                if (symbolTable.getFields().stream().anyMatch(s -> s.getName().equals(symbol.getName()))) {
                    Report newReport = new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(jmmNode.get("lineStart")), Integer.parseInt(jmmNode.get("colStart")), "Variable already defined ");
                    reports.add(newReport);
                }
                else {
                    symbolTable.addFields(symbol);
                }
            }
            /*
            else {
                visit(node, symbolTable);
            }*/

        }
        //
        return true;
    }

    private Boolean dealWithFunctionMethod(JmmNode jmmNode, MySymbolTable symbolTable) {
        // must
        String functionName = jmmNode.get("funcName");;
        Type returnType = utils.getType(jmmNode.getJmmChild(0), symbolTable);
        List<Symbol> parameters = new ArrayList<>();
        List<Symbol> variables = new ArrayList<>();

        for(JmmNode node : jmmNode.getChildren()) {
            if(node.getKind().equals("VarDeclaration")) {
                Symbol symbol = utils.getSymbol(node, symbolTable);
                if (Stream.concat(variables.stream(), parameters.stream()).anyMatch(s -> s.getName().equals(symbol.getName()))){
                    Report newReport = new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(jmmNode.get("lineStart")), Integer.parseInt(jmmNode.get("colStart")), "Already defined ");
                    reports.add(newReport);
                }
                else {
                    variables.add(symbol);
                }
            }

            if(node.getKind().equals("Parameter")) {
                Symbol param = new Symbol(utils.getType(node.getJmmChild(0), symbolTable), node.getJmmChild(0).get("name"));
                if (parameters.stream().anyMatch(s -> s.getName().equals(param.getName()))) {
                    Report newReport = new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(jmmNode.get("lineStart")), Integer.parseInt(jmmNode.get("colStart")), "Already defined ");
                    reports.add(newReport);
                }
                else {
                    parameters.add(param);
                }
            }
        }

        StringBuilder signature = new StringBuilder();
        signature.append(functionName);
        for (Symbol param : parameters){
            signature.append("#");
            signature.append(param.getType().print());
        }
        //String stringSignature = signature.toString();

        /*if (symbolTable.getMethods().contains(signature.toString())){
            Report newReport = new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(jmmNode.get("lineStart")), Integer.parseInt(jmmNode.get("colStart")), "Already defined method");
            reports.add(newReport);
        }
        else {
            symbolTable.addMethods(signature.toString(), parameters, variables, returnType);
        }*/

        symbolTable.addMethods(functionName, parameters, variables, returnType);

        jmmNode.put("signature", signature.toString());
        return true;
    }

    private Boolean dealWithMainMethod(JmmNode jmmNode, MySymbolTable symbolTable) {
        List<Symbol> parameters = new ArrayList<>();
        List<Symbol> variables = new ArrayList<>();

        for(JmmNode node : jmmNode.getChildren()) {
            if(node.getKind().equals("VarDeclaration")) {
                Symbol symbol = utils.getSymbol(node, symbolTable);
                variables.add(symbol);
            }

            if(node.getKind().equals("Parameter")) {
                Symbol param = new Symbol(utils.getType(node.getJmmChild(0), symbolTable), node.get("name"));
                parameters.add(param);
            }
        }

        symbolTable.addMethods("main", parameters, variables, new Type("void", false));
        return true;
    }

    public List<Report> getReports() {
        return reports;
    }
}
