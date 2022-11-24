package com.example.link;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.link.Adapter.Adapder_lsMess_TrangChinh;
import com.example.link.Adapter.Adapter_lsNoiDungTinNhan;
import com.example.link.Entity.IPAddress;
import com.example.link.Entity.NhomChat;
import com.example.link.Entity.TinNhan;
import com.example.link.Entity.User;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class ChuyenTiepTinNhan extends AppCompatActivity {
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
    TextView txtToi;
    ListView lvNguoiDung;
    Button btnXacNhan;
    ImageView btnBack;
    String mySDT;
    List<User> lsUser = new ArrayList<>();
    List<TinNhan> lsTinNhan = new ArrayList<>();
    String noiDung;
    String toi = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chuyen_tiep_tin_nhan);

        Intent intent = getIntent();
        mySDT = intent.getStringExtra("sdt");
        noiDung = intent.getStringExtra("noiDung");

        txtToi = findViewById(R.id.txtToi);
        lvNguoiDung = findViewById(R.id.lvDanhSachBanBe_CT);
        btnXacNhan = findViewById(R.id.btnXacNhan_CT);
        btnBack = findViewById(R.id.btnBack_CT);

        mSocket.connect();

        mSocket.emit("GetLsBanBe", mySDT);
        mSocket.on("lsBanBe", kQTimBanBe);

        lvNguoiDung.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String nguoiNhan = lsUser.get(position).getSdt();
                if(nguoiNhan.equals("ChatNhom")){
                    nguoiNhan = "NhomChat" + lsUser.get(position).getName();
                }
                toi += lsUser.get(position).getName();
                txtToi.setText("Tới : " + toi);
                TinNhan tinNhan = new TinNhan(mySDT,nguoiNhan,noiDung,"text");
                lsTinNhan.add(tinNhan);
                Log.d("", "onItemClick: " + lsTinNhan);
            }
        });
        btnXacNhan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (TinNhan i:lsTinNhan) {
                    Log.d("", "onClick: " + i);
                    Gson gson = new Gson();
                    mSocket.emit("SendMess", gson.toJson(i));
                }
                finish();
            }
        });
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
    private final Emitter.Listener kQTimBanBe = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONArray data = (JSONArray) args[0];
                    int n = data.length();
                    for (int i = 0; i < n; i++){
                        User user = null;
                        try {
                            user = new User(data.getJSONObject(i).optString("sdt"),
                                    data.getJSONObject(i).optString("matKhau"),
                                    data.getJSONObject(i).optString("name"));
                            if(!user.getSdt().equals(mySDT)){
                                lsUser.add(user);
                            }else{
                                JSONArray arr = (JSONArray) args[1];
                                if(arr.length() != 0){
                                    for (int j = 0;j< arr.length();j++){
                                        User user2 = new User("ChatNhom","",arr.getString(j));
                                        lsUser.add(user2);
                                    }
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    Adapder_lsMess_TrangChinh adapder_lsMess_trangChinh = new Adapder_lsMess_TrangChinh(ChuyenTiepTinNhan.this, lsUser);
                    lvNguoiDung.setAdapter(adapder_lsMess_trangChinh);

                }
            });
        }
    };
}