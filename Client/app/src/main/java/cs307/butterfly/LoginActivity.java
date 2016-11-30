package cs307.butterfly;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.firebase.iid.FirebaseInstanceId;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity implements
        View.OnClickListener, GoogleApiClient.OnConnectionFailedListener {

    Uri personPhoto;
    // UI references.
    private TextView status;
    private static final String TAG = "SignInActivity";
    private GoogleApiClient mGoogleApiClient;
    private static final int RC_SIGN_IN = 9001;
    boolean failed = true;

    private static final int REQUEST = 1;
    private static String[] PERMISSIONS = {
            Manifest.permission.GET_ACCOUNTS,
            // Manifest.permission.READ_CONTACTS,
            Manifest.permission.INTERNET};

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        for (String PERMISSION : PERMISSIONS) {
            int permission = ActivityCompat.checkSelfPermission(this, PERMISSION);
            if (permission != PackageManager.PERMISSION_GRANTED) {
                // We don't have permission so prompt the user
                ActivityCompat.requestPermissions(
                        this,
                        PERMISSIONS,
                        REQUEST
                );
            }
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        findViewById(R.id.sign_in_button).setOnClickListener(this);
        findViewById(R.id.sign_out_button).setOnClickListener(this);
        findViewById(R.id.sign_out_button).setVisibility(View.GONE);

        status = (TextView) findViewById(R.id.status);
        status.setText(R.string.welcome);

        //IP changer
        final TextView ip = (TextView) findViewById(R.id.ip);
        final EditText setIP = (EditText) findViewById(R.id.setIP);
        setIP.setText(MainActivity.ip);
        ip.setText(MainActivity.ip);
        Button ipButton = (Button) findViewById(R.id.ipButton);
        ipButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.ip = setIP.getText().toString();
                ip.setText(MainActivity.ip);
            }
        });

        //Server checkbox
        final CheckBox checkServer = (CheckBox) findViewById(R.id.check_server);
        checkServer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.server = checkServer.isChecked();
            }
        });

        //logToken button
        Button logTokenButton = (Button) findViewById(R.id.logTokenButton);
        logTokenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get token
                String token = FirebaseInstanceId.getInstance().getToken();

                // Log and toast
                String msg = getString(R.string.msg_token_fmt, token);
                Log.d(TAG, msg);
                Toast.makeText(LoginActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
        });

        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        // Build a GoogleApiClient with access to the Google Sign-In API and the
        // options specified by gso.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);

            handleSignInResult(result);
        }
    }

    //TODO Use AsyncTask to implement the socket connection
    //https://developer.android.com/reference/android/os/AsyncTask.html
    //https://developer.android.com/training/basics/network-ops/connecting.html
    /*private class Connect extends AsyncTask <GoogleSignInAccount, Void, Void> {
        @Override
        protected Void doInBackground(GoogleSignInAccount... params) {

            return null;
        }
    }*/

    private void handleSignInResult(GoogleSignInResult result) {
        Log.d(TAG, "handleSignInResult:" + result.isSuccess());
        failed = true;
        //Sign in to Google was successful
        if (result.isSuccess()) {

            GoogleSignInAccount acct = result.getSignInAccount();
            //Google account is not null

            if (acct != null) {
                //Create stuff for the client to connect to the app
                final Socket[] socket = new Socket[1];
                final OutputStream[] outputStream = new OutputStream[1];
                final DataOutputStream[] dataOutputStream = new DataOutputStream[1];
                final JSONObject object = new JSONObject();
                final JSONObject object2 = new JSONObject();

                //Store user's info in variables
                final String personName = acct.getDisplayName();
                final String personGivenName = acct.getGivenName();
                final String personFamilyName = acct.getFamilyName();
                MainActivity.googleID = acct.getEmail();
                personPhoto = acct.getPhotoUrl();
                Picasso.with(this).load(personPhoto).into((ImageView) findViewById(R.id.imageView3));

                try {
                    //Store googleID in file
                    deleteFile("googleID");
                    FileOutputStream fileOutputStream = openFileOutput("googleID", MODE_APPEND);
                    fileOutputStream.write(MainActivity.googleID.getBytes());
                    fileOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                //Set status to "Signed in as ..."
                status.setText(getString(R.string.signed_in_fmt, personName));
                status.setVisibility(View.VISIBLE);

                //Check if app is in online mode
                if (MainActivity.server) {

                    //Create new thread for the socket to connect to the server
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                //Connect to server
                                socket[0] = new Socket(MainActivity.ip, MainActivity.port);
                                outputStream[0] = socket[0].getOutputStream();
                                dataOutputStream[0] = new DataOutputStream(outputStream[0]);

                                //If execution reaches till here, everything is working
                                failed = false;

                                //Send user info from Google to Server
                                object.put("function", "addUser");
                                object.put("firstName", personGivenName);
                                object.put("lastName", personFamilyName);
                                object.put("googleID", MainActivity.googleID);
                                dataOutputStream[0].writeUTF(object.toString());

                                //android.os.SystemClock.sleep(300);

                                //Close everything
                                dataOutputStream[0].close();
                                outputStream[0].close();
                                socket[0].close();

                                //Connect to server again
                                socket[0] = new Socket(MainActivity.ip, MainActivity.port);
                                outputStream[0] = socket[0].getOutputStream();
                                dataOutputStream[0] = new DataOutputStream(outputStream[0]);

                                //Send token to server
                                object2.put("function", "updateInstanceID");
                                object2.put("googleID", MainActivity.googleID);
                                object2.put("instanceID", FirebaseInstanceId.getInstance().getToken());
                                dataOutputStream[0].writeUTF(object2.toString());

                                //Close everything
                                dataOutputStream[0].close();
                                outputStream[0].close();
                                socket[0].close();
                            } catch (IOException | JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                    android.os.SystemClock.sleep(1000);
                }

                updateUI(true);
                return;
            }
        }
        //Google Sign in Failed
        status.setText(R.string.google_sign_in_error);
        status.setVisibility(View.VISIBLE);
        //   findViewById(R.id.imageView2).setVisibility(View.GONE);
        findViewById(R.id.sign_in_button).setVisibility(View.VISIBLE);
        findViewById(R.id.sign_out_button).setVisibility(View.GONE);
    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    // [START signOut]
    private void signOut() {
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        // [START_EXCLUDE]
                        updateUI(false);
                        // [END_EXCLUDE]
                    }
                });
    }
    // [END signOut]

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
    }


    private void updateUI(boolean signedIn) {
        if (signedIn) {
            //Check if app is in online mode and if everything was successful
            if ((MainActivity.server && !failed) || (failed && !MainActivity.server)) {
                Intent intent = new Intent(this, CommunityActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            } else {
                //Either server was offline or the connection to the server failed
                status.setText(R.string.server_not_found);
            }
            findViewById(R.id.imageView2).setVisibility(View.VISIBLE);
            findViewById(R.id.sign_in_button).setVisibility(View.GONE);
            findViewById(R.id.sign_out_button).setVisibility(View.VISIBLE);
        } else {
            status.setText(R.string.signed_out);
            status.setVisibility(View.VISIBLE);
            findViewById(R.id.imageView2).setVisibility(View.GONE);
            findViewById(R.id.sign_in_button).setVisibility(View.VISIBLE);
            findViewById(R.id.sign_out_button).setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sign_in_button:
                signIn();
                break;
            case R.id.sign_out_button:
                signOut();
                break;

        }
    }

}


