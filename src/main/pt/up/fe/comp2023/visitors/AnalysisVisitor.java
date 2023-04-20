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
    private final String defaultMessage = "DEFAULT ERROR MESSAGE";

    @Override
    protected void buildVisitor() {
        addVisit("importDeclaration", this::dealWithImports);
        addVisit("Class", this::dealWithClass);
        addVisit("FunctionMethodDeclaration", this::dealWithFunctionMethod);
        addVisit("MainMethodDeclaration", this::dealWithMainMethod);
        this.setDefaultVisit(this::myVisitAllChildren);
    }

    private Boolean myVisitAllChildren(JmmNode jmmNode, MySymbolTable symbolTable) {

        for (JmmNode child : jmmNode.getChildren()){
            visit(child, symbolTable);
        }
        return true;
    }

    private Boolean dealWithImports(JmmNode jmmNode, MySymbolTable symbolTable) {
        StringBuilder path = new StringBuilder();

        for (JmmNode node : jmmNode.getChildren()) {
            path.append(node.get("name"));
            path.append('.');
        }
        path.deleteCharAt(path.length() -1);

        if (!symbolTable.getImports().contains(path.toString())){
            String last = path.toString().substring(path.toString().lastIndexOf('.')+1);
            Optional<String> match = symbolTable
                    .getImports()
                    .stream()
                    .filter(s -> s.substring(s.lastIndexOf('.')+1).equals(last))
                    .findAny();

            if (match.isPresent()){
                Report newReport = new Report(ReportType.ERROR, Stage.SEMANTIC, 0, 0, defaultMessage);
                reports.add(newReport);
                return true;
            }
            symbolTable.addImports(path.toString());
        }

        return true;
    }

    private Boolean dealWithClass(JmmNode jmmNode, MySymbolTable symbolTable) {
        symbolTable.addClassName(jmmNode.get("name"));


        if (jmmNode.getAttributes().contains("superClass"))
            symbolTable.addSupers(jmmNode.get("superClass"));

        for (JmmNode node : jmmNode.getChildren()){

            if (node.getKind().equals("varDeclaration")) {
                Symbol symbol = utils.getSymbol(node);

                // checks whether a symbol with the same name
                // as the symbol object already exists in the
                // symbol table's list of fields.
                if (symbolTable.getFields().stream().anyMatch(s -> s.getName().equals(symbol.getName()))){
                    Report newReport = new Report(ReportType.ERROR, Stage.SEMANTIC, 0, 0, defaultMessage);
                    reports.add(newReport);
                }
                else {
                    symbolTable.addFields(symbol);
                }
            }
            else {
                visit(node, symbolTable);
            }
        }
        return true;
    }

    private Boolean dealWithFunctionMethod(JmmNode jmmNode, MySymbolTable symbolTable) {

        String functionName = jmmNode.get("funcName");
        Type returnType = utils.getType(jmmNode.getJmmChild(0));
        List<Symbol> parameters = new ArrayList<>();
        List<Symbol> variables = new ArrayList<>();


        for (JmmNode node : jmmNode.getChildren()) {

            if (node.getKind().equals("parameter")) {
                Symbol param = new Symbol(utils.getType(node.getJmmChild(0)), node.getJmmChild(0).get("name"));

                if (parameters.stream().anyMatch(s -> s.getName().equals(param.getName()))) {
                    Report newReport = new Report(ReportType.ERROR, Stage.SEMANTIC, 0, 0,defaultMessage);
                    reports.add(newReport);
                } else {
                    parameters.add(param);
                }
            }

            if (node.getKind().equals("varDeclaration")) {
                Symbol symbol = utils.getSymbol(node);
                if (Stream.concat(variables.stream(), parameters.stream()).anyMatch(s -> s.getName().equals(symbol.getName()))) {

                    Report newReport = new Report(ReportType.ERROR, Stage.SEMANTIC, 0, 0, defaultMessage);
                    reports.add(newReport);
                } else {
                    variables.add(symbol);
                }
            }

        }

        StringBuilder method = new StringBuilder();
        method.append(functionName);
        for (Symbol parameter: parameters) {
            method.append("#");
            method.append(parameter.getType().print());
        }

        if (symbolTable.getMethods().contains(method.toString())){
            Report newReport = new Report(ReportType.ERROR, Stage.SEMANTIC, 0, 0, defaultMessage);
            reports.add(newReport);
        }
        else {
            symbolTable.addMethods(functionName, parameters, variables, returnType);
        }

        jmmNode.put("signature", method.toString());
        return true;
    }

    
    private Boolean dealWithMainMethod(JmmNode jmmNode, MySymbolTable symbolTable) {
        List<Symbol> parameters = new ArrayList<>();
        List<Symbol> variables = new ArrayList<>();

        symbolTable.addMethods("main", parameters, variables, new Type("void", false));
        return true;
    }

    public List<Report> getReports() {
        return reports;
    }
}
