package com.yc.patrol.utils;


import com.baidu.idl.face.main.activity.BaseActivity;

import java.util.Stack;

public class ScreenManager {

	private static Stack<BaseActivity> activityStack;
	private static ScreenManager instance = new ScreenManager();

	private ScreenManager() {
		if (activityStack == null) {
			activityStack = new Stack<BaseActivity>();
		}
	}

	public static ScreenManager getScreenManager() {
		if (instance == null) {
			instance = new ScreenManager();
		}
		return instance;

	}

	public BaseActivity getActivity(Class<?> cls) {

		for (int i = 0; i < activityStack.size(); i++) {
			BaseActivity activity = activityStack.get(i);
			if (activity.getClass().equals(cls)) {
				return activity;
			}
		}
		return null;

	}

	// 退出栈顶Activity
	public void popActivity() {

		popActivity(currentActivity());
	}
	public void popActivity(BaseActivity activity) {

		if (activity != null) {
			// activity.finish();
			activityStack.remove(activity);
			activity = null;
		}
	}

	public void popActivity(BaseActivity activity, boolean quit) {
		if (activity != null) {
			activity.finish();
			activityStack.remove(activity);
			activity = null;
		}
	}

	// 获得当前栈顶Activity
	public BaseActivity currentActivity() {
		try {
			BaseActivity activity = activityStack.get(activityStack.size() - 1);
			return activity;
		} catch (Exception e) {
			return null;
		}
	}

	// 将当前Activity推入栈中
	public void pushActivity(BaseActivity activity) {
		if (activityStack == null) {
			activityStack = new Stack<BaseActivity>();
		}
		activityStack.add(activity);
	}

	public boolean isHave(Class<?> cls) {
		if (activityStack == null) {
			return false;
		}
		boolean ishave = false;
		for (int i = 0; i < activityStack.size(); i++) {
			BaseActivity activity = activityStack.get(i);
			if (activity.getClass().equals(cls)) {
				return true;
			}
		}
		return ishave;
	}

	public BaseActivity popAllActivityExceptOne(Class<?> cls) {
		boolean bquit = true;
		while (bquit) {
			// println();
			BaseActivity activity = currentActivity();
			if (activity != null) {
				if (activity.getClass().equals(cls)) {
					return activity;
				}
				if (bquit) {
					popActivity(activity, true);
				}
			} else {
				bquit = false;
			}
		}
		return null;
	}
}