package plu.capstone;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.webkit.WebView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity implements LoaderCallbacks<Cursor> {

    /**
     * Id to identity READ_CONTACTS permission request.
     */
    private static final int REQUEST_READ_CONTACTS = 0;

    /**
     * A dummy authentication store containing known user names and passwords.
     * TODO: remove after connecting to a real authentication system.
     */
    private static final String[] DUMMY_CREDENTIALS = new String[]{
            "foo@example.com:hello", "bar@example.com:world"
    };
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // Set up the login form.
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
        populateAutoComplete();

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);

        //Remove this when login works
        Button skipButton = (Button) findViewById(R.id.button);
        skipButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                goToMain();
            }
        });
    }
    //remove this when login works
    private void goToMain(){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    private void populateAutoComplete() {
        if (!mayRequestContacts()) {
            return;
        }

        getLoaderManager().initLoader(0, null, this);
    }

    private boolean mayRequestContacts() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (checkSelfPermission(READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        if (shouldShowRequestPermissionRationale(READ_CONTACTS)) {
            Snackbar.make(mEmailView, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, new View.OnClickListener() {
                        @Override
                        @TargetApi(Build.VERSION_CODES.M)
                        public void onClick(View v) {
                            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
                        }
                    });
        } else {
            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
        }
        return false;
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_READ_CONTACTS) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                populateAutoComplete();
            }
        }
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new UserLoginTask(email, password);
            mAuthTask.execute((Void) null);
        }
    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email
                .CONTENT_ITEM_TYPE},

                // Show primary email addresses first. Note that there won't be
                // a primary email address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> emails = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }

        addEmailsToAutoComplete(emails);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(LoginActivity.this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        mEmailView.setAdapter(adapter);
    }


    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mEmail;
        private final String mPassword;

        UserLoginTask(String email, String password) {
            mEmail = email;
            mPassword = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.

            try {
                // Simulate network access.
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                return false;
            }

            for (String credential : DUMMY_CREDENTIALS) {
                String[] pieces = credential.split(":");
                if (pieces[0].equals(mEmail)) {
                    // Account exists, return true if the password matches.
                    return pieces[1].equals(mPassword);
                }
            }

            // TODO: register the new account here.
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) {
                finish();
            } else {
                mPasswordView.setError(getString(R.string.error_incorrect_password));
                mPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }
    //start added stuff
    /*
    public class NetworkActivity extends Activity {
        public static final String WIFI = "Wi-Fi";
        public static final String ANY = "Any";
        private static final String URL =
                "https://25livepub.collegenet.com/calendars/all.rss";

        // Whether there is a Wi-Fi connection.
        private static boolean wifiConnected = false;
        // Whether there is a mobile connection.
        private static boolean mobileConnected = false;
        // Whether the display should be refreshed.
        public static boolean refreshDisplay = true;

        // The user's current network preference setting.
        public static String sPref = null;

        // The BroadcastReceiver that tracks network connectivity changes.
        private NetworkReceiver receiver = new NetworkReceiver();

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Register BroadcastReceiver to track connection changes.
            IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
            receiver = new NetworkReceiver();
            this.registerReceiver(receiver, filter);
        }

        // Refreshes the display if the network connection and the
        // pref settings allow it.
        @Override
        public void onStart() {
            super.onStart();

            // Gets the user's network preference settings
            SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

            // Retrieves a string value for the preferences. The second parameter
            // is the default value to use if a preference value is not found.
            sPref = sharedPrefs.getString("listPref", "Wi-Fi");

            updateConnectedFlags();

            // Only loads the page if refreshDisplay is true. Otherwise, keeps previous
            // display. For example, if the user has set "Wi-Fi only" in prefs and the
            // device loses its Wi-Fi connection midway through the user using the app,
            // you don't want to refresh the display--this would force the display of
            // an error page instead of stackoverflow.com content.
            if (refreshDisplay) {
                loadPage();
            }
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            if (receiver != null) {
                this.unregisterReceiver(receiver);
            }
        }

        // Checks the network connection and sets the wifiConnected and mobileConnected
        // variables accordingly.
        private void updateConnectedFlags() {
            ConnectivityManager connMgr =
                    (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

            NetworkInfo activeInfo = connMgr.getActiveNetworkInfo();
            if (activeInfo != null && activeInfo.isConnected()) {
                wifiConnected = activeInfo.getType() == ConnectivityManager.TYPE_WIFI;
                mobileConnected = activeInfo.getType() == ConnectivityManager.TYPE_MOBILE;
            } else {
                wifiConnected = false;
                mobileConnected = false;
            }
        }

        // Uses AsyncTask subclass to download the XML feed from stackoverflow.com.
        // This avoids UI lock up. To prevent network operations from
        // causing a delay that results in a poor user experience, always perform
        // network operations on a separate thread from the UI.
        private void loadPage() {
            if (((sPref.equals(ANY)) && (wifiConnected || mobileConnected))
                    || ((sPref.equals(WIFI)) && (wifiConnected))) {
                // AsyncTask subclass
                new DownloadXmlTask().execute(URL);
            }// else {
               // showErrorPage();
            //}
        }

        // Displays an error if the app is unable to load content.
        /*private void showErrorPage() {
            setContentView(R.layout.main);

            // The specified network connection is not available. Displays error message.
            WebView myWebView = (WebView) findViewById(R.id.webview);
            myWebView.loadData(getResources().getString(R.string.connection_error),
                    "text/html", null);
        }

        // Populates the activity's options menu.
        @Override
        public boolean onCreateOptionsMenu(Menu menu) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.mainmenu, menu);
            return true;
        }

        // Handles the user's menu selection.
        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            switch (item.getItemId()) {
                case R.id.settings:
                    Intent settingsActivity = new Intent(getBaseContext(), SettingsActivity.class);
                    startActivity(settingsActivity);
                    return true;
                case R.id.refresh:
                    loadPage();
                    return true;
                default:
                    return super.onOptionsItemSelected(item);
            }
        }

        // Implementation of AsyncTask used to download XML feed from stackoverflow.com.
        private class DownloadXmlTask extends AsyncTask<String, Void, String> {

            @Override
            protected String doInBackground(String... urls) {
                try {
                    return loadXmlFromNetwork(urls[0]);
                } catch (IOException e) {
                    return getResources().getString(R.string.connection_error);
                } catch (XmlPullParserException e) {
                    return getResources().getString(R.string.xml_error);
                }
            }

            @Override
            protected void onPostExecute(String result) {
                setContentView(R.layout.main);
                // Displays the HTML string in the UI via a WebView
                WebView myWebView = (WebView) findViewById(R.id.webview);
                myWebView.loadData(result, "text/html", null);
            }
        }

        // Uploads XML from stackoverflow.com, parses it, and combines it with
        // HTML markup. Returns HTML string.
        private String loadXmlFromNetwork(String urlString) throws XmlPullParserException, IOException {
            InputStream stream = null;
            StackOverflowXmlParser stackOverflowXmlParser = new StackOverflowXmlParser();
            List<Map.Entry> entries = null;
            String title = null;
            String url = null;
            String summary = null;
            Calendar rightNow = Calendar.getInstance();
            DateFormat formatter = new SimpleDateFormat("MMM dd h:mmaa");

            // Checks whether the user set the preference to include summary text
            SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
            boolean pref = sharedPrefs.getBoolean("summaryPref", false);

            StringBuilder htmlString = new StringBuilder();
            htmlString.append("<h3>" + getResources().getString(R.string.page_title) + "</h3>");
            htmlString.append("<em>" + getResources().getString(R.string.updated) + " " +
                    formatter.format(rightNow.getTime()) + "</em>");

            try {
                stream = downloadUrl(urlString);
                entries = stackOverflowXmlParser.parse(stream);
                // Makes sure that the InputStream is closed after the app is
                // finished using it.
            } finally {
                if (stream != null) {
                    stream.close();
                }
            }

            // StackOverflowXmlParser returns a List (called "entries") of Entry objects.
            // Each Entry object represents a single post in the XML feed.
            // This section processes the entries list to combine each entry with HTML markup.
            // Each entry is displayed in the UI as a link that optionally includes
            // a text summary.
            for (Entry entry : entries) {
                htmlString.append("<p><a href='");
                htmlString.append(entry.link);
                htmlString.append("'>" + entry.title + "</a></p>");
                // If the user set the preference to include summary text,
                // adds it to the display.
                if (pref) {
                    htmlString.append(entry.summary);
                }
            }
            return htmlString.toString();
        }

        // Given a string representation of a URL, sets up a connection and gets
        // an input stream.
        private InputStream downloadUrl(String urlString) throws IOException {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            //conn.setReadTimeout(10000 /* milliseconds *///);
            //conn.setConnectTimeout(15000 /* milliseconds */);
           /* conn.setRequestMethod("GET");
            conn.setDoInput(true);
            // Starts the query
            conn.connect();
            InputStream stream = conn.getInputStream();
            return stream;
        }

        /**
         *
         * This BroadcastReceiver intercepts the android.net.ConnectivityManager.CONNECTIVITY_ACTION,
         * which indicates a connection change. It checks whether the type is TYPE_WIFI.
         * If it is, it checks whether Wi-Fi is connected and sets the wifiConnected flag in the
         * main activity accordingly.
         *
         */
        /*public class NetworkReceiver extends BroadcastReceiver {

            @Override
            public void onReceive(Context context, Intent intent) {
                ConnectivityManager connMgr =
                        (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

                // Checks the user prefs and the network connection. Based on the result, decides
                // whether
                // to refresh the display or keep the current display.
                // If the userpref is Wi-Fi only, checks to see if the device has a Wi-Fi connection.
                if (WIFI.equals(sPref) && networkInfo != null
                        && networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                    // If device has its Wi-Fi connection, sets refreshDisplay
                    // to true. This causes the display to be refreshed when the user
                    // returns to the app.
                    refreshDisplay = true;
                    Toast.makeText(context, R.string.wifi_connected, Toast.LENGTH_SHORT).show();

                    // If the setting is ANY network and there is a network connection
                    // (which by process of elimination would be mobile), sets refreshDisplay to true.
                } else if (ANY.equals(sPref) && networkInfo != null) {
                    refreshDisplay = true;

                    // Otherwise, the app can't download content--either because there is no network
                    // connection (mobile or Wi-Fi), or because the pref setting is WIFI, and there
                    // is no Wi-Fi connection.
                    // Sets refreshDisplay to false.
                } else {
                    refreshDisplay = false;
                    Toast.makeText(context, R.string.lost_connection, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }*/
            //end added stuff
}

