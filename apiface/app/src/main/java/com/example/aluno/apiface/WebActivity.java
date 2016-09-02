package com.example.aluno.apiface;

/**
 * Created by aluno on 19/05/16.
 */
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.WebView;

import com.facebook.CallbackManager;
import com.facebook.share.widget.LikeView;


public class WebActivity extends AppCompatActivity {

    LikeView mLikeView;
    private CallbackManager callbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);

        callbackManager = CallbackManager.Factory.create();

        mLikeView = (LikeView) findViewById(R.id.like_view);
        mLikeView.setObjectIdAndType(
                "http://btv.ifsp.edu.br/site/",
                LikeView.ObjectType.PAGE);

        WebView myWebView = (WebView) findViewById(R.id.webview);
        myWebView.loadUrl("http://btv.ifsp.edu.br/site/");
    }

    @Override
    protected void onActivityResult(int requestCode, int responseCode, Intent data) {
        super.onActivityResult(requestCode, responseCode, data);

        callbackManager.onActivityResult(requestCode, responseCode, data);
    }
}