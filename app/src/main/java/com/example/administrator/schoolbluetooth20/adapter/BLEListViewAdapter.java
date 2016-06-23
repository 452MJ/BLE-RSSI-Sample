package com.example.administrator.schoolbluetooth20.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.administrator.schoolbluetooth20.R;
import com.example.administrator.schoolbluetooth20.bean.BLEObj;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2016/3/10.
 */
public class BLEListViewAdapter extends RecyclerView.Adapter<BLEListViewAdapter.ViewHolder>{

    //数据集
    private List<BLEObj> mList = new ArrayList<>();

    OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener{
        void onClick(int position, BLEObj obj);
    }
    public class ViewHolder extends RecyclerView.ViewHolder{

        TextView tv_device_name;
        TextView tv_device_address;
        TextView tv_device_rssi;
        TextView tv_device_distance;
        TextView tv_device_timestamp;
        TextView btn_connect;
        View view;


        public ViewHolder(View itemView) {
            super(itemView);
            view = itemView;
            tv_device_name = (TextView) itemView.findViewById(R.id.tv_device_name);
            tv_device_address = (TextView) itemView.findViewById(R.id.tv_device_address);
            tv_device_rssi = (TextView) itemView.findViewById(R.id.tv_device_rssi);
            tv_device_distance = (TextView) itemView.findViewById(R.id.tv_device_distance);
            tv_device_timestamp = (TextView) itemView.findViewById(R.id.tv_device_timestamp);
            btn_connect = (TextView) itemView.findViewById(R.id.btn_connect);

            btn_connect.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemClickListener.onClick(getPosition(), mList.get(getPosition()));
                }
            });
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.listitem, null);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.tv_device_name.setText("" + mList.get(position).getDevice().getName());
        holder.tv_device_address.setText("设备地址：" + mList.get(position).getAddress());

        int rssi = Math.abs(mList.get(position).getRssi());
        if (rssi >= 0 && rssi < 70){
            holder.tv_device_rssi.setText("信号强度RSSI(dBm)：-" + rssi);
            holder.tv_device_rssi.setTextColor(holder.view.getContext().getResources().getColor(R.color.distance_high));
        }else if (rssi >= 70 && rssi < 120){
            holder.tv_device_rssi.setText("信号强度RSSI(dBm)：-" + rssi);
            holder.tv_device_rssi.setTextColor(holder.view.getContext().getResources().getColor(R.color.distance_mid));
        }else if (rssi >= 120){
            holder.tv_device_rssi.setText("信号强度RSSI(dBm)：-" + rssi);
            holder.tv_device_rssi.setTextColor(holder.view.getContext().getResources().getColor(R.color.distance_low));
        }else {
            holder.tv_device_rssi.setText("信号强度RSSI(dBm)：-" + rssi);
            holder.tv_device_rssi.setTextColor(holder.view.getContext().getResources().getColor(R.color.distance_low));
        }

        holder.tv_device_distance.setText("计算距离Distance(m)：" + calculateDistance(rssi) + "米");
        holder.tv_device_timestamp.setText("扫描时间：" + mList.get(position).getTimestamp());

        if (mList.get(position).isConnected() == true) {
            holder.btn_connect.setText("断开");
            holder.btn_connect.setTextColor(holder.view.getContext().getResources().getColor(R.color.device_connected));
        }else {
            holder.btn_connect.setText("连接");
            holder.btn_connect.setTextColor(holder.view.getContext().getResources().getColor(R.color.device_disconnected));
        }
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    /**
     * 计算距离值
     * @param rssi
     * @return
     */
    private float calculateDistance(int rssi){
        //手持： 79.119 12.02
        //平放： 72.496 12.012
        float power = (float) ((rssi - 79.119)/(12.02));
        return (float) Math.pow(Math.E, power);
    }

    /**
     * 设置数据
     * @param map
     */
    public void setDatas(Map<String, BLEObj> map){
        mList.clear();
        for (String key : map.keySet()) {
            mList.add(map.get(key));
        }
//        this.notifyDataSetChanged();
    }

}
