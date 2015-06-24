package test.bt;

import android.bluetooth.BluetoothDevice;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
* Created by seth.yang on 2015/6/23.
*/
class BluetoothDeviceListAdapter extends BaseAdapter {
    private BT bt;
    private List<BluetoothDevice> devices = new ArrayList<BluetoothDevice> ();

    public BluetoothDeviceListAdapter (BT bt) {
        this.bt = bt;
    }

    @Override
    public int getCount () {
        return devices.size ();
    }

    public void addItem (BluetoothDevice device) {
        devices.add (device);
        notifyDataSetChanged ();
    }

    @Override
    public BluetoothDevice getItem (int position) {
        return devices.get (position);
    }

    @Override
    public long getItemId (int position) {
        return position;
    }

    @Override
    public View getView (int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from (bt).inflate (R.layout.list_item, null);
            holder = new ViewHolder ();
            holder.name = (TextView) convertView.findViewById (R.id.name);
            holder.mac = (TextView) convertView.findViewById (R.id.mac);
            convertView.setTag (holder);
        } else {
            holder = (ViewHolder) convertView.getTag ();
        }

        BluetoothDevice device = getItem (position);
        holder.name.setText (device.getName ());
        holder.mac.setText (device.getAddress ());
        return convertView;
    }

    public void fireDataChanged () {
        super.notifyDataSetChanged ();
    }

    private static class ViewHolder {
        TextView name, mac;
    }
}
