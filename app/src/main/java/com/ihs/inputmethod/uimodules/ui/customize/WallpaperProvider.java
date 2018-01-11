package com.ihs.inputmethod.uimodules.ui.customize;

import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.SparseArray;

import java.util.ArrayList;

/**
 * {@link android.content.ContentProvider} interface for local wallpaper data.
 */
public class WallpaperProvider extends ContentProvider {

    // --Commented out by Inspection (18/1/11 下午2:41):private static final String TAG = WallpaperProvider.class.getSimpleName();

    public final static String METHOD_APPLY_WALLPAPER = "applyWallpaper";
    // --Commented out by Inspection (18/1/11 下午2:41):public final static String METHOD_CLEAN_CURRENT_WALLPAPER = "cleanCurrentWallpaper";
    // --Commented out by Inspection (18/1/11 下午2:41):public final static String BUNDLE_KEY_WALLPAPER = "wallpaper_info";
    // --Commented out by Inspection (18/1/11 下午2:41):public final static String BUNDLE_KEY_LEGACY_LOCAL_WALLPAPERS = "legacy_local_wallpapers";

    public final static String TABLE_WALLPAPER = "wallpaper";
    public final static String COLUMN_TYPE = "type";

    public final static String COLUMN_DRAWABLE_NAME = "drawableName";
    public final static String COLUMN_THUMBNAIL_URL = "thumbnailUrl";
    public final static String COLUMN_HD_URL = "hdUrl";
    public final static String COLUMN_PATH = "path";

    public final static String COLUMN_EDIT = "edit";
    public final static String COLUMN_CREATE_TIME = "createTime";
    public final static String COLUMN_EDIT_TIME = "editTime";

    public final static String COLUMN_IS_APPLIED = "isApplied";

    public static final String AUTHORITY = "com.honeycomb.launcher.wallpaper";
    // --Commented out by Inspection (18/1/11 下午2:41):public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + TABLE_WALLPAPER);

    public static final int WALLPAPER_QUERY = 1;
    // Defines a helper object that matches content URIs to table-specific parameters
    private static final UriMatcher sUriMatcher = new UriMatcher(0);
    // Stores the MIME types served by this provider
    private static final SparseArray<String> sMimeTypes = new SparseArray<>();

    static {
        // Adds a URI "match" entry that maps picture URL content URIs to a numeric code
        sUriMatcher.addURI(AUTHORITY, TABLE_WALLPAPER, WALLPAPER_QUERY);
        sMimeTypes.put(WALLPAPER_QUERY, "vnd.android.cursor.item/vnd." + AUTHORITY + "." + TABLE_WALLPAPER);
    }

    private WallpaperDB mHelper;

    @Override
    public boolean onCreate() {
        mHelper = new WallpaperDB(getContext());
        return true;
    }

    @Nullable
    @Override
    public Bundle call(@NonNull String method, String arg, Bundle extras) {
        if (method.equals(METHOD_APPLY_WALLPAPER)) {
//            HSLog.i(TAG, "applyWallpaper command invoked on process " + LauncherApplication.getProcessName());
//
//            // Set in a PathClassLoader (instead of BootClassLoader which cannot load application classes)
//            // to unmarshall WallpaperInfo instance.
//            extras.setClassLoader(LauncherApplication.class.getClassLoader());
//
//            WallpaperMgr.getInstance().initLocalWallpapers(extras.getString(BUNDLE_KEY_LEGACY_LOCAL_WALLPAPERS), null);
//            WallpaperMgr.getInstance().saveCurrentWallpaper((WallpaperInfo) extras.getParcelable(BUNDLE_KEY_WALLPAPER),
//                    new Runnable() {
//                        @Override
//                        public void run() {
//                            HSGlobalNotificationCenter.sendNotificationOnMainThread(WallpaperMgr.NOTIFICATION_WALLPAPER_SET);
//                        }
//                    });
            Bundle bundle = new Bundle();
            bundle.putBoolean("success", true);
            return bundle;
        }
        throw new IllegalArgumentException("Wallpaper provider error method call: " + method);
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri,
                        String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteDatabase db = mHelper.getReadableDatabase();
        // Decodes the content URI and maps it to a code
        switch (sUriMatcher.match(uri)) {
            case WALLPAPER_QUERY:
                Cursor returnCursor = db.query(TABLE_WALLPAPER, projection, selection, selectionArgs, null, null, sortOrder);

                // Sets the ContentResolver to watch this content URI for data changes
                Context context = getContext();
                if (context != null)
                    returnCursor.setNotificationUri(context.getContentResolver(), uri);
                return returnCursor;
        }
        return null;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return sMimeTypes.get(sUriMatcher.match(uri));
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        switch (sUriMatcher.match(uri)) {
            case WALLPAPER_QUERY:
                SQLiteDatabase localSQLiteDatabase = mHelper.getWritableDatabase();

                // Inserts the row into the table and returns the new row's _id value
                long id = localSQLiteDatabase.insert(TABLE_WALLPAPER, null, values);

                // If the insert succeeded, notify a change and return the new row's content URI.
                if (-1 != id) {
                    Context context = getContext();
                    if (context != null) context.getContentResolver().notifyChange(uri, null);
                    return Uri.withAppendedPath(uri, Long.toString(id));
                } else {
                    throw new SQLiteException("Insert error:" + uri);
                }
        }
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        switch (sUriMatcher.match(uri)) {
            case WALLPAPER_QUERY:
                SQLiteDatabase localSQLiteDatabase = mHelper.getWritableDatabase();

                // Updates the table
                int rows = localSQLiteDatabase.delete(TABLE_WALLPAPER, selection, selectionArgs);

                // If the update succeeded, notify a change and return the number of updated rows.
                if (0 != rows) {
                    Context context = getContext();
                    if (context != null) context.getContentResolver().notifyChange(uri, null);
                    return rows;
                }
        }
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        switch (sUriMatcher.match(uri)) {
            case WALLPAPER_QUERY:
                SQLiteDatabase localSQLiteDatabase = mHelper.getWritableDatabase();

                // Updates the table
                int rows = localSQLiteDatabase.update(TABLE_WALLPAPER, values, selection, selectionArgs);

                // If the update succeeded, notify a change and return the number of updated rows.
                if (0 != rows) {
                    Context context = getContext();
                    if (context != null) context.getContentResolver().notifyChange(uri, null);
                    return rows;
                }
        }
        return -1;
    }

    /**
     * Apply multiple operations in a transaction.
     * <p>
     * Implementation from Google's iosched ScheduleProvider.
     */
    @Override
    public
    @NonNull
    ContentProviderResult[] applyBatch(
            @NonNull ArrayList<ContentProviderOperation> operations)
            throws OperationApplicationException {
        final SQLiteDatabase db = mHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            final int numOperations = operations.size();
            final ContentProviderResult[] results = new ContentProviderResult[numOperations];
            for (int i = 0; i < numOperations; i++) {
                results[i] = operations.get(i).apply(this, results, i);
            }
            db.setTransactionSuccessful();
            return results;
        } finally {
            db.endTransaction();
        }
    }

    /**
     * Database for storing wallpapers and their editing states.
     */
    private static class WallpaperDB extends SQLiteOpenHelper {
        private final static int DB_VERSION = 1;
        private static final String WALLPAPER_DB = "wallpaper.db";


        WallpaperDB(Context context) {
            super(context, WALLPAPER_DB, null, DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_WALLPAPER + " (" +
                    COLUMN_TYPE + " INTEGER NOT NULL DEFAULT -1, " +
                    COLUMN_DRAWABLE_NAME + " TEXT NOT NULL DEFAULT '', " +
                    COLUMN_THUMBNAIL_URL + " TEXT NOT NULL DEFAULT '', " +
                    COLUMN_HD_URL + " TEXT NOT NULL DEFAULT '', " +
                    COLUMN_PATH + " TEXT NOT NULL DEFAULT '', " +
                    COLUMN_EDIT + " TEXT NOT NULL DEFAULT '', " +
                    COLUMN_CREATE_TIME + " INTEGER NOT NULL DEFAULT -1, " +
                    COLUMN_EDIT_TIME + " INTEGER NOT NULL DEFAULT -1, " +
                    COLUMN_IS_APPLIED + " INTEGER NOT NULL DEFAULT 0 " +
                    ");");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            if (oldVersion != newVersion) {
                clearDB(db);
            }
        }

        @Override
        public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            if (oldVersion != newVersion) {
                clearDB(db);
            }
        }

        void clearDB(SQLiteDatabase db) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_WALLPAPER);
            onCreate(db);
        }
    }
}

