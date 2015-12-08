/*
 * Copyright(c) 2014 by LG Electronics. All Rights Reserved.
 */
package com.lge.qremotesdk.sample;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import com.lge.hardware.IRBlaster.*;
import com.lge.qremotesdk.sample.LearnTestDialogFragment.DismissCallback;
import com.lge.qremotesdk.sample.LearnTestDialogFragment.LearnCompleteCallback;

/**
 * Main Activity - test class for IRBlaster API
 * 
 * @hide
 * @exclude
 */
public class QRemoteSDK_Sample extends Activity {

	private IRBlaster mIR;
	private final static String TAG = "QRemoteSDKSample_Main";
	private Device[] mDevices;
	boolean mIR_ready = false;
	private TextView mTextView;
	private Handler mHandler;
	private Spinner mSpinner;
	private Spinner mSpinnerFunctions;
	private String[] mDeviceNames;
	private String[] mFunctionNames;
	private IRFunction mSelectedFunction;
	private Device mDeviceSelected = null;
	private Button buttonC;
	private Button buttonT;
	private Button buttonD;
	private Button buttonLearnSelectedDev;
	private Button buttonCreateDev;

	// An example to use IR function labels.[S]
	private ImageButton mPowerBtn;
	private ImageButton mVolUpBtn;
	private ImageButton mVolDownBtn;

	private ArrayList<View> mBtnsArray = new ArrayList<View>();
	// An example to use IR function labels.[E]

	private LearnTestDialogFragment learningDlgFragment = null;
	ReceiveMessageServer mReceiveMessageServer;

	Handler mServerHandler = new Handler(Looper.getMainLooper()) {
		@Override
		public void handleMessage(Message inputMessage) {
			switch(inputMessage.what){
				case ReceiveMessageServer.SIMSOCK_DATA :
					String msg = (String) inputMessage.obj;
					int keyCode = -1;
					String functionName = "";
					Log.d("OUT",  msg);
					mTextView.append("socket message: " + msg + "\n");
					switch(Integer.parseInt(msg)) {
						case 1:
							functionName = IRFunctionLabels.IR_FUNCTION_LABEL_POWER_TV;
							sendIR(functionName);
							break;
						case 2:
							functionName = IRFunctionLabels.IR_FUNCTION_LABEL_CHANNEL_UP_TV;
							sendIR(functionName);
							break;
						case 3:
							functionName = IRFunctionLabels.IR_FUNCTION_LABEL_CHANNEL_DOWN_TV;
							sendIR(functionName);
							break;
						case 4:
							functionName = IRFunctionLabels.IR_FUNCTION_LABEL_VOLUME_UP_TV;
							sendIR(functionName);
							break;
						case 5:
							functionName = IRFunctionLabels.IR_FUNCTION_LABEL_VOLUME_DOWN_TV;
							sendIR(functionName);
							break;
						default:
							break;
					}
					break;

				case ReceiveMessageServer.SIMSOCK_CONNECTED :
					//mTextView.append("SIMSOCK_CONNECTED\n");
					break;

				case ReceiveMessageServer.SIMSOCK_DISCONNECTED :
					//mTextView.append("SIMSOCK_DISCONNECTED\n");
					break;
				case ReceiveMessageServer.STOP_IR_SIGNAL :
					mIR.stopIR();
					break;

			}
		}
	};

	private void sendIR(final String functionName) {
		final int code = getFunctionKeyCode(functionName);;
		if (code != -1) {
			final Runnable r = new Runnable() {
				public void run() {
					String result;
					if (mDeviceSelected != null) {
						result = mDeviceSelected.Name
								+ " -->"
								+ functionName
								+ " ->"
								+ ResultCode.getString(mIR.sendIR(new IRAction(
								mDeviceSelected.Id, code, 0))) + "\n";
					} else {
						result = "No devices" + "\n";
					}
					mTextView.append(result);
				}
			};
			mHandler.post(r);
		}
		Message stopIRmsg = Message.obtain(mServerHandler, ReceiveMessageServer.STOP_IR_SIGNAL);
		mServerHandler.sendMessageDelayed(stopIRmsg, 100);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		ReceiveMessageServer mReceiveMessageServer = new ReceiveMessageServer(this, mServerHandler);
		mReceiveMessageServer.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[]) null);

		buttonT = (Button)findViewById(R.id.buttonTest);
		buttonT.setOnTouchListener(mButtonAddOnTouchListener);
		buttonC = (Button)findViewById(R.id.buttonClear);
		buttonC.setOnTouchListener(mButtonAddOnTouchListener);
		buttonD = (Button)findViewById(R.id.buttonDevices);
		buttonD.setOnTouchListener(mButtonAddOnTouchListener);
		buttonLearnSelectedDev = (Button)findViewById(R.id.buttonLearnSelectedDev);
		buttonLearnSelectedDev.setOnClickListener(mButtonLearnSelectedDevListener);
		buttonCreateDev = (Button)findViewById(R.id.buttonCreateDev);
		buttonCreateDev.setOnClickListener(mButtonCreateDevListener);

		// An example to use IR function labels.[S]
		mPowerBtn = (ImageButton)findViewById(R.id.power_btn);
		mVolUpBtn = (ImageButton)findViewById(R.id.vol_btn_plus);
		mVolDownBtn = (ImageButton)findViewById(R.id.vol_btn_minus);

		mPowerBtn.setTag(IRFunctionLabels.IR_FUNCTION_LABEL_POWER_TV);
		mVolUpBtn.setTag(IRFunctionLabels.IR_FUNCTION_LABEL_VOLUME_UP_TV);
		mVolDownBtn.setTag(IRFunctionLabels.IR_FUNCTION_LABEL_VOLUME_DOWN_TV);

		mBtnsArray.add(mPowerBtn);
		mBtnsArray.add(mVolUpBtn);
		mBtnsArray.add(mVolDownBtn);

		for (View v: mBtnsArray) {
			v.setOnTouchListener(mIrButtonTouchListener);
		}

		// Disable all buttons until a device is selected.
		checkBtnsEnabled();
		// An example to use IR function labels.[E]

		mTextView = (TextView)findViewById(R.id.textView1);
		mSpinner = (Spinner)findViewById(R.id.spinner1);
		mSpinner.setOnItemSelectedListener(mOnItemSelectedListener);
		mSpinnerFunctions = (Spinner)findViewById(R.id.spinner2);
		mSpinnerFunctions.setOnItemSelectedListener(mOnItemSelectedListenerFunctions);
		mHandler = new Handler();

		mIR = null;
		if (IRBlaster.isSdkSupported(this)) {
			mIR = IRBlaster.getIRBlaster(this, mIrBlasterReadyCallback);
		}
		if (mIR == null) {
			Toast.makeText(getApplicationContext(), "No IR Blaster in this device", Toast.LENGTH_SHORT).show();
			Log.e(TAG, "No IR Blaster in this device");
			return;
		}

		Button rawLgTvPower = (Button)findViewById(R.id.raw_ir_btn);
		rawLgTvPower.setOnTouchListener(new View.OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
					case MotionEvent.ACTION_DOWN:
						sendLgTvPower();
						break;
					case MotionEvent.ACTION_OUTSIDE:
					case MotionEvent.ACTION_CANCEL:
					case MotionEvent.ACTION_UP:
						mIR.stopIR();
						break;
				}
				return false;
			}
		});
		Button learnedIrBtn = (Button)findViewById(R.id.learned_ir_btn);
		learnedIrBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (learningDlgFragment != null) {
					LearnedIrData learnedIr = learningDlgFragment.getLastLearnedData();
					if (learnedIr != null) {
						int result = mIR.sendIrWithLearnedData(learnedIr.Data, learnedIr.Id, 200,
						        false);
						Log.d("test", "result = " + ResultCode.getString(result));
					} else
						Log.d("test", "no learned data");
				}
			}
		});
	}

	// An example to send raw IR data.[S]
	private void sendLgTvPower() {
		// test sendIRPattern()
		int[] LGTVPower = {9000, 4500, 578, 552, 578, 552, 578, 1684, 578, 552, 578, 552, 578, 552,
		        578, 552, 578, 552, 578, 1684, 578, 1684, 578, 552, 578, 1684, 578, 1684, 578,
		        1684, 578, 1684, 578, 1684, 578, 552, 578, 552, 578, 552, 578, 1684, 578, 552, 578,
		        552, 578, 552, 578, 552, 578, 1684, 578, 1684, 578, 1684, 578, 552, 578, 1684, 578,
		        1684, 578, 1684, 578, 1684, 578, 39342, 9000, 2236, 578, 96184, 9000, 2236, 578,
		        96184};
		mIR.sendIRPattern(38000, LGTVPower);
	}

	// An example to send raw IR data.[E]

	public IRBlasterCallback mIrBlasterReadyCallback = new IRBlasterCallback() {

		@Override
		public void IRBlasterReady() {
			Log.d(TAG, "IRBlaster is really ready");

			final Runnable r = new Runnable() {
				public void run() {
					getDevices();
				}
			};
			mHandler.post(r);
			mIR_ready = true;
		}

		@Override
		public void learnIRCompleted(int status) {
			Log.d(TAG, "Learn IR complete");
			if (learningDlgFragment != null) {
				LearnCompleteCallback callback = learningDlgFragment.getLearnCompleteCallback();
				if (callback != null) {
					callback.onComplete(status);
				}
			}
		}

		@Override
		public void newDeviceId(int id) {
			Log.d(TAG, "Added Device Id: " + id);
			Toast.makeText(QRemoteSDK_Sample.this, "New device id: " + id, Toast.LENGTH_SHORT)
			        .show();
		}

		@Override
		public void failure(int error) {
			Log.e(TAG, "Error: " + ResultCode.getString(error));
		}
	};

	public void getDevices() {
		mDevices = mIR.getDevices();
		if (mDevices == null || mDevices.length == 0) {
			mDeviceSelected = null;
			mSelectedFunction = null;
			mDeviceNames = new String[1];
			mDeviceNames[0] = "";
			mFunctionNames = new String[1];
			mFunctionNames[0] = "";
			return;
		}
		mDeviceNames = new String[mDevices.length];
		int i = 0;
		for (Device d: mDevices) {
			mDeviceNames[i++] = d.Name;
			Log.d(TAG, d.Name);
		}
		if (mDevices.length > 0) {
			mDeviceSelected = mDevices[0];
			List<IRFunction> functions = mDeviceSelected.KeyFunctions;
			if ((functions != null) && !functions.isEmpty()) {
				mSelectedFunction = functions.get(0);
				mFunctionNames = new String[functions.size()];
				int j = 0;
				for (IRFunction fun: functions) {
					mFunctionNames[j++] = fun.Name;
				}
			} else {
				mFunctionNames = new String[0];
			}
		}

		// An example to use IR function labels.[S]
		checkBtnsEnabled();
		// An example to use IR function labels.[E]

		attachSpinner();
		attachSpinnerFun();
	}

	public void attachSpinner() {
		try {
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
			        android.R.layout.simple_spinner_dropdown_item, mDeviceNames);

			mSpinner.setAdapter(adapter);
		} catch (NullPointerException e) {
			Log.e(TAG, "Devices names is null");
			e.printStackTrace();
		}

	}

	public void attachSpinnerFun() {
		try {
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
			        android.R.layout.simple_spinner_dropdown_item, mFunctionNames);
			mSpinnerFunctions.setAdapter(adapter);
		} catch (NullPointerException e) {
			Log.e(TAG, "Devices function list is null");
			e.printStackTrace();
		}
	}

	public void Clear(View v) {
		mTextView.setText("");
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.d(TAG, "on resume");
		if (mIR_ready == true && mIR != null) {
			getDevices();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater menuInflater = getMenuInflater();
		menuInflater.inflate(R.layout.menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		int itemId = item.getItemId();
		if (itemId == R.id.add_device) {
			if (mIR_ready == true && mIR != null) {
				int result = mIR.addDevice();
				Toast.makeText(getApplicationContext(), "Adding device", Toast.LENGTH_SHORT).show();
				mTextView.append("Adding device. Result: " + ResultCode.getString(result) + "\n");
			}
			return true;
		} else if (itemId == R.id.delete_device) {
			if (mIR_ready == true && mIR != null) {
				if (mDevices == null || mDevices.length == 0)
					return false;
				int result = mIR.deleteDevice(mDeviceSelected.Id);
				Toast.makeText(getApplicationContext(), "Delete device", Toast.LENGTH_SHORT).show();
				mTextView.append("Delete device. Result: " + ResultCode.getString(result) + "\n");
				getDevices();

			}
			return true;
		} else {
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mIR != null) {
			mIR.close();
		}
	}

	private OnTouchListener mButtonAddOnTouchListener = new OnTouchListener() {

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			int id = v.getId();
			if (id == R.id.buttonTest) {
				int action = event.getAction();
				if (action == MotionEvent.ACTION_DOWN) {
					final Runnable r = new Runnable() {
						public void run() {
							String result;
							if (mDeviceSelected != null) {
								result = mDeviceSelected.Name
								        + " -->"
								        + mSelectedFunction.Name
								        + " ->"
								        + ResultCode.getString(mIR.sendIR(new IRAction(
								                mDeviceSelected.Id, mSelectedFunction.Id, 0)))
								        + "\n";
							} else {
								result = "No devices" + "\n";
							}
							mTextView.append(result);
						}
					};
					mHandler.post(r);
				} else if (action == MotionEvent.ACTION_OUTSIDE
				        || action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
					if (mDeviceSelected != null) {
						mIR.stopIR();
					}
				}
			} else if (id == R.id.buttonClear) {
				int action = event.getAction();
				if (action == MotionEvent.ACTION_DOWN) {
					mTextView.setText("");
				}
			} else if (id == R.id.buttonDevices) {
				int action = event.getAction();
				if (action == MotionEvent.ACTION_DOWN) {
					if (mIR_ready == true) {
						getDevices();
						if (mDevices == null || mDevices.length == 0)
							return false;
						mTextView.setText("");
						mTextView.append("getDevices:\n");
						for (Device d: mDevices) {
							mTextView.append(d.Name + "\n");
						}
					}
				}
			}
			return false;
		}
	};

	// Show LearnTestDialog to learn IR data and add it to the selected device
	private OnClickListener mButtonLearnSelectedDevListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			showLearningDlg(mDeviceSelected);
		}
	};

	// Show LearnTestDialog to learn IR data and add it to a new device
	private OnClickListener mButtonCreateDevListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			showLearningDlg(null);
		}
	};

	private void showLearningDlg(Device dev) {
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		Fragment prev = getFragmentManager().findFragmentByTag("dialog");
		if (prev != null) {
			ft.remove(prev);
		}
		ft.addToBackStack(null);

		// Create and show the dialog.
		learningDlgFragment = new LearnTestDialogFragment();
		learningDlgFragment.setIrBlaster(mIR);
		learningDlgFragment.setDevice(dev);
		learningDlgFragment.setDismissCallback(new DismissCallback() {

			@Override
			public void onDismiss() {
				getDevices();
			}
		});

		learningDlgFragment.show(ft, "dialog");
	}

	// An example to use IR function labels.[S]
	// This listener is similar to the part of the listener mButtonAddOnTouchListener sending IR.

	// The power button supports an On/Off toggle.
	private boolean mIsPowerOnOff = false;
	// The flag defines if the device is on
	// when the power button is a toggle.
	private boolean mIsDeviceOn = false;

	OnTouchListener mIrButtonTouchListener = new OnTouchListener() {

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					if (v.getTag() != null) {
						final String keyLabel = (String)v.getTag();
						Log.e(TAG, "v.getTag():" + keyLabel);

						final int keyCode = getFunctionKeyCode(keyLabel);

						if (keyCode != -1) {
							final Runnable r = new Runnable() {
								public void run() {
									String result;
									if (mDeviceSelected != null) {
										result = mDeviceSelected.Name
										        + " -->"
										        + keyLabel
										        + " ->"
										        + ResultCode.getString(mIR.sendIR(new IRAction(
										                mDeviceSelected.Id, keyCode, 0))) + "\n";
									} else {
										result = "No devices" + "\n";
									}
									mTextView.append(result);
								}
							};
							mHandler.post(r);
						}
					}
					break;
				case MotionEvent.ACTION_OUTSIDE:
				case MotionEvent.ACTION_CANCEL:
				case MotionEvent.ACTION_UP:
					// Handle a special case for the toggle button.
					if (mIsPowerOnOff) {
						if (!mIsDeviceOn) {
							mPowerBtn.setTag(IRFunctionLabels.IR_FUNCTION_LABEL_POWER_OFF_TV);
							mIsDeviceOn = true;
						} else {
							mPowerBtn.setTag(IRFunctionLabels.IR_FUNCTION_LABEL_POWER_ON_TV);
							mIsDeviceOn = false;
						}
					}

					if (v.getTag() != null) {
						int keyCode = getFunctionKeyCode((String)v.getTag());
						if (keyCode != -1) {
							if (mDeviceSelected != null) {
								mIR.stopIR();
							}
						}
					}
					break;
				default:
					break;
			}
			return false;
		}
	};

	/**
	 * Returns the ID for the function label of the selected device.
	 * 
	 * @param functionLabel
	 *            the name of the function
	 * @return Function ID the ID for the function label of the selected device or -1 if not found
	 *         or device is not selected
	 * @throws RemoteException
	 *             the remote exception
	 */
	private int getFunctionKeyCode(String funcLabel) {

		if (mDeviceSelected == null) {
			Log.e(TAG, "A device is not selected.");
			return -1;
		}

		if ((mFunctionNames != null) && (mFunctionNames.length > 0)) {
			for (IRFunction function: mDeviceSelected.KeyFunctions) {

				if (function.Name.equalsIgnoreCase(funcLabel)) {
					return function.Id;
				}
			}
			Log.e(TAG, "[" + funcLabel + "] search function failed");

			return -1;
		} else {
			Log.e(TAG, "The list of function names doesn't exist.");
			return -1;
		}
	}

	// An example to use IR function labels.[E]

	private OnItemSelectedListener mOnItemSelectedListener = new OnItemSelectedListener() {

		@Override
		public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long id) {
			if (mDevices == null || mDevices.length == 0)
				return;
			mDeviceSelected = mDevices[position];
			if (mDeviceSelected != null) {
				List<IRFunction> functions = mDeviceSelected.KeyFunctions;
				if ((functions != null) && !functions.isEmpty()) {
					mSelectedFunction = functions.get(0);
					mFunctionNames = new String[functions.size()];
					int i = 0;
					for (IRFunction fun: functions) {
						mFunctionNames[i++] = fun.Name;
					}
				} else {
					mFunctionNames = new String[0];
				}
			}

			attachSpinnerFun();

			// An example to use IR function labels.[S]
			checkBtnsEnabled();
			// An example to use IR function labels.[E]
		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0) {
		}
	};

	private OnItemSelectedListener mOnItemSelectedListenerFunctions = new OnItemSelectedListener() {
		@Override
		public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long id) {
			if (mDevices == null || mDevices.length == 0)
				return;
			mSelectedFunction = mDeviceSelected.KeyFunctions.get(position);
		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0) {
		}
	};

	// An example to use IR function labels.[S]
	private void checkBtnsEnabled() {
		if (mBtnsArray != null) {
			mIsPowerOnOff = false;
			mIsDeviceOn = false;
			mPowerBtn.setTag(IRFunctionLabels.IR_FUNCTION_LABEL_POWER_TV);

			for (final View v: mBtnsArray) {
				String label = (String)v.getTag();
				int keyCode = getFunctionKeyCode(label);

				// Handle a special case for the toggle button.
				if ((keyCode == -1)
				        && label.equalsIgnoreCase(IRFunctionLabels.IR_FUNCTION_LABEL_POWER_TV)) {
					// Check if there is a toggle.
					keyCode = getFunctionKeyCode(IRFunctionLabels.IR_FUNCTION_LABEL_POWER_ON_TV);
					if (keyCode != -1) {
						mIsPowerOnOff = true;
						mPowerBtn.setTag(IRFunctionLabels.IR_FUNCTION_LABEL_POWER_ON_TV);
					}
				}

				if (keyCode == -1) {

					runOnUiThread(new Runnable() {

						@Override
						public void run() {
							v.setEnabled(false);
						}
					});

					Log.d(TAG, "check [" + label + "] result disabled");
				} else {
					runOnUiThread(new Runnable() {

						@Override
						public void run() {
							v.setEnabled(true);
						}
					});

					Log.d(TAG, "check [" + label + "] result enabled");
				}
			}
		}
	}
}
