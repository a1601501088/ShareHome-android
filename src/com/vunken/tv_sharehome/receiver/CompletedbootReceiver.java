package com.vunken.tv_sharehome.receiver;

import com.vunken.tv_sharehome.activity.LoginActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class CompletedbootReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		context.startActivity(new Intent(context, LoginActivity.class)
				.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));

	}

}
