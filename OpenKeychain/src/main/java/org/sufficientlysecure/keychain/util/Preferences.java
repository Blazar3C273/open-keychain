/*
 * Copyright (C) 2012-2014 Dominik Schürmann <dominik@dominikschuermann.de>
 * Copyright (C) 2010-2014 Thialfihar <thi@thialfihar.org>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.sufficientlysecure.keychain.util;

import android.content.Context;
import android.content.SharedPreferences;

import android.content.res.Resources;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;

import org.sufficientlysecure.keychain.Constants;
import org.sufficientlysecure.keychain.Constants.Pref;
import org.sufficientlysecure.keychain.R;
import org.sufficientlysecure.keychain.service.KeyserverSyncAdapterService;

import java.io.Serializable;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

/**
 * Singleton Implementation of a Preference Helper
 */
public class Preferences {
    private static Preferences sPreferences;
    private SharedPreferences mSharedPreferences;
    private Resources mResources;

    private static String PREF_FILE_NAME = "APG.main";
    private static int PREF_FILE_MODE = Context.MODE_MULTI_PROCESS;

    public static synchronized Preferences getPreferences(Context context) {
        return getPreferences(context, false);
    }

    public static synchronized Preferences getPreferences(Context context, boolean forceNew) {
        if (sPreferences == null || forceNew) {
            sPreferences = new Preferences(context);
        } else {
            // to make it safe for multiple processes, call getSharedPreferences everytime
            sPreferences.updateSharedPreferences(context);
        }
        return sPreferences;
    }

    private Preferences(Context context) {
        mResources = context.getResources();
        updateSharedPreferences(context);
    }

    public static void setPreferenceManagerFileAndMode(PreferenceManager manager) {
        manager.setSharedPreferencesName(PREF_FILE_NAME);
        manager.setSharedPreferencesMode(PREF_FILE_MODE);
    }

    public void updateSharedPreferences(Context context) {
        // multi-process safe preferences
        mSharedPreferences = context.getSharedPreferences(PREF_FILE_NAME, PREF_FILE_MODE);
    }

    public String getLanguage() {
        return mSharedPreferences.getString(Constants.Pref.LANGUAGE, "");
    }

    public void setLanguage(String value) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(Constants.Pref.LANGUAGE, value);
        editor.commit();
    }

    public CacheTTLPrefs getPassphraseCacheTtl() {
        Set<String> pref = mSharedPreferences.getStringSet(Constants.Pref.PASSPHRASE_CACHE_TTLS, null);
        if (pref == null) {
            return CacheTTLPrefs.getDefault();
        }
        int def = mSharedPreferences.getInt(Pref.PASSPHRASE_CACHE_DEFAULT, 0);
        return new CacheTTLPrefs(pref, def);
    }

    public void setPassphraseCacheTtl(int value) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putInt(Constants.Pref.PASSPHRASE_CACHE_TTLS, value);
        editor.commit();
    }

    public boolean getPassphraseCacheSubs() {
        return mSharedPreferences.getBoolean(Pref.PASSPHRASE_CACHE_SUBS, false);
    }

    public void setPassphraseCacheSubs(boolean value) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putBoolean(Pref.PASSPHRASE_CACHE_SUBS, value);
        editor.commit();
    }

    public boolean getCachedConsolidate() {
        return mSharedPreferences.getBoolean(Pref.CACHED_CONSOLIDATE, false);
    }

    public void setCachedConsolidate(boolean value) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putBoolean(Pref.CACHED_CONSOLIDATE, value);
        editor.commit();
    }

    public boolean isFirstTime() {
        return mSharedPreferences.getBoolean(Constants.Pref.FIRST_TIME, true);
    }

    public boolean useNumKeypadForYubiKeyPin() {
        return mSharedPreferences.getBoolean(Pref.USE_NUMKEYPAD_FOR_YUBIKEY_PIN, true);
    }

    public void setUseNumKeypadForYubiKeyPin(boolean useNumKeypadForYubikeyPin) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putBoolean(Pref.USE_NUMKEYPAD_FOR_YUBIKEY_PIN, useNumKeypadForYubikeyPin);
        editor.commit();
    }

    public void setFirstTime(boolean value) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putBoolean(Constants.Pref.FIRST_TIME, value);
        editor.commit();
    }

    public String[] getKeyServers() {
        String rawData = mSharedPreferences.getString(Constants.Pref.KEY_SERVERS,
                Constants.Defaults.KEY_SERVERS);
        if (rawData.equals("")) {
            return new String[0];
        }
        Vector<String> servers = new Vector<>();
        String chunks[] = rawData.split(",");
        for (String c : chunks) {
            String tmp = c.trim();
            if (tmp.length() > 0) {
                servers.add(tmp);
            }
        }
        return servers.toArray(chunks);
    }

    public String getPreferredKeyserver() {
        String[] keyservers = getKeyServers();
        return keyservers.length == 0 ? null : keyservers[0];
    }

    public void setKeyServers(String[] value) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        String rawData = "";
        for (String v : value) {
            String tmp = v.trim();
            if (tmp.length() == 0) {
                continue;
            }
            if (!"".equals(rawData)) {
                rawData += ",";
            }
            rawData += tmp;
        }
        editor.putString(Constants.Pref.KEY_SERVERS, rawData);
        editor.commit();
    }

    public void setSearchKeyserver(boolean searchKeyserver) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putBoolean(Pref.SEARCH_KEYSERVER, searchKeyserver);
        editor.commit();
    }

    public void setSearchKeybase(boolean searchKeybase) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putBoolean(Pref.SEARCH_KEYBASE, searchKeybase);
        editor.commit();
    }

    public void setFilesUseCompression(boolean compress) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putBoolean(Pref.FILE_USE_COMPRESSION, compress);
        editor.commit();
    }

    public boolean getFilesUseCompression() {
        return mSharedPreferences.getBoolean(Pref.FILE_USE_COMPRESSION, true);
    }

    public void setTextUseCompression(boolean compress) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putBoolean(Pref.TEXT_USE_COMPRESSION, compress);
        editor.commit();
    }

    public boolean getTextUseCompression() {
        return mSharedPreferences.getBoolean(Pref.TEXT_USE_COMPRESSION, true);
    }

    public String getTheme() {
        return mSharedPreferences.getString(Pref.THEME, Pref.Theme.LIGHT);
    }

    public void setTheme(String value) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(Constants.Pref.THEME, value);
        editor.commit();
    }

    public void setUseArmor(boolean useArmor) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putBoolean(Pref.USE_ARMOR, useArmor);
        editor.commit();
    }

    public boolean getUseArmor() {
        return mSharedPreferences.getBoolean(Pref.USE_ARMOR, false);
    }

    public void setEncryptFilenames(boolean encryptFilenames) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putBoolean(Pref.ENCRYPT_FILENAMES, encryptFilenames);
        editor.commit();
    }

    public boolean getEncryptFilenames() {
        return mSharedPreferences.getBoolean(Pref.ENCRYPT_FILENAMES, true);
    }

    // proxy preference functions start here

    public boolean getUseNormalProxy() {
        return mSharedPreferences.getBoolean(Constants.Pref.USE_NORMAL_PROXY, false);
    }

    public boolean getUseTorProxy() {
        return mSharedPreferences.getBoolean(Constants.Pref.USE_TOR_PROXY, false);
    }

    public String getProxyHost() {
        return mSharedPreferences.getString(Constants.Pref.PROXY_HOST, null);
    }

    /**
     * we store port as String for easy interfacing with EditTextPreference, but return it as an integer
     *
     * @return port number of proxy
     */
    public int getProxyPort() {
        return Integer.parseInt(mSharedPreferences.getString(Pref.PROXY_PORT, "-1"));
    }

    /**
     * we store port as String for easy interfacing with EditTextPreference, but return it as an integer
     *
     * @param port proxy port
     */
    public void setProxyPort(String port) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(Pref.PROXY_PORT, port);
        editor.commit();
    }

    public Proxy.Type getProxyType() {
        final String typeHttp = Pref.ProxyType.TYPE_HTTP;
        final String typeSocks = Pref.ProxyType.TYPE_SOCKS;

        String type = mSharedPreferences.getString(Pref.PROXY_TYPE, typeHttp);

        switch (type) {
            case typeHttp:
                return Proxy.Type.HTTP;
            case typeSocks:
                return Proxy.Type.SOCKS;
            default:  // shouldn't happen
                Log.e(Constants.TAG, "Invalid Proxy Type in preferences");
                return null;
        }
    }

    public ProxyPrefs getProxyPrefs() {
        boolean useTor = getUseTorProxy();
        boolean useNormalProxy = getUseNormalProxy();

        if (useTor) {
            return new ProxyPrefs(true, false, Constants.Orbot.PROXY_HOST, Constants.Orbot.PROXY_PORT,
                    Constants.Orbot.PROXY_TYPE);
        } else if (useNormalProxy) {
            return new ProxyPrefs(false, true, getProxyHost(), getProxyPort(), getProxyType());
        } else {
            return new ProxyPrefs(false, false, null, -1, null);
        }
    }

    public static class ProxyPrefs {
        public final ParcelableProxy parcelableProxy;
        public final boolean torEnabled;
        public final boolean normalPorxyEnabled;

        /**
         * torEnabled and normalProxyEnabled are not expected to both be true
         *
         * @param torEnabled         if Tor is to be used
         * @param normalPorxyEnabled if user-specified proxy is to be used
         */
        public ProxyPrefs(boolean torEnabled, boolean normalPorxyEnabled, String hostName, int port, Proxy.Type type) {
            this.torEnabled = torEnabled;
            this.normalPorxyEnabled = normalPorxyEnabled;
            if (!torEnabled && !normalPorxyEnabled) this.parcelableProxy = new ParcelableProxy(null, -1, null);
            else this.parcelableProxy = new ParcelableProxy(hostName, port, type);
        }

        @NonNull
        public Proxy getProxy() {
            return parcelableProxy.getProxy();
        }

    }

    public static class CacheTTLPrefs implements Serializable {
        public static final Map<Integer,Integer> CACHE_TTL_NAMES;
        public static final ArrayList<Integer> CACHE_TTLS;
        static {
            HashMap<Integer,Integer> cacheTtlNames = new HashMap<>();
            cacheTtlNames.put(60 * 5, R.string.cache_ttl_five_minutes);
            cacheTtlNames.put(60 * 60, R.string.cache_ttl_one_hour);
            cacheTtlNames.put(60 * 60 * 3, R.string.cache_ttl_three_hours);
            cacheTtlNames.put(60 * 60 * 24, R.string.cache_ttl_one_day);
            cacheTtlNames.put(60 * 60 * 24 * 3, R.string.cache_ttl_three_days);
            CACHE_TTL_NAMES = Collections.unmodifiableMap(cacheTtlNames);

            CACHE_TTLS = new ArrayList<>(CacheTTLPrefs.CACHE_TTL_NAMES.keySet());
            Collections.sort(CACHE_TTLS);
        }


        public HashSet<Integer> ttlTimes;
        public int defaultTtl;

        public CacheTTLPrefs(Collection<String> ttlStrings, int defaultTtl) {
            this.defaultTtl = defaultTtl;
            ttlTimes = new HashSet<>();
            for (String ttlString : ttlStrings) {
                ttlTimes.add(Integer.parseInt(ttlString));
            }
        }

        public static CacheTTLPrefs getDefault() {
            ArrayList<String> ttlStrings = new ArrayList<>();
            ttlStrings.add(Integer.toString(60 * 5));
            ttlStrings.add(Integer.toString(60 * 60));
            ttlStrings.add(Integer.toString(60 * 60 * 24));
            return new CacheTTLPrefs(ttlStrings, 60 * 5);
        }

    }

    // cloud prefs

    public CloudSearchPrefs getCloudSearchPrefs() {
        return new CloudSearchPrefs(mSharedPreferences.getBoolean(Pref.SEARCH_KEYSERVER, true),
                mSharedPreferences.getBoolean(Pref.SEARCH_KEYBASE, true),
                getPreferredKeyserver());
    }

    public static class CloudSearchPrefs {
        public final boolean searchKeyserver;
        public final boolean searchKeybase;
        public final String keyserver;

        /**
         * @param searchKeyserver should passed keyserver be searched
         * @param searchKeybase   should keybase.io be searched
         * @param keyserver       the keyserver url authority to search on
         */
        public CloudSearchPrefs(boolean searchKeyserver, boolean searchKeybase, String keyserver) {
            this.searchKeyserver = searchKeyserver;
            this.searchKeybase = searchKeybase;
            this.keyserver = keyserver;
        }
    }

    // experimental prefs

    public void setExperimentalEnableWordConfirm(boolean enableWordConfirm) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putBoolean(Pref.EXPERIMENTAL_ENABLE_WORD_CONFIRM, enableWordConfirm);
        editor.commit();
    }

    public boolean getExperimentalEnableWordConfirm() {
        return mSharedPreferences.getBoolean(Pref.EXPERIMENTAL_ENABLE_WORD_CONFIRM, false);
    }

    public void setExperimentalEnableLinkedIdentities(boolean enableLinkedIdentities) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putBoolean(Pref.EXPERIMENTAL_ENABLE_LINKED_IDENTITIES, enableLinkedIdentities);
        editor.commit();
    }

    public boolean getExperimentalEnableLinkedIdentities() {
        return mSharedPreferences.getBoolean(Pref.EXPERIMENTAL_ENABLE_LINKED_IDENTITIES, false);
    }

    public void setExperimentalEnableKeybase(boolean enableKeybase) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putBoolean(Pref.EXPERIMENTAL_ENABLE_KEYBASE, enableKeybase);
        editor.commit();
    }

    public boolean getExperimentalEnableKeybase() {
        return mSharedPreferences.getBoolean(Pref.EXPERIMENTAL_ENABLE_KEYBASE, false);
    }

    public void upgradePreferences(Context context) {
        if (mSharedPreferences.getInt(Constants.Pref.PREF_DEFAULT_VERSION, 0) !=
                Constants.Defaults.PREF_VERSION) {
            switch (mSharedPreferences.getInt(Constants.Pref.PREF_DEFAULT_VERSION, 0)) {
                case 1:
                    // fall through
                case 2:
                    // fall through
                case 3: {
                    // migrate keyserver to hkps
                    String[] serversArray = getKeyServers();
                    ArrayList<String> servers = new ArrayList<>(Arrays.asList(serversArray));
                    ListIterator<String> it = servers.listIterator();
                    while (it.hasNext()) {
                        String server = it.next();
                        if (server == null) {
                            continue;
                        }
                        if (server.equals("pool.sks-keyservers.net")) {
                            // use HKPS!
                            it.set("hkps://hkps.pool.sks-keyservers.net");
                        } else if (server.equals("pgp.mit.edu")) {
                            // use HKPS!
                            it.set("hkps://pgp.mit.edu");
                        } else if (server.equals("subkeys.pgp.net")) {
                            // remove, because often down and no HKPS!
                            it.remove();
                        }

                    }
                    setKeyServers(servers.toArray(new String[servers.size()]));
                }
                // fall through
                case 4: {
                    setTheme(Constants.Pref.Theme.DEFAULT);
                }
                // fall through
                case 5: {
                    KeyserverSyncAdapterService.enableKeyserverSync(context);
                }
                // fall through
                case 6: {
                }
            }

            // write new preference version
            mSharedPreferences.edit()
                    .putInt(Constants.Pref.PREF_DEFAULT_VERSION, Constants.Defaults.PREF_VERSION)
                    .commit();
        }
    }

    public void clear() {
        mSharedPreferences.edit().clear().commit();
    }

}
