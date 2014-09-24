package sk.mikme.universitysync.activities;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import java.util.ArrayList;

import sk.mikme.universitysync.R;
import sk.mikme.universitysync.provider.Provider;
import sk.mikme.universitysync.provider.User;
import sk.mikme.universitysync.sync.AccountService;
import sk.mikme.universitysync.sync.AuthAccountTask;
import sk.mikme.universitysync.sync.Session;
import sk.mikme.universitysync.sync.SyncAdapter;

/**
 * Created by fic on 20.9.2014.
 */
public class LoginActivity extends AccountAuthenticatorActivity
    implements AuthAccountTask.AuthAccountListener {

    public static final String PARAM_EMAIL = "email";
    public static final String PARAM_PASSWORD = "password";
    public static final String PARAM_USER = "user";

    private EditText mEmailView;
    private EditText mPasswordView;

    private LinearLayout mLoginForm;
    private FrameLayout mProgressBar;

    private String mEmail;
    private String mPassword;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        setTitle(R.string.login);

        mLoginForm = (LinearLayout) findViewById(R.id.form);
        mProgressBar = (FrameLayout) findViewById(R.id.progress_bar);
        mEmailView = (EditText) findViewById(R.id.email);
        mPasswordView = (EditText) findViewById(R.id.password);
        Button logInButton = (Button) findViewById(R.id.sign_in);
        Button cancelButton = (Button) findViewById(R.id.cancel);

        logInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkCredentials();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        String email = getIntent().getStringExtra(PARAM_EMAIL);
        if (email != null)
            mEmailView.setText(email);
    }

    private void checkCredentials() {

        mEmailView.setError(null);
        mPasswordView.setError(null);

        mEmail = mEmailView.getText().toString().trim();
        String mPasswrod = mPasswordView.getText().toString().trim();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(mPasswrod)) {
            mPasswordView.setError(getString(R.string.error_password_required));
            focusView = mPasswordView;
            cancel = true;
        }
        else if (!isPasswordValid(mPasswrod)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(mEmail)) {
            mEmailView.setError(getString(R.string.error_email_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(mEmail)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            new AuthAccountTask(this, mEmail, mPasswrod).execute();
        }
    }

    private boolean isEmailValid(String email) {
        return true;
    }

    private boolean isPasswordValid(String password) {
        return true;
    }

    @Override
    public void onAccountAuth(User user) {
        if (user != null) {
            // put the user into the db
            ContentResolver resolver = getApplicationContext().getContentResolver();
            ArrayList<ContentProviderOperation> batch = new ArrayList<ContentProviderOperation>();
            batch.add(ContentProviderOperation.newInsert(User.URI)
                    .withValue(User.COLUMN_NAME_USER_ID, user.getUserId())
                    .withValue(User.COLUMN_NAME_NAME, user.getName())
                    .withValue(User.COLUMN_NAME_SURNAME, user.getSurname())
                    .withValue(User.COLUMN_NAME_EMAIL, user.getEmail())
                    .withValue(User.COLUMN_NAME_UNIVERSITY, user.getUniversity())
                    .withValue(User.COLUMN_NAME_INFO, user.getInfo())
                    .withValue(User.COLUMN_NAME_RANK, user.getRank())
                    .build());
            try {
                resolver.applyBatch(Provider.AUTHORITY, batch);
            } catch (RemoteException e) {
                e.printStackTrace();
                setResult(RESULT_CANCELED);
                finish();
            } catch (OperationApplicationException e) {
                e.printStackTrace();
                setResult(RESULT_CANCELED);
                finish();
            }
            resolver.notifyChange(User.URI,  null, false);

            // set extras for account service
            Parcelable authResponse = null;
            if (getIntent() != null && getIntent().getExtras() != null)
                authResponse = getIntent().getParcelableExtra(
                        AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE);
            AccountService.addAccount(getApplicationContext(), mEmail, mPassword, authResponse);
            Intent intent = new Intent();
            intent.putExtra(AccountManager.KEY_ACCOUNT_NAME, mEmail);
            intent.putExtra(AccountManager.KEY_ACCOUNT_TYPE, AccountService.ACCOUNT_TYPE);
            intent.putExtra(AccountManager.KEY_AUTHTOKEN, user.getAuthToken());
            setAccountAuthenticatorResult(intent.getExtras());

            // create the session for sync adapter and save it to shared prefs
            Session session = new Session(user.getUserId(), user.getEmail(), user.getAuthToken());
            session.save(getApplicationContext());
            SyncAdapter.setSession(session);

            // find the account and add it to sync adapter
            Account account = AccountService.findAccountBySession(getApplicationContext(), session);
            SyncAdapter.setAccount(account);

            setResult(RESULT_OK, intent);
            finish();
        }
    }

    @Override
    public void showProgressBar(boolean show) {
        mLoginForm.setVisibility(show ? View.INVISIBLE : View.VISIBLE);
        mProgressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }
}
