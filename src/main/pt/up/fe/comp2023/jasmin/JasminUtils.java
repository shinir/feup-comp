package pt.up.fe.comp2023.jasmin;
import org.specs.comp.ollir.*;

public class JasminUtils {

    public static String getSuperPath(ClassUnit classUnit, String className) {
        for (String importString : classUnit.getImports()) {
            String[] split = importString.split("\\.");
            String last;
            if (split.length == 0) {
                last = importString;
            } else {
                last = split[split.length - 1];
            }
            if (last.equals(className)) {
                return importString.replace('.', '/');
            }
        }
        return classUnit.getClassName().replace("\\.", "/");
    }

}
