/*
 * Copyright(c) 2014 by LG Electronics. All Rights Reserved.
 */
package com.lge.qremotesdk.sample;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.lge.hardware.IRBlaster.Device;
import com.lge.hardware.IRBlaster.IRAction;
import com.lge.hardware.IRBlaster.IRBlaster;
import com.lge.hardware.IRBlaster.IRFunction;
import com.lge.hardware.IRBlaster.LearnedIrData;
import com.lge.hardware.IRBlaster.ResultCode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LearnTestDialogFragment extends DialogFragment {

	public interface DismissCallback {
		public void onDismiss();
	}

	public interface LearnCompleteCallback {
		public void onComplete(int status);
	}

	private final static String TAG = "QRemoteSDKSample_LearnTestDialog";

	/***** IR Blaster related fields *****/
	private IRBlaster mIr = null;
	private Device mDev = null;

	private String mLearningFuncName = null;
	private int mLearningFuncId = -1;

	private LearnedIrData mLearnedIrData = null;

	/***** Layout related fields *****/
	private ListView mFuncLv = null;
	private EditText mFuncNameEt = null;
	private Button mStartLearnBtn = null;
	private ProgressDialog mLearningProgressDlg = null;

	/** The delay of effects to highlight states of the learning process. */
	protected int EFFECT_DELAY = 0;
	protected int EFFECT_FAIL_DELAY = 3000;
	protected int DISMISS_DELAY = 1500;

	private DismissCallback mDismissCallback = null;

	/**
	 * Messages to the Handler related to the learning process.
	 */
	protected static final int START_IR_LEARNING = 1;
	protected static final int DISMISS_LEARNING = 2;
	protected static final int SHOW_LEARNING_ANI = 3;
	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {

				case START_IR_LEARNING:
					// start learning process
					mIr.startIrLearning();

					int result = mIr.getLastResultcode();

					if (result == ResultCode.SUCCESS) {
						Log.d(TAG, "Learning process is started. Waiting for the callback...");

						Context ctx = getActivity();
						if ((ctx != null) && (ctx instanceof Activity)) {
							((Activity)ctx).getWindow().addFlags(
							        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
						}
						// Show a progress dialog
						if (mHandler != null) {
							mHandler.removeMessages(SHOW_LEARNING_ANI);
							mHandler.sendMessageDelayed(mHandler.obtainMessage(SHOW_LEARNING_ANI),
							        EFFECT_DELAY);
						}
					} else {
						// Check the state of the blaster before next action.
						if (mHandler != null) {
							mHandler.removeMessages(START_IR_LEARNING);
							mHandler.sendMessageDelayed(mHandler.obtainMessage(START_IR_LEARNING),
							        EFFECT_DELAY);
						}
					}
					break;
				case SHOW_LEARNING_ANI:
					// show the progress dialog during the process
					mLearningProgressDlg = new ProgressDialog(getActivity());
					mLearningProgressDlg.setMessage("Learning IR data...");
					mLearningProgressDlg.setCancelable(false);
					mLearningProgressDlg.show();
					break;
				default:
			}
		}
	};

	private LearnCompleteCallback mLearnCompleteCallback = new LearnCompleteCallback() {

		@Override
		public void onComplete(int status) {
			// dismiss the progress dialog
			if (mLearningProgressDlg != null && mLearningProgressDlg.isShowing()) {
				mLearningProgressDlg.dismiss();
			}

			final Context ctx = getActivity();
			if ((ctx != null) && (ctx instanceof Activity)) {
				((Activity)ctx).runOnUiThread(new Runnable() {

					@Override
					public void run() {
						((Activity)ctx).getWindow().clearFlags(
						        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
					}
				});
			}

			// handle result status
			if (status == ResultCode.SUCCESS) {

				mLearnedIrData = mIr.getLearnedData();
				if (mLearnedIrData == null) {
					Log.e(TAG, "Learned data is NULL.");
				}

				// Add or edit the learned function into the SDK.
				if (mLearningFuncId != -1) {
					// Edit the existing function.
					mIr.editIrFunctionWithLearnedData(mDev.Id, mLearningFuncId);
				} else {
					// Save new function.
					mLearningFuncId = mIr.addLearnedIrFunction(mDev.Id, mLearningFuncName);

					if (mLearningFuncId == -1) {
						Log.e(TAG, "Problem to save the button.");

						getActivity().runOnUiThread(new Runnable() {

							@Override
							public void run() {
								Toast.makeText(getActivity(), "Button save failed",
								        Toast.LENGTH_SHORT).show();

								mHandler.removeMessages(START_IR_LEARNING);
								mHandler.sendMessageDelayed(
								        mHandler.obtainMessage(START_IR_LEARNING),
								        EFFECT_FAIL_DELAY);
							}
						});

						return;
					}
				}
				updateDevice();

				getActivity().runOnUiThread(new Runnable() {

					@Override
					public void run() {
						Toast.makeText(getActivity(), "Button saved", Toast.LENGTH_SHORT).show();

						mHandler.removeMessages(DISMISS_LEARNING);
						mHandler.sendMessageDelayed(mHandler.obtainMessage(DISMISS_LEARNING),
						        DISMISS_DELAY);
					}
				});
			} else {

				if (status == ResultCode.IRLEARNING_ABORTED) {
					// Learning is cancelled by a user.
					// Do nothing.
				} else {

					if (status == ResultCode.IRLEARNING_TIMEOUT) {
						getActivity().runOnUiThread(new Runnable() {

							@Override
							public void run() {
								Toast.makeText(getActivity(), "Learning timeout",
								        Toast.LENGTH_SHORT).show();
							}
						});
					} else if (status == ResultCode.IRLEARNING_FAILED) {
						getActivity().runOnUiThread(new Runnable() {

							@Override
							public void run() {
								Toast.makeText(getActivity(), "Learning failed", Toast.LENGTH_SHORT)
								        .show();
							}
						});
					}
				}
			}
		}
	};

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View layout = inflater.inflate(R.layout.dialog_learn_test, container, false);
		mFuncLv = (ListView)layout.findViewById(R.id.func_list);
		mFuncNameEt = (EditText)layout.findViewById(R.id.func_name_input);
		mStartLearnBtn = (Button)layout.findViewById(R.id.start_learn);

		initFuncList();

		mStartLearnBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String funcName = mFuncNameEt.getText().toString();
				if ((funcName != null) && !funcName.isEmpty()) {
					if (!funcName.equals(mLearningFuncName)) {
						mLearningFuncName = funcName;
						mLearningFuncId = -1;
					}
					if (mHandler != null) {
						mHandler.removeMessages(START_IR_LEARNING);
						mHandler.sendMessageDelayed(mHandler.obtainMessage(START_IR_LEARNING),
						        EFFECT_DELAY);
					}
				} else {
					Log.w(TAG, "Function is not inputed or is not selected.");
					Toast.makeText(getActivity(), "Please input or select function name",
					        Toast.LENGTH_SHORT).show();
				}
			}
		});

		return layout;
	}

	public void setIrBlaster(IRBlaster blaster) {
		mIr = blaster;
	}

	public void setDevice(Device dev) {
		mDev = dev;
	}

	public LearnCompleteCallback getLearnCompleteCallback() {
		return mLearnCompleteCallback;
	}

	public void setDismissCallback(DismissCallback cb) {
		mDismissCallback = cb;
	}

	public LearnedIrData getLastLearnedData() {
		return mLearnedIrData;
	}

	@Override
	public void onDismiss(DialogInterface dialog) {
		if (mDismissCallback != null) {
			mDismissCallback.onDismiss();
		}
		super.onDismiss(dialog);
	}

	private void initFuncList() {
		if (mDev != null) {
			Log.i(TAG, "Get functions from an existing device with ID [" + mDev.Id + "].");
			updateListView();
		} else {
			Log.i(TAG, "Create a new device.");
			mDev = mIr.createDevice("learn device", "learn test", "learn test");
		}
	}

	private void updateListView() {
		getActivity().runOnUiThread(new Runnable() {

			@Override
			public void run() {
				final List<IRFunction> keyFunctions = mDev.KeyFunctions;
				List<String> keyFuncNames = new ArrayList<String>();
				if (keyFunctions != null) {
					for (IRFunction keyFunc: keyFunctions) {
						keyFuncNames.add(keyFunc.Name);
					}
				}
				mFuncLv.setAdapter(new ArrayAdapter<String>(getActivity(),
				        android.R.layout.simple_list_item_1, keyFuncNames));
				mFuncLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
						if ((keyFunctions != null) && (pos < keyFunctions.size())) {
							mLearningFuncId = keyFunctions.get(pos).Id;
							mIr.sendIR(new IRAction(mDev.Id, mLearningFuncId, 300));
							mLearningFuncName = keyFunctions.get(pos).Name;
							mFuncNameEt.setText(mLearningFuncName);
						}
					}
				});
			}
		});
	}

	private void updateDevice() {
		if (mDev != null) {
			Device device = mIr.getDeviceById(mDev.Id);
			if (device != null) {
				Log.d(TAG, "Device with ID [" + mDev.Id + "] is found." + " Update.");
				mDev = device;
				updateListView();
			}
		} else {
			Log.w(TAG, "Cannot find the device, instance is null.");
		}
	}

}
