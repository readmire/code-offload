package readmire.offloadingframework;

import java.io.Serializable;

import readmire.offloadinglibrary.ConstructorParam;
import readmire.offloadinglibrary.OffloadingFactory;

public class A  implements Serializable {

    public void doA() {
        OffloadingFactory offManager = OffloadingFactory.getInstance();
        ConstructorParam cp = new ConstructorParam();
        cp.setConstructorArgTypes(Byte.class);
        cp.setConstructorArgValues(new byte[600*800]);

        B b = offManager.create(B.class,offManager.getApplicationContext(),cp);
        C c = offManager.create(C.class,offManager.getApplicationContext(),null);

        b.doB(new byte[40]);
        c.doC(new byte[80]);
    }

}
