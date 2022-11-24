package com.example.link;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.example.link.Adapter.Adapter_lsNoiDungTinNhan;
import com.example.link.Entity.IPAddress;
import com.example.link.Entity.TinNhan;
import com.google.gson.Gson;
import com.vanniktech.emoji.EmojiPopup;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Timer;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class PhongChat extends AppCompatActivity {
    private final  int CHUP_HINH = 123;
    private final  int CHON_HINH = 321;
    private static final int PERMISSION_CODE = 1001;
    IPAddress ip = new IPAddress();
    String IP = ip.getIp();
    private Socket mSocket;
    {
        try {
            //InetAddress ip4 = InetAddress.getLocalHost();
            mSocket = IO.socket("http://"+IP+":3000");

        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }
    ImageView btnGui, btnChonHinh, btnChupHinh, btnEmoji, btnBack, btnMenu;
    ListView lsTinNhan_lV;
    TextView txtChaoMung, txtTenBanBe;
    EditText edtNoiDung;

    List<TinNhan> lsTinNhan_arr = new ArrayList<TinNhan>();
    String mySDT;
    String frind_sdt;
    String nameFriend;
    int soLuongTinNhan;
    String nhomTruong;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phong_chat);
        Intent intent = getIntent();
        frind_sdt = intent.getStringExtra("friend_sdt"); // là ChatNhom nếu chat nhóm
        mySDT = intent.getStringExtra("profile_sdt");
        nameFriend = intent.getStringExtra("name"); // là tên nhóm nếu chat nhóm
        nhomTruong = intent.getStringExtra("nhomTruong");
        mSocket.connect();

        mSocket.emit("ImOnline", mySDT);
        //Join vào nhóm để chát real time
        if(frind_sdt.equals("ChatNhom")){
            mSocket.emit("joinNhom", nameFriend + "");
            Gson gson = new Gson();
            TinNhan tinNhan = new TinNhan(mySDT,nameFriend);
            mSocket.emit("loadMessGroup", gson.toJson(tinNhan));
        }else{
            //gửi yêu cầu lầy toàn bộ danh sách tin nhắn đơn
            Gson gson = new Gson();
            TinNhan tinNhan = new TinNhan(mySDT,frind_sdt);
            mSocket.emit("loadMess", gson.toJson(tinNhan));
        }
        //Nhận thông tin nhóm
        mSocket.on("ThongTinNhom",thongTinNhom);
        //nhận số lượng tin nhắn
        mSocket.on("soLuongTinNhan", soLuonTin);
        //Nhận về danh sách tin nhắn
        mSocket.on("lsMess", LoadLsMess);
        //Lắng nghe khi có người nhắn tin đến
        mSocket.on("getMess",getMess);

        btnBack = findViewById(R.id.btnBackChat);
        btnEmoji = findViewById(R.id.btn_emoji);
        btnChonHinh = findViewById(R.id.btnChonHinh);
        btnChupHinh = findViewById(R.id.btnChupHinh);
        btnMenu = findViewById(R.id.btnMenu_Chat);
        btnGui = findViewById(R.id.btnGuiTinNhan);
        lsTinNhan_lV = findViewById(R.id.lsNoiDungTinNhan);
        txtTenBanBe = findViewById(R.id.txtTenBanBe);
        edtNoiDung = findViewById(R.id.edtNhapTinNhan);

        txtTenBanBe.setText(intent.getStringExtra("name"));

        EmojiPopup popup = EmojiPopup.Builder.fromRootView(
                findViewById(R.id.root_view)
        ).build(edtNoiDung);

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PhongChat.this, GiaoDienChinh.class);
                intent.putExtra("sdt",mySDT);
                startActivity(intent);
            }
        });
        btnEmoji.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popup.toggle();
            }
        });
        btnGui.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!edtNoiDung.getText().toString().equals("")){
                    if(frind_sdt.equals("ChatNhom")){
                        frind_sdt = "NhomChat" + nameFriend;
                    }
                    TinNhan tinNhan = new TinNhan(mySDT,frind_sdt,edtNoiDung.getText().toString(),"text");
                    edtNoiDung.getText().clear();
                    Gson gson = new Gson();
                    mSocket.emit("SendMess", gson.toJson(tinNhan));
                    lsTinNhan_arr.add(tinNhan);
                    Adapter_lsNoiDungTinNhan ls = new Adapter_lsNoiDungTinNhan(lsTinNhan_arr,mySDT);
                    lsTinNhan_lV.setAdapter(ls);
                    lsTinNhan_lV.setSelection(lsTinNhan_arr.size()-1);
                }
            }
        });
        btnChonHinh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                    if(checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED){
                        String[] permission ={Manifest.permission.READ_EXTERNAL_STORAGE};
                        requestPermissions(permission,PERMISSION_CODE);
                    }else{
                        ChonHinh();
                    }
                }else{
                    ChonHinh();
                }
            }
        });
        btnChupHinh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ChupHinh();
            }
        });
        btnMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu pm = new PopupMenu(PhongChat.this, v);
                if(frind_sdt.equals("ChatNhom") || frind_sdt.equals("NhomChat" + nameFriend)){
                    pm.getMenuInflater().inflate(R.menu.menu_nhom_chat, pm.getMenu());
                }else{
                    pm.getMenuInflater().inflate(R.menu.menu_chat_don, pm.getMenu());
                }
                pm.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.toString()){
                            case "QL thành viên" : {
                                if(nhomTruong.equals(mySDT)){
                                    Intent intent = new Intent(PhongChat.this,QuanLyThanhVien.class);
                                    intent.putExtra("sdt",mySDT);
                                    intent.putExtra("tenNhom",nameFriend);
                                    startActivity(intent);
                                }else{
                                    Toast.makeText(PhongChat.this, "Chỉ nhóm trưởng", Toast.LENGTH_SHORT).show();
                                }

                                break;
                            }
                            case  "Đổi nhóm trưởng" : {
                                if(nhomTruong.equals(mySDT)){
                                    Intent intent = new Intent(PhongChat.this,UyQuyenNhomTruong.class);
                                    intent.putExtra("sdt",mySDT);
                                    intent.putExtra("tenNhom",nameFriend);
                                    intent.putExtra("nhomTruong",nhomTruong);
                                    startActivity(intent);
                                }else{
                                    Toast.makeText(PhongChat.this, "Chỉ nhóm trưởng", Toast.LENGTH_SHORT).show();
                                }
                                break;
                            }
                            case "Xem thành viên nhóm": {
                                Intent intent = new Intent(PhongChat.this,DanhSachThanhVien.class);
                                intent.putExtra("sdt",mySDT);
                                intent.putExtra("tenNhom",nameFriend);
                                intent.putExtra("nhomTruong",nhomTruong);
                                startActivity(intent);
                                break;
                            }
                            case "Rời nhóm": {
                                Log.d("", "onMenuItemClick: " + nhomTruong + "//" + mySDT);
                                if(nhomTruong.equals(mySDT)){
                                    Toast.makeText(PhongChat.this, "Nhóm trưởng không thể rời nhóm", Toast.LENGTH_SHORT).show();
                                }else{
                                    mSocket.emit("XoaThanhVien",mySDT,nameFriend);
                                    Intent intent = new Intent(PhongChat.this, GiaoDienChinh.class);
                                    intent.putExtra("sdt",mySDT);
                                    startActivity(intent);
                                    finish();
                                }
                                break;
                            }
                        }
                        return  false;
                    }
                });
                pm.show();
            }
        });
        lsTinNhan_lV.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                PopupMenu popupMenu = new PopupMenu(PhongChat.this,view);
                popupMenu.getMenuInflater().inflate(R.menu.menu_tin_nhan, popupMenu.getMenu());
                popupMenu.show();
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.toString()){
                            case "Gỡ tin nhắn" : {
                                mSocket.emit("GoTinNhan",mySDT,frind_sdt,
                                        position * 2 + 1);
                                break;
                            }
                            case "Chuyển tiếp" : {
                                Intent intent = new Intent(PhongChat.this, ChuyenTiepTinNhan.class);
                                intent.putExtra("sdt",mySDT);
                                intent.putExtra("noiDung",lsTinNhan_arr.get(position).getNoiDung());
                                startActivity(intent);
                            }
                        }
                        return false;
                    }
                });
                return false;
            }
        });
    }
    private final Emitter.Listener soLuonTin = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                   soLuongTinNhan = (int) args[0];
                   lsTinNhan_arr.clear();
                }
            });
        }
    };
    private final Emitter.Listener getMess = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    TinNhan tinNhan = new TinNhan(data.optString("nguoiGui"),
                            data.optString("nguoiNhan"),
                            data.optString("noiDung"),
                            data.optString("loai"));
                    if(tinNhan.getLoai().equals("Image")){
                        byte[] imageUser = (byte[]) args[1];
                        Bitmap bitmap = BitmapFactory.decodeByteArray(imageUser,0,imageUser.length);
                        tinNhan.setHinhAnh(bitmap);
                    }
                    lsTinNhan_arr.add(tinNhan);
                    Adapter_lsNoiDungTinNhan ls = new Adapter_lsNoiDungTinNhan(lsTinNhan_arr, mySDT);
                    lsTinNhan_lV.setAdapter(ls);
                    lsTinNhan_lV.setSelection(lsTinNhan_arr.size() -1);
                }
            });
        }
    };
    private final Emitter.Listener LoadLsMess = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String nguoiGui = (String) args[0];
                    String noiDung = (String) args[1];
                    int index = (int) args[3];

                    String nguoiNhan = mySDT;
                    if(nguoiGui.equals(mySDT)){
                        nguoiNhan = frind_sdt;
                    }
                    TinNhan tinNhan = new TinNhan(nguoiGui,mySDT,noiDung,"");
                    tinNhan.setStt(index);
                    if(tinNhan.getNoiDung().contains("imageUser/") && tinNhan.getNoiDung().contains(".jpg")){
                        byte[] imageByte = (byte[]) args[2];
                        Bitmap bitmap = BitmapFactory.decodeByteArray(imageByte, 0, imageByte.length);
                        tinNhan.setHinhAnh(bitmap);
                        tinNhan.setLoai("Image");
                    }else{
                        tinNhan.setLoai("Text");
                    }
                    lsTinNhan_arr.add(tinNhan);
                    if (lsTinNhan_arr.size() == soLuongTinNhan){
                        sapXepTinNhan();
                        Log.d("ls", "run: "+lsTinNhan_arr);
                        Adapter_lsNoiDungTinNhan ls = new Adapter_lsNoiDungTinNhan(lsTinNhan_arr,mySDT);
                        lsTinNhan_lV.setAdapter(ls);
                        lsTinNhan_lV.setSelection(lsTinNhan_arr.size() -1);
                    }
                }
            });
        }
    };
    private final Emitter.Listener thongTinNhom = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    nhomTruong = data.optString("nhomTruong");
                    Log.d("", "run: " + nhomTruong);
                }
            });
        }
    };

    private void sapXepTinNhan(){
        Collections.sort(lsTinNhan_arr, new Comparator<TinNhan>() {
            @Override
            public int compare(TinNhan o1, TinNhan o2) {
                return o1.getStt() - o2.getStt();
            }
        });
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    ChonHinh();
                } else {
                    Toast.makeText(this, "Denied !", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
    private void ChupHinh(){
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent,CHUP_HINH);
    }
    private void ChonHinh(){
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent,CHON_HINH);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(frind_sdt.equals("ChatNhom")){
            frind_sdt = "NhomChat" + nameFriend;
        }
        if (requestCode == CHON_HINH && resultCode == RESULT_OK) {
            try {
                Uri uriImage = data.getData();
                InputStream is = getContentResolver().openInputStream(uriImage);
                Bitmap bm = BitmapFactory.decodeStream(is);
                byte[] bt = getByteArrFromBitmap(bm);
                TinNhan tinNhan = new TinNhan(mySDT,frind_sdt,"","Image");
                Gson gson = new Gson();
                mSocket.emit("sendImage", bt);
                mSocket.emit("SendMess", gson.toJson(tinNhan));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        } else if (requestCode == CHUP_HINH && resultCode == RESULT_OK) {
            Bitmap bm = (Bitmap) data.getExtras().get("data");
            byte[] bt = getByteArrFromBitmap(bm);
            TinNhan tinNhan = new TinNhan(mySDT,frind_sdt,edtNoiDung.getText().toString(),"Image");
                Gson gson = new Gson();

            mSocket.emit("sendImage", bt);
            mSocket.emit("SendMess", gson.toJson(tinNhan));
            tinNhan.setHinhAnh(bm);
            if(!frind_sdt.equals("NhomChat" + nameFriend)){
                lsTinNhan_arr.add(tinNhan);
                Adapter_lsNoiDungTinNhan ls = new Adapter_lsNoiDungTinNhan(lsTinNhan_arr,mySDT);
                lsTinNhan_lV.setAdapter(ls);
            }
        }
    }
    public byte[] getByteArrFromBitmap(Bitmap bitmap){
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        return byteArray;
    }}