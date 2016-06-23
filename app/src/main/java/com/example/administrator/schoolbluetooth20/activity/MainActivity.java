package com.example.administrator.schoolbluetooth20.activity;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.TextViewCompat;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.administrator.schoolbluetooth20.R;
import com.example.administrator.schoolbluetooth20.adapter.BLEListViewAdapter;
import com.example.administrator.schoolbluetooth20.bean.BLEObj;
import com.example.administrator.schoolbluetooth20.service.RssiService;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    public long exitTime;
    public static final int SERVICE_RSSI = 100;

    private FloatingActionButton btn_scan;

    private BluetoothManager btManager;
    private BluetoothAdapter mBluetoothAdapter;
    private boolean mScanning = false;//扫描标志位
    private boolean mWarning = false;//b报警标志位
    private int SCAN_PERIOD = 10000;//扫描周期10秒

    private TextView tv_count;
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private BLEListViewAdapter mAdapter;
    private Map<String, BLEObj> mDeviceMap = new HashMap<>();

    //Service相关
    private RssiService mService = null;
    private RssiService.MyBinder mBinder;
    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            mBinder = (RssiService.MyBinder) binder;
            mService = mBinder.getService();
            mBinder.setHandler(mHandler);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
        }
    };

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case SERVICE_RSSI:{
//                    Toast.makeText(MainActivity.this, "SERVICE_RSSI", Toast.LENGTH_SHORT).show();
                    //得到连接设备列表
                    Map<String, BLEObj> mConnectedMap = mBinder.getConnectedMap();
                    tv_count.setText("连接设备：" + mConnectedMap.size() + "/5");

                    String strName = "";
                    mWarning = false;//重置报警标记位
                    for (String key : mDeviceMap.keySet()) {
                        mDeviceMap.get(key).setIsConnected(false);
                    }

                    for (String key : mConnectedMap.keySet()) {
                        //后台Service管理的数据
                        BLEObj objService = mConnectedMap.get(key);
                        //当前UI所显示的数据
                        BLEObj objActivity = mDeviceMap.get(key);
                        //设置信号强度
                        int rssi = Math.abs(objService.getRssi());
                        objActivity.setRssi(rssi);
                        if (rssi > 100 && rssi != 127){
                            mWarning = true;
                            strName = strName + " " + objService.getDevice().getName();
                        }
                        //设置时间戳
                        objActivity.setTimestamp(objService.getTimestamp());
                        //设置连接状态
                        objActivity.setIsConnected(true);

                    }
                    //根据mWarning标志位判断是否发送广播
                    if (mWarning == true) {
                        Intent intent = new Intent("com.example.administrator.schoolbluetooth20.broadcast.RssiReceiver");
                        intent.putExtra("msg", strName);
                        sendBroadcast(intent);
                    }
                    mWarning = false;
                    //更新UI
                    if (mAdapter != null){
                        mAdapter.setDatas(mDeviceMap);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mAdapter.notifyDataSetChanged();
                            }
                        });
                    }
                }break;
                default:break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //工具栏、侧滑菜单初始化
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitleTextColor(Color.parseColor("#ffffff"));
        setSupportActionBar(toolbar);

        exitTime = System.currentTimeMillis();

        //初始化Service
        Intent serviceIntent = new Intent(MainActivity.this, RssiService.class);
        bindService(serviceIntent, conn, Service.BIND_AUTO_CREATE);

        //初始化蓝牙设备
        initBluetooth();
        //初始化页面
        initView();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(conn);
    }

    private void initBluetooth() {
        btManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        mBluetoothAdapter = btManager.getAdapter();
    }

    private void initView() {

        btn_scan = (FloatingActionButton) findViewById(R.id.btn_scan);
        btn_scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mService.disconnectBLE();
                mDeviceMap.clear();
                mAdapter.setDatas(mDeviceMap);
                scanLeDevice(true);
            }
        });

        //textview连接设备数量
        tv_count = (TextView) findViewById(R.id.tv_count);

        //初始化RecyclerView相关
        mRecyclerView = (RecyclerView) findViewById(R.id.listview);
        mLayoutManager = new LinearLayoutManager(this);
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mAdapter = new BLEListViewAdapter();
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);

        mAdapter.setOnItemClickListener(new BLEListViewAdapter.OnItemClickListener() {
            @Override
            public void onClick(int position, BLEObj obj) {
                if (obj.isConnected() == false) {
                    boolean test = mService.connectBLE(obj);
                    if (test == false) {
                        Toast.makeText(MainActivity.this, "连接失败！请检查设备状态或是否超过连接最大数！", Toast.LENGTH_SHORT).show();
                    }
                }else {
                    mService.disconnectBLE(obj);
                }
            }
        });
    }

    /**
     * 扫描附近蓝牙设备
     */
    private void scanLeDevice(boolean enable){
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    Toast.makeText(MainActivity.this, "搜索结束！", Toast.LENGTH_SHORT).show();
                }
            }, SCAN_PERIOD);
            if (!mBluetoothAdapter.isEnabled()) {
                mBluetoothAdapter.enable();
            }
            mScanning = true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
    }

    /**
     * 扫描设备的回调
     */
    BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice bluetoothDevice, int rssi, byte[] scanRecord) {
            BLEObj obj = new BLEObj();
            obj.setAddress(bluetoothDevice.getAddress());
            obj.setDevice(bluetoothDevice);
            obj.setRssi(rssi);
            obj.setTimestamp(System.currentTimeMillis());
            if (!mDeviceMap.containsKey(bluetoothDevice.getAddress())) {
                mDeviceMap.put(obj.getAddress(), obj);
                mAdapter.setDatas(mDeviceMap);
            }else {
                mDeviceMap.get(bluetoothDevice.getAddress()).setRssi(rssi);//刷新信号强度
                mDeviceMap.get(bluetoothDevice.getAddress()).setTimestamp(System.currentTimeMillis());//刷新扫描时间
                mAdapter.setDatas(mDeviceMap);
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mAdapter.notifyDataSetChanged();
                }
            });


        }
    };





    @Override
    public void onBackPressed() {
        if ((System.currentTimeMillis() - exitTime) > 2000) {
            Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
            exitTime = System.currentTimeMillis();
        } else {
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){

            //设置
            case R.id.action_settings:{

            }break;

            //关于
            case R.id.action_about:{

            }break;
        }

        return super.onOptionsItemSelected(item);
    }



}





