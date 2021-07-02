package eu.siacs.conversations.services;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;

import eu.siacs.conversations.Config;
import eu.siacs.conversations.http.NoSSLv3SocketFactory;

public class ProviderService extends AsyncTask<String, Object, Boolean> {
    public static List<String> providers = new ArrayList<>();

    // in accordance with cat B (https://invent.kde.org/melvo/xmpp-providers/)
    public static boolean REGISTRATION = true;
    public static boolean FREE = true;
    public static int COMPLIANCE = 90;
    public static String RATING = "A";

    public ProviderService() {
    }

    public static List<String> getProviders() {
        final HashSet<String> provider = new HashSet<>(Config.DOMAIN.DOMAINS);
        if (!providers.isEmpty()) {
            provider.addAll(providers);
        }
        return new ArrayList<>(provider);
    }

    @Override
    protected Boolean doInBackground(String... params) {
        StringBuilder jsonString = new StringBuilder();
        boolean isError = false;

        SSLContext sslcontext = null;
        SSLSocketFactory NoSSLv3Factory = null;
        try {
            sslcontext = SSLContext.getInstance("TLSv1");
            if (sslcontext != null) {
                sslcontext.init(null, null, null);
                NoSSLv3Factory = new NoSSLv3SocketFactory(sslcontext.getSocketFactory());
            }
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            e.printStackTrace();
        }
        HttpsURLConnection.setDefaultSSLSocketFactory(NoSSLv3Factory);
        HttpsURLConnection connection = null;
        try {
            URL url = new URL(Config.PROVIDER_URL);
            Log.d(Config.LOGTAG, "ProviderService: Updating provider list from " + Config.PROVIDER_URL);
            connection = (HttpsURLConnection) url.openConnection();
            connection.setConnectTimeout(Config.SOCKET_TIMEOUT * 1000);
            connection.setReadTimeout(Config.SOCKET_TIMEOUT * 1000);
            connection.setRequestProperty("User-agent", System.getProperty("http.agent"));
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                jsonString.append(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
            isError = true;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }

        try {
            parseJson(new JSONObject(jsonString.toString()));
        } catch (JSONException e) {
            e.printStackTrace();
            isError = true;
        }
        if (isError) {
            Log.d(Config.LOGTAG, "ProviderService: Updating provider list failed");
        }
        return !isError;
    }

    private void parseJson(JSONObject jsonObject) {
        if (jsonObject != null) {
            try {
                for (int i = 0; i < jsonObject.length(); i++) {
                    boolean inBandRegistration = false;
                    boolean freeOfCharge = false;
                    String ratingC2S = null;
                    String ratingS2S = null;
                    int ratingXmppComplianceTester = 0;
                    final String provider = jsonObject.names().getString(i);
                    if (provider.length() > 0) {
                        for (int ii = 0; ii < jsonObject.length(); ii++) {
                            final JSONObject json = new JSONObject(jsonObject.get(provider).toString());
                            String featureName = json.names().getString(ii);
                            final JSONObject subjson = new JSONObject(json.get(json.names().getString(ii)).toString());
                            if (featureName.equals("inBandRegistration")) {
                                inBandRegistration = subjson.getBoolean("content");
                            }
                            if (featureName.equals("ratingXmppComplianceTester")) {
                                ratingXmppComplianceTester = subjson.getInt("content");
                            }
                            if (featureName.equals("freeOfCharge")) {
                                freeOfCharge = subjson.getBoolean("content");
                            }
                            if (featureName.equals("ratingImObservatoryClientToServer")) {
                                ratingC2S = subjson.getString("content");
                            }
                            if (featureName.equals("ratingImObservatoryServerToServer")) {
                                ratingS2S = subjson.getString("content");
                            }
                            if (!Config.DOMAIN.BLACKLISTED_DOMAINS.contains(provider)
                                    && inBandRegistration == REGISTRATION
                                    && ratingXmppComplianceTester >= COMPLIANCE
                                    && freeOfCharge == FREE
                                    && (ratingC2S != null && ratingC2S.equalsIgnoreCase(RATING))
                                    && (ratingS2S != null && ratingS2S.equalsIgnoreCase(RATING))) {
                                //Log.d(Config.LOGTAG, "ProviderService: Updating provider list. Adding " + provider + " (Registration: " + inBandRegistration + " Compliance: " + ratingXmppComplianceTester + " Free: " + freeOfCharge + " Rating C2S/S2S: " + ratingC2S + "/" + ratingS2S + ")");
                                providers.add(provider);
                            }
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}