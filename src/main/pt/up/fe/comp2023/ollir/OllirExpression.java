package pt.up.fe.comp2023.ollir;

public class OllirExpression {
    public String value;
    public String prefix;

    public OllirExpression(String prefix, String value) {
        this.value = value;
        this.prefix = prefix;
    }

    public OllirExpression(){
        this.value = "";
        this.prefix = "";
    }

    @Override
    public String toString(){
        return "Prefix code:\n" + this.prefix + "\nValue: " + this.value + "\n";
    }
}
