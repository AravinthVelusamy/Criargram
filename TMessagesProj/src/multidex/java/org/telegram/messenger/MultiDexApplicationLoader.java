package org.criargram.messenger;

import android.content.Context;
import android.support.multidex.MultiDex;

public class MultiDexApplicationLoader extends ApplicationLoader{

	@Override
	protected void attachBaseContext(Context base){
		super.attachBaseContext(base);
		MultiDex.install(this);
	}
}