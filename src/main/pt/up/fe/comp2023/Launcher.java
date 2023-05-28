package pt.up.fe.comp2023;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import pt.up.fe.comp.TestUtils;
import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp.jmm.parser.JmmParserResult;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.comp2023.ollir.JmmOptimize;
import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.SpecsLogs;
import pt.up.fe.specs.util.SpecsSystem;

public class Launcher {

    public static void main(String[] args) throws Exception {
        // Setups console logging and other things
        SpecsSystem.programStandardInit();

        // Parse arguments as a map with predefined options
        var config = parseArgs(args);

        // Get input file
        File inputFile = new File(config.get("inputFile"));

        // Check if file exists
        if (!inputFile.isFile()) {
            throw new RuntimeException("Expected a path to an existing input file, got '" + inputFile + "'.");
        }

        // Read contents of input file
        String code = SpecsIo.read(inputFile);
        //code = SpecsIo.read("test/pt/up/fe/comp/cp2/ollir/CompileBasic.jmm");
        //code = SpecsIo.read("test/pt/up/fe/comp/cp2/ollir/CompileAssignment.jmm");
        //code = SpecsIo.read("test/pt/up/fe/comp/cpf/3_ollir/control_flow/SimpleIfElseStat.jmm");
        //code = SpecsIo.read("test/pt/up/fe/comp/cp2/ollir/CompileArithmetic.jmm");
        code = SpecsIo.read("test/pt/up/fe/comp/cpf/3_ollir/control_flow/SimpleWhileStat.jmm");
        // Instantiate JmmParser
        SimpleParser parser = new SimpleParser();

        // Parse stage
        JmmParserResult parserResult = parser.parse(code, config);

        System.out.println(parserResult.getRootNode().toTree());

        // Check if there are parsing errors
        TestUtils.noErrors(parserResult.getReports());

        Analysis analysis = new Analysis();
        JmmSemanticsResult semanticsResult = analysis.semanticAnalysis(parserResult);

        JmmOptimize optimizer = new JmmOptimize();
        OllirResult ollirResult = optimizer.toOllir(semanticsResult);

        System.out.println("\nCODE OLLIR:\n");
        System.out.println(ollirResult.getOllirCode());

        /*
        if(TestUtils.getNumErrors(parserResult.getReports()) != 0) {
            parserResult.getReports().add(new Report(ReportType.ERROR, Stage.SYNTATIC, 0, "AST root node is null."));
            return new parserResult.getReports(null, parser.);
        }
        */

        // ... add remaining stages
    }

    private static Map<String, String> parseArgs(String[] args) {
        SpecsLogs.info("Executing with args: " + Arrays.toString(args));

        // Check if there is at least one argument
        if (args.length != 1) {
            throw new RuntimeException("Expected a single argument, a path to an existing input file.");
        }

        // Create config
        Map<String, String> config = new HashMap<>();
        config.put("inputFile", args[0]);
        config.put("optimize", "false");
        config.put("registerAllocation", "-1");
        config.put("debug", "false");

        return config;
    }

}
