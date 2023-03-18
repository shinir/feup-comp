package pt.up.fe.comp2023.analysis;

import pt.up.fe.comp.jmm.analysis.JmmAnalysis;
import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.parser.JmmParserResult;
import pt.up.fe.comp2023.analysis.table.MySymbolTable;
import pt.up.fe.comp2023.analysis.table.AnalysisVisitor;

import java.util.Collections;

public class Analysis implements JmmAnalysis {

    @Override
    public JmmSemanticsResult semanticAnalysis(JmmParserResult jmmParserResult) {
        MySymbolTable symbolTable = new MySymbolTable();
        var Visitor = new AnalysisVisitor();
        JmmNode root = jmmParserResult.getRootNode();
        Visitor.visit(root, symbolTable);

        return new JmmSemanticsResult(jmmParserResult, symbolTable, Collections.emptyList());
    }

}