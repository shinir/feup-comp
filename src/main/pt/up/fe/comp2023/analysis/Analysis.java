package pt.up.fe.comp2023.analysis;

import pt.up.fe.comp.jmm.analysis.JmmAnalysis;
import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.parser.JmmParserResult;
import pt.up.fe.comp2023.analysis.table.MySymbolTable;
import pt.up.fe.comp2023.analysis.table.Visitor;

import java.util.Collections;

public class Analysis implements JmmAnalysis {

    @Override
    public JmmSemanticsResult semanticAnalysis(JmmParserResult jmmParserResult) {
        MySymbolTable symbolTable = new MySymbolTable();
        Visitor visitor = new Visitor(symbolTable);
        JmmNode root = jmmParserResult.getRootNode();

        var Visitor = new Visitor(symbolTable);
        Visitor.visit(root, symbolTable);
        System.out.println("Imports: " + symbolTable.getImports());

        return new JmmSemanticsResult(jmmParserResult, symbolTable, Collections.emptyList());
    }

}
