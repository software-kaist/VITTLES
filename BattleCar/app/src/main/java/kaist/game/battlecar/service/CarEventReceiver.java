package kaist.game.battlecar.service;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class CarEventReceiver extends AsyncTask<Void, String, Void> {
	private static final int SERVER_PORT = 4445;
	private Context mContext;
    private Handler mHandler;
	private ServerSocket serverSocket;
	private BufferedReader buffRecv;

	public static final int SIMSOCK_CONNECTED = 1;
	public static final int SIMSOCK_DATA = 2;
	public static final int SIMSOCK_DISCONNECTED = 3;

	public CarEventReceiver(Context context, Handler handler){
		mContext = context;
        mHandler = handler;
	}
	
	@Override
	protected Void doInBackground(Void... params) {
		try {
			serverSocket = new ServerSocket(SERVER_PORT);
			while(true){
                if (isCancelled())
                    break;
				Socket clientSocket = serverSocket.accept();

				try {
					buffRecv = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				String aLine = null;

				try {
					aLine = buffRecv.readLine();
					if(aLine != null) makeMessage(SIMSOCK_DATA, aLine);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				InetAddress senderAddr = clientSocket.getInetAddress();
				clientSocket.close();
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
        
		return null;
	}

	@Override
	protected void onCancelled() {
		try {
			serverSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
            serverSocket = null;
        }
        super.onCancelled();
	}

	@Override
	protected void onProgressUpdate(String... values) {
		super.onProgressUpdate(values);
	}

	private void makeMessage(int what, Object obj)
	{
		Message msg = Message.obtain();
		msg.what = what;
		msg.obj  = obj;
		mHandler.sendMessage(msg);
	}
	
}
