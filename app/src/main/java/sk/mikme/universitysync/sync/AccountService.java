package sk.mikme.universitysync.sync;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcelable;

import sk.mikme.universitysync.activities.LoginActivity;
import sk.mikme.universitysync.provider.Provider;
import sk.mikme.universitysync.provider.User;


public class AccountService extends Service {
    private static final String TAG = "AccountService";
    public static final String ACCOUNT_TYPE = "sk.mikme.universitysync.account";
    public static final String AUTHTOKEN_TYPE = ACCOUNT_TYPE;
    private static final Object sAccountServiceLock = new Object();

    private Authenticator mAuthenticator;

    @Override
    public void onCreate() {
        synchronized (sAccountServiceLock) {
            if (mAuthenticator == null)
                mAuthenticator = new Authenticator(this);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mAuthenticator.getIBinder();
    }

    public static Account findAccountBySession(Context context, Session session) {
        AccountManager manager = AccountManager.get(context);
        if (session != null) {
            Account[] accounts = manager.getAccountsByType(ACCOUNT_TYPE);
            Account account = null;
            for (Account a : accounts) {
                if (a.name.equals(session.getUserEmail())) {
                    account = a;
                    break;
                }
            }
            if (account != null) {
                //manager.setAuthToken(account, AccountService.AUTHTOKEN_TYPE, user.getAuthToken());
                //mUser = user;
                //SyncAdapter.setAccount(account);
                return account;
            }
        }
        return null;
    }

    public static void addAccount(Context context,
                                  String username,
                                  String password,
                                  Parcelable response) {
        AccountAuthenticatorResponse authResponse = (AccountAuthenticatorResponse)response;
        Bundle result = addAccount(context, username, password);
        if(authResponse != null)
            authResponse.onResult(result);
    }

    private static Bundle addAccount(Context context,
                             String username,
                             String password) {
        Bundle result = null;
        Account account = new Account(username, ACCOUNT_TYPE);
        AccountManager am = AccountManager.get(context);
        if (am.addAccountExplicitly(account, password, null)) {
            // Inform the system that this account supports sync
            ContentResolver.setIsSyncable(account, Provider.AUTHORITY, 1);
            // Inform the system that this account is eligible for auto sync when the network is up
            ContentResolver.setSyncAutomatically(account, Provider.AUTHORITY, true);
            // Recommend a schedule for automatic synchronization. The system may modify this based
            // on other scheduled syncs and network utilization.
            ContentResolver.addPeriodicSync(
                    account, Provider.AUTHORITY, new Bundle(), SyncAdapter.SYNC_FREQUENCY);
            result = new Bundle();
            result.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
            result.putString(AccountManager.KEY_ACCOUNT_TYPE, account.type);
        }
        return result;
    }

//    public static boolean hasAccount(Context context) {
//        AccountManager manager = AccountManager.get(context);
//        Account[] accounts = manager.getAccountsByType(ACCOUNT_TYPE);
//        if (accounts != null && accounts.length > 0)
//            return true;
//        else
//            return false;
//    }

    private class Authenticator extends AbstractAccountAuthenticator {

        private Context mContext;

        public Authenticator(Context context) {
            super(context);
            mContext = context;
        }

        @Override
        public Bundle editProperties(AccountAuthenticatorResponse accountAuthenticatorResponse, String s) {
            return null;
        }

        @Override
        public Bundle addAccount(AccountAuthenticatorResponse response,
                                 String accountType,
                                 String authTokenType,
                                 String[] requiredFeatures,
                                 Bundle options) throws NetworkErrorException {
            Intent intent = new Intent(mContext, LoginActivity.class);
            intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
            Bundle bundle = new Bundle();
            bundle.putParcelable(AccountManager.KEY_INTENT, intent);
            return bundle;
        }

        @Override
        public Bundle confirmCredentials(AccountAuthenticatorResponse accountAuthenticatorResponse, Account account, Bundle bundle) throws NetworkErrorException {
            return null;
        }

        @Override
        public Bundle getAuthToken(AccountAuthenticatorResponse accountAuthenticatorResponse, Account account, String s, Bundle bundle) throws NetworkErrorException {
            return null;
        }

        @Override
        public String getAuthTokenLabel(String s) {
            return null;
        }

        @Override
        public Bundle updateCredentials(AccountAuthenticatorResponse accountAuthenticatorResponse, Account account, String s, Bundle bundle) throws NetworkErrorException {
            return null;
        }

        @Override
        public Bundle hasFeatures(AccountAuthenticatorResponse accountAuthenticatorResponse, Account account, String[] strings) throws NetworkErrorException {
            return null;
        }
    }
}
