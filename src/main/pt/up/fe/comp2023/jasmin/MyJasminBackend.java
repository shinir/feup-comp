package pt.up.fe.comp2023.jasmin;

import org.specs.comp.ollir.ClassUnit;
import pt.up.fe.comp.jmm.jasmin.JasminBackend;
import pt.up.fe.comp.jmm.jasmin.JasminResult;
import pt.up.fe.comp.jmm.ollir.OllirResult;

import java.util.Collections;

public class MyJasminBackend implements JasminBackend {

    @Override
    public JasminResult toJasmin(OllirResult ollirResult) {
        String code = new OLLIRtoJasmin(ollirResult.getOllirClass()).buildBasic();
        return new JasminResult(ollirResult, code, Collections.emptyList());
    }
}
