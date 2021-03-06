package com.tws.plugin.manager;

import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CancellationSignal;

import com.tws.plugin.content.PluginDescriptor;
import com.tws.plugin.core.PluginLoader;
import com.tws.plugin.core.compat.CompatForContentProvider;

import java.util.ArrayList;

/**
 * @author yongchen
 */
public class PluginManagerProviderClient {

    @SuppressWarnings("unchecked")
    public static ArrayList<PluginDescriptor> queryAll() {
        Bundle bundle = CompatForContentProvider.call(PluginManagerProvider.buildUri(),
                PluginManagerProvider.ACTION_QUERY_ALL, null, null);

        ArrayList<PluginDescriptor> list = null;
        if (bundle != null) {
            list = (ArrayList<PluginDescriptor>) bundle.getSerializable(PluginManagerProvider.QUERY_ALL_RESULT);
        }
        //防止NPE
        if (list == null) {
            list = new ArrayList<PluginDescriptor>();
        }
        return list;
    }

    public static PluginDescriptor queryById(String pluginId) {
        Bundle bundle = CompatForContentProvider.call(PluginManagerProvider.buildUri(),
                PluginManagerProvider.ACTION_QUERY_BY_ID, pluginId, null);
        if (bundle != null) {
            return (PluginDescriptor) bundle.getSerializable(PluginManagerProvider.QUERY_BY_ID_RESULT);
        }
        return null;
    }

    public static PluginDescriptor queryByClass(String clazzName) {
        Bundle bundle = CompatForContentProvider.call(PluginManagerProvider.buildUri(),
                PluginManagerProvider.ACTION_QUERY_BY_CLASS_NAME, clazzName, null);
        if (bundle != null) {
            return (PluginDescriptor) bundle.getSerializable(PluginManagerProvider.QUERY_BY_CLASS_NAME_RESULT);
        }
        return null;
    }

    public static PluginDescriptor queryByFragment(String clazzId) {

        Bundle bundle = CompatForContentProvider.call(PluginManagerProvider.buildUri(),
                PluginManagerProvider.ACTION_QUERY_BY_FRAGMENT_ID, clazzId, null);
        if (bundle != null) {
            return (PluginDescriptor) bundle.getSerializable(PluginManagerProvider.QUERY_BY_FRAGMENT_ID_RESULT);
        }
        return null;
    }

    public static int install(String srcFile) {
        Bundle bundle = CompatForContentProvider.call(PluginManagerProvider.buildUri(),
                PluginManagerProvider.ACTION_INSTALL, srcFile, null);

        int result = 7;//install-Fail
        if (bundle != null) {
            result = bundle.getInt(PluginManagerProvider.INSTALL_RESULT);
        }
        return result;
    }

    public static synchronized int remove(String pluginId) {
        Bundle result = CompatForContentProvider.call(PluginManagerProvider.buildUri(),
                PluginManagerProvider.ACTION_REMOVE, pluginId, null);
        if (result != null) {
            return result.getInt(PluginManagerProvider.REMOVE_RESULT, InstallResult.INSTALL_FAIL);
        }
        return InstallResult.INSTALL_FAIL;
    }

    public static synchronized boolean removeAll() {
        Bundle bundle = CompatForContentProvider.call(PluginManagerProvider.buildUri(),
                PluginManagerProvider.ACTION_REMOVE_ALL, null, null);
        if (bundle != null) {
            return bundle.getBoolean(PluginManagerProvider.REMOVE_ALL_RESULT);
        }
        return false;
    }

    public static String bindStubReceiver(String className) {
        Bundle bundle = CompatForContentProvider.call(PluginManagerProvider.buildUri(),
                PluginManagerProvider.ACTION_BIND_RECEIVER, className, null);
        if (bundle != null) {
            return bundle.getString(PluginManagerProvider.BIND_RECEIVER_RESULT);
        }
        return null;
    }

    public static String bindStubActivity(String pluginActivityClassName, int launchMode, String packageName, String themeId, String orientation) {
        Bundle arg = new Bundle();
        arg.putInt("launchMode", launchMode);
        arg.putString("packageName", packageName);
        arg.putString("themeId", themeId);
        if (orientation != null) {
            arg.putInt("orientation", Integer.valueOf(orientation));
        }
        Bundle bundle = CompatForContentProvider.call(PluginManagerProvider.buildUri(),
                PluginManagerProvider.ACTION_BIND_ACTIVITY,
                pluginActivityClassName, arg);
        if (bundle != null) {
            return bundle.getString(PluginManagerProvider.BIND_ACTIVITY_RESULT);
        }
        return null;
    }

    public static boolean isExact(String name, int type) {
        Bundle arg = new Bundle();
        arg.putInt("type", type);
        Bundle bundle = CompatForContentProvider.call(PluginManagerProvider.buildUri(),
                PluginManagerProvider.ACTION_IS_EXACT,
                name, arg);
        if (bundle != null) {
            return bundle.getBoolean(PluginManagerProvider.IS_EXACT_RESULT);
        }
        return false;
    }

    public static void unBindLaunchModeStubActivity(String activityName, String className) {
        Bundle arg = new Bundle();
        arg.putString("className", className);
        CompatForContentProvider.call(PluginManagerProvider.buildUri(),
                PluginManagerProvider.ACTION_UNBIND_ACTIVITY,
                activityName, arg);
    }

    public static String getBindedPluginServiceName(String stubServiceName) {
        Bundle bundle = CompatForContentProvider.call(PluginManagerProvider.buildUri(),
                PluginManagerProvider.ACTION_GET_BINDED_SERVICE,
                stubServiceName, null);
        if (bundle != null) {
            return bundle.getString(PluginManagerProvider.GET_BINDED_SERVICE_RESULT);
        }
        return null;
    }

    public static String bindStubService(String pluginServiceClassName) {
        Bundle bundle = CompatForContentProvider.call(PluginManagerProvider.buildUri(),
                PluginManagerProvider.ACTION_BIND_SERVICE,
                pluginServiceClassName, null);
        if (bundle != null) {
            return bundle.getString(PluginManagerProvider.BIND_SERVICE_RESULT);
        }
        return null;
    }

    public static void unBindStubService(String pluginServiceName) {
        CompatForContentProvider.call(PluginManagerProvider.buildUri(),
                PluginManagerProvider.ACTION_UNBIND_SERVICE,
                pluginServiceName, null);
    }

    public static boolean isStub(String className) {
        //这里如果约定stub组件的名字以特定词开头可以省去provider调用，减少跨进程，提高效率
        Bundle bundle = CompatForContentProvider.call(PluginManagerProvider.buildUri(),
                PluginManagerProvider.ACTION_IS_STUB,
                className, null);
        if (bundle != null) {
            return bundle.getBoolean(PluginManagerProvider.IS_STUB_RESULT);
        }
        return false;
    }

    public static String dumpServiceInfo() {
        Bundle bundle = CompatForContentProvider.call(PluginManagerProvider.buildUri(),
                PluginManagerProvider.ACTION_DUMP_SERVICE_INFO,
                null, null);
        if (bundle != null) {
            return bundle.getString(PluginManagerProvider.DUMP_SERVICE_INFO_RESULT);
        }
        return null;
    }

    public static boolean isRunning(String pluginId) {
        //这里如果约定stub组件的名字以特定词开头可以省去provider调用，减少跨进程，提高效率
        Bundle bundle = CompatForContentProvider.call(PluginManagerProvider.buildUri(),
                PluginManagerProvider.ACTION_IS_PLUGIN_RUNNING,
                pluginId, null);
        if (bundle != null) {
            return bundle.getBoolean(PluginManagerProvider.IS_PLUGIN_RUNNING_RESULT);
        }
        return false;
    }

    public static boolean wakeup(String pluginid) {
        //这里如果约定stub组件的名字以特定词开头可以省去provider调用，减少跨进程，提高效率
        Bundle bundle = CompatForContentProvider.call(PluginManagerProvider.buildUri(),
                PluginManagerProvider.ACTION_WAKEUP_PLUGIN,
                pluginid, null);
        if (bundle != null) {
            return bundle.getBoolean(PluginManagerProvider.WAKEUP_PLUGIN_RESULT);
        }
        return false;
    }

    public static Cursor query(Uri url, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Uri newUri = buildNewUri(url);
        ContentResolver resolver = PluginLoader.getApplication().getContentResolver();
        return resolver.query(newUri, projection, selection, selectionArgs, sortOrder);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public static Cursor query(Uri url, String[] projection, String selection, String[] selectionArgs, String sortOrder, CancellationSignal cancellationSignal) {
        Uri newUri = buildNewUri(url);
        ContentResolver resolver = PluginLoader.getApplication().getContentResolver();
        return resolver.query(newUri, projection, selection, selectionArgs, sortOrder, cancellationSignal);
    }

//    @TargetApi(Build.VERSION_CODES.O)
//    public static Cursor query(Uri url, String[] projection, Bundle queryArgs, CancellationSignal cancellationSignal) {
//        Uri newUri = buildNewUri(url);
//        ContentResolver resolver = PluginLoader.getApplication().getContentResolver();
//        return resolver.query(newUri, projection, queryArgs, cancellationSignal);
//    }

    public static String getType(Uri url) {
        Uri newUri = buildNewUri(url);
        ContentResolver resolver = PluginLoader.getApplication().getContentResolver();
        return resolver.getType(newUri);
    }

    public static Uri insert(Uri url, ContentValues contentValues) {
        Uri newUri = buildNewUri(url);
        ContentResolver resolver = PluginLoader.getApplication().getContentResolver();
        return resolver.insert(newUri, contentValues);
    }

    public static int delete(Uri url, String where, String[] selectionArgs) {
        Uri newUri = buildNewUri(url);
        ContentResolver resolver = PluginLoader.getApplication().getContentResolver();
        return resolver.delete(newUri, where, selectionArgs);
    }

    public static int update(Uri url, ContentValues values, String where, String[] selectionArgs) {
        Uri newUri = buildNewUri(url);
        ContentResolver resolver = PluginLoader.getApplication().getContentResolver();
        return resolver.update(newUri, values, where, selectionArgs);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static Bundle call(String method, String arg, Bundle extras) {
        ContentResolver resolver = PluginLoader.getApplication().getContentResolver();
        return resolver.call(PluginManagerProvider.buildUri(), method, arg, extras);
    }

    private static Uri buildNewUri(Uri url) {
        return PluginManagerProvider.buildUri().buildUpon().appendQueryParameter("targetUrl", url.toString()).build();
    }
}
