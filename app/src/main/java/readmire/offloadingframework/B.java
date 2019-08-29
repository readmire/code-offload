package readmire.offloadingframework;

import java.io.Serializable;

import readmire.offloadinglibrary.OffloadingFactory;

public class B implements Serializable {
    Byte[] image;

    public B(Byte[] image){
        this.image = image;
    }
    public void doB(byte[] b)  {

        try {
            Thread.sleep(60);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        OffloadingFactory offManager = OffloadingFactory.getInstance();
        D d = offManager.create(D.class,offManager.getApplicationContext(),null);
        E e = offManager.create(E.class,offManager.getApplicationContext(),null);

        e.doE(new byte[20]);
        d.doD(new byte[30]);
    }
}
