package com.example.aluno.apiface;


import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.share.ShareApi;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;

import java.util.Arrays;
import java.util.List;

public class PostActivity extends AppCompatActivity {

    private FacebookCallback callbackM;
    private TextView textNome, textTexto;
    private ImageView imageView;
    private Button btVoltar;
    private CallbackManager callbackManager;
    private LoginManager manager;
    private Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        textNome = (TextView) findViewById(R.id.textNome);
        textTexto = (TextView) findViewById(R.id.textTexto);
        imageView = (ImageView) findViewById(R.id.imageView);
        btVoltar = (Button) findViewById(R.id.btVoltar);
        callbackManager = CallbackManager.Factory.create();
        List<String> permissionNeeds = Arrays.asList("publish_actions");

        //Pega dados da MainActivity
        Intent intent = getIntent();
        String nome = intent.getStringExtra("my_name");
        String texto = intent.getStringExtra("my_text");
        bitmap = intent.getParcelableExtra("my_Photo");
        textNome.setText(nome);
        textTexto.setText(texto);
        imageView.setImageBitmap(bitmap);

        //Loga o facebook com requisição para publicar e publica
        manager = LoginManager.getInstance();
        manager.logInWithPublishPermissions(this, permissionNeeds);

        manager.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                sharePhotoToFacebook();
                Toast.makeText(getApplicationContext(), "Foto Compatilhada!", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onCancel() {
                System.out.println("onCancel");
            }

            @Override
            public void onError(FacebookException exception) {
                System.out.println("onError");
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int responseCode, Intent data) {
        super.onActivityResult(requestCode, responseCode, data);
        callbackManager.onActivityResult(requestCode, responseCode, data);
    }

    public void voltar(View v) {
        finish();
    }

    //Compartilha automatico
    private void sharePhotoToFacebook() {

        SharePhoto photo = new SharePhoto.Builder()
                .setBitmap(bitmap)
                .setCaption(textTexto.getText().toString())
                .build();

        SharePhotoContent content = new SharePhotoContent.Builder()
                .addPhoto(photo)
                .build();

        ShareApi.share(content, callbackM);

    }



}
