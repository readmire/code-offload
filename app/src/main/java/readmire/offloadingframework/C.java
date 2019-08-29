package readmire.offloadingframework;

import java.io.Serializable;

import readmire.offloadinglibrary.OffloadingFactory;

public class C implements Serializable {

    public void doC(byte[] b){
        try {
            Thread.sleep(20);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        OffloadingFactory offManager = OffloadingFactory.getInstance();
        D d = offManager.create(D.class, offManager.getApplicationContext(),null);
        d.doD(new byte[10]);
    }
}
