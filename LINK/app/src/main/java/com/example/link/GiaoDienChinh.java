package com.example.link;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;


import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
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

import com.example.link.Adapter.Adapder_lsMess_TrangChinh;
import com.example.link.Entity.IPAddress;
import com.example.link.Entity.User;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class GiaoDienChinh extends AppCompatActivity {

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
    TextView txtMyProfile;
    ImageView btnMenu;
    EditText edtTimBanBe;
    ListView lsTinNhan;
    List<User> lsUser = new ArrayList<>();
    String mySDT;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_giao_dien_chinh);

        Intent intent = getIntent();
        mySDT = intent.getStringExtra("sdt");

        mSocket.connect();
        //lấy danh sách user
        mSocket.emit("GetLsBanBe", mySDT);
        mSocket.on("lsBanBe", kQTimBanBe);
        mSocket.emit("ImOnline", mySDT);
        mSocket.on("VaoNhom", vaoNhom);

        txtMyProfile = findViewById(R.id.txtMyProfile);
        btnMenu = findViewById(R.id.btnMenu);
        lsTinNhan = findViewById(R.id.lsTinNhan);

        txtMyProfile.setText(mySDT);

        btnMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu pm = new PopupMenu(GiaoDienChinh.this, v);
                pm.getMenuInflater().inflate(R.menu.menu, pm.getMenu());
                pm.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        if(item.toString().equals("Tạo nhóm")){
                            Intent intent = new Intent(GiaoDienChinh.this,TaoNhom.class);
                            intent.putExtra("sdt",mySDT);
                            startActivity(intent);
                            finish();
                        }
                        return false;
                    }
                });
                pm.show();
            }
        });
        lsTinNhan.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                User user = lsUser.get(i);
                Intent intent = new Intent(GiaoDienChinh.this,PhongChat.class);
                intent.putExtra("friend_sdt", user.getSdt());
                intent.putExtra("profile_sdt",mySDT);
                intent.putExtra("name", user.getName());
                startActivity(intent);
            }
        });
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu,menu);
        return true;
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
                    Adapder_lsMess_TrangChinh adapder_lsMess_trangChinh = new Adapder_lsMess_TrangChinh(GiaoDienChinh.this, lsUser);
                    lsTinNhan.setAdapter(adapder_lsMess_trangChinh);

                }
            });
        }
    };
    private final Emitter.Listener vaoNhom = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    lsUser.clear();
                    mSocket.emit("GetLsBanBe", mySDT);
                    Toast.makeText(GiaoDienChinh.this, "bạn đã đc thêm vào nhóm " + args[0], Toast.LENGTH_SHORT).show();
                }
            });
        }
    };
}

