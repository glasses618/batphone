package org.servalproject.group.secret;

import com.tiemens.secretshare.engine.SecretShare;
import com.tiemens.secretshare.math.BigIntUtilities;
import com.tiemens.secretshare.exceptions.SecretShareException;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.io.UnsupportedEncodingException;

public class SecretController {

    private SecretKey mMasterkey;
    private SecretShare.PublicInfo mPublicInfo;
    private ArrayList<SecretKey> mSubkeyList;
    private static final int KEY_LENGTH = 4096;
    public SecretController() {

    }

    public ArrayList<SecretKey> generateSubkeyList() {
        mSubkeyList = new ArrayList<SecretKey>();
        if (masterKeyExist() && mPublicInfo != null) {
            SecretShare ss = new SecretShare(mPublicInfo);
            SecureRandom random = new SecureRandom();
            SecretShare.SplitSecretOutput out = ss.split(mMasterkey.getKeyBigInteger(), random);
            List<SecretShare.ShareInfo> shareInfo = out.getShareInfos();
            for (SecretShare.ShareInfo share : shareInfo) {
                mSubkeyList.add(new SecretKey(share.getShare()));
            }
        }
        return mSubkeyList;
    }

    public void generatePublicInfo(Integer n, Integer k) {
        mPublicInfo = new SecretShare.PublicInfo(
            n, k, SecretShare.getPrimeUsedFor4096bigSecretPayload(), "");
    }
    
    public String getPublicInfo() {
        return mPublicInfo.toString();
    }

    public SecretKey reconstructMasterkey(
        ArrayList<SecretKey> subkeyList) {

        SecretKey masterkey = new SecretKey("");
        SecretShare solver = new SecretShare(mPublicInfo);
        ArrayList<SecretShare.ShareInfo> shareInfoList =
            new ArrayList<SecretShare.ShareInfo>();
        for (int i = 0; i < subkeyList.size(); i++) {
            shareInfoList.add(new SecretShare.ShareInfo(i + 1, subkeyList.get(i).getKeyBigInteger(), mPublicInfo));
        }
        try {
            SecretShare.CombineOutput solved = solver.combine(shareInfoList);
            masterkey = new SecretKey(solved.getSecret());
        } catch (SecretShareException e) {
            System.out.println("Not enough keys");
        }
        mMasterkey = masterkey;
        return masterkey;
    }

    public ArrayList<SecretKey> updateSubkeyList() {

        ArrayList<SecretKey> keyList =
            generateSubkeyList();
        return keyList;
    }

    public ArrayList<SecretKey> getSubkeyList() {
        return mSubkeyList;
    }

    public SecretKey getMasterkey() {
        return mMasterkey;
    }
    public void generateMasterkey() {
        SecureRandom random = new SecureRandom();
        mMasterkey = new SecretKey(new BigInteger(KEY_LENGTH, random).toString(32));

    }

    public boolean masterKeyExist() {
        return (mMasterkey != null) ? true : false;
    }

    public String encryptData(String input, SecretKey key) {

        StringBuffer output = new StringBuffer();
        String keyString = key.getKey();
        for (int i = 0; i < input.length(); i++) {
            output.append((char) (input.charAt(i) ^ keyString.charAt(i % keyString.length())));
        }

        return output.toString();
    }
    public String decryptData(String input, SecretKey key) {
        return encryptData(input, key);
    }
}
