package com.itheima.mobileguard.activities;

import android.app.Activity;
import android.content.pm.IPackageDataObserver;
import android.content.pm.IPackageStatsObserver;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageStats;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.text.format.Formatter;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.itheima.mobileguard.R;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class CleanCacheActivity extends Activity {
	protected static final int SCANNING = 1;
	protected static final int FINISH = 2;
	private PackageManager pm;
	private TextView tv_scan_status;
	private LinearLayout ll_loading;
	private LinearLayout ll_container;

	private List<CacheInfo> cacheInfos;

	private Handler handler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
				case SCANNING:
					PackageInfo info = (PackageInfo) msg.obj;
					tv_scan_status.setText("扫描："+info.applicationInfo.loadLabel(pm));
					break;

				case FINISH:
					ll_loading.setVisibility(View.INVISIBLE);
					if(cacheInfos.size()==0){
						Toast.makeText(getApplicationContext(), "恭喜您，您的手机干净无比。..", 1).show();
					}else{
						Toast.makeText(getApplicationContext(), "你的手机是垃圾堆，赶紧清理把", 1).show();
						for(final CacheInfo cacheinfo : cacheInfos){
							View view = View.inflate(getApplicationContext(), R.layout.item_cache_info, null);
							ImageView iv_icon = (ImageView) view.findViewById(R.id.iv_app_icon);
							TextView tv_name = (TextView) view.findViewById(R.id.tv_app_name);
							TextView tv_cache = (TextView) view.findViewById(R.id.tv_cache_size);
							iv_icon.setImageDrawable(cacheinfo.icon);
							tv_name.setText(cacheinfo.appname);
							tv_cache.setText(Formatter.formatFileSize(getApplicationContext(), cacheinfo.cachesize));
							ll_container.addView(view);
//						view.findViewById(R.id.bt_clean).setOnClickListener(new OnClickListener() {
//							@Override
//							public void onClick(View v) {
//								//清理缓存了。
//								//  mPm.deleteApplicationCacheFiles(packageName, mClearCacheObserver);
//								Method[] methods = PackageManager.class.getMethods();
//								for(Method method:methods){
//									if("deleteApplicationCacheFiles".equals(method.getName())){
//										try {
//											method.invoke(pm, cacheinfo.packname,new ClearCacheObserver());
//										} catch (Exception e) {
//											e.printStackTrace();
//										}
//									}
//								}
//							}
//						});
						}
					}
					break;
			}
		};
	};

	class ClearCacheObserver extends IPackageDataObserver.Stub {
		public void onRemoveCompleted(final String packageName, final boolean succeeded) {
			System.out.println(succeeded);
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_clean_cache);
		tv_scan_status = (TextView) findViewById(R.id.tv_scan_status);
		ll_container = (LinearLayout) findViewById(R.id.ll_container);
		ll_loading = (LinearLayout) findViewById(R.id.ll_loading);
		new Thread(){
			public void run() {
				cacheInfos = new ArrayList<CleanCacheActivity.CacheInfo>();
				// 遍历手机里面的所有的应用程序。
				pm = getPackageManager();
				List<PackageInfo> infos = pm.getInstalledPackages(0);
				for (PackageInfo info : infos) {
					getCacheSize(info);
					try {
						Thread.sleep(50);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					Message msg = Message.obtain();
					msg.what = SCANNING;
					msg.obj = info;
					handler.sendMessage(msg);
				}

				Message msg = Message.obtain();
				msg.what = FINISH;
				handler.sendMessage(msg);
			};
		}.start();

	}

	/**
	 * 获取某个包名对应的应用程序的缓存大小
	 *
	 * @param info
	 *            应用程序的包信息
	 */
	public void getCacheSize(PackageInfo info) {
		try {
			Method method = PackageManager.class.getDeclaredMethod(
					"getPackageSizeInfo", String.class,
					IPackageStatsObserver.class);
			method.invoke(pm, info.packageName, new MyPackObserver(info));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private class MyPackObserver extends
			android.content.pm.IPackageStatsObserver.Stub {
		private PackageInfo info;

		public MyPackObserver(PackageInfo info) {
			this.info = info;
		}

		@Override
		public void onGetStatsCompleted(PackageStats pStats, boolean succeeded)
				throws RemoteException {
			long cachesize = pStats.cacheSize;
			if (cachesize > 0) {
				System.out.println("应用程序有缓存："
						+ info.applicationInfo.loadLabel(pm)
						+ "--"
						+ Formatter.formatFileSize(getApplicationContext(),
						cachesize));
				CacheInfo cacheInfo = new CacheInfo();
				cacheInfo.cachesize = cachesize;
				cacheInfo.packname = info.packageName;
				cacheInfo.appname = info.applicationInfo.loadLabel(pm).toString();
				cacheInfo.icon = info.applicationInfo.loadIcon(pm);
				cacheInfos.add(cacheInfo);
			}
		}

	}

	class CacheInfo {
		String packname;
		String appname;
		long cachesize;
		Drawable icon;
	}


	public void cleanAll(View view){
		//清除全部 缓存 利用Android系统的一个漏洞。 freeStorageAndNotify
		Method[] methods = PackageManager.class.getMethods();
		for(Method method:methods){
			if("freeStorageAndNotify".equals(method.getName())){
				try {
					method.invoke(pm, Integer.MAX_VALUE,new ClearCacheObserver());
				} catch (Exception e) {
					e.printStackTrace();
				}
				return ;
			}
		}
		Toast.makeText(this, "清理完毕", 0).show();

	}
}
