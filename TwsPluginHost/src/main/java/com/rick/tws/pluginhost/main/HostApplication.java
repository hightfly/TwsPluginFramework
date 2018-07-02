package com.rick.tws.pluginhost.main;

import android.content.Context;

import com.rick.tws.framework.HostProxy;
import com.rick.tws.pluginhost.main.widget.Hotseat;
import com.tws.plugin.core.PluginApplication;
import com.tws.plugin.core.PluginLoader;
import com.tws.plugin.util.ProcessUtils;

/**
 * Created by Administrator on 2017/11/6 0006.
 */
public class HostApplication extends PluginApplication {
    private static final String TAG = "HostApplication";

    private String mFouceTabClassId = Hotseat.HOST_HOME_FRAGMENT;

    @Override
    public void onCreate() {
        super.onCreate();

        //宿主进程只有一个，插件进程却又好几个，放宿主进程也可以保证插件的加载只有一次
        //插件的具体安装操作确是在Provider进程，不过provider本身能夸进程，不影响宿主获取插件info
        if (ProcessUtils.isHostProcess(this)) {
            //加载插件：如果是新安装或者升级，需要安装
            PluginLoader.loadPlugins(this);

            // 提前启动宿主Host的依赖插件[比如：手表助手DM的启动依赖登录插件]
            startAppDependentPlugin();

            // 随DM启动的插件 时机调整到application的onCreate里面
            startNeedPowerbootPlugin();
        }
    }

    private void startAppDependentPlugin() {
        // 宿主的启动依赖一些插件，需要提前加载好这些插件
//        final String loginPackageName = "com.*.plugin.login";
//        final String loginPluginFileName = "TwsPluginLogin.apk";
//        boolean hasLoginPlugin = false;
//
//        Collection<PluginDescriptor> plugins = PluginManagerHelper.getPlugins();
//        Iterator<PluginDescriptor> itr = plugins.iterator();
//        while (itr.hasNext()) {
//            final PluginDescriptor pluginDescriptor = itr.next();
//            QRomLog.i(TAG, "plugin packageName=" + pluginDescriptor.getPackageName());
//            String packageName = pluginDescriptor.getPackageName();
//            if (packageName.equals(loginPackageName)) {
//                PluginLauncher.instance().startPlugin(pluginDescriptor);
//                hasLoginPlugin = true;
//            }
//            if (hasLoginPlugin) {
//                return;
//            }
//        }
//        if (!hasLoginPlugin) {
//            boolean isInstallPluginDirLogin = PluginLoader.copyAndInstall("plugins" + File.separator
//                    + loginPluginFileName);
//            QRomLog.i(TAG, "isInstallPluginDirLogin = " + isInstallPluginDirLogin);
//            PluginLauncher.instance().startPlugin(loginPackageName);
//        }
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        HostProxy.setApplication(this);
    }

    public String getFouceTabClassId() {
        return mFouceTabClassId;
    }
}
