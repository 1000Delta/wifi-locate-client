package com.example.wifi_locate_client.ui.home;

import android.net.wifi.WifiManager;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class HomeViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    private MutableLiveData<String> hostText;

    private MutableLiveData<Double> xText;
    private MutableLiveData<Double> yText;

    public HomeViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is home fragment");

        hostText = new MutableLiveData<>();
        hostText.setValue("");

        xText = new MutableLiveData<>();
        yText = new MutableLiveData<>();
    }

    public LiveData<String> getText() {
        return mText;
    }

    public void addText(String text) {
        mText.setValue(mText.getValue() + text);
    }

    public void setText(String text) {
        mText.setValue(text);
    }

    public LiveData<String> getHost() {
        return hostText;
    }

    public void setHost(String text) {
        hostText.setValue(text);
    }

    public LiveData<Double> getX() {
        return xText;
    }

    public void setX(Double text) {
        xText.setValue(text);
    }

    public LiveData<Double> getY() {
        return yText;
    }

    public void setY(Double text) {
        yText.setValue(text);
    }
}