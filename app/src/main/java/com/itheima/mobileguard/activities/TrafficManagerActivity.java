package com.itheima.mobileguard.activities;

import android.app.Activity;
import android.os.Bundle;

import com.itheima.mobileguard.R;

public class TrafficManagerActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
/*
		//rx receive 接收 下载
		//手机的2g/3g/4g 产生流量
		long mobileRx = TrafficStats.getMobileRxBytes();

		//transfer 发送  上传
		long mobileTx = TrafficStats.getMobileTxBytes();
		//全部的网络信息  wifi + 手机卡
		long totalRx = TrafficStats.getTotalRxBytes();

		long totalTx = TrafficStats.getTotalTxBytes();

		//uid 用户id
		int uid = 0;
		TrafficStats.getUidRxBytes(10041);
		TrafficStats.getUidTxBytes(10041);

		///proc/uid_stat/10041/tcp_rcv  存储的就是下载的流量
		//proc/uid_stat/10041/tcp_snd 上传的流量

		ConnectivityManager  cm = 	(ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		cm.getActiveNetworkInfo().getTypeName();
		*/

		setContentView(R.layout.activity_traffic_manager);

	}
}
