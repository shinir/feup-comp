package pt.up.fe.comp2023.ollir;

public class OllirType {
    private final String type;
    private final Boolean toTemp;

    public OllirType (String type, Boolean toTemp){
        this.type = type;
        this.toTemp = toTemp;
    }

    public OllirType (Boolean toTemp) {
        this.type = null;
        this.toTemp = toTemp;
    }

    public String getType(){
        return type;
    }

    public Boolean getToTemp(){
        return toTemp;
    }
}
