package ru.bmstu.evernote.account;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
import android.app.ProgressDialog;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.evernote.edam.type.User;
import com.evernote.edam.userstore.AuthenticationResult;

import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.EvernoteApi;
import org.scribe.exceptions.OAuthException;
import org.scribe.model.Token;
import org.scribe.model.Verifier;
import org.scribe.oauth.OAuthService;
import org.scribe.utils.OAuthEncoder;


import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ru.bmstu.evernote.R;

/**
 * Created by Ivan on 08.12.2014.
 */
public class LoginActivity extends AccountAuthenticatorActivity {
    private static final String LOGTAG = LoginActivity.class.getSimpleName();

    private static final String HOST = EvernoteSession.HOST_SANDBOX;

    public static final String EXTRA_TOKEN_TYPE = "EXTRA_TOKEN_TYPE";

    private static final String CONSUMER_KEY = "krygin";
    private static final String CONSUMER_SECRET = "3898941278b76f9b";
    private static final String CALLBACK_SCHEME = "bmstu-oauth";

    private static final Pattern NOTESTORE_REGEX = Pattern.compile("edam_noteStoreUrl=([^&]+)");
    private static final Pattern WEBAPI_REGEX = Pattern.compile("edam_webApiUrlPrefix=([^&]+)");

    private String mRequestToken = null;
    private String mRequestTokenSecret = null;

    private WebView mWebView;

    private AsyncTask mBeginAuthSyncTask = null;
    private AsyncTask mCompleteAuthSyncTask = null;

    private WebViewClient mWebViewClient = new WebViewClient() {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Uri uri = Uri.parse(url);
            if (uri.getScheme().equals(CALLBACK_SCHEME)) {
                if (mCompleteAuthSyncTask == null) {
                    mCompleteAuthSyncTask = new CompleteAuthAsyncTask().execute(uri);
                }
                return true;
            }
            return super.shouldOverrideUrlLoading(view, url);
        }
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mWebView = (WebView) findViewById(R.id.webview);
        mWebView.setWebViewClient(mWebViewClient);
        mWebView.getSettings().setJavaScriptEnabled(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mBeginAuthSyncTask == null)
            mBeginAuthSyncTask = new BootstrapAsyncTask().execute();
    }

    private OAuthService createService() {
        OAuthService builder = null;
        Class apiClass = null;

        if (HOST.equals(EvernoteSession.HOST_SANDBOX)) {
            apiClass = EvernoteApi.Sandbox.class;
        } else if (HOST.equals(EvernoteSession.HOST_PRODUCTION)) {
            apiClass = EvernoteApi.class;
        } else {
            throw new IllegalArgumentException("Unsupported Evernote host: " +
                    HOST);
        }
        builder = new ServiceBuilder()
                .provider(apiClass)
                .apiKey(CONSUMER_KEY)
                .apiSecret(CONSUMER_SECRET)
                .callback(CALLBACK_SCHEME + "://callback")
                .build();
        return builder;
    }


    private void onAuthTokenReceived(EvernoteAuthToken evernoteAuthToken) {
        String username = evernoteAuthToken.getUser().getUsername();
        String token = evernoteAuthToken.getToken();
        Account account = new EvernoteAccount(username);
        AccountManager accountManager = AccountManager.get(this);
        final Bundle result = new Bundle();
        Bundle userdata = new Bundle();
        String noteStoreUrl = evernoteAuthToken.getNoteStoreUrl();
        String webApiUrlPrefix = evernoteAuthToken.getWebApiUrlPrefix();
        userdata.putString(EvernoteAccount.EXTRA_NOTE_STORE_URL, noteStoreUrl);
        userdata.putString(EvernoteAccount.EXTRA_WEB_API_URL_PREFIX, webApiUrlPrefix);
        userdata.putInt(EvernoteAccount.EXTRA_LAST_UPDATED_COUNT, 0);
        userdata.putInt(EvernoteAccount.EXTRA_LAST_UPDATED_COUNT, 0);
        if (accountManager.addAccountExplicitly(account, null, userdata)) {
            result.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
            result.putString(AccountManager.KEY_ACCOUNT_TYPE, account.type);
            result.putString(AccountManager.KEY_AUTHTOKEN, token);
            accountManager.setAuthToken(account, account.type, token);
        } else {
            result.putString(AccountManager.KEY_ERROR_MESSAGE, "Failed to add user");
        }
        setAccountAuthenticatorResult(result);
        setResult(RESULT_OK);
        finish();
    }

    private class BootstrapAsyncTask extends AsyncTask<Void, Void, String> {
        ProgressDialog progressDialog = null;

        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(LoginActivity.this, "Loading", "Request token loading");
        }

        @Override
        protected String doInBackground(Void... params) {
            String url = null;
            try {
                OAuthService service = createService();
                Log.i(LOGTAG, "Retrieving OAuth request token...");
                Token reqToken = service.getRequestToken();
                mRequestToken = reqToken.getToken();
                mRequestTokenSecret = reqToken.getSecret();
                Log.i(LOGTAG, "Redirecting user for authorization...");
                url = service.getAuthorizationUrl(reqToken);
            } catch (Exception ex) {
                Log.e(LOGTAG, "Failed to obtain OAuth request token", ex);
            }
            return url;
        }

        @Override
        protected void onPostExecute(String url) {
            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            mWebView.loadUrl(url);
        }
    }

    private class CompleteAuthAsyncTask extends AsyncTask<Uri, Void, EvernoteAuthToken> {
        ProgressDialog progressDialog = null;

        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(LoginActivity.this, "Loading", "Authentication token loading");
        }

        @Override
        protected EvernoteAuthToken doInBackground(Uri... uris) {
            EvernoteAuthToken evernoteAuthToken = null;
            if (uris == null || uris.length == 0) {
                return null;
            }
            Uri uri = uris[0];

            if (!TextUtils.isEmpty(mRequestToken)) {
                OAuthService service = createService();
                String verifierString = uri.getQueryParameter("oauth_verifier");
                String appLnbString = uri.getQueryParameter("sandbox_lnb");

                if (TextUtils.isEmpty(verifierString)) {
                    Log.i(LOGTAG, "User did not authorize access");
                } else {
                    Verifier verifier = new Verifier(verifierString);
                    Log.i(LOGTAG, "Retrieving OAuth access token...");
                    try {
                        Token reqToken = new Token(mRequestToken, mRequestTokenSecret);
                        Token authToken = service.getAccessToken(reqToken, verifier);
                        User user = EvernoteSession.getInstance().getClientFactory().getUserStoreClient().getUser(authToken.getToken());
                        evernoteAuthToken = new EvernoteAuthToken(authToken, user);
                    } catch (Exception ex) {
                        Log.e(LOGTAG, "Failed to obtain OAuth access token", ex);
                    }
                }
            } else {
                Log.d(LOGTAG, "Unable to retrieve OAuth access token, no request token");
            }
            return evernoteAuthToken;
        }

        /**
         * Save the authentication information resulting from a successful
         * OAuth authorization and complete the activity.
         */

        @Override
        protected void onPostExecute(EvernoteAuthToken evernoteAuthToken) {
            if (progressDialog.isShowing())
                progressDialog.dismiss();
            onAuthTokenReceived(evernoteAuthToken);
        }
    }
}