package com.example.aluno.apiface;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private CallbackManager callbackManager;
    private List<String> facebookPermitions;
    private LoginManager loginManager;
    private TextView textID, textName;
    private ImageView imFoto;
    private String localFoto, myURL;
    private Boolean mudarFoto;

    private EditText textPost;
    private Button btPostFoto;
    private AlertDialog alerta;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btPostFoto = (Button) findViewById(R.id.btPostFoto);
        textPost = (EditText) findViewById(R.id.textPost);
        mudarFoto = false;
        FacebookSdk.sdkInitialize(getApplicationContext()); //inicializa o FacebookSDK
        callbackManager = CallbackManager.Factory.create(); //responsável por gerenciar as ações em
        // suas aplicações após o retorno das chamadas ao FacebookSDK.
        facebookPermitions = Arrays.asList("email", "public_profile", "user_friends", "user_posts");

        imFoto = (ImageView) findViewById(R.id.imFoto);

        //o loginManager ajuda a eliminate o botão de Login na Interface
        loginManager = LoginManager.getInstance();
        loginManager.logInWithReadPermissions(this, facebookPermitions);

        loginManager.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                GraphRequest request = GraphRequest.newMeRequest(loginResult.getAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                        try {
                            new GraphRequest(
                                    AccessToken.getCurrentAccessToken(),
                                    "me/feed",
                                    null,
                                    HttpMethod.GET,
                                    new GraphRequest.Callback() {
                                        @Override
                                        public void onCompleted(GraphResponse response) {
                                            Log.v("feed", response.toString());
                                        }
                                    }
                            );

                            String id = object.getString("id");
                            textID = (TextView) findViewById(R.id.textId);
                            textID.setText(id.toString());

                            myURL = "https://www.facebook.com/profile.php?id="+id;

                            String name = object.getString("name");
                            textName = (TextView) findViewById(R.id.textName);
                            textName.setText(name.toString());

                            if (!mudarFoto) {
                                GetImage getImage = new GetImage();
                                getImage.execute(id);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
                Bundle parameters = new Bundle();
                parameters.putString("fields", "id,name,email,posts");
                request.setParameters(parameters);
                request.executeAsync();
            }

            @Override
            public void onCancel() {
                Toast.makeText(getApplicationContext(), "CANCEL!", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onError(FacebookException error) {
                Toast.makeText(getApplicationContext(), "ERROR!", Toast.LENGTH_LONG).show();
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int responseCode, Intent data) {
        super.onActivityResult(requestCode, responseCode, data);
        callbackManager.onActivityResult(requestCode, responseCode, data);

        if (requestCode == 2) {  //retorno FotoActivity
            Bitmap bitmap = data.getParcelableExtra("MyData");
            localFoto = data.getStringExtra("MyWay");
            mudarFoto = data.getBooleanExtra("MyBoolean", mudarFoto);
            if (mudarFoto)
                imFoto.setImageBitmap(bitmap);
        }
    }

    public class GetImage extends AsyncTask<String, Void, Bitmap> {

        @Override
        protected void onPostExecute(Bitmap bitmap) {   // insere a imagem na variável após exec
            if (bitmap != null && !mudarFoto) {
                imFoto.setImageBitmap(bitmap);
            }
        }

        @Override
        protected Bitmap doInBackground(String... params) {// Busca a imagem
            String userId = params[0];
            return getFacebookProfilePicture(userId);
        }
    }

    public static Bitmap getFacebookProfilePicture(String userID) {
        Bitmap bitmap = null;
        try {
            URL imageURL = new URL("https://graph.facebook.com/" + userID + "/picture?type=large");
            bitmap = BitmapFactory.decodeStream(imageURL.openConnection().getInputStream());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return bitmap;
    }

    public void fotografar(View v){
        final Intent nextActivity = new Intent(this, CameraActivity.class);
        startActivityForResult(nextActivity, 2);
    }

    //Solução para o problema de recarregar a Activity
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putString("MyString", localFoto);
        savedInstanceState.putBoolean("MyBoolean", mudarFoto);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mudarFoto=savedInstanceState.getBoolean("MyBoolean");
        if (mudarFoto) {
            localFoto = savedInstanceState.getString("MyString");
            Bitmap bitmap = BitmapFactory.decodeFile(localFoto);
            imFoto.setImageBitmap(bitmap);
        }
    }

    public void postFoto (View v){  //Passa dados para a PostActivity

        //Cria o gerador do AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirmar");       //define o titulo
        builder.setMessage("Compartilhar Foto");   //define a mensagem

        //Gera a próxima tela criando os parâmetros para serem enviados
        final Intent nextActivity = new Intent(this, PostActivity.class);
        String texto = textPost.getText().toString(); //Parâmetros a serem passados para a próxima tela
        String nome = textName.getText().toString();
        Bitmap bitmap = ((BitmapDrawable) imFoto.getDrawable()).getBitmap();
        nextActivity.putExtra("my_name", nome);
        nextActivity.putExtra("my_text", texto);
        nextActivity.putExtra("my_Photo", bitmap);

        builder.setPositiveButton("Sim", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                startActivity(nextActivity);
            }
        });
        //define um botão como negativo.
        builder.setNegativeButton("Não", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
            }
        });

        alerta = builder.create(); //cria o AlertDialog
        alerta.show();  //Exibe
    }


    public void news (View v){

        final Intent nextActivity = new Intent(this, WebActivity.class);

        startActivity(nextActivity);

    }


}