import org.servalproject.group.secret.SecretController;
import org.servalproject.group.secret.SecretKey;
import com.tiemens.secretshare.engine.SecretShare;
import com.tiemens.secretshare.math.BigIntUtilities;
import com.tiemens.secretshare.exceptions.SecretShareException;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class SecretControllerTest {
    SecretController secretController = new SecretController();
    SecretKey masterkey = new SecretKey("");
    ArrayList<SecretKey> subkeyList = new ArrayList<SecretKey>();
    @Test   
    public void keyTest() {
        secretController.generateMasterkey();
        masterkey = secretController.getMasterkey();
        System.out.println("generate masterkey...");
        System.out.println(masterkey.getKey());
        secretController.generatePublicInfo(5, 3);
        System.out.println(secretController.getPublicInfo());
        System.out.println("generate subkeys with (5,3) scheme...");
        subkeyList = secretController.generateSubkeyList();
        for (SecretKey key: subkeyList) {
            System.out.println(key.getKey());
        }
        SecretKey reconstructedkey = secretController.reconstructMasterkey(subkeyList);
        System.out.println("reconstruct masterbkeys...");
        System.out.println(reconstructedkey.getKey());
        System.out.println("update subkeys...");
        subkeyList = secretController.updateSubkeyList();
        for (SecretKey key: subkeyList) {
            System.out.println(key.getKey());
        }
        reconstructedkey = secretController.reconstructMasterkey(subkeyList);
        System.out.println("reconstruct masterkeys...");
        System.out.println(reconstructedkey.getKey());

        ArrayList<SecretKey> enoughSubkeys = 
            new ArrayList<SecretKey>(subkeyList.subList(0,3));
        reconstructedkey = secretController.reconstructMasterkey(enoughSubkeys);
        System.out.println("reconstruct masterkeys with 3 subkeys...");
        System.out.println(reconstructedkey.getKey());

        ArrayList<SecretKey> notEnoughSubkeys = 
            new ArrayList<SecretKey>(subkeyList.subList(0,1));
        System.out.println("reconstruct masterkeys with 2 subkeys...");
        reconstructedkey = secretController.reconstructMasterkey(notEnoughSubkeys);
        System.out.println(reconstructedkey.getKey());
        System.out.println("original data...");
        String oriData = "hello, world 我是秘密哈哈!@#&(*&(*)(#";
        System.out.println(oriData);
        String enData =  secretController.encryptData(oriData, masterkey);
        System.out.println("encrypted data...");
        System.out.println(enData);
        String data =  secretController.decryptData(enData, masterkey);
        System.out.println("decrypted data...");
        System.out.println(data);
    }

}

