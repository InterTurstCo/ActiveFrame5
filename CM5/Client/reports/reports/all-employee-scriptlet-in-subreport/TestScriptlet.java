import java.util.Random;

import net.sf.jasperreports.engine.JRDefaultScriptlet;

public class TestScriptlet extends JRDefaultScriptlet {
    private Random rnd = new Random();

    public String getRndString() {
        return String.valueOf(rnd.nextLong());
    }
}
