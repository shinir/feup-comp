package pt.up.fe.comp2023.analysis.table;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;

import javax.sound.midi.SysexMessage;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public class Visitor extends PreorderJmmVisitor<MySymbolTable, Boolean> {
    private AnalysisUtils utils;
    @Override
    protected void buildVisitor() {
        addVisit("Program", this::dealWithProgram);
        addVisit("Import", this::dealWithImports);
        addVisit("Class", this::dealWithClass);
        //addVisit("VariableDeclaration", this::dealWithVarDeclaration);
        //addVisit("FunctionMethod", this::dealWithFunctMethods);
        //addVisit("MainMethod", this::dealWithMainMethods);
        //addVisit("Type", this::dealWithTypes);
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

        for (var child : jmmNode.getChildren()){
            if (!child.getKind().equals("VarDeclaration")) break;
            //symbolTable.addFields(new Symbol( (jmmNode.get("functName"), jmmNode.getOptional("name")) ), child.get("name"));
        }
        return true;
    }

    /*
    private Boolean dealWithVarDeclaration(JmmNode jmmNode, MySymbolTable symbolTable) {
        for (var child : jmmNode.getChildren()) {
            symbolTable.addFields(new Symbol(utils.getType(child.getJmmChild(0)), child.get("name")));
        }
        return true;
    }

    private Boolean dealWithFunctMethods(JmmNode jmmNode, MySymbolTable symbolTable) {
        var type = jmmNode.get("name");
        System.out.println(type);
        return true;
    }

    private Boolean dealWithMainMethods(JmmNode jmmNode, MySymbolTable symbolTable) {
        System.out.println();
        return true;
    }

    private Boolean dealWithTypes(JmmNode jmmNode, MySymbolTable symbolTable) {

        return true;
    }

    private Boolean dealWithLength(JmmNode jmmNode, MySymbolTable symbolTable) {

        return true;
    }
    */
    /*
    private Boolean dealWithLiteral(JmmNode jmmNode, MySymbolTable symbolTable) {
    }

    private Boolean dealWithExprStmt(JmmNode jmmNode, MySymbolTable symbolTable) {
    }

    private Boolean dealWithBinaryOp(JmmNode jmmNode, MySymbolTable symbolTable) {
    }
*/
}
