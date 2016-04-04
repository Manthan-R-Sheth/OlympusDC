package org.self.example;

import android.app.IntentService;
import android.content.Intent;

/**
 * Created by manthan on 4/4/16.
 */
public class PeerConnect extends IntentService {


    @Override
    protected void onHandleIntent(Intent intent) {
        connectToPeer();
    }
    public PeerConnect(){
        super("PeerConnect");
    }
    private void connectToPeer(){
    }
}
