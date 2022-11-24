package com.example.link.Adapter;

import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.link.Entity.TinNhan;
import com.example.link.R;

import java.util.List;

public class Adapter_lsNoiDungTinNhan extends BaseAdapter {
    List<TinNhan> lsTinNhan;
    String nguoiGui;
    LinearLayout linearLayout;
    public Adapter_lsNoiDungTinNhan(List<TinNhan> lsTinNhan, String nguoiGui) {
        this.lsTinNhan = lsTinNhan;
        this.nguoiGui = nguoiGui;
    }

    @Override
    public int getCount() {
        return lsTinNhan.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        View viewTinNhan;
        if (view == null){
            viewTinNhan = View.inflate(viewGroup.getContext(),R.layout.row_tinnhan,null);
        }else{
            viewTinNhan = view;
        }
        ImageView imgHinhAnh = viewTinNhan.findViewById(R.id.imgHinhAnh);
        TextView txtTinNhan = viewTinNhan.findViewById(R.id.txtNoiDung);
        linearLayout = viewTinNhan.findViewById(R.id.linearMess);

        if(lsTinNhan.get(i).getNoiDung().equals("Đoạn tin đã đã bị gỡ")){
            txtTinNhan.setTextColor(Color.WHITE);
        }
        if(lsTinNhan.get(i) != null){
            if (lsTinNhan.get(i).getNguoiGui().equals(nguoiGui)){
                linearLayout.setGravity(Gravity.RIGHT);
                if(lsTinNhan.get(i).getHinhAnh() != null){
                    txtTinNhan.setVisibility(View.GONE);
                    imgHinhAnh.setVisibility(View.VISIBLE);
                    imgHinhAnh.setImageBitmap(lsTinNhan.get(i).getHinhAnh());
                }
            }else{
                linearLayout.setGravity(Gravity.LEFT);
                if(lsTinNhan.get(i).getHinhAnh() != null){
                    txtTinNhan.setVisibility(View.GONE);
                    imgHinhAnh.setVisibility(View.VISIBLE);
                    imgHinhAnh.setImageBitmap(lsTinNhan.get(i).getHinhAnh());
                }
            }
            txtTinNhan.setText(lsTinNhan.get(i).getNoiDung());
        }
        return viewTinNhan;
    }
}
