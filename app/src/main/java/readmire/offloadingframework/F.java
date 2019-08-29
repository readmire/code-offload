package readmire.offloadingframework;

import java.io.Serializable;

public class F implements Serializable {
    public void doF(byte[] b){
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
