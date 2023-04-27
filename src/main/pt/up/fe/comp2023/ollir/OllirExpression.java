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


}
