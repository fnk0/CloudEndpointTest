package gabilheri.cloudendpointtest;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;

import java.io.InputStream;

public class MyActivity extends Activity implements View.OnClickListener, GoogleApiClient.ConnectionCallbacks , GoogleApiClient.OnConnectionFailedListener {

    private static final int RC_SIGN_IN = 0;

    private static final String LOG_TAG = "Cloud Sender";
    private static final int PROFILE_PIC_SIZE = 400;

    private static GoogleApiClient mGoogleServices;

    private boolean mIntentInProgress;
    private boolean mSignInClicked;
    private static ConnectionResult mConnectionResult;
    private static SignInButton mSignIn;
    private static Button btnSignOut, btnRevokeAccess;
    private static ImageView imageProfilePic;
    private static TextView txtName, txtEmail;
    private static LinearLayout mProfileLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
        mGoogleServices = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this).addApi(Plus.API)
                .addScope(Plus.SCOPE_PLUS_LOGIN).build();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.my, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_my, container, false);

            mSignIn = (SignInButton) rootView.findViewById(R.id.signInButton);
            btnSignOut = (Button) rootView.findViewById(R.id.btn_sign_out);
            btnRevokeAccess = (Button) rootView.findViewById(R.id.btn_revoke_access);
            mProfileLayout = (LinearLayout) rootView.findViewById(R.id.userProfile);
            imageProfilePic = (ImageView) rootView.findViewById(R.id.profilePicture);
            txtName = (TextView) rootView.findViewById(R.id.userName);
            txtEmail = (TextView) rootView.findViewById(R.id.userEmail);

            mSignIn.setOnClickListener((MyActivity) getActivity());
            btnSignOut.setOnClickListener((MyActivity) getActivity());
            btnRevokeAccess.setOnClickListener((MyActivity) getActivity());

            return rootView;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleServices.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(mGoogleServices.isConnected()) {
            mGoogleServices.disconnect();
        }
    }

    private void resolveSignInError() {
        if(mConnectionResult.hasResolution()) {
            try {
                mIntentInProgress = true;
                mConnectionResult.startResolutionForResult(this, RC_SIGN_IN);
            } catch (IntentSender.SendIntentException ex) {
                mIntentInProgress = false;
                mGoogleServices.connect();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onConnected(Bundle bundle) {
        mSignInClicked = false;
        Toast.makeText(this, "User is connected!", Toast.LENGTH_SHORT).show();

        // get user information.
        getProfileInformation();

        updateUI(true);

    }

    private void updateUI(boolean isSignedIn) {

        if(isSignedIn) {
            mSignIn.setVisibility(View.GONE);
            btnSignOut.setVisibility(View.VISIBLE);
            btnRevokeAccess.setVisibility(View.VISIBLE);
            mProfileLayout.setVisibility(LinearLayout.VISIBLE);
        } else {
            mSignIn.setVisibility(View.VISIBLE);
            btnSignOut.setVisibility(View.GONE);
            btnRevokeAccess.setVisibility(View.GONE);
            mProfileLayout.setVisibility(View.GONE);
        }
    }

    private void getProfileInformation() {

        try {
            if(Plus.PeopleApi.getCurrentPerson(mGoogleServices) != null) {
                Person mCurrentPerson = Plus.PeopleApi.getCurrentPerson(mGoogleServices);
                String personName = mCurrentPerson.getDisplayName();
                String personPhotoUrl = mCurrentPerson.getImage().getUrl();
                String PersonGooglePlusProfile = mCurrentPerson.getUrl();
                final String personEmail = Plus.AccountApi.getAccountName(mGoogleServices);

                txtName.setText(personName);
                txtEmail.setText(personEmail);

                // by default the profile url gives 50x50 px image only
                // we can replace the value with whatever dimension we 2want by
                // replacing sz=X
                personPhotoUrl = personPhotoUrl.substring(0, personPhotoUrl.length() -2) + PROFILE_PIC_SIZE;

                new LoadProfileImage(imageProfilePic).execute(personPhotoUrl);
                /*
                Runnable mRun = new Runnable() {
                    @Override
                    public void run() {
                        SenderCloud sender = new SenderCloud(personEmail);
                        sender.pushToRemote();
                        //sender.pullFromRemote();
                    }
                };

                new Thread(mRun).start();
                */
            } else {
                Toast.makeText(getApplicationContext(),
                        "Person information is null", Toast.LENGTH_LONG).show();
            }
        } catch (Exception ex) {
            ex.printStackTrace();

        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleServices.connect();
        updateUI(false);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.signInButton:
                signInWithGplus();
                break;
            case R.id.btn_sign_out:
                signOutFromGplus();
                break;
            case R.id.btn_revoke_access:
                revokeGplusAccess();
                break;
        }
    }

    /**
     *  Sign In with GPLUS
     */
    private void signInWithGplus() {
        if(!mGoogleServices.isConnecting()) {
            mSignInClicked = true;
            resolveSignInError();
        }
    }

    /**
     *  Sign Out with GPLUS
     */
   private void signOutFromGplus() {
       if(mGoogleServices.isConnected()) {
           Plus.AccountApi.clearDefaultAccount(mGoogleServices);
           mGoogleServices.disconnect();
           mGoogleServices.connect();
           updateUI(false);
       }
   }

    /**
     * Revoce access from Gplus
     */
    private void revokeGplusAccess() {
        if(mGoogleServices.isConnected()) {
            Plus.AccountApi.clearDefaultAccount(mGoogleServices);
            Plus.AccountApi.revokeAccessAndDisconnect(mGoogleServices)
                    .setResultCallback(new ResultCallback<Status>() {
                        @Override
                        public void onResult(Status status) {
                            Log.i(LOG_TAG, "User access revoked");
                            mGoogleServices.connect();
                            updateUI(false);
                        }
                    });
        }
    }

    /**
     *
     * @param connectionResult
     */
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if(!connectionResult.hasResolution()) {
            GooglePlayServicesUtil.getErrorDialog(connectionResult.getErrorCode(), this, 0).show();
            return;
        }

        if(!mIntentInProgress) {

            // Store the result for later usage
            mConnectionResult = connectionResult;

            if(mSignInClicked) {
                // The user has already clicked sign-in so we attempt to resolve all errors
                // until the user is signed in, or cancel.
                resolveSignInError();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == RC_SIGN_IN) {
            if(resultCode != RESULT_OK) {
                mSignInClicked = false;
            }

            mIntentInProgress = false;

            if(!mGoogleServices.isConnecting()) {
                mGoogleServices.connect();
            }
        }
    }

    /**
     * Background Async task to load user profile picture from url
     * */
    private class LoadProfileImage extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public LoadProfileImage(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }


}
