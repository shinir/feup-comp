package pt.up.fe.comp2023;

import pt.up.fe.comp.jmm.analysis.JmmAnalysis;
import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.parser.JmmParserResult;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp2023.visitors.AnalysisVisitor;
import pt.up.fe.comp2023.visitors.VariableVisitor;

import java.util.ArrayList;
import java.util.List;

public class Analysis implements JmmAnalysis {

    @Override
    public JmmSemanticsResult semanticAnalysis(JmmParserResult jmmParserResult) {
        MySymbolTable symbolTable = new MySymbolTable();
        var Visitor = new AnalysisVisitor();
        //var VariableVisitor = new VariableVisitor();
        JmmNode root = jmmParserResult.getRootNode();
        List<Report> reports = new ArrayList<Report>();

        Visitor.visit(root, symbolTable);
        //VariableVisitor.visit(root, symbolTable);
        System.out.println("SymbolTable: \n" + symbolTable.print());

        return new JmmSemanticsResult(jmmParserResult, symbolTable, reports);
    }

}
