package pt.up.fe.comp2023.analysis.table;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AnalysisVisitor extends PreorderJmmVisitor<MySymbolTable, Boolean> {
    private final AnalysisUtils utils = new AnalysisUtils();

    // Operators
    List<String> Types = Arrays.asList("int[]", "boolean", "char", "string");
    List<String> Operations = Arrays.asList("+", "-", "*", "/");
    List<String> Comparison = Arrays.asList("<", "<=", ">", ">=");
    List<String> Logical = Arrays.asList("&&", "||", "!");

    @Override
    protected void buildVisitor() {
        addVisit("Program", this::dealWithProgram);
        addVisit("Import", this::dealWithImports);
        addVisit("Class", this::dealWithClass);
        addVisit("VarDeclaration", this::dealWithClass);
        addVisit("MethodDeclaration", this::dealWithMethods);
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
        for (var node : jmmNode.getChildren()){
            if (node.getKind().equals("VarDeclaration")){
                Symbol symbol = new Symbol(utils.getType(node.getJmmChild(0)), node.get("name"));
                symbolTable.addFields(symbol);
            }
        }
        return true;
    }

    private Boolean dealWithMethods(JmmNode jmmNode, MySymbolTable symbolTable) {
        String functionName;
        Type returnType = utils.getType(jmmNode.getJmmChild(0));
        List<Symbol> parameters = new ArrayList<>();
        List<Symbol> variables = new ArrayList<>();

        if(jmmNode.getKind().equals("FunctionMethod"))
            functionName = jmmNode.get("funcName");
        System.out.println(jmmNode.getKind());

        // Parameters
        for(JmmNode node : jmmNode.getChildren()){
            if(node.getKind().equals("VarDeclaration")) {
                //System.out.println(node.getKind());
            }
            else {
                Symbol symbol = new Symbol(utils.getType(node.getJmmParent()), node.getJmmParent().getKind());
                parameters.add(symbol);
            }
        }

        return true;
    }

    private Boolean dealWithTypes(JmmNode jmmNode, MySymbolTable symbolTable) {
        Type type = utils.getType(jmmNode);
        //System.out.println("node " + jmmNode);


        return false;
    }
}
