package com.youlu.test01;

import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.os.SystemClock;
import android.util.Log;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

// 实现aidl文件接口

public class BookManagerService extends Service {

    private static final String TAG = "BookManagerService";

    private AtomicBoolean mIsServiceDestoryed = new AtomicBoolean(false);
//    支持并发读写 aidl方法是在服务端的Binder线程池中执行 自动线程同步
    private CopyOnWriteArrayList<Book> mBookList = new CopyOnWriteArrayList<Book>();

//    private CopyOnWriteArrayList<IOnNewBookArrivedListener> mListeners =
//            new CopyOnWriteArrayList<IOnNewBookArrivedListener>();

    private RemoteCallbackList<IOnNewBookArrivedListener> mListeners =
            new RemoteCallbackList<IOnNewBookArrivedListener>();

    private Binder mBinder = new IBookManager.Stub() {


        @Override
        public List<Book> getBookList() throws RemoteException {
            SystemClock.sleep(5000);
            return mBookList;
        }

        @Override
        public void addBook(Book book) throws RemoteException {
            mBookList.add(book);
        }
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags)
                throws RemoteException {
            int check = checkCallingOrSelfPermission("com.youlu.test01.permission.ACCESS_BOOK_SERVICE");
            Log.d(TAG, "check=" + check);
            if (check == PackageManager.PERMISSION_DENIED) {
                return false;
            }

            String packageName = null;
            String[] packages = getPackageManager().getPackagesForUid(
                    getCallingUid());
            if (packages != null && packages.length > 0) {
                packageName = packages[0];
            }
            Log.d(TAG, "onTransact: " + packageName);
            if (!packageName.startsWith("com.youlu")) {
                return false;
            }

            return super.onTransact(code, data, reply, flags);
        }

        @Override
        public void registerListener(IOnNewBookArrivedListener listener) throws RemoteException {
//            if (!mListeners.contains(listener)){
//               mListeners.add(listener);
//            }else{
//                Log.d(TAG,"already exists.");
//            }
//            Log.d(TAG,"registerListener, size:" + mListeners.size());
            mListeners.register(listener);
            final int N = mListeners.beginBroadcast();
            mListeners.finishBroadcast();
            Log.d(TAG, "registerListener, current size:" + N);
        }

        @Override
        public void unregisterListener(IOnNewBookArrivedListener listener) throws RemoteException {
//            if (mListeners.contains(listener)){
//                mListeners.remove(listener);
//                Log.d(TAG,"unregister Listener succeed");
//            }else{
//                Log.d(TAG,"not found, can not unregister");
//            }
//            Log.d(TAG,"unregisterListener, current size:" + mListeners.size());
            boolean success = mListeners.unregister(listener);

            if (success){
                Log.d(TAG, "unregister success.");
            }else{
                Log.d(TAG, "not found, can not unregister.");
            }
            final int N = mListeners.beginBroadcast();
            mListeners.finishBroadcast();
            Log.d(TAG, "unregisterListener, current size:" + N);
        }

    };

    public BookManagerService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
//        初始化
        mBookList.add(new Book(1,"android"));
        mBookList.add(new Book(2,"Ios"));
        new Thread(new ServiceWorker()).start();
    }

    @Override
    public void onDestroy() {
        mIsServiceDestoryed.set(true);
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        int check = checkCallingOrSelfPermission("");
        Log.d(TAG, "onbind check=" + check);
        if (check == PackageManager.PERMISSION_DENIED) {
            return null;
        }
        return mBinder;
    }

    // 开启一个线程，每隔5s就向书库中增加一本新书并通知所有感兴趣的用户
    private class ServiceWorker implements Runnable{
        @Override
        public void run() {
            while (!mIsServiceDestoryed.get()){
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                int bookId = mBookList.size() + 1;
                Book newBook = new Book(bookId,"new book#"+bookId);
                try {
                    onNewBookArrived(newBook);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }

        private void onNewBookArrived(Book newBook) throws RemoteException {
            mBookList.add(newBook);
            final int N = mListeners.beginBroadcast(); // 通知
            for(int i=0; i<N; i++){
                IOnNewBookArrivedListener listener = mListeners.getBroadcastItem(i);
                if (listener != null){
                    try {
                        listener.onNewBookArrived(newBook);
                    }catch (RemoteException e){
                        e.printStackTrace();
                    }
                }
                mListeners.finishBroadcast();
            }
//            Log.d(TAG,"onNewBookArrived, notify listener size:" + mListeners.size());
//            for (int i=0; i< mListeners.size(); i++){
//                IOnNewBookArrivedListener listener = mListeners.get(i);
//                Log.d(TAG,"onNewBookArrived, notify listener：" + mListeners);
//                listener.onNewBookArrived(newBook);
//
//            }
        }
    }
}
