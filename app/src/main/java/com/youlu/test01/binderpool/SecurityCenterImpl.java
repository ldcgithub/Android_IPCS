package com.youlu.test01.binderpool;

import android.os.IBinder;
import android.os.RemoteException;

public class SecurityCenterImpl extends ISecurityCenter.Stub {

    private static final char SECRET_CODE = '^';

    // 加密
    @Override
    public String encrypt(String content) throws RemoteException {
        char[] chars = content.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            chars[i] ^= SECRET_CODE;
        }
        return new String(chars);
    }

    // 解密
    @Override
    public String decrypt(String password) throws RemoteException {
        return encrypt(password);
    }
}
