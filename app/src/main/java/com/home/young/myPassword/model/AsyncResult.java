package com.home.young.myPassword.model;

import android.os.Bundle;

public class AsyncResult<Data> {
    private int result;
    private Data data;
    private Bundle bundle = new Bundle();

    public int getResult() {
        return result;
    }

    public void setResult(int result) {
        this.result = result;
    }

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public Bundle getBundle() {
        return bundle;
    }
}
