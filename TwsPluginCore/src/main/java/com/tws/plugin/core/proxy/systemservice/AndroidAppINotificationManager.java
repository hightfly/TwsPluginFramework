package com.tws.plugin.core.proxy.systemservice;

import java.io.File;
import java.lang.reflect.Method;

import qrom.component.log.QRomLog;

import android.app.Notification;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.os.Build;
import android.text.TextUtils;

import com.tws.plugin.content.PluginDescriptor;
import com.tws.plugin.core.PluginIntentResolver;
import com.tws.plugin.core.PluginLoader;
import com.tws.plugin.core.android.HackNotificationManager;
import com.tws.plugin.core.android.HackPendingIntent;
import com.tws.plugin.core.android.HackRemoteViews;
import com.tws.plugin.core.proxy.MethodDelegate;
import com.tws.plugin.core.proxy.MethodProxy;
import com.tws.plugin.core.proxy.ProxyUtils;
import com.tws.plugin.manager.PluginManagerHelper;
import com.tws.plugin.util.PluginFileUtils;
import com.tws.plugin.util.ResourceUtils;

/**
 * Created by yongchen
 */
public class AndroidAppINotificationManager extends MethodProxy {

    private static final String TAG = "rick_Print:AndroidAppINotificationManager";

    static {
        sMethods.put("enqueueNotification", new enqueueNotification());
        sMethods.put("enqueueNotificationWithTag", new enqueueNotificationWithTag());
        sMethods.put("enqueueNotificationWithTagPriority", new enqueueNotificationWithTagPriority());
    }

    public static void installProxy() {
        QRomLog.i(TAG, "安装NotificationManagerProxy");
        Object androidAppINotificationStubProxy = HackNotificationManager.getService();
        Object androidAppINotificationStubProxyProxy = ProxyUtils.createProxy(androidAppINotificationStubProxy,
                new AndroidAppINotificationManager());
        HackNotificationManager.setService(androidAppINotificationStubProxyProxy);
        QRomLog.i(TAG, "安装完成");
    }

    public static class enqueueNotification extends MethodDelegate {
        @Override
        public Object beforeInvoke(Object target, Method method, Object[] args) {
            QRomLog.i(TAG, "beforeInvoke：" + method.getName());
            args[0] = PluginLoader.getApplication().getPackageName();
            for (Object obj : args) {
                if (obj instanceof Notification) {
                    resolveRemoteViews((Notification) obj);
                    break;
                }
            }
            return super.beforeInvoke(target, method, args);
        }
    }

    public static class enqueueNotificationWithTag extends MethodDelegate {
        @Override
        public Object beforeInvoke(Object target, Method method, Object[] args) {
            QRomLog.i(TAG, "beforeInvoke:" + method.getName());
            args[0] = PluginLoader.getApplication().getPackageName();
            for (Object obj : args) {
                if (obj instanceof Notification) {
                    resolveRemoteViews((Notification) obj);
                    break;
                }
            }
            return super.beforeInvoke(target, method, args);
        }
    }

    public static class enqueueNotificationWithTagPriority extends MethodDelegate {
        @Override
        public Object beforeInvoke(Object target, Method method, Object[] args) {
            QRomLog.i(TAG, "beforeInvoke:" + method.getName());
            args[0] = PluginLoader.getApplication().getPackageName();
            for (Object obj : args) {
                if (obj instanceof Notification) {
                    resolveRemoteViews((Notification) obj);
                    break;
                }
            }
            return super.beforeInvoke(target, method, args);
        }
    }

    private static void resolveRemoteViews(Notification notification) {

        String hostPackageName = PluginLoader.getApplication().getPackageName();

        if (Build.VERSION.SDK_INT >= 23) {
//            Icon mSmallIcon = (Icon)RefInvoker.getField(notification, Notification.class, "mSmallIcon");
//            Icon mLargeIcon = (Icon)RefInvoker.getField(notification, Notification.class, "mLargeIcon");
//            if (mSmallIcon != null) {
//                RefInvoker.setField(mSmallIcon, Icon.class, "mString1", hostPackageName);
//            }
//            if (mLargeIcon != null) {
//                RefInvoker.setField(mLargeIcon, Icon.class, "mString1", hostPackageName);
//            }
        }

        if (Build.VERSION.SDK_INT >= 21) {

            int layoutId = 0;
            if (notification.tickerView != null) {
                layoutId = new HackRemoteViews(notification.tickerView).getLayoutId();
            }
            if (layoutId == 0) {
                if (notification.contentView != null) {
                    layoutId = new HackRemoteViews(notification.contentView).getLayoutId();
                }
            }
            if (layoutId == 0) {
                if (notification.bigContentView != null) {
                    layoutId = new HackRemoteViews(notification.bigContentView).getLayoutId();
                }
            }
            if (layoutId == 0) {
                if (notification.headsUpContentView != null) {
                    layoutId = new HackRemoteViews(notification.headsUpContentView).getLayoutId();
                }
            }

            if (layoutId == 0) {
                return;
            }

            // 检查资源布局资源Id是否属于宿主
            if (ResourceUtils.isMainResId(layoutId)) {
                return;
            }

            // 检查资源布局资源Id是否属于系统
            if (layoutId >> 24 == 0x1f) {
                return;
            }

            if ("Xiaomi".equals(Build.MANUFACTURER)) {
                QRomLog.e(TAG, "Xiaomi, not support, caused by MiuiResource");
                if (notification.contentView != null) {
                    // 重置layout，避免crash
                    new HackRemoteViews(notification.contentView).setLayoutId(android.R.layout.test_list_item);
                }
                notification.bigContentView = null;
                notification.headsUpContentView = null;
                notification.tickerView = null;
                return;
            }

            ApplicationInfo newInfo = new ApplicationInfo();
            String packageName = null;

            if (notification.tickerView != null) {
                packageName = notification.tickerView.getPackage();
                new HackRemoteViews(notification.tickerView).setApplicationInfo(newInfo);
            }
            if (notification.contentView != null) {
                if (packageName == null) {
                    packageName = notification.contentView.getPackage();
                }
                new HackRemoteViews(notification.contentView).setApplicationInfo(newInfo);
            }
            if (notification.bigContentView != null) {
                if (packageName == null) {
                    packageName = notification.bigContentView.getPackage();
                }
                new HackRemoteViews(notification.bigContentView).setApplicationInfo(newInfo);
            }
            if (notification.headsUpContentView != null) {
                if (packageName == null) {
                    packageName = notification.headsUpContentView.getPackage();
                }
                new HackRemoteViews(notification.headsUpContentView).setApplicationInfo(newInfo);
            }

            ApplicationInfo applicationInfo = PluginLoader.getApplication().getApplicationInfo();
            newInfo.packageName = applicationInfo.packageName;
            newInfo.sourceDir = applicationInfo.sourceDir;
            newInfo.dataDir = applicationInfo.dataDir;

            if (packageName != null && !packageName.equals(hostPackageName)) {

                PluginDescriptor pluginDescriptor = PluginManagerHelper.getPluginDescriptorByPluginId(packageName);
                newInfo.packageName = pluginDescriptor.getPackageName();
                // 要确保publicSourceDir这个路径可以被SystemUI应用读取，
                newInfo.publicSourceDir = prepareNotificationResourcePath(pluginDescriptor.getInstalledPath(),
                        PluginLoader.getApplication().getExternalCacheDir().getAbsolutePath() + "/notification_res.apk");

            } else if (packageName != null && packageName.equals(hostPackageName)) {
                // 如果packageName是宿主，由于前面已经判断出，layoutid不是来自插件，则尝试查找notifications的目标页面，如果目标是插件，则尝试使用此插件作为通知栏的资源来源
                if (notification.contentIntent != null) {// 只处理contentIntent，其他不管
                    Intent intent = new HackPendingIntent(notification.contentIntent).getIntent();
                    final String action = intent != null ? intent.getAction() : null;
                    if (action != null && action.contains(PluginIntentResolver.CLASS_SEPARATOR)) {
                        String[] targetClassName = action.split(PluginIntentResolver.CLASS_SEPARATOR);

                        final String pid = 2 < targetClassName.length ? targetClassName[2] : "";
                        PluginDescriptor pluginDescriptor = null;
                        if (!TextUtils.isEmpty(pid)) {
                            pluginDescriptor = PluginManagerHelper.getPluginDescriptorByPluginId(pid);
                        }

                        if (pluginDescriptor == null) {
                            PluginManagerHelper.getPluginDescriptorByClassName(targetClassName[0]);
                        }

                        newInfo.packageName = pluginDescriptor.getPackageName();
                        // 要确保publicSourceDir这个路径可以被SystemUI应用读取，
                        newInfo.publicSourceDir = prepareNotificationResourcePath(pluginDescriptor.getInstalledPath(),
                                PluginLoader.getApplication().getExternalCacheDir().getAbsolutePath() + "/notification_res.apk");
                    }
                }
            }
        } else if (Build.VERSION.SDK_INT >= 11) {
            if (notification.tickerView != null) {
                new HackRemoteViews(notification.tickerView).setPackage(hostPackageName);
            }
            if (notification.contentView != null) {
                new HackRemoteViews(notification.contentView).setPackage(hostPackageName);
            }
        }
    }

    private static String prepareNotificationResourcePath(String pluginInstalledPath, String worldReadablePath) {
        QRomLog.i(TAG, "正在为通知栏准备插件资源。。。这里现在暂时是同步复制，注意大文件卡顿！！");
        File worldReadableFile = new File(worldReadablePath);

        if (PluginFileUtils.copyFile(pluginInstalledPath, worldReadableFile.getAbsolutePath())) {
            QRomLog.i(TAG, "通知栏插件资源准备完成，请确保此路径SystemUi有读权限:" + worldReadableFile.getAbsolutePath());
            return worldReadableFile.getAbsolutePath();
        } else {
            QRomLog.e(TAG, "不应该到这里来，直接返回这个路径SystemUi没有权限读取");
            return pluginInstalledPath;
        }
    }

}
