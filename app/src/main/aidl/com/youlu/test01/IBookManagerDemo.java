package com.youlu.test01;

import android.os.IBinder;
import android.os.IInterface;
import android.os.RemoteException;

import java.util.List;

public interface IBookManagerDemo extends IInterface{
        static final String DESCRIPTOR = "com.youlu.test01.IBookManager";

        static final int TRANSACTION_getBookList = (IBinder.FIRST_CALL_TRANSACTION + 0);
        static final int TRANSACTION_addBook = (IBinder.FIRST_CALL_TRANSACTION + 1);

        public List<Book> getBookList() throws RemoteException;

        public void addBook(Book book) throws RemoteException;
}
