package com.example.link.Entity;

import android.graphics.Bitmap;

import java.util.Timer;

public class TinNhan {
    private String nguoiGui;
    private String nguoiNhan;
    private String noiDung;
    private Timer thoiGianGui;
    private String loai;
    private Bitmap hinhAnh;
    private int stt;

    public TinNhan() {
    }

    public TinNhan(String nguoiGui, String nguoiNhan, String noiDung, String loai, Bitmap hinhAnh) {
        this.nguoiGui = nguoiGui;
        this.nguoiNhan = nguoiNhan;
        this.noiDung = noiDung;
        this.loai = loai;
        this.hinhAnh = hinhAnh;
    }

    public TinNhan(String nguoiGui, String nguoiNhan) {
        this.nguoiGui = nguoiGui;
        this.nguoiNhan = nguoiNhan;
    }

    public TinNhan(String nguoiGui, String nguoiNhan, String noiDung, String loai) {
        this.nguoiGui = nguoiGui;
        this.nguoiNhan = nguoiNhan;
        this.noiDung = noiDung;
        this.loai = loai;
    }

    public int getStt() {
        return stt;
    }

    public void setStt(int stt) {
        this.stt = stt;
    }

    public Bitmap getHinhAnh() {
        return hinhAnh;
    }

    public void setHinhAnh(Bitmap hinhAnh) {
        this.hinhAnh = hinhAnh;
    }

    public String getNguoiGui() {
        return nguoiGui;
    }

    public void setNguoiGui(String nguoiGui) {
        this.nguoiGui = nguoiGui;
    }

    public String getNguoiNhan() {
        return nguoiNhan;
    }

    public void setNguoiNhan(String nguoiNhan) {
        this.nguoiNhan = nguoiNhan;
    }

    public String getNoiDung() {
        return noiDung;
    }

    public void setNoiDung(String noiDung) {
        this.noiDung = noiDung;
    }

    public Timer getThoiGianGui() {
        return thoiGianGui;
    }

    public void setThoiGianGui(Timer thoiGianGui) {
        this.thoiGianGui = thoiGianGui;
    }

    public String getLoai() {
        return loai;
    }

    public void setLoai(String loai) {
        this.loai = loai;
    }

    @Override
    public String toString() {
        return "TinNhan{" +
                "nguoiGui='" + nguoiGui + '\'' +
                ", nguoiNhan='" + nguoiNhan + '\'' +
                ", noiDung='" + noiDung + '\'' +
                ", thoiGianGui=" + thoiGianGui +
                ", loai='" + loai + '\'' +
                ", hinhAnh=" + hinhAnh +
                '}';
    }
}
