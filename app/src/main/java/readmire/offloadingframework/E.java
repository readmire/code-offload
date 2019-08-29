package readmire.offloadingframework;

import java.io.Serializable;

public class E implements Serializable {
    public void doE(byte[] b){
        try {
            Thread.sleep(40);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
