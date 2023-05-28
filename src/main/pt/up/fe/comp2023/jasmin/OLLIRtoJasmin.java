package pt.up.fe.comp2023.jasmin;

import org.specs.comp.ollir.*;
import pt.up.fe.specs.util.exceptions.NotImplementedException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Collectors;

public class OLLIRtoJasmin {
    private final ClassUnit classUnit;
    private int lbl = 0;

    public OLLIRtoJasmin(ClassUnit classUnit) {
        this.classUnit = classUnit;
        this.classUnit.buildVarTables();
    }

    public String buildBasic() {
        StringBuilder code = new StringBuilder();

        // Class
        code.append(".class public ").append(this.classUnit.getClassName()).append("\n");

        // Super Class
        String superClass = this.classUnit.getSuperClass();
        String superName;
        if(superClass != null) {
            superName = JasminUtils.getSuperPath(classUnit, superClass);
        }
        else {
            superName = "java/lang/Object";
        }

        code.append(".super ").append(superName).append("\n\n");

        // Fields
        for (Field field : this.classUnit.getFields()) {
            code.append(this.getField(field));
        }

        // Constructor
        code.append(".method public <init>()V\n").append("aload_0\n").append("invokespecial ").append(superName).append("/<init>()V\n")
                .append("return\n").append(".end method\n");

        // Methods
        for (Method method : this.classUnit.getMethods()) {
            if (!method.isConstructMethod()) {
                code.append(getMethod(method));
            }
        }

        return code.toString();
    }

    public String getField(Field field) {
        StringBuilder code = new StringBuilder();

        // Modifiers
        AccessModifiers access = field.getFieldAccessModifier();
        StringBuilder fields = new StringBuilder();
        if (access != AccessModifiers.DEFAULT) fields.append(access.name().toLowerCase()).append(" ");
        if (field.isStaticField()) fields.append("static ");
        if (field.isFinalField()) fields.append("final ");
        code.append(".field ").append(fields);

        // Name
        code.append(field.getFieldName()).append(" ");
        // Return type
        code.append(getJasminType(field.getFieldType()));

        // Initialization
        if (field.isInitialized()) code.append(" = ").append(field.getInitialValue());

        code.append("\n");
        return code.toString();
    }

    public String getMethod(Method method) {
        StringBuilder code = new StringBuilder();

        AccessModifiers access = method.getMethodAccessModifier();
        String methods = "";
        if (access != AccessModifiers.DEFAULT) methods = access.name().toLowerCase() + " ";
        code.append(".method ").append(methods);
        if (method.isStaticMethod()) code.append("static ");

        code.append(method.getMethodName()).append("(");
        String params = method.getParams().stream().map(element -> getJasminType(element.getType())).collect(Collectors.joining());
        code.append(params).append(")").append(getJasminType(method.getReturnType())).append("\n");
        code.append(".limit stack 99\n").append(".limit locals 99\n");

        HashMap<String, Instruction> methodLabels = method.getLabels();

        for (Instruction instruction : method.getInstructions()) {
            code.append(getCode(method, instruction));

            for(String label : methodLabels.keySet()) {
                if(methodLabels.get(label).equals(instruction)) {
                    code.append(":\n");
                }
            }

            if(instruction.getInstType().equals(InstructionType.CALL)) {
                ElementType type = ((CallInstruction) instruction).getReturnType().getTypeOfElement();
                if(((CallInstruction) instruction).getInvocationType() == CallType.invokespecial || type != ElementType.VOID) {
                    code.append("pop\n");
                }
            }
        }

        code.append(".end method\n");
        return code.toString();
    }

    public String getCode(Method method, Instruction instruction) {
        if (instruction instanceof AssignInstruction) return getCode(method, (AssignInstruction) instruction);
        if (instruction instanceof CallInstruction) return getCode(method, (CallInstruction) instruction);
        if (instruction instanceof GetFieldInstruction) return getCode(method, (GetFieldInstruction) instruction);
        if (instruction instanceof PutFieldInstruction) return getCode(method, (PutFieldInstruction) instruction);
        if (instruction instanceof CondBranchInstruction) return getCode(method, (CondBranchInstruction) instruction);
        if (instruction instanceof GotoInstruction) return getCode(method, (GotoInstruction) instruction);
        if (instruction instanceof ReturnInstruction) return getCode(method, (ReturnInstruction) instruction);
        throw new NotImplementedException(instruction.getClass());
    }

    public String getCode(Method method, GetFieldInstruction fieldInstruction) {
        StringBuilder code = new StringBuilder();

        code.append(getLoad(method.getVarTable(), fieldInstruction.getFirstOperand()));
        code.append(getField(fieldInstruction.getFirstOperand(), fieldInstruction.getSecondOperand()));

        return code.toString();
    }

    public String getCode(Method method, PutFieldInstruction fieldInstruction) {
        StringBuilder code = new StringBuilder();
        // class, field, value
        code.append(getLoad(method.getVarTable(), fieldInstruction.getFirstOperand()));
        code.append(getLoad(method.getVarTable(), fieldInstruction.getThirdOperand()));
        code.append(setField(fieldInstruction.getFirstOperand(), fieldInstruction.getSecondOperand()));

        return code.toString();
    }

    public String getCode(Method method, CallInstruction callInstruction) {
        return switch (callInstruction.getInvocationType()) {
            case NEW -> getCodeNew(callInstruction, method);
            case arraylength -> getCodeArray(callInstruction, method);
            case invokestatic -> getInvokeStatic(callInstruction, method);
            case invokespecial -> getInvokeSpecial(callInstruction, method);
            case invokevirtual -> getInvokeVirtual(callInstruction, method);
            default -> throw new NotImplementedException(callInstruction.getInvocationType());
        };
    }

    public String getCodeNew(CallInstruction callInstruction, Method method) {
        StringBuilder code = new StringBuilder();
        callInstruction.show();

        if(callInstruction.getReturnType().getTypeOfElement() != ElementType.ARRAYREF) {
            String returnType = ((ClassType)callInstruction.getReturnType()).getName();
            code.append("new ").append(JasminUtils.getSuperPath(classUnit,returnType)).append("\n").append("dup\n");
        }
        else {
            String load = getLoad(method.getVarTable(), callInstruction.getListOfOperands().get(0));
            code.append(load).append("newarray int\n");
        }

        return code.toString();
    }

    public String getCodeArray(CallInstruction callInstruction, Method method) {
        StringBuilder code = new StringBuilder();
        code.append(getLoad(method.getVarTable(), callInstruction.getFirstArg()))
            .append("arraylength\n");

        return code.toString();
    }

    public String getCode(Method method, AssignInstruction assignInstruction) {
        StringBuilder code = new StringBuilder();
        String rhs = getOperand(method, assignInstruction.getRhs());

        code.append(getStore(assignInstruction.getDest(), rhs, method.getVarTable()));

        return code.toString();
    }

    public String getCode(Method method, ReturnInstruction returnInstruction) {
        if(returnInstruction.hasReturnValue()) {
            StringBuilder code = new StringBuilder();
            Element result = returnInstruction.getOperand();
            ElementType type = result.getType().getTypeOfElement();

            code.append(getLoad(method.getVarTable(), result));

            if(type == ElementType.INT32 || type == ElementType.BOOLEAN) {
                code.append("ireturn\n");
            }
            else {
                code.append("areturn\n");
            }
            return code.toString();
        }
        return "return\n";
    }

    public String getCode(Method method, CondBranchInstruction instruction) {
        StringBuilder code = new StringBuilder();
        var opInstruction = (SingleOpInstruction) instruction.getCondition();
        String noper = getNoper(method.getVarTable(), opInstruction);
        code.append(noper);
        if(opInstruction.getSingleOperand().isLiteral()) code.append("ifne").append(instruction.getLabel()).append("\n");
        else code.append("if_icmplt").append(instruction.getLabel()).append("\n");

        return code.toString();
    }

    public String getCode(Method method, GotoInstruction instruction) {
        StringBuilder code = new StringBuilder();
        code.append("goto ").append(instruction.getLabel()).append("\n");
        return code.toString();
    }

    private String getField(Element classElement, Element fieldElement) {
        StringBuilder code = new StringBuilder();
        String field = ((Operand)fieldElement).getName();
        String name = JasminUtils.getSuperPath(classUnit, ((ClassType)classElement.getType()).getName());

        code.append("getfield ").append(name).append("/");
        code.append(field).append(" ").append(getJasminType(fieldElement.getType())).append("\n");

        return code.toString();
    }

    private String setField(Element classElement, Element fieldElement) {
        StringBuilder code = new StringBuilder();
        String field = ((Operand)fieldElement).getName();
        String name = JasminUtils.getSuperPath(classUnit, ((ClassType)classElement.getType()).getName());

        code.append("putfield ").append(name).append("/");
        code.append(field).append(" ").append(getJasminType(fieldElement.getType())).append("\n");

        return code.toString();
    }

    private String call(String className) {
        String out = JasminUtils.getSuperPath(classUnit, className);
        return "new " + out +  '\n';
    }

    private String getStore(Element l, String r, HashMap<String, Descriptor> hash) {
        ElementType type = l.getType().getTypeOfElement();
        Descriptor reg = hash.get(((Operand) l).getName());

        if (type == ElementType.INT32 || type == ElementType.STRING || type == ElementType.BOOLEAN) {
            if(reg.getVarType().getTypeOfElement() == ElementType.ARRAYREF) {
                return getLoad(hash, l) + "iaload\n";
            }
            return r + "istore" + store(reg.getVirtualReg());
        }
        else if (type == ElementType.OBJECTREF || type == ElementType.THIS || type == ElementType.ARRAYREF) {
            return r + "astore" + store(reg.getVirtualReg());
        }
        return "";
    }

    private String store(int reg) {
        StringBuilder code = new StringBuilder();
        if (reg >= 0 && reg <= 3) {
            code.append("_");
        }
        else {
            code.append(" ");
        }
        return code.toString() + reg + "\n";
    }

    private String getOperand(Method method, Instruction instruction) {
        return switch (instruction.getInstType()) {
            case NOPER -> getNoper(method.getVarTable(), (SingleOpInstruction) instruction);
            case UNARYOPER -> getUnary(method.getVarTable(), (UnaryOpInstruction) instruction);
            case BINARYOPER -> getBinary(method.getVarTable(), (BinaryOpInstruction) instruction);
            case CALL -> getCode(method, (CallInstruction) instruction);
            case GETFIELD -> getCode(method, (GetFieldInstruction) instruction);
            default -> "\n";
        };
    }

    private String getNoper(HashMap<String, Descriptor> hash, SingleOpInstruction instruction) {
        Element element = instruction.getSingleOperand();
        return getLoad(hash, element);
    }

    private String getUnary(HashMap<String, Descriptor> hash, UnaryOpInstruction instruction) {
        StringBuilder code = new StringBuilder();

        if (instruction.getOperation().getOpType() == OperationType.NOTB) {
            code.append(iconst("1"))
                .append(getLoad(hash, instruction.getOperand()))
                .append("isub\n");
        }
        return code.toString();
    }

    private String getBinary(HashMap<String, Descriptor> hash, BinaryOpInstruction instruction) {
        StringBuilder code = new StringBuilder();
        OperationType operation = instruction.getOperation().getOpType();
        String left = getLoad(hash, instruction.getLeftOperand());
        String right = getLoad(hash, instruction.getRightOperand());

        /*
        if(operation == OperationType.LTH) {
            code.append(left).append(right);
            code.append(lessThan());
        }
        else if(operation == OperationType.ANDB) {
            andBoolean(instruction.getLeftOperand(), instruction.getRightOperand());
        }
        else {*/
            code.append(left).append(right);
            switch (operation) {
                case ADD -> code.append("iadd\n");
                case SUB -> code.append("isub\n");
                case MUL -> code.append("imul\n");
                case DIV -> code.append("idiv\n");
                case OR -> code.append("ior\n");
                default -> code.append("");
            }
        //}
        return code.toString();
    }

    private String lessThan() {
        StringBuilder code = new StringBuilder();
        String first = "LTH_" + next();
        String second = "LTH_" + next();
        code.append("if_icmplt ").append(first).append("\n")
                .append(iconst("0")).append("goto ")
                .append(second).append("\n").append(first).append(":\n")
                .append(iconst("1")).append(second).append(":\n");
        return code.toString();
    }

    private String andBoolean(Element left, Element right) {
        StringBuilder code = new StringBuilder();
        String first = "ANDB_" + next();
        String second = "ANDB_" + next();

        code.append(left)
            .append("ifeq ").append(first).append("\n")
            .append(right)
            .append("ifeq ").append(first).append("\n")
            .append(iconst("1"))
            .append("goto ").append(second).append("\n")
            .append(first).append(":\n")
            .append(iconst("0"))
            .append(second).append(":\n");

        return code.toString();
    }

    private int next() {
        return this.lbl++;
    }

    private String getLoad(HashMap<String, Descriptor> hash, Element element) {
        ElementType type = element.getType().getTypeOfElement();
        // iconst
        if (element.isLiteral()) {
            return iconst(((LiteralElement) element).getLiteral());
        }

        // iload
        if (type == ElementType.INT32 || type == ElementType.STRING || type == ElementType.BOOLEAN) {
            int reg = hash.get(((Operand) element).getName()).getVirtualReg();
            String instruction = "iload";
            if (reg >= 0 && reg <= 3) {
                instruction = instruction + "_";
            }
            else {
                instruction = instruction + " ";
            }
            return instruction + reg + "\n";
        }

        // aload
        if (type == ElementType.OBJECTREF || type == ElementType.ARRAYREF || type == ElementType.THIS) {
            int reg = hash.get(((Operand) element).getName()).getVirtualReg();
            String instruction = "aload";
            if (reg >= 0 && reg <= 3) {
                instruction = instruction + "_";
            } else {
                instruction = instruction + " ";
            }
            return instruction + reg + "\n";
        }
        return "";
    }

    private String iconst(String num) {
        int numI = Integer.parseInt(num);
        String code = "";
        if (numI == -1) code = "iconst_m1";
        else if (numI >= 0 && numI <= 5) code = "iconst_" + num;
        else if (numI >= -128 && numI <= 127) code = "bipush " + num;
        else if (numI >= -32768 && numI <= 32767) code = "sipush " + num;
        else code = "ldc " + num;
        return code + "\n";
    }
    
    public String getInvokeStatic(CallInstruction instruction, Method method) {
        StringBuilder code = new StringBuilder();
        HashMap<String, Descriptor> hash = method.getVarTable();

        for (Element parameter : instruction.getListOfOperands()) {
            code.append(getLoad(hash, parameter));
        }

        String methodClass = ((Operand) instruction.getFirstArg()).getName();

        code.append("invokestatic ")
            .append(JasminUtils.getSuperPath(classUnit, methodClass)).append("/")
            .append(((LiteralElement) instruction.getSecondArg()).getLiteral().replace("\"", ""))
            .append(getArguments(instruction.getListOfOperands()))
            .append(getJasminType(instruction.getReturnType()))
            .append("\n");
        return code.toString();
    }

    public String getInvokeSpecial(CallInstruction instruction, Method method) {
        StringBuilder code = new StringBuilder();
        HashMap<String, Descriptor> hash = method.getVarTable();
        ArrayList<Element> parameters = instruction.getListOfOperands();
        String methodName = ((LiteralElement) instruction.getSecondArg()).getLiteral().replace("\"", "");
        String className = JasminUtils.getSuperPath(classUnit, ((ClassType) instruction.getFirstArg().getType()).getName());
        Type returnType = instruction.getReturnType();

        for (Element parameter : parameters) {
            code.append(getLoad(hash, parameter));
        }

        code.append(getLoad(hash, instruction.getFirstArg()));

        code.append("invokespecial ").append(className).append("/")
            .append(methodName.replace("\"", ""))
            .append(getArguments(parameters))
            .append(getJasminType(returnType)).append("\n");
        return code.toString();
    }

    public String getInvokeVirtual(CallInstruction instruction, Method method) {
        HashMap<String, Descriptor> hash = method.getVarTable();
        StringBuilder code = new StringBuilder();
        String methodName = ((LiteralElement) instruction.getSecondArg()).getLiteral().replace("\"", "");
        String className = JasminUtils.getSuperPath(classUnit, ((ClassType) instruction.getFirstArg().getType()).getName());
        Type returnType = instruction.getReturnType();

        code.append(getLoad(hash, instruction.getFirstArg()));
        for (Element parameter : instruction.getListOfOperands()) {
            code.append(getLoad(hash, parameter));
        }

        code.append("invokevirtual ").append(className).append("/")
            .append(methodName.replace("\"", ""))
            .append(getArguments(instruction.getListOfOperands()))
            .append(getJasminType(returnType)).append("\n");

        return code.toString();
    }

    private String getArguments(ArrayList<Element> elements) {
        StringBuilder code = new StringBuilder();
        code.append("(");

        for (Element argument : elements) {
            code.append(getJasminType(argument.getType()));
        }

        code.append(")");
        return code.toString();
    }

    public String getJasminType(Type type) {
        if (type instanceof ArrayType) {
            return "[" + getJasminType(((ArrayType)type).getTypeOfElements());
        }
        if (type instanceof ClassType) {
            return "L" + JasminUtils.getSuperPath(classUnit, ((ClassType) type).getName()) + ";";
        }
        return getJasminType(type.getTypeOfElement());
    }

    public String getJasminType(ElementType elementType) {
        return switch (elementType) {
            case INT32 -> "I";
            case STRING -> "Ljava/lang/String;";
            case VOID -> "V";
            case BOOLEAN -> "Z";
            default -> throw new NotImplementedException(elementType);
        };
    }
}