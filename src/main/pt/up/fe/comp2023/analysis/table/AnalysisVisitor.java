package pt.up.fe.comp2023.analysis.table;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AnalysisVisitor extends PreorderJmmVisitor<MySymbolTable, Boolean> {
    private final AnalysisUtils utils = new AnalysisUtils();

    @Override
    protected void buildVisitor() {
        addVisit("Import", this::dealWithImports);
        addVisit("Class", this::dealWithClass);
        addVisit("FunctionMethodDeclaration", this::dealWithFunctionMethod);
        addVisit("MainMethodDeclaration", this::dealWithMainMethod);
        this.setDefaultVisit(this::myVisitAllChildren);
    }

    private Boolean myVisitAllChildren(JmmNode jmmNode, MySymbolTable symbolTable) {
        return true;
    }

    private Boolean dealWithImports(JmmNode jmmNode, MySymbolTable symbolTable) {
        symbolTable.addImports("." + jmmNode.get("importName"));
        return true;
    }

    private Boolean dealWithClass(JmmNode jmmNode, MySymbolTable symbolTable) {
        symbolTable.addClassName(jmmNode.get("name"));
        if(jmmNode.hasAttribute("superClass")) {
            symbolTable.addSupers(jmmNode.get("superClass"));
        }
        for (var node : jmmNode.getChildren()){
            if (node.getKind().equals("VarDeclaration")) {
                Symbol symbol = new Symbol(utils.getType(node.getJmmChild(0)), node.get("name"));
                symbolTable.addFields(symbol);
            }
        }
        return true;
    }

    private Boolean dealWithFunctionMethod(JmmNode jmmNode, MySymbolTable symbolTable) {
        String functionName;
        Type returnType = utils.getType(jmmNode.getJmmChild(0));
        List<Symbol> parameters = new ArrayList<>();
        List<Symbol> variables = new ArrayList<>();

        functionName = jmmNode.get("funcName");

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
}
