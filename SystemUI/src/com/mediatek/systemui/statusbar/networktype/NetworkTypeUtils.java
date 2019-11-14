
package com.mediatek.systemui.statusbar.networktype;

import android.telephony.ServiceState;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.android.systemui.R;
import com.android.systemui.statusbar.policy.NetworkControllerImpl.Config;

import java.util.HashMap;
import java.util.Map;

/**
 * An utility class to access network type.
 */
public class NetworkTypeUtils {
    private static final String TAG = "NetworkTypeUtils";

    public static final int VOLTE_ICON = R.drawable.ic_stat_sys_hd;//stat_sys_volte
    public static final int VOLTE_DIS_ICON = R.drawable.stat_sys_volte_dis;
    public static final int WFC_ICON = R.drawable.stat_sys_wfc;

    //For 4G+W
    public static final int LWA_ICON = R.drawable.stat_sys_data_fully_connected_4gaw;
    public static final int LWA_STATE_CONNCTED = 1;
    public static final int LWA_STATE_DISCONNCTED = 0;
    public static final int LWA_STATE_UNKNOWN = -1;
    // Connection phase
    public static final String LWA_STATE_CHANGE_ACTION =
        "com.mediatek.server.lwa.LWA_STATE_CHANGE_ACTION";
    public static final String EXTRA_PHONE_ID =
        "com.mediatek.server.lwa.EXTRA_PHONE_ID"; // int phone id
    public static final String EXTRA_STATE =
        "com.mediatek.server.lwa.EXTRA_STATE"; //0: disconnected, 1: connected

    static final Map<Integer, Integer> sNetworkTypeIcons = new HashMap<Integer, Integer>() {
        {
            // For CDMA 3G
            put(TelephonyManager.NETWORK_TYPE_EVDO_0, R.drawable.stat_sys_network_type_3g);
            put(TelephonyManager.NETWORK_TYPE_EVDO_A, R.drawable.stat_sys_network_type_3g);
            put(TelephonyManager.NETWORK_TYPE_EVDO_B, R.drawable.stat_sys_network_type_3g);
            put(TelephonyManager.NETWORK_TYPE_EHRPD, R.drawable.stat_sys_network_type_3g);
            // For CDMA 1x
            put(TelephonyManager.NETWORK_TYPE_CDMA, R.drawable.stat_sys_network_type_1x);
            put(TelephonyManager.NETWORK_TYPE_1xRTT, R.drawable.stat_sys_network_type_1x);
            // Edge
            put(TelephonyManager.NETWORK_TYPE_EDGE, R.drawable.stat_sys_network_type_e);
            // 3G
            put(TelephonyManager.NETWORK_TYPE_UMTS, R.drawable.stat_sys_network_type_3g);
            // For 4G
            put(TelephonyManager.NETWORK_TYPE_LTE, R.drawable.stat_sys_network_type_4g);
            // 3G
            put(TelephonyManager.NETWORK_TYPE_HSDPA, R.drawable.stat_sys_network_type_3g);
            put(TelephonyManager.NETWORK_TYPE_HSUPA, R.drawable.stat_sys_network_type_3g);
            put(TelephonyManager.NETWORK_TYPE_HSPA, R.drawable.stat_sys_network_type_3g);
            put(TelephonyManager.NETWORK_TYPE_HSPAP, R.drawable.stat_sys_network_type_3g);
            put(TelephonyManager.NETWORK_TYPE_IWLAN, 0);
        }
    };

    //20190513 pjz add show small networkType
    static final Map<Integer, Integer> sNetworkTypeSmallIcons = new HashMap<Integer, Integer>() {
        {
            // For CDMA 3G
            put(TelephonyManager.NETWORK_TYPE_EVDO_0, R.drawable.stat_sys_data_fully_connected_3g);
            put(TelephonyManager.NETWORK_TYPE_EVDO_A, R.drawable.stat_sys_data_fully_connected_3g);
            put(TelephonyManager.NETWORK_TYPE_EVDO_B, R.drawable.stat_sys_data_fully_connected_3g);
            put(TelephonyManager.NETWORK_TYPE_EHRPD, R.drawable.stat_sys_data_fully_connected_3g);
            // For CDMA 1x
            put(TelephonyManager.NETWORK_TYPE_CDMA, R.drawable.stat_sys_data_fully_connected_1x);
            put(TelephonyManager.NETWORK_TYPE_1xRTT, R.drawable.stat_sys_data_fully_connected_1x);
            // Edge
            put(TelephonyManager.NETWORK_TYPE_EDGE, R.drawable.stat_sys_data_fully_connected_e);
            // 3G
            put(TelephonyManager.NETWORK_TYPE_UMTS, R.drawable.stat_sys_data_fully_connected_3g);
            // For 4G
            put(TelephonyManager.NETWORK_TYPE_LTE, R.drawable.stat_sys_data_fully_connected_4g);
            // 3G
            put(TelephonyManager.NETWORK_TYPE_HSDPA, R.drawable.stat_sys_data_fully_connected_3g);
            put(TelephonyManager.NETWORK_TYPE_HSUPA, R.drawable.stat_sys_data_fully_connected_3g);
            put(TelephonyManager.NETWORK_TYPE_HSPA, R.drawable.stat_sys_data_fully_connected_3g);
            put(TelephonyManager.NETWORK_TYPE_HSPAP, R.drawable.stat_sys_data_fully_connected_3g);
            put(TelephonyManager.NETWORK_TYPE_IWLAN, 0);
        }
    };

    /**
     * Map the network type into the related icons.
     * @param serviceState ServiceState to get current network type.
     * @param config Config passed in.
     * @param hasService true for in service.
     * @return Network type's icon.
     */
    public static int getNetworkTypeIcon(ServiceState serviceState, Config config,
            boolean hasService) {
        if (!hasService) {
            // Not in service, no network type.
            return 0;
        }
        int tempNetworkType = getNetworkType(serviceState);

        //201905113 pjz change  sNetworkTypeIcons to sNetworkTypeSmallIcons, show small networkType
        //Integer iconId = sNetworkTypeIcons.get(tempNetworkType);
        Integer iconId = sNetworkTypeSmallIcons.get(tempNetworkType);//add
        if (iconId == null) {
            iconId = tempNetworkType == TelephonyManager.NETWORK_TYPE_UNKNOWN ? 0 :
                     config.showAtLeast3G ? R.drawable.stat_sys_data_fully_connected_3g/*stat_sys_network_type_3g*/ :
                                            R.drawable.stat_sys_data_fully_connected_g/*stat_sys_network_type_g*/;
        }
        Log.i("ccz", "Operator=="+ serviceState.getOperatorAlphaLong());
        return iconId.intValue();
    }

    private static int getNetworkType(ServiceState serviceState) {
        int type = TelephonyManager.NETWORK_TYPE_UNKNOWN;
        if (serviceState != null) {
            type = serviceState.getDataNetworkType() != TelephonyManager.NETWORK_TYPE_UNKNOWN ?
                    serviceState.getDataNetworkType() : serviceState.getVoiceNetworkType();
        }
        return type;
    }

    //pjz add for operatortype
    static final int[] OPERATOR_TYPE = {
            R.string.operator_cmcc,//CHINA_MOBILE
            R.string.operator_cucc,//CHINA_UNICOM
            R.string.operator_ctcc//CHINA_TELECOM
    };

    public static int getOperatorType(TelephonyManager telephonyManager) {
        int type = 0;
        String operator = telephonyManager.getSimOperator();

        switch (operator) {
            case "46000":
            case "46002":
            case "46007":
            case "41004": 
                type = OPERATOR_TYPE[0];
                break;
            case "46001":
            case "46006":
                type = OPERATOR_TYPE[1];
                break;
            case "46003":
            case "46011":
                type = OPERATOR_TYPE[2];
                break;
            default:
                break;
        }
        return type;
    }


}
