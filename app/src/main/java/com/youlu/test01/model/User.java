package com.youlu.test01.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.youlu.test01.Book;

public class User implements Parcelable {


    public int userId;
    public String userName;
    public boolean isMale;

    public Book book;

    public User(int userId, String userName, boolean isMale, Book book) {
        this.userId = userId;
        this.userName = userName;
        this.isMale = isMale;
        this.book = book;
    }

// 返回当前对象的内容描述，如果含有文件描述符，返回1，否则返回0
    @Override
    public int describeContents() {
        return 0;
    }
// 将当前对象写入序列化结构中，其中flags有2中值：0或者1，为1标识当前对象需要作为返回值返回，
//    不能立即释放资源
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.userId);
        dest.writeString(this.userName);
        dest.writeByte(this.isMale ? (byte) 1 : (byte) 0);
        dest.writeParcelable(this.book, flags);
    }

    public User() {
    }
//  从序列化后的对象中创建原始对象
    protected User(Parcel in) {
        this.userId = in.readInt();
        this.userName = in.readString();
        this.isMale = in.readByte() != 0;
        this.book = in.readParcelable(Book.class.getClassLoader());
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        // 从序列化后的对象中创建原始对象
        @Override
        public User createFromParcel(Parcel source) {
            return new User(source);
        }
        // 创建指定长度的原始对象数组
        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    @Override
    public String toString() {
        return String.format(
                "User:{userId:%s, userName:%s, isMale:%s}, with child:{%s}",
                userId, userName, isMale, book);
    }
}
