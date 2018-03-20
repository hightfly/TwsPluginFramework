package com.rick.tws.pluginhost.main.ui.fragment;

import android.annotation.SuppressLint;
import android.app.TwsFragment;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import com.rick.tws.framework.HostProxy;
import com.rick.tws.pluginhost.R;
import com.rick.tws.pluginhost.main.HostApplication;
import com.rick.tws.pluginhost.main.content.HomeFragmentContentItem;
import com.rick.tws.pluginhost.main.content.HostDisplayItem;
import com.rick.tws.pluginhost.main.content.TestBundleObject;
import com.rick.tws.pluginhost.main.ui.MessageManagerActivity;
import com.rick.tws.pluginhost.main.ui.SettingsActivity;
import com.tws.plugin.content.DisplayItem;
import com.tws.plugin.manager.PluginManagerHelper;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;

import qrom.component.log.QRomLog;

@SuppressLint("ValidFragment")
public class HomeFragment extends TwsFragment implements OnClickListener {
    private static final String TAG = "rick_Print:HomeFragment";

    private static final int FIX_LOCATION_BEGIN = 99;

    private Resources mResources;

    private LinearLayout mFragmentContainer;
    private RelativeLayout mWatchInfoLayout;
    private ImageView mWatchImg;
    private TextView mConnectText, mWatchNameText;
    private ImageView mNotiRedpointImg, mOtaRedpoint;// mUpgradeRedPointImageView

    // 只做初始化构造存储用，后面不可在用
    private ArrayList<HostDisplayItem> mDisplayItems = new ArrayList<HostDisplayItem>();

    // 这个列表在安装和卸载插件的时候都需要维护
    private ArrayList<HomeFragmentContentItem> mContentItems = new ArrayList<HomeFragmentContentItem>();

    // 通知管理 和 设置是DM 固有的两项
    private HomeFragmentContentItem mMessageMgrItem = null, mSettingsItem = null;

    private TextView mNotificationDescTextView;

    private int item_layout_height;
    private int item_paddingLeft;

    public HomeFragment(ArrayList<HostDisplayItem> displayItems) {
        super();
        mDisplayItems.addAll(displayItems);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        QRomLog.i(TAG, "=========onCreateView=========");

        View rootView = inflater.inflate(R.layout.fragment_my_watch_revision, container, false);

        mResources = getResources();
        item_layout_height = (int) mResources.getDimension(R.dimen.HOST_HOME_FRAGMENT_revision_item_height);
        item_paddingLeft = (int) mResources.getDimension(R.dimen.HOST_HOME_FRAGMENT_revision_item_margin_left);

        initView(rootView);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setToolbarTextAndActionColor(R.color.tws_black);
    }

    @Override
    public String getFeaturePathName() {
        return "HomeFragment";
    }

    @Override
    public void onResume() {
        super.onResume();
        QRomLog.i(TAG, "=========onResume=========");
    }

    @Override
    public void onStart() {
        super.onStart();
        QRomLog.i(TAG, "=========onStart=========");
    }

    /**
     * 注意这里的index是指fragment列表PaceWear这一列的后面开始计算
     */
    public void addContentItem(HostDisplayItem di) {
        // 当前显示在首页My_Watch的内容暂只接收activity
        if (di.action_type != DisplayItem.TYPE_ACTIVITY) {
            return;
        }

        if (!isEnabledDisplayItem(di)) {
            QRomLog.e(TAG, "info is illegal(already exists), This will be ignored!");
            return;
        }

        HomeFragmentContentItem item = new HomeFragmentContentItem(mFragmentContainer.getContext(), true);
        LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, item_layout_height);
        item.setPadding(item_paddingLeft, 0, 0, 0);
        item.setLayoutParams(lp);
        item.setBackgroundResource(R.drawable.list_selector_background);// R.drawable.dm_common_single_item_selector
        item.setImageViewImageDrawable(PluginManagerHelper.getPluginIcon(di.normalResName));

        final ContextThemeWrapper context = getActivity();
        final Resources res = context == null ? HostApplication.getInstance().getResources() : context.getResources();
        final Locale locale = res.getConfiguration().locale;
        if ("zh".equals(locale.getLanguage())) {
            if ("HK".equals(locale.getCountry())) {
                item.setText(di.title_zh_HK);
            } else if ("TW".equals(locale.getCountry())) {
                item.setText(di.title_zh_TW);
            } else {
                item.setText(di.title_zh_CN);
            }
        } else {
            item.setText(di.title_en);
        }

        item.setOnClickListener(this);
        item.mStatKey = di.statistic_key;
        item.setActionClass(di.action_id, di.action_type);
        item.setPluginPackageName(di.pid);
        item.setLocation(di.x < 0 ? FIX_LOCATION_BEGIN - 1 : di.x);
        item.setVisibility(di.establishedDependOn ? View.VISIBLE : View.GONE);

        boolean insertRlt = false;
        int index = 0;
        for (index = 0; index < mContentItems.size(); index++) {
            if (item.getLocation() < mContentItems.get(index).getLocation()) {
                insertRlt = true;
                mContentItems.add(index, item);
                break;
            }
        }
        if (!insertRlt) {
            mContentItems.add(item);
        }

        mFragmentContainer.addView(item, index + 1);// +1是fragment顶部有一个固定的linelayout
    }

    public boolean isEnabledDisplayItem(final HostDisplayItem di) {
        if (di == null || TextUtils.isEmpty(di.pid) || TextUtils.isEmpty(di.action_id))
            return false;

        for (HomeFragmentContentItem item : mContentItems) {
            if (di.pid.equals(item.getPluginPackageName()) && di.action_id.equals(item.getClassId())) {
                return false;
            }
        }

        return true;
    }

    public void printContentItemsInfo() {
        QRomLog.i(TAG, "============== begin printContentItemsInfo ==============");
        for (int index = 0; index < mContentItems.size(); index++) {
            final HomeFragmentContentItem item = mContentItems.get(index);
            QRomLog.i(
                    TAG,
                    "mContentItems[" + index + "] text is " + item.getTextViewText() + " Location is "
                            + item.getLocation());
        }
        QRomLog.i(TAG, "============== end printContentItemsInfo ==============");
    }

    private void initView(View rootView) {
        mFragmentContainer = (LinearLayout) rootView.findViewById(R.id.my_watch_revision_item_layout);
        mWatchInfoLayout = (RelativeLayout) rootView.findViewById(R.id.my_watch_revision_watch_info_layout);

        final int statusBarH = HostProxy.getStatusBarHeight();
        mWatchInfoLayout.setPadding(0, statusBarH, 0, 0);
//        boolean hasOverlayActionbar = getActivity().getWindow().hasFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
//        if (hasOverlayActionbar) {
//            int top = (int) getResources().getDimension(R.dimen.tws_action_bar_height);
//            if (getActivity() instanceof TwsActivity) {
//                top += TwsActivity.getStatusBarHeight();
//            }
//            mWatchInfoLayout.setPadding(0, top, 0, 0);
//        }

        mWatchInfoLayout.setOnClickListener(this);

        mWatchImg = (ImageView) rootView.findViewById(R.id.my_watch_revision_watch_img);
        mWatchImg.setImageResource(R.mipmap.twatch_dm_png_default);

        mConnectText = (TextView) rootView.findViewById(R.id.my_watch_revision_connect_text);
        mConnectText.setText("未连接");

        mWatchNameText = (TextView) rootView.findViewById(R.id.my_watch_revision_watch_name);
        mWatchNameText.setText(R.string.watch_name);

        mOtaRedpoint = (ImageView) rootView.findViewById(R.id.my_watch_revision_redpoint_img);
        mOtaRedpoint.setImageResource(R.mipmap.red_point);

        final Resources res = rootView.getContext().getResources();

        final int thickSplitHeight = (int) res.getDimension(R.dimen.HOST_HOME_FRAGMENT_revision_item_big_divider);
        final Drawable thickSplitBackground = res.getDrawable(R.color.tws_stipple);

        // 注意：插件提供的Item 索引值应该是从1开始的，上面有一个mWatchInfoLayout
        if (mDisplayItems != null) {
            for (HostDisplayItem item : mDisplayItems) {
                addContentItem(item);
            }

            printContentItemsInfo();
        }

        // add 通知管理
        mMessageMgrItem = new HomeFragmentContentItem(rootView.getContext());
        mMessageMgrItem.setToNotify();
        LayoutParams lp_notify = new LayoutParams(LayoutParams.MATCH_PARENT, item_layout_height);
        mMessageMgrItem.setPadding(item_paddingLeft, 0, 0, 0);
        mMessageMgrItem.setLayoutParams(lp_notify);
        mMessageMgrItem.setBackgroundResource(R.drawable.list_selector_background);// R.drawable.dm_common_single_item_selector
        mMessageMgrItem.setImageViewImageDrawable(R.mipmap.home_item_notification_normal);
        mMessageMgrItem.setText(res.getString(R.string.message_mgr));
        mMessageMgrItem.setOnClickListener(this);
        mMessageMgrItem.mSpecialFlg = HomeFragmentContentItem.ITEM_MESSAGE;
        mMessageMgrItem.setActionClass(MessageManagerActivity.class.getName(), DisplayItem.TYPE_ACTIVITY);
        mMessageMgrItem.setLocation(FIX_LOCATION_BEGIN);
        mFragmentContainer.addView(mMessageMgrItem);
        // mContentItems.add(notifyMgr); //notifyMgr 作为DM固有的item可以不参与管理

        mNotiRedpointImg = mMessageMgrItem.getNotifyImageView();
        mNotificationDescTextView = mMessageMgrItem.getNotifyTextView();

        // 添加分割线 - 粗的
        insertSplit(mFragmentContainer, thickSplitHeight, 0, thickSplitBackground);
        // 最后添加Settings
        mSettingsItem = new HomeFragmentContentItem(rootView.getContext(), true);
        LayoutParams lp_settings = new LayoutParams(LayoutParams.MATCH_PARENT, item_layout_height);
        mSettingsItem.setPadding(item_paddingLeft, 0, 0, 0);
        mSettingsItem.setLayoutParams(lp_settings);
        mSettingsItem.setBackgroundResource(R.drawable.list_selector_background);// R.drawable.dm_common_single_item_selector
        mSettingsItem.setText(res.getString(R.string.settings));
        mSettingsItem.setImageViewImageDrawable(R.mipmap.home_item_my_settings);
        mSettingsItem.setOnClickListener(this);
        mSettingsItem.mSpecialFlg = HomeFragmentContentItem.ITEM_SETTINGS;
        mSettingsItem.setActionClass(SettingsActivity.class.getName(), DisplayItem.TYPE_ACTIVITY);
        mSettingsItem.setLocation(FIX_LOCATION_BEGIN + 1);
        mFragmentContainer.addView(mSettingsItem);
        // mContentItems.add(settings); //Settings 作为DM固有的item可以不参与管理
    }

    private void insertSplit(LinearLayout root, int height, int marginLeft, Drawable background) {
        TextView tv = new TextView(root.getContext());
        tv.setBackground(background);
        LinearLayout.LayoutParams lp_split = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, height);
        lp_split.leftMargin = marginLeft;
        root.addView(tv, lp_split);
    }

    @Override
    public void onClick(View view) {
        if (view instanceof HomeFragmentContentItem) {
            final HomeFragmentContentItem item = (HomeFragmentContentItem) view;

            Intent intent = new Intent();
            if (TextUtils.isEmpty(item.getPluginPackageName())) {
                intent.setClassName(getActivity(), item.getClassId());
            } else {
                intent.setClassName(item.getPluginPackageName(), item.getClassId());
            }

            intent.putExtra(TestBundleObject.INTENT_EXTRA_NAME, ((Parcelable) new TestBundleObject("TestName", 21)));

            switch (item.getComponentType()) {
                case DisplayItem.TYPE_ACTIVITY:
                    startActivity(intent);
                    break;
                default:
                    break;
            }

            if (item.mSpecialFlg == HomeFragmentContentItem.ITEM_MESSAGE && item.isNotify()
                    && mNotiRedpointImg.getVisibility() == View.VISIBLE) {
                mNotiRedpointImg.setVisibility(View.INVISIBLE);
            }
        } else {
            switch (view.getId()) {
                case R.id.my_watch_revision_watch_info_layout:
                    mOtaRedpoint.setVisibility(View.INVISIBLE);
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        QRomLog.i(TAG, "=========onDestroyView=========");
    }

    public void removePlugin(String packageName) {
        if (TextUtils.isEmpty(packageName))
            return;

        ArrayList<HomeFragmentContentItem> removeItems = new ArrayList<HomeFragmentContentItem>();
        Iterator<HomeFragmentContentItem> iter = mContentItems.iterator();
        removeItems.clear();
        while (iter.hasNext()) {
            HomeFragmentContentItem item = iter.next();
            if (packageName.equals(item.getPluginPackageName())) {
                mFragmentContainer.removeView(item);
                removeItems.add(item);
            }
        }

        if (0 < removeItems.size()) {
            mContentItems.removeAll(removeItems);
        }
    }

    public void unEstablishedDependOnForPlugin(String pid) {
        if (TextUtils.isEmpty(pid))
            return;

        for (HomeFragmentContentItem item : mContentItems) {
            if (pid.equals(item.getPluginPackageName())) {
                item.setVisibility(View.GONE);
            }
        }
    }

    public void establishedDependOnForPlugin(String pid) {
        if (TextUtils.isEmpty(pid))
            return;

        for (HomeFragmentContentItem item : mContentItems) {
            if (pid.equals(item.getPluginPackageName())) {
                item.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        QRomLog.i(TAG, "=========onPause=========");
    }

    @Override
    public void onStop() {
        super.onStop();
        QRomLog.i(TAG, "=========onStop=========");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        QRomLog.i(TAG, "=========onDestroy=========");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        QRomLog.i(TAG, "=========onDetach=========");
    }
}
