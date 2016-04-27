package cn.com.yyt.mybluetooth;

import java.io.IOException;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

public class AccpetThread extends Thread {
	private static final UUID MY_UUID = UUID
			.fromString("00001101-0000-1000-8000-00805F9B34FB");
	private static final String NAME_INSECURE = "VIP.PRINT.GUOJIN";
	private BluetoothServerSocket serverSocket;

	public AccpetThread(BluetoothAdapter mBluetoothAdapter, boolean secure) {
		BluetoothServerSocket temp = null;
		try {
			temp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(
					NAME_INSECURE, MY_UUID);
		} catch (IOException e) {
			// TODO: handle exception
			Log.e("app", "侦听失败:" + e.getMessage());
		}
		serverSocket = temp;
	}

	public void run() {
		BluetoothSocket socket = null;
		while (true) {
			try {
				socket = serverSocket.accept();
			} catch (IOException e) {
				// TODO: handle exception
				Log.e("app", "接入连接失败:" + e.getMessage());
				break;
			}
		}
		if (socket != null) {
			// 开始数据交换
		}
	}
}
