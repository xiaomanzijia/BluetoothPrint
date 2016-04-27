package cn.com.yyt.mybluetooth;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint("NewApi")
public class MainActivity extends Activity {

	GridView grid = null;
	Button btnFindDevice = null;
	Button btnPrint = null;
	EditText txt = null;
	private final static int REQUEST_ENABLE_BT = 1;
	BluetoothAdapter mBluetoothAdapter = null;
	ArrayList<HashMap<String, Object>> lstImageItem = new ArrayList<HashMap<String, Object>>();
	HashMap<String, Object> map = null;
	ProgressDialog progressDialog = null;
	private static final UUID MY_UUID = UUID
			.fromString("00001101-0000-1000-8000-00805F9B34FB");
	BluetoothSocket socket = null;
	OutputStream outStream = null;

	private String dir = android.os.Environment.getExternalStorageDirectory()
			.getAbsolutePath() + "/";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		grid = (GridView) findViewById(R.id.gridDvice);
		btnFindDevice = (Button) findViewById(R.id.btnFindDevice);
		btnPrint = (Button) findViewById(R.id.btnPrint);
		this.progressDialog = new ProgressDialog(this);
		this.progressDialog.setTitle("请稍候...");
		this.progressDialog.setMessage("正在搜索周边蓝牙设备,请稍候...");
		this.progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

		// 单击蓝牙设备，建立连接
		grid.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				// setSecretKey();
				TextView txt = (TextView) arg0.findViewById(R.id.ItemAddress);
				String remoteAddress = txt.getText().toString();
				showMessage("正在尝试连接蓝牙设备,请稍候...");
				BluetoothDevice device = mBluetoothAdapter
						.getRemoteDevice(remoteAddress);
				try {
					socket = device.createRfcommSocketToServiceRecord(MY_UUID);
					mBluetoothAdapter.cancelDiscovery();
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					socket.connect();
					showMessage("已成功连接蓝牙设备...");
				} catch (IOException e) {
					// TODO: handle exception
					showMessage("连接蓝牙设备失败:" + e.getMessage());
					try {
						socket.close();
					} catch (IOException e1) {
						// TODO: handle exception
					}
				}
			}

		});

		// 查找蓝牙设备按钮事件
		btnFindDevice.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				openDevices();
				if (mBluetoothAdapter.isEnabled()) {
					progressDialog.show();
					new Thread(new Runnable() {
						@Override
						public void run() {
							// TODO Auto-generated method stub
							lstImageItem.clear();
							findDevices();
						}
					}).start();
				}
			}
		});

		// 发送并打印
		btnPrint.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				// new AccpetThread(mBluetoothAdapter, true).start();

				PackageInfo packageInfo;
				try {
					packageInfo = getPackageManager().getPackageInfo(
							"com.dynamixsoftware.printershare", 0);
				} catch (NameNotFoundException e) {
					packageInfo = null;
					e.printStackTrace();
				}

				if (packageInfo == null) {
					Log.e("Waring...", "not installed");

				} else {
					Log.e("Waring...", "not installed");
				}

				if (packageInfo != null) {

					Intent intent = new Intent();
					ComponentName comp = new ComponentName(
							"com.dynamixsoftware.printershare",
							"com.dynamixsoftware.printershare.ActivityPrintPDF");
					intent = new Intent();
					intent.setComponent(comp);
					intent.setAction("android.intent.action.VIEW");
					intent.setType("application/pdf");
					File file = new File(dir + "safety/lawbook.pdf");
					intent.setData(Uri.fromFile(file));
					startActivity(intent);
				} else {

					new AlertDialog.Builder(MainActivity.this)
							.setTitle("提示")
							.setMessage("检测到尚未安装打印插件,是否立即安装?")
							.setNegativeButton("Yes",
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialog,
												int which) {
											String filePath = dir
													+ "safety/print.apk"; // 文件需有可读权限
											Intent intent = new Intent();
											intent.setAction(android.content.Intent.ACTION_VIEW);
											intent.setDataAndType(
													Uri.parse("file://"
															+ filePath),
													"application/vnd.android.package-archive");
											intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
											startActivity(intent);
										}
									})
							.setPositiveButton("No",
									new DialogInterface.OnClickListener() {

										@Override
										public void onClick(
												DialogInterface arg0, int arg1) {
											// TODO Auto-generated method
											// stub

										}
									}).show();
				}
			}

		});
	}

	private void openDevices() {
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (mBluetoothAdapter == null) {
			Toast.makeText(this, "您好,该设备不支持蓝牙技术", Toast.LENGTH_SHORT).show();
		} else {
			if (!mBluetoothAdapter.isEnabled()) {
				// 蓝牙尚未开启，则开启蓝牙
				Intent enableIntent = new Intent(
						BluetoothAdapter.ACTION_REQUEST_ENABLE);
				startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
				Toast.makeText(this, "正在开启蓝牙...", Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(this, "蓝牙已打开...", Toast.LENGTH_SHORT).show();
			}
		}
	}

	public void OnActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_ENABLE_BT) {
			if (resultCode == RESULT_OK) {
				// 蓝牙已开启
				Toast.makeText(this, "蓝牙已打开...", Toast.LENGTH_SHORT).show();
			}
		}
	}

	private void findDevices() {
		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		this.registerReceiver(mReceiver, filter);
		filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		this.registerReceiver(mReceiver, filter);
		mBluetoothAdapter.startDiscovery();
	}

	private void bindDevices() {
		// 生成适配器的ImageItem <====> 动态数组的元素，两者一一对应
		SimpleAdapter saImageItems = new SimpleAdapter(this, // 没什么解释
				lstImageItem,// 数据来源
				R.layout.griditem,// night_item的XML实现

				// 动态数组与ImageItem对应的子项
				new String[] { "ItemImage", "ItemText", "ItemAddress" },

				// ImageItem的XML文件里面的一个ImageView,两个TextView ID
				new int[] { R.id.ItemImage, R.id.ItemText, R.id.ItemAddress });
		// 添加并且显示
		grid.setAdapter(saImageItems);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	private BroadcastReceiver mReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			String action = intent.getAction();
			if (BluetoothDevice.ACTION_FOUND.equals(action)) {
				BluetoothDevice device = intent
						.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				map = new HashMap<String, Object>();
				map.put("ItemImage", R.drawable.ic_launcher);// 添加图像资源的ID
				map.put("ItemText", device.getName());// 按序号做ItemText
				map.put("ItemAddress", device.getAddress());// 按序号做ItemText
				lstImageItem.add(map);
			} else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED
					.equals(action)) {
				if (lstImageItem.size() == 0) {
					showMessage("暂时没有搜索到周边蓝牙设备...");
				} else {
					bindDevices();
				}
				progressDialog.dismiss();
			}

		}
	};

	private void showMessage(String msg) {
		Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
	}

	/**
	 * 卷筒打印机初始化
	 */
	@SuppressWarnings("unused")
	public void printInit() {

		// 设置黑标模式
		try {
			outStream.write(0x1B);
			outStream.write(0x4E);
			outStream.write(0x7);
			outStream.write(1); // 0 非黑标 1 黑标
			// 结束
			// 设置打印宽度指令
			outStream.write(0x1B);
			outStream.write(0x4E);
			outStream.write(0x0A);
			outStream.write(1); // 0 57 1 86
			// 结束
			outStream.write(0x1D); // 切撕纸偏移量
			outStream.write(0x28);
			outStream.write(0x46);
			outStream.write(0x4);
			outStream.write(0x0);
			outStream.write(0x2);
			outStream.write(0x0);
			// mmOutStream.write(Constant.PAPER_CUT_OFFSET);
			outStream.write(0x0);
			outStream.write(0x1D); // 打印位置偏移量
			outStream.write(0x28);
			outStream.write(0x46);
			outStream.write(0x4);
			outStream.write(0x0);
			outStream.write(0x1);
			outStream.write(0x0);
			// mmOutStream.write(Constant.PRINT_POSITION_OFFSET);
			outStream.write(0x0);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 打印机实时状态指令
	 */
	@SuppressWarnings("unused")
	public void printState() {
		try {
			outStream.write(0x10);
			outStream.write(0x04);
			outStream.write(4);
			outStream.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
