package pt.up.fe.comp2023;

import pt.up.fe.comp.jmm.analysis.JmmAnalysis;
import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.parser.JmmParserResult;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp2023.visitors.AnalysisVisitor;
import pt.up.fe.comp2023.visitors.VariableVisitor;
import pt.up.fe.specs.util.SpecsCollections;

import java.util.ArrayList;
import java.util.List;

public class Analysis implements JmmAnalysis {

    @Override
    public JmmSemanticsResult semanticAnalysis(JmmParserResult jmmParserResult) {
        MySymbolTable symbolTable = new MySymbolTable();

        var analysisVisitor = new AnalysisVisitor(); //Symbol Table
        var variableVisitor = new VariableVisitor(); //Semantic

        JmmNode root = jmmParserResult.getRootNode();

        analysisVisitor.visit( root, symbolTable );
        variableVisitor.visit( root, symbolTable );


        List<Report> reports = new ArrayList<Report>();

        //VariableVisitor.visit(root, symbolTable);
        System.out.println("SymbolTable: \n" + symbolTable.print());

        //reports = SpecsCollections.concat(analysisVisitor.getReports(), variableVisitor.getReports());

        return new JmmSemanticsResult(jmmParserResult, symbolTable, reports);
    }

}
