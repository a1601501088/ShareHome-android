package com.vunken.tv_sharehome;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class MySQLiteHelper extends SQLiteOpenHelper {

	public MySQLiteHelper(Context context) {
		super(context, "mydb.db", null, 1);

	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		Log.e("MySQLiteHelper", "MySQLiteHelper");
		db.execSQL("create table whiteContanct(_id integer primary key autoincrement, username varchar(20),moblie varchar(20),date varchar(40))");

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

	}

}
