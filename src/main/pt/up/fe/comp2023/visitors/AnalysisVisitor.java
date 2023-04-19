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
        addVisit("importDeclaration", this::dealWithImports);
        addVisit("Class", this::dealWithClass);
        addVisit("FunctionMethodDeclaration", this::dealWithFunctionMethod);
        addVisit("MainMethodDeclaration", this::dealWithMainMethod);
        this.setDefaultVisit(this::myVisitAllChildren);
    }

    private Boolean myVisitAllChildren(JmmNode jmmNode, MySymbolTable symbolTable) {
        //List<Report> report = new ArrayList<>();
        for (JmmNode child : jmmNode.getChildren()){
            visit(child, symbolTable);
        }
        return true;
    }

    private Boolean dealWithImports(JmmNode jmmNode, MySymbolTable symbolTable) {
    /*
        StringBuilder importPath = new StringBuilder();

        for (JmmNode child: jmmNode.getChildren()) {
            importPath.append(child.get("importName"));
            importPath.append('.');
        }

        importPath.deleteCharAt(importPath.length()-1);
        String importPathString = importPath.toString();
        if (!symbolTable.getImports().contains(importPathString)) {
            String lastName = importPathString.substring(importPathString.lastIndexOf('.') + 1);
            Optional<String> match = symbolTable
                    .getImports()
                    .stream()
                    .filter(s -> s.substring(s.lastIndexOf('.') + 1).equals(lastName))
                    .findAny();
            if (match.isPresent()) {

                StringBuilder message = new StringBuilder();
                message.append("a type with the same simple name ");
                message.append(lastName);
                message.append(" has already been imported from ");
                message.append(match.get());



                //Report newReport = new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(jmmNode.get("line")), Integer.parseInt(jmmNode.get("col")), message.toString());
                //reports.add(newReport);

            }
            //symbolTable.addImports(importPathString);
        }
*/
        return true;
    }

    private Boolean dealWithClass(JmmNode jmmNode, MySymbolTable symbolTable) {
        //List<Report> reports = new ArrayList<>();

        symbolTable.addClassName(jmmNode.get("name"));
        if(jmmNode.hasAttribute("superClass")) {
            symbolTable.addSupers(jmmNode.get("superClass"));
        }

        for (JmmNode node : jmmNode.getChildren()){
            if (node.getKind().equals("VarDeclaration")) {
                Type type = utils.getType(node.getJmmChild(0));
                Symbol symbol = new Symbol(type, node.getJmmChild(0).get("name"));

                if (symbolTable.getFields().stream().anyMatch(s -> s.getName().equals(symbol.getName()))) {
                    StringBuilder message = new StringBuilder();
                    message.append("variable ");
                    message.append(symbol.getName());
                    message.append(" is already defined in ");
                    message.append("class");
                    message.append(" ");
                    message.append(symbolTable.getClassName());
                    Report newReport = new Report(ReportType.ERROR, Stage.SEMANTIC, 0, 0, message.toString());
                    reports.add(newReport);
                    return false;
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

        //functionName = jmmNode.get("funcName");

        for (JmmNode node : jmmNode.getChildren()) {

            if (node.getKind().equals("Parameter")) {
                Symbol param = new Symbol(utils.getType(node.getJmmChild(0)), node.getJmmChild(0).get("name"));

                if (parameters.stream().anyMatch(s -> s.getName().equals(param.getName()))) {

                    StringBuilder message = new StringBuilder();
                    message.append("variable ");
                    message.append(param.getName());
                    message.append(" is already defined in method ");
                    message.append(functionName);

                    Report newReport = new Report(ReportType.ERROR, Stage.SEMANTIC, 0, 0, message.toString());
                    reports.add(newReport);

                } else {
                    parameters.add(param);
                }
            }

            if (node.getKind().equals("VarDeclaration")) {
                Symbol symbol = utils.getSymbol(node);
                variables.add(symbol);
                if (Stream.concat(variables.stream(), parameters.stream()).anyMatch(s -> s.getName().equals(symbol.getName()))) {

                    StringBuilder message = new StringBuilder();
                    message.append("variable ");
                    message.append(symbol.getName());
                    message.append(" is already defined in method ");
                    message.append(functionName);


                    Report newReport = new Report(ReportType.ERROR, Stage.SEMANTIC, 0, 0, message.toString());
                    reports.add(newReport);
                } else {
                    variables.add(symbol);
                }
            }

        }

        symbolTable.addMethods(functionName, parameters, variables, returnType);
        return true;
    }

    private Boolean dealWithMainMethod(JmmNode jmmNode, MySymbolTable symbolTable) {
        List<Symbol> parameters = new ArrayList<>();
        List<Symbol> variables = new ArrayList<>();

        for(JmmNode node : jmmNode.getChildren()) {
            if(node.getKind().equals("VarDeclaration")) {
                Symbol symbol = utils.getSymbol(node);
                variables.add(symbol);
            }

            if(node.getKind().equals("Parameter")) {
                Symbol param = new Symbol(utils.getType(node.getJmmChild(0)), node.get("name"));
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
