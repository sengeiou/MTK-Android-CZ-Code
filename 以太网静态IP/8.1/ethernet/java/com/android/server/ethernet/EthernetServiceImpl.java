/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.server.ethernet;

import android.content.Context;
import android.content.pm.PackageManager;
import android.database.ContentObserver; 
import android.net.IEthernetManager;
import android.net.IEthernetServiceListener;
import android.net.IpConfiguration;
import android.net.IpConfiguration.IpAssignment;
import android.net.IpConfiguration.ProxySettings;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.net.Uri;
import android.provider.Settings;
import android.provider.Settings.System;
import android.util.Log;
import android.util.PrintWriterPrinter;
import android.net.StaticIpConfiguration;
import android.net.LinkAddress;

import com.android.internal.util.IndentingPrintWriter;

import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.concurrent.atomic.AtomicBoolean;
  import java.net.InetAddress;

/**
 * EthernetServiceImpl handles remote Ethernet operation requests by implementing
 * the IEthernetManager interface.
 *
 * @hide
 */
     
	 
public class EthernetServiceImpl extends IEthernetManager.Stub {
    private static final String TAG = "EthernetServiceImpl";

     public static final String IS_ETHERNET_OPEN = Settings.IS_ETHERNET_OPEN;
     public static final String ETHERNET_USE_STATIC_IP = Settings.IS_ETHERNET_STATUC_OPEN; 
    private final Context mContext;
    private final EthernetConfigStore mEthernetConfigStore;
    private final AtomicBoolean mStarted = new AtomicBoolean(false);
    private IpConfiguration mIpConfiguration;
     private final EthernetOpenedObserver mOpenObserver = new EthernetOpenedObserver();
     private final EthernetStaticObserver mStaticObserver = new EthernetStaticObserver(); 

    private Handler mHandler;
    private final EthernetNetworkFactory mTracker;
    private final RemoteCallbackList<IEthernetServiceListener> mListeners =
            new RemoteCallbackList<IEthernetServiceListener>();

    public EthernetServiceImpl(Context context) {
        mContext = context;
        Log.i(TAG, "Creating EthernetConfigStore");
        mEthernetConfigStore = new EthernetConfigStore();
        mIpConfiguration = mEthernetConfigStore.readIpAndProxyConfigurations();

        Log.i(TAG, "Read stored IP configuration: " + mIpConfiguration);

       
 
        mTracker = new EthernetNetworkFactory(mListeners);
	       mContext.getContentResolver().registerContentObserver(
                System.getUriFor(IS_ETHERNET_OPEN), false, mOpenObserver);

               	mContext.getContentResolver().registerContentObserver(
                System.getUriFor(ETHERNET_USE_STATIC_IP), false, mStaticObserver);    
              
    }

    private void enforceAccessPermission() {
        mContext.enforceCallingOrSelfPermission(
                android.Manifest.permission.ACCESS_NETWORK_STATE,
                "EthernetService");
    }

    private void enforceChangePermission() {
        mContext.enforceCallingOrSelfPermission(
                android.Manifest.permission.CHANGE_NETWORK_STATE,
                "EthernetService");
    }
  
    private void enforceConnectivityInternalPermission() {
        mContext.enforceCallingOrSelfPermission(
                android.Manifest.permission.CONNECTIVITY_INTERNAL,
                "ConnectivityService");
    }

    public void start() {
        Log.i(TAG, "Starting Ethernet service");

        HandlerThread handlerThread = new HandlerThread("EthernetServiceThread");
        handlerThread.start();
        mHandler = new Handler(handlerThread.getLooper());
	       if (getState() == 1) {	         	    
    	        if (isStatic()) {
    	        
                    StaticIpConfiguration staticIpConfiguration = new StaticIpConfiguration();
                    staticIpConfiguration.domains = Settings.System.getString(mContext.getContentResolver(),Settings.ETHERNET_STATIC_NETMASK);
                   
                    try {
                        staticIpConfiguration.gateway = InetAddress.getByName(Settings.System.getString(mContext.getContentResolver(),Settings.ETHERNET_STATIC_GATEWAY));
                        staticIpConfiguration.ipAddress = new LinkAddress(InetAddress.getByName(Settings.System.getString(mContext.getContentResolver(),Settings.ETHERNET_STATIC_IP)), 24);
                        staticIpConfiguration.dnsServers.add(InetAddress.getByName(Settings.System.getString(mContext.getContentResolver(),Settings.ETHERNET_STATIC_DNS1)));
    		            staticIpConfiguration.dnsServers.add(InetAddress.getByName(Settings.System.getString(mContext.getContentResolver(),Settings.ETHERNET_STATIC_DNS2)));
                       
    				    }
    				    catch (Exception e) 
    				    {
                            e.printStackTrace();
                       }
    	  	        mIpConfiguration.staticIpConfiguration = staticIpConfiguration;

    	    }
        mTracker.start(mContext, mHandler);
            mStarted.set(true);
           }   

    }


    /*private boolean isStatic()*/
   
    private boolean isStatic()
    {
        Log.e(TAG, "EthernetServiceImpl isStatic()  " 
            + Settings.System.getInt(mContext.getContentResolver(),ETHERNET_USE_STATIC_IP,0));
	 return Settings.System.getInt(mContext.getContentResolver(),ETHERNET_USE_STATIC_IP,0) ==1;
    }	
 

    private int getState()
    {
        int state = Settings.System.getInt(mContext.getContentResolver(), IS_ETHERNET_OPEN,0);
        Log.e(TAG, "EthernetServiceImpl getState()  " + state);
	   return state;

    }

    /**
     * Get Ethernet configuration
     * @return the Ethernet Configuration, contained in {@link IpConfiguration}.
     */
    @Override
    public IpConfiguration getConfiguration() {
        enforceAccessPermission();

        synchronized (mIpConfiguration) {
            return new IpConfiguration(mIpConfiguration);
        }
    }

    /**
     * Set Ethernet configuration
     */
    @Override
    public void setConfiguration(IpConfiguration config) {
        if (!mStarted.get()) {
            Log.w(TAG, "System isn't ready enough to change ethernet configuration");
        }

       enforceChangePermission();  
        enforceConnectivityInternalPermission();

        synchronized (mIpConfiguration) {
            mEthernetConfigStore.writeIpAndProxyConfigurations(config);

            // TODO: this does not check proxy settings, gateways, etc.
            // Fix this by making IpConfiguration a complete representation of static configuration.
            if (!config.equals(mIpConfiguration)) {
                mIpConfiguration = new IpConfiguration(config);
                mTracker.stop();
                mTracker.start(mContext, mHandler);
            }
        }
    }

    /**
     * Indicates whether the system currently has one or more
     * Ethernet interfaces.
     */
    @Override
    public boolean isAvailable() {
        enforceAccessPermission();
        return mTracker.isTrackingInterface();
    }

    /**
     * Addes a listener.
     * @param listener A {@link IEthernetServiceListener} to add.
     */
    public void addListener(IEthernetServiceListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("listener must not be null");
        }
        enforceAccessPermission();
        mListeners.register(listener);
    }

    /**
     * Removes a listener.
     * @param listener A {@link IEthernetServiceListener} to remove.
     */
    public void removeListener(IEthernetServiceListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("listener must not be null");
        }
        enforceAccessPermission();
        mListeners.unregister(listener);
    }

    @Override
    protected void dump(FileDescriptor fd, PrintWriter writer, String[] args) {
        final IndentingPrintWriter pw = new IndentingPrintWriter(writer, "  ");
        if (mContext.checkCallingOrSelfPermission(android.Manifest.permission.DUMP)
                != PackageManager.PERMISSION_GRANTED) {
            pw.println("Permission Denial: can't dump EthernetService from pid="
                    + Binder.getCallingPid()
                    + ", uid=" + Binder.getCallingUid());
            return;
        }

        pw.println("Current Ethernet state: ");
        pw.increaseIndent();
        mTracker.dump(fd, pw, args);
        pw.decreaseIndent();

        pw.println();
        pw.println("Stored Ethernet configuration: ");
        pw.increaseIndent();
        pw.println(mIpConfiguration);
        pw.decreaseIndent();

        pw.println("Handler:");
        pw.increaseIndent();
        mHandler.dump(new PrintWriterPrinter(pw), "EthernetServiceImpl");
        pw.decreaseIndent();
    }
        
    private final class EthernetOpenedObserver extends ContentObserver {
        public EthernetOpenedObserver() {
            super(new Handler());
        }

        @Override
        public void onChange(boolean selfChange, Uri uri, int userId) {
          super.onChange(selfChange, uri, userId);
            Log.i(TAG, "EthernetServiceImpl isEthernetOpen onChange....");
    	    if (getState() == 1) {
    	 	     if (isStatic()) {
                    StaticIpConfiguration staticIpConfiguration = new StaticIpConfiguration();
                    staticIpConfiguration.domains = Settings.System.getString(mContext.getContentResolver(),Settings.ETHERNET_STATIC_NETMASK);
                   
                    try {
                        staticIpConfiguration.gateway = InetAddress.getByName(Settings.System.getString(mContext.getContentResolver(),Settings.ETHERNET_STATIC_GATEWAY));
                        staticIpConfiguration.ipAddress = new LinkAddress(InetAddress.getByName(Settings.System.getString(mContext.getContentResolver(),Settings.ETHERNET_STATIC_IP)), 24);
                        staticIpConfiguration.dnsServers.add(InetAddress.getByName(Settings.System.getString(mContext.getContentResolver(),Settings.ETHERNET_STATIC_DNS1)));
    		            staticIpConfiguration.dnsServers.add(InetAddress.getByName(Settings.System.getString(mContext.getContentResolver(),Settings.ETHERNET_STATIC_DNS2)));
                       
    				    }
    				    catch (Exception e){
                            e.printStackTrace();
                        }
                	  	mIpConfiguration.staticIpConfiguration = staticIpConfiguration;
                		
                  }
                  mStarted.set(true);
    		      mTracker.start(mContext, mHandler);
    	    } 
    	  else {	
    		mTracker.stop(); 		
    	   }
        }
    }

    private final class EthernetStaticObserver extends ContentObserver {
        public EthernetStaticObserver() {
            super(new Handler());
        }

        @Override
        public void onChange(boolean selfChange, Uri uri, int userId) {
            super.onChange(selfChange, uri, userId);
            Log.i(TAG, "EthernetServiceImpl isEthernetStaticOpen onChange....");
            if (!isStatic()) {
          	
	            mIpConfiguration = mEthernetConfigStore.readIpAndProxyConfigurations();
                mTracker.stop();
                mStarted.set(true);
                mTracker.start(mContext, mHandler);           
	       }  
        }
     }
}
