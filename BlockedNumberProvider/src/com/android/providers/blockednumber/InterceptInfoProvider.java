package com.android.providers.blockednumber;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.annotation.NonNull;
import android.annotation.Nullable;
import android.util.Log;

/**
 * Created by  on 2019/7/6.
 */

public class InterceptInfoProvider extends ContentProvider {

    static final String TAG = "InterceptInfos";

    public static final String AUTHORITY = "cn.kaer.blockeddata";

    public static final int User_Code = 2000;

    public static final Uri CONTENT_URI = Uri.withAppendedPath(Uri.parse("content://" + AUTHORITY),
            "intercept");

    private static final UriMatcher mMatcher;
    static{
        mMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        mMatcher.addURI(AUTHORITY,"intercept", User_Code);
    }

    protected BlockedNumberDatabaseHelper mDbHelper;

    @Override
    public boolean onCreate() {
        mDbHelper = BlockedNumberDatabaseHelper.getInstance(getContext());
        return true;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        Uri blockedUri = insertInterceptInfo(values);
        getContext().getContentResolver().notifyChange(blockedUri, null);
        Log.e(TAG, "insertInterceptInfo()....");
        return blockedUri;
    }

    private Uri insertInterceptInfo(ContentValues values) {
        final long id = mDbHelper.getWritableDatabase().insertWithOnConflict(
                BlockedNumberDatabaseHelper.Tables.BLOCKED_INTERCEPT, null, values,
                SQLiteDatabase.CONFLICT_REPLACE);

        return ContentUris.withAppendedId(CONTENT_URI, id);
    }


    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        final SQLiteDatabase db = mDbHelper.getWritableDatabase();
        int ret = db.delete(BlockedNumberDatabaseHelper.Tables.BLOCKED_INTERCEPT, selection, selectionArgs);
        getContext().getContentResolver().notifyChange(uri, null);
        return ret;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        Cursor cursor = queryInterceptTelSmsData(projection, selection, selectionArgs, sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    private Cursor queryInterceptTelSmsData(String[] projection, String selection, String[] selectionArgs,
                                    String sortOrder) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setStrict(true);
        qb.setTables(BlockedNumberDatabaseHelper.Tables.BLOCKED_INTERCEPT);

        return qb.query(mDbHelper.getReadableDatabase(), projection, selection, selectionArgs,
                /* groupBy =*/ null, /* having =*/null, sortOrder,
                /* limit =*/ null);
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }
}
