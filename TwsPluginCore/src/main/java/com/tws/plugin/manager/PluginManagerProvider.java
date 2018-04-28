package com.tws.plugin.manager;

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.CancellationSignal;

import com.tws.plugin.content.DisplayItem;
import com.tws.plugin.content.LoadedPlugin;
import com.tws.plugin.content.PluginDescriptor;
import com.tws.plugin.core.PluginLauncher;
import com.tws.plugin.core.PluginLoader;
import com.tws.plugin.util.ProcessUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;

import qrom.component.log.QRomLog;

/**
 * @author yongchen
 *         利用ContentProvider实现同步跨进程调用
 *         ContentProvider的相关操作应该放在installContentProvider之后执行,
 *         而installContentProvider是ActivityThread在调用application的attach之后,onCreate之前执行
 */
public class PluginManagerProvider extends ContentProvider {

    private static final String TAG = "rick_Print:PluginManagerProvider";

    private static Uri CONTENT_URI;

    public static final String ACTION_INSTALL = "install";
    public static final String INSTALL_RESULT = "install_result";

    public static final String ACTION_REMOVE = "remove";
    public static final String REMOVE_RESULT = "remove_result";

    public static final String ACTION_REMOVE_ALL = "remove_all";
    public static final String REMOVE_ALL_RESULT = "remove_all_result";

    public static final String ACTION_QUERY_BY_ID = "query_by_id";
    public static final String QUERY_BY_ID_RESULT = "query_by_id_result";

    public static final String ACTION_QUERY_BY_CLASS_NAME = "query_by_class_name";
    public static final String QUERY_BY_CLASS_NAME_RESULT = "query_by_class_name_result";

    public static final String ACTION_QUERY_BY_FRAGMENT_ID = "query_by_fragment_id";
    public static final String QUERY_BY_FRAGMENT_ID_RESULT = "query_by_fragment_id_result";

    public static final String ACTION_QUERY_ALL = "query_all";
    public static final String QUERY_ALL_RESULT = "query_all_result";

    public static final String ACTION_BIND_ACTIVITY = "bind_activity";
    public static final String BIND_ACTIVITY_RESULT = "bind_activity_result";

    public static final String ACTION_UNBIND_ACTIVITY = "unbind_activity";
    public static final String UNBIND_ACTIVITY_RESULT = "unbind_activity_result";

    public static final String ACTION_BIND_SERVICE = "bind_service";
    public static final String BIND_SERVICE_RESULT = "bind_service_result";

    public static final String ACTION_GET_BINDED_SERVICE = "get_binded_service";
    public static final String GET_BINDED_SERVICE_RESULT = "get_binded_service_result";

    public static final String ACTION_UNBIND_SERVICE = "unbind_service";
    public static final String UNBIND_SERVICE_RESULT = "unbind_service_result";

    public static final String ACTION_BIND_RECEIVER = "bind_receiver";
    public static final String BIND_RECEIVER_RESULT = "bind_receiver_result";

    public static final String ACTION_IS_EXACT = "is_exact";
    public static final String IS_EXACT_RESULT = "is_exact_result";

    public static final String ACTION_IS_STUB = "is_stub";
    public static final String IS_STUB_RESULT = "is_stub_result";

    public static final String ACTION_IS_PLUGIN_RUNNING = "is_plugin_running";
    public static final String IS_PLUGIN_RUNNING_RESULT = "is_plugin_running_result";

    public static final String ACTION_WAKEUP_PLUGIN = "wakeup_plugin";
    public static final String WAKEUP_PLUGIN_RESULT = "wakeup_plugin_result";

    public static final String ACTION_DUMP_SERVICE_INFO = "dump_service_info";
    public static final String DUMP_SERVICE_INFO_RESULT = "dump_service_info_result";

    //upgrade plugin
    public static final String ACTION_QUERY_UPGRADE_PLUGINS_INFO = "query_upgrade_pluginsinfo";
    public static final String QUERY_UPGRADE_PLUGINS_INFO_RESULT = "query_upgrade_pluginsinfo_result";
    public static final String ACTION_UPDATE_UPGRADE_INFO = "update_upgrade_plugin_info";
    public static final String EXTRAS_BUNDLE_PACKAGE_NAME = "extras_upgrade_packagename";
    public static final String EXTRAS_BUNDLE_PLUGIN_PATH = "extras_upgrade_plugin_path";

    // for debug parameter EXTRAS KEY
    public static final String EXTRAS_FOR_DEBUG = "extras_for_debug";

    private PluginManagerImpl manager;
    private PluginCallback changeListener;

    public static Uri buildUri() {
        if (CONTENT_URI == null) {
            CONTENT_URI = Uri.parse("content://" + PluginLoader.getApplication().getPackageName() + ".manager" + "/call");
        }
        return CONTENT_URI;
    }

    @Override
    public boolean onCreate() {
        manager = new PluginManagerImpl();
        changeListener = new PluginCallbackImpl();
        manager.loadInstalledPlugins();
        manager.loadsUpgradePluginsInfo();
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Uri targetUrl = Uri.parse(uri.getQueryParameter("targetUrl"));
        return getContext().getContentResolver().query(targetUrl, projection, selection, selectionArgs, sortOrder);
    }

//    @TargetApi(Build.VERSION_CODES.O)
//    @Override
//    public Cursor query(Uri uri, String[] projection, Bundle queryArgs, CancellationSignal cancellationSignal) {
//        Uri targetUrl = Uri.parse(uri.getQueryParameter("targetUrl"));
//        return getContext().getContentResolver().query(targetUrl, projection, queryArgs, cancellationSignal);
//    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder, CancellationSignal cancellationSignal) {
        Uri targetUrl = Uri.parse(uri.getQueryParameter("targetUrl"));
        return getContext().getContentResolver().query(targetUrl, projection, selection, selectionArgs, sortOrder, cancellationSignal);
    }

    @Override
    public String getType(Uri uri) {
        Uri targetUrl = Uri.parse(uri.getQueryParameter("targetUrl"));
        return getContext().getContentResolver().getType(targetUrl);
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        Uri targetUrl = Uri.parse(uri.getQueryParameter("targetUrl"));
        return getContext().getContentResolver().insert(targetUrl, values);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        Uri targetUrl = Uri.parse(uri.getQueryParameter("targetUrl"));
        return getContext().getContentResolver().delete(targetUrl, selection, selectionArgs);
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        Uri targetUrl = Uri.parse(uri.getQueryParameter("targetUrl"));
        return getContext().getContentResolver().update(targetUrl, values, selection, selectionArgs);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public Bundle call(String method, String arg, Bundle extras) {

        if (extras != null && extras.getParcelable("target_call") != null) {
            Uri targetUrl = extras.getParcelable("target_call");
            return getContext().getContentResolver().call(targetUrl, method, arg, extras);
        }

        if (Build.VERSION.SDK_INT >= 19) {
            try {
                Binder.clearCallingIdentity();
                QRomLog.i(TAG, "callingPackage = " + getCallingPackage());
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        }

        QRomLog.i(TAG, "Thead id=" + Thread.currentThread().getId() + " name=" + Thread.currentThread().getName()
                + " method=" + method + ", arg=" + arg);

        Bundle bundle = new Bundle();
        if (ACTION_INSTALL.equals(method)) {

            InstallResult result = manager.installPlugin(arg, extras == null ? false : extras.getBoolean(EXTRAS_FOR_DEBUG, false));
            bundle.putInt(INSTALL_RESULT, result.getResult());

            changeListener.onInstall(result.getResult(), result.getPackageName(), result.getVersion(), arg);

            return bundle;

        } else if (ACTION_REMOVE.equals(method)) {

            boolean success = manager.remove(arg, false);
            bundle.putBoolean(REMOVE_RESULT, success);

            changeListener.onRemove(arg, success);

            return bundle;

        } else if (ACTION_REMOVE_ALL.equals(method)) {

            boolean success = manager.removeAll();
            bundle.putBoolean(REMOVE_ALL_RESULT, success);

            changeListener.onRemoveAll(success);

            return bundle;

        } else if (ACTION_QUERY_BY_ID.equals(method)) {

            PluginDescriptor pluginDescriptor = manager.getPluginDescriptorByPluginId(arg);
            bundle.putSerializable(QUERY_BY_ID_RESULT, pluginDescriptor);

            return bundle;

        } else if (ACTION_QUERY_BY_CLASS_NAME.equals(method)) {

            PluginDescriptor pluginDescriptor = manager.getPluginDescriptorByClassName(arg);
            bundle.putSerializable(QUERY_BY_CLASS_NAME_RESULT, pluginDescriptor);

            return bundle;

        } else if (ACTION_QUERY_BY_FRAGMENT_ID.equals(method)) {

            PluginDescriptor pluginDescriptor = manager.getPluginDescriptorByFragmenetId(arg);
            bundle.putSerializable(QUERY_BY_FRAGMENT_ID_RESULT, pluginDescriptor);

            return bundle;

        } else if (ACTION_QUERY_ALL.equals(method)) {

            Collection<PluginDescriptor> pluginDescriptorList = manager.getPlugins();
            ArrayList<PluginDescriptor> result = new ArrayList<PluginDescriptor>(pluginDescriptorList.size());
            result.addAll(pluginDescriptorList);
            bundle.putSerializable(QUERY_ALL_RESULT, result);

            return bundle;

        } else if (ACTION_QUERY_UPGRADE_PLUGINS_INFO.equals(method)) {

            Hashtable<String, String> result = manager.getUpgradePluginsInfo();
            bundle.putSerializable(QUERY_UPGRADE_PLUGINS_INFO_RESULT, result);

            return bundle;

        }else if (ACTION_BIND_ACTIVITY.equals(method)) {
            int launchMode = extras.getInt(PluginManagerHelper.CONSTANT_KEY_LAUNCH_MODE);
            int processIndex = extras.getInt(PluginManagerHelper.CONSTANT_KEY_PROCESS_INDEX, ProcessUtil.PLUGIN_PROCESS_INDEX_HOST);
            bundle.putString(BIND_ACTIVITY_RESULT, PluginStubBinding.bindStubActivity(arg, launchMode, processIndex));

            return bundle;

        } else if (ACTION_UNBIND_ACTIVITY.equals(method)) {

            PluginStubBinding.unBindLaunchModeStubActivity(arg, extras.getString(PluginManagerHelper.CONSTANT_KEY_CLASS_NAME));

        } else if (ACTION_BIND_SERVICE.equals(method)) {
            int process = extras.getInt(PluginManagerHelper.CONSTANT_KEY_LAUNCH_MODE, ProcessUtil.PLUGIN_PROCESS_INDEX_HOST);
            bundle.putString(BIND_SERVICE_RESULT, PluginStubBinding.bindStubService(arg, process));

            return bundle;

        } else if (ACTION_GET_BINDED_SERVICE.equals(method)) {
            bundle.putString(GET_BINDED_SERVICE_RESULT, PluginStubBinding.getBindedPluginServiceName(arg));

            return bundle;

        } else if (ACTION_UNBIND_SERVICE.equals(method)) {

            PluginStubBinding.unBindStubService(arg);

        } else if (ACTION_BIND_RECEIVER.equals(method)) {
            bundle.putString(BIND_RECEIVER_RESULT, PluginStubBinding.bindStubReceiver());

            return bundle;

        } else if (ACTION_IS_STUB.equals(method)) {
            int type = extras.getInt(PluginManagerHelper.CONSTANT_KEY_COMPONENT_TYPE, DisplayItem.TYPE_UNKOWN);
            bundle.putBoolean(IS_STUB_RESULT, PluginStubBinding.isStub(arg, type));

            return bundle;

        } else if (ACTION_DUMP_SERVICE_INFO.equals(method)) {
            bundle.putString(DUMP_SERVICE_INFO_RESULT, PluginStubBinding.dumpServieInfo());
            return bundle;
        } else if (ACTION_IS_PLUGIN_RUNNING.equals(method)) {
            bundle.putBoolean(IS_PLUGIN_RUNNING_RESULT, PluginLauncher.instance().isRunning(arg));

            return bundle;
        } else if (ACTION_WAKEUP_PLUGIN.equals(method)) {
            LoadedPlugin loadedPlugin = PluginLauncher.instance().startPlugin(arg);
            bundle.putBoolean(WAKEUP_PLUGIN_RESULT, loadedPlugin != null);

            return bundle;
        } else if(ACTION_UPDATE_UPGRADE_INFO.equals(method)){
            String packagename = extras.getString(EXTRAS_BUNDLE_PACKAGE_NAME,arg);
            String pluginPath = extras.getString(EXTRAS_BUNDLE_PLUGIN_PATH,arg);
            manager.updateUpgradePluginPackageInfo(packagename, pluginPath);
        }

        return null;
    }
}
