package com.example.wifi_locate_client.ui.home;

import android.net.wifi.WifiManager;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class HomeViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public HomeViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is home fragment");
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
}