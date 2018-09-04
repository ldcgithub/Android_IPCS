package com.youlu.test01.binderpool;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import java.util.concurrent.CountDownLatch;

public class BinderPool {
    public static final int BINDER_NONE = -1;
    public static final int BINDER_SECURITY_CENTER = 1;
    public static final int BINDER_COMPUTE = 0;
    private static final String TAG = "BinderPool";

    private Context mContext;
    private IBinderPool mBinderPool;
    // 保证一定的可见性 （原子性N 有序性Y） --并发编程
    private static volatile BinderPool sInstance;
    // 同步工具类(允许一个或多个线程一直等待，直到其他线程的操作执行完后再执行)
    private CountDownLatch mConnectBinderPoolCountDownLatch;

    private IBinder.DeathRecipient mbinderPoolDeathRecipier = new IBinder.DeathRecipient() {
        @Override
        public void binderDied() {
            Log.w(TAG, "binder Died");
            mBinderPool.asBinder().unlinkToDeath(mbinderPoolDeathRecipier,0);
            mBinderPool = null;
            connectBinderPoolService();
        }
    };

    private ServiceConnection mBinderPoolConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

            mBinderPool = IBinderPool.Stub.asInterface(service);
            try {
                mBinderPool.asBinder().linkToDeath(mbinderPoolDeathRecipier, 0);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            mConnectBinderPoolCountDownLatch.countDown();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        // ignored
        }
    };

    public BinderPool(Context context) {
        mContext = context.getApplicationContext();
        connectBinderPoolService();
    }

    // 同步方法 初始化工作 绑定远程服务
    private synchronized void connectBinderPoolService() {
//        初始化一个线程-- 计数器初始值
        mConnectBinderPoolCountDownLatch = new CountDownLatch(1);
        Intent service = new Intent(mContext,BinderPoolService.class);
        mContext.bindService(service,mBinderPoolConnection,Context.BIND_AUTO_CREATE);
        try {
            mConnectBinderPoolCountDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
    // 单例实现 初始化一次
    public static BinderPool getInstance(Context context) {
        if (sInstance == null){
            // 同步 线程安全
            synchronized (BinderPool.class) {
                sInstance = new BinderPool(context);
            }
        }
        return sInstance;
    }

    public IBinder queryBinder(int binderCode){
       IBinder binder = null;
       try {
           if (mBinderPool != null){
               binder = mBinderPool.queryBinder(binderCode);
           }
       }catch (RemoteException e){
           e.printStackTrace();
       }
       return binder;
    }

    public static class BinderPoolImpl extends IBinderPool.Stub{

        public BinderPoolImpl() {
        }

        @Override
        public IBinder queryBinder(int binderCode) throws RemoteException {
            IBinder binder = null;
            switch (binderCode){
                case BINDER_SECURITY_CENTER:{
                    binder = new SecurityCenterImpl();
                    break;
                }
                case BINDER_COMPUTE:{
                    binder = new ComputeImpl();
                    break;
                }
                default:
                    break;
            }
            return binder;
        }
    }
}
