package kaist.game.battlecar.AsyncTasks;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

public class SendMessageClient extends AsyncTask<String, String, String> {
	private static final String TAG = SendMessageClient.class.getSimpleName();
	private static final int SERVER_PORT = 24068;
	private String mServerAddr;

	private Socket socket;
	private BufferedReader networkReader;
	private BufferedWriter networkWriter;
    private OutputStream outputStream;
	private String ip = "xxx.xxx.xxx.xxx";

    private static SendMessageClient mInstance;
    private boolean isRunning = false;
    private static Context mContext;

    public static SendMessageClient getInstance(Context context, String serverAddr) {
        mContext = context;
        if(mInstance == null) {
            mInstance = new SendMessageClient(serverAddr);
        }
        return mInstance;
    }

    public boolean isRunning() {
        return isRunning;
    }

	public SendMessageClient(String serverAddr){
		mServerAddr = serverAddr;
	}
	
	@Override
	protected String doInBackground(String... msg) {
		Log.v(TAG, "doInBackground");
		//Display le message on the sender before sending it
		//publishProgress(msg);
		//Send the message
        /*try {
            ip = mServerAddr.substring("http://".length(), mServerAddr.lastIndexOf(":"));
            socket = new Socket(ip, SERVER_PORT);
            networkWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            networkReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            isRunning = true;

        } catch (IOException e) {
            System.out.println(e);
            e.printStackTrace();
        }*/
        ip = mServerAddr.substring("http://".length(), mServerAddr.lastIndexOf(":"));
		Socket socket = new Socket();

		try {
			socket.setReuseAddress(true);
			socket.bind(null);
			socket.connect(new InetSocketAddress(InetAddress.getByName(ip), SERVER_PORT), 3000);
			Log.v(TAG, "doInBackground: connect succeeded");

            outputStream = socket.getOutputStream();
			outputStream.write(msg[0].getBytes());
            outputStream.flush();
            outputStream.close();
            socket.close();
		    Log.v(TAG, "doInBackground: send message succeeded");
		} catch (IOException e) {
		} finally{

		}
		
		return msg[0];
	}

	public void sendCommand(final String command) {
        if (networkWriter != null) {
            PrintWriter out = new PrintWriter(networkWriter, true);
            out.println(command);
        } else {
            Toast.makeText(mContext, "no connection", Toast.LENGTH_SHORT).show();
        }
        /*new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (outputStream != null) {
                        outputStream.write(command.getBytes());
                        outputStream.flush();

                    } else {
                        //Toast.makeText(mContext, "no connection", Toast.LENGTH_SHORT).show();
                    }
                } catch (IOException e) {
                    System.out.println(e);
                    e.printStackTrace();
                }
            }
        }).start();*/
	}

	@Override
	protected void onProgressUpdate(String... values) {
		super.onProgressUpdate(values);
	}

	@Override
	protected void onPostExecute(String result) {
		super.onPostExecute(result);
	}
}
