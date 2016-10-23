package cs307.butterfly;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Calendar;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        findViewById(R.id.sign_in_button).setOnClickListener(this);
        findViewById(R.id.sign_out_button).setOnClickListener(this);
        findViewById(R.id.sign_out_button).setVisibility(View.GONE);

        status = (TextView) findViewById(R.id.status);
        status.setText("Welcome!");

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

    private void handleSignInResult(GoogleSignInResult result) {
        Log.d(TAG, "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            GoogleSignInAccount acct = result.getSignInAccount();

            //Gather user's info
            if (acct != null) {
                final Socket[] socket = new Socket[1];
                final OutputStream[] outputStream = new OutputStream[1];
                final DataOutputStream[] dataOutputStream = new DataOutputStream[1];
                final JSONObject object = new JSONObject();

                final String personName = acct.getDisplayName();
                final String personGivenName = acct.getGivenName();
                final String personFamilyName = acct.getFamilyName();
                final String personEmail = acct.getEmail();
                final String personId = acct.getId();
                personPhoto = acct.getPhotoUrl();
                Picasso.with(this).load(personPhoto).into((ImageView) findViewById(R.id.imageView2));
                status.setText(getString(R.string.signed_in_fmt, personName));
                status.setVisibility(View.VISIBLE);

                //date
                String dateString = "";
                Calendar calendar = Calendar.getInstance();
                int month = calendar.get(Calendar.MONTH);
                int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
                int year = calendar.get(Calendar.YEAR);

                dateString = dateString.concat(String.valueOf(year));
                dateString = dateString.concat("-");
                dateString = dateString.concat(String.valueOf(month + 1));
                dateString = dateString.concat("-");
                dateString = dateString.concat(String.valueOf(dayOfMonth));

                final String finalDateString = dateString;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            socket[0] = new Socket(MainActivity.ip, 3300);
                            outputStream[0] = socket[0].getOutputStream();
                            dataOutputStream[0] = new DataOutputStream(outputStream[0]);
                            object.put("function", "addUser");
                            object.put("idUsers", 404);
                            object.put("firstName", personGivenName);
                            object.put("lastName", personFamilyName);
                            object.put("GoogleID", personEmail);
                            object.put("dateCreated", finalDateString);
                            dataOutputStream[0].writeUTF(object.toString());
                        } catch (IOException | JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();


            } else {
                status.setText(R.string.login_error);
            }

            updateUI(true);

        } else

        {
            // Signed out, show unauthenticated UI.
            updateUI(false);
        }

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
                    public void onResult(Status status) {
                        // [START_EXCLUDE]
                        updateUI(false);
                        // [END_EXCLUDE]
                    }
                });
    }
    // [END signOut]

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
    }


    private void updateUI(boolean signedIn) {
        if (signedIn) {

            Intent intent = new Intent(this, CommunityActivity.class);
            startActivity(intent);
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


