package com.example.attendme;

import android.os.AsyncTask;

public class MyAsyncTask extends AsyncTask<Void, Void, Void> {
    private String recipientToken;

    public MyAsyncTask(String recipientToken) {
        this.recipientToken = recipientToken;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        sendNotification(recipientToken);
        return null;
    }

    private void sendNotification(String recipientToken) {
        try {
            // Your notification sending code here
            // ...
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
