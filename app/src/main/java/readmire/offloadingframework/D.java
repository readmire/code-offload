package readmire.offloadingframework;

import java.io.Serializable;

import readmire.offloadinglibrary.OffloadingFactory;

public class D implements Serializable {
    public void doD(byte[] b){
        try {
            Thread.sleep(40);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        OffloadingFactory offManager = OffloadingFactory.getInstance();
        E e = offManager.create(E.class,offManager.getApplicationContext(),null);
        F f = offManager.create(F.class,offManager.getApplicationContext(),null);

        e.doE(new byte[40]);
        f.doF(new byte[50]);

    }
}
