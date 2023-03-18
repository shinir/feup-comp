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
        addVisit("Program", this::dealWithProgram);
        addVisit("Import", this::dealWithImports);
        addVisit("Class", this::dealWithClass);
        addVisit("VarDeclaration", this::dealWithVarDeclaration);
        addVisit("FunctionMethod", this::dealWithFunctionMethod);
        addVisit("MainMethod", this::dealWithMainMethod);
        addVisit("Type", this::dealWithTypes);
        //addVisit("Statement", this::dealWithStatements);
        //addVisit("Expression", this::dealWithExpressions);
        //addVisit("Return", this::dealWithReturn);
        //addVisit("GetLength", this::dealWithLength);
    }

    private Boolean dealWithProgram(JmmNode jmmNode, MySymbolTable symbolTable) {
        for(JmmNode node : jmmNode.getChildren()) {
            if(!node.getKind().equals("importName"))
                return false;
            visit(node, symbolTable);
        }
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
        return true;
    }

    private Boolean dealWithVarDeclaration(JmmNode jmmNode, MySymbolTable symbolTable) {
        for (var node : jmmNode.getChildren()){
            if (!jmmNode.getKind().equals("VarDeclaration")) return false;
            Symbol symbol = new Symbol(utils.getType(node), node.getKind());
            symbolTable.addFields(symbol);
        }
        return true;
    }

    private Boolean dealWithFunctionMethod(JmmNode jmmNode, MySymbolTable symbolTable) {
        String functionName;
        Type functionType = utils.getType(jmmNode.getJmmChild(0));
        Type returnType = new Type(jmmNode.getJmmChild(jmmNode.getNumChildren()-1).get("name"), Objects.equals(jmmNode.getKind(), "Array"));
        List<Symbol> parameters = new ArrayList<>();
        List<Symbol> variables = new ArrayList<>();

        functionName = jmmNode.get("funcName");

        if(jmmNode.hasAttribute("type")) {
            Symbol param = new Symbol(utils.getType(jmmNode), jmmNode.get("name"));
            parameters.add(param);
        }

        for(JmmNode node : jmmNode.getChildren()) {
            if(node.getKind().equals("VarDeclaration")) {
                Symbol symbol = utils.getSymbol(node);
                variables.add(symbol);
            }
        }

        if(!functionType.equals(returnType)) return false;

        symbolTable.addMethods(functionName, parameters, variables, returnType);

        return true;
    }

    private Boolean dealWithMainMethod(JmmNode jmmNode, MySymbolTable symbolTable) {
        return true;
    }

    private Boolean dealWithTypes(JmmNode jmmNode, MySymbolTable symbolTable) {
        Type type;
        System.out.println(jmmNode.getKind());
        if(jmmNode.getKind().equals("Variable")) {
            type = new Type(jmmNode.getJmmChild(0).get("name"), Objects.equals(jmmNode.getKind(), "Array"));
        }
        else {
            type = utils.getType(jmmNode);
        }

        return true;
    }
}
