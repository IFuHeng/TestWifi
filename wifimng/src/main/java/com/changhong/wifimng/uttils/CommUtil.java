package com.changhong.wifimng.uttils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.RadioGroup;

import androidx.annotation.ColorInt;
import androidx.core.graphics.drawable.DrawableCompat;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.RandomAccessFile;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.NetworkInterface;
import java.security.MessageDigest;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommUtil {
    public static int sNotificationId = -1;
    public static boolean isDownApk = false;
    private static NotificationManager sUpdateNotificationManager;
    private static String sApkFilePath;
    private static Notification sUpdateNotification;

    private static void installApk(final Context context, boolean showProgressDialog) {
        // if (showProgressDialog)
        // sDownApkProgressDialog.dismiss();
        startInstallApkActivity(context);
        sUpdateNotificationManager.cancel(sNotificationId);
        sNotificationId = -1;
    }

    private static void startInstallApkActivity(final Context context) {
        Uri uri = Uri.fromFile(new File(sApkFilePath));
        Intent installIntent = new Intent(Intent.ACTION_VIEW);
        installIntent.setDataAndType(uri, "application/vnd.android.package-archive");
        installIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(installIntent);
    }

    /**
     * 半角转换为全角
     *
     * @param input
     * @return
     */
    public static String ToDBC(String input) {
        char[] c = input.toCharArray();
        for (int i = 0; i < c.length; i++) {
            if (c[i] == 12288) {
                c[i] = (char) 32;
                continue;
            }
            if (c[i] > 65280 && c[i] < 65375)
                c[i] = (char) (c[i] - 65248);
        }
        return new String(c);
    }

    /**
     * 判断是否是邮箱地址格式
     *
     * @param address
     * @return
     */
    public static boolean isEmail(String address) {
        return address.matches("^([a-z0-9A-Z]+[-|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$");
    }

    private static AlertDialog sAlartDialog;

    // public static void showAlert(Context context, CharSequence title,
    // CharSequence message, boolean cancelAble, final
    // DialogInterface.OnClickListener listener) {
    //
    // dismissAlert();
    //
    // AlertDialog.Builder builder = new AlertDialog.Builder(context);
    // sAlartDialog = builder.create();
    //
    // sAlartDialog.show();
    // sAlartDialog.setCancelable(cancelAble);
    // Window view = sAlartDialog.getWindow();
    // view.setContentView(R.layout.return_alert_button_dialog);
    // LayoutInflater flater = LayoutInflater.from(context);
    // // View view = flater.inflate(R.layout.return_alert_button_dialog,
    // // null);
    // Button btnreturn = (Button) view.findViewById(R.id.cancle);//
    // 注：“View.”别忘了，不然则找不到资源
    // btnreturn.setText(R.string.returntxt);
    //
    // TextView tv_title = (TextView) view.findViewById(R.id.dialog_title);
    // View tv_title_view = (View) view.findViewById(R.id.dialog_titl_view);
    // if (title != null && !TextUtils.isEmpty(title)) {
    // tv_title.setText(title);
    // tv_title.setVisibility(View.VISIBLE);
    // tv_title_view.setVisibility(View.VISIBLE);
    // } else {
    // tv_title.setVisibility(View.GONE);
    // tv_title_view.setVisibility(View.GONE);
    // }
    // TextView tv_msg = (TextView) view.findViewById(R.id.dialog_message);
    // tv_msg.setText(message);
    // // builder.setView(view);
    // btnreturn.setOnClickListener(new OnClickListener() {
    //
    // @Override
    // public void onClick(View v) {
    // sAlartDialog.dismiss();
    // if (listener != null)
    // listener.onClick(sAlartDialog, 0);
    // }
    // });
    //
    // // new
    // //
    // AlertDialog.Builder(context).setTitle(title).setMessage(message).setPositiveButton(R.string.Return,
    // // listener).show();
    // }

    // public static void showAlert(Context context, CharSequence title,
    // CharSequence message, final DialogInterface.OnClickListener listener) {
    // showAlert(context, title, message, true, listener);
    // }

    public static void dismissAlert() {
        if (sAlartDialog != null && sAlartDialog.isShowing())
            sAlartDialog.dismiss();
    }

    /**
     * 展开列表,避免scrollview和livtview的冲突
     *
     * @param adapter
     * @param listView
     */
    public static void expandListView(BaseAdapter adapter, ListView listView) {
        // 统计高度
        int totalHeight = 0;
        for (int i = 0, len = adapter.getCount(); i < len; i++) { // listAdapter.getCount()返回数据项的数目
            View listItem = adapter.getView(i, null, listView);
            listItem.measure(0, 0); // 计算子项View 的宽高
            totalHeight += listItem.getMeasuredHeight(); // 统计所有子项的总高度
        }
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (adapter.getCount() - 1)) + listView.getPaddingTop() + listView.getPaddingBottom();
        // listView.getDividerHeight()获取子项间分隔符占用的高度
        // params.height最后得到整个ListView完整显示需要的高度
        listView.setLayoutParams(params);
    }

    /**
     * 移动数据开启和关闭
     *
     * @param context
     * @param enabled
     */
    public static void setMobileDataStatus(Context context, boolean enabled) {
        ConnectivityManager conMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        // ConnectivityManager类
        Class<?> conMgrClass = null;
        // ConnectivityManager类中的字段
        Field iConMgrField = null;
        // IConnectivityManager类的引用
        Object iConMgr = null;
        // IConnectivityManager类
        Class<?> iConMgrClass = null;
        // setMobileDataEnabled方法
        Method setMobileDataEnabledMethod = null;
        try {
            // 取得ConnectivityManager类
            conMgrClass = Class.forName(conMgr.getClass().getName());
            // 取得ConnectivityManager类中的对象Mservice
            iConMgrField = conMgrClass.getDeclaredField("mService");
            // 设置mService可访问
            iConMgrField.setAccessible(true);
            // 取得mService的实例化类IConnectivityManager
            iConMgr = iConMgrField.get(conMgr);
            // 取得IConnectivityManager类
            iConMgrClass = Class.forName(iConMgr.getClass().getName());

            // 取得IConnectivityManager类中的setMobileDataEnabled(boolean)方法
            setMobileDataEnabledMethod = iConMgrClass.getDeclaredMethod("setMobileDataEnabled", Boolean.TYPE);

            // 设置setMobileDataEnabled方法是否可访问
            setMobileDataEnabledMethod.setAccessible(true);
            // 调用setMobileDataEnabled方法
            setMobileDataEnabledMethod.invoke(iConMgr, enabled);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取移动数据开关状态
     *
     * @param context
     * @param getMobileDataEnabled
     * @return
     */
    @SuppressWarnings("rawtypes")
    public static boolean getMobileDataStatus(Context context, String getMobileDataEnabled) {
        ConnectivityManager cm;
        cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        Class cmClass = cm.getClass();
        Class[] argClasses = null;
        Object[] argObject = null;
        Boolean isOpen = false;
        try {
            Method method = cmClass.getMethod(getMobileDataEnabled, argClasses);
            isOpen = (Boolean) method.invoke(cm, argObject);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return isOpen;
    }

    /**
     * @param radiogroup
     * @return 获得当前选中的序号，没选中，返回-1
     */
    public static int getCheckedPositionInRadioGroup(RadioGroup radiogroup) {
        int id = radiogroup.getCheckedRadioButtonId();
        if (id != -1)
            return radiogroup.indexOfChild(radiogroup.findViewById(id));
        return -1;
    }

    /**
     * 去除字符串中非数字的字符
     *
     * @param str
     * @return
     */
    public static String wipeOffUnDigitChar(String str) {
        if (TextUtils.isEmpty(str))
            return null;

        StringBuffer sb = new StringBuffer(str.length());
        for (int i = 0; i < str.length(); ++i) {
            char c = str.charAt(i);
            if ((c >= '0' && c <= '9') || c == '-' || c == '.')
                sb.append(c);
        }
        return sb.toString();
    }

    public static float parse2Float(String str) {
        float result = 0;
        String temp = wipeOffUnDigitChar(str);

        if (!TextUtils.isEmpty(temp))
            temp = wipeOffUnDigitChar(temp);
        if (!TextUtils.isEmpty(temp))
            result = Float.parseFloat(temp);

        return result;
    }

    public static double parse2Double(String str) {
        double result = 0;
        String temp = wipeOffUnDigitChar(str);

        if (!TextUtils.isEmpty(temp))
            temp = wipeOffUnDigitChar(temp);
        if (!TextUtils.isEmpty(temp))
            result = Double.parseDouble(temp);

        return result;
    }

    /**
     * 转换为数字，转换失败，为默认数
     *
     * @param str
     * @param defaultInt
     * @return
     */
    public static int parse2Integer(String str, int defaultInt) {
        try {
            defaultInt = Integer.parseInt(str);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return defaultInt;
    }

    /**
     * 转换为数字，转换失败，为默认数
     *
     * @param str
     * @param defaultInt
     * @return
     */
    public static Long parse2Long(String str, long defaultInt) {
        if (str != null)
            try {
                defaultInt = Long.parseLong(str);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        return defaultInt;
    }

    /**
     * 获取前N+1个月的显示格式字符串
     *
     * @param pastNum 前几个月。0为前一个月
     * @return 显示格式为yyyyMM
     */
    public static String getMonthPast(int pastNum) {
        return getMonthPast("%04d%02d", pastNum);
    }

    /**
     * @param format  仅限年月两个属性
     * @param pastNum
     * @return 获取前N+1个月的显示格式字符串
     */
    public static String getMonthPast(String format, int pastNum) {

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);

        month -= pastNum;
        if (month < 0) {
            year--;
            month += 12;
        }

        return String.format(format, year, month + 1);
    }

    /**
     * @param str
     * @return 判断是否包含中文字符
     */
    public static boolean isContainChinese(String str) {

        Pattern p = Pattern.compile("[\u4e00-\u9fa5]");
        Matcher m = p.matcher(str);
        if (m.find()) {
            return true;
        }
        return false;
    }

    /**
     * 判断是否是数字
     *
     * @param str
     * @return
     */
    public static boolean isNumber(String str) {
        Pattern p = Pattern.compile("^[0-9]*$");
        Matcher m = p.matcher(str);
        if (m.find()) {
            return true;
        }
        return false;
    }

    /**
     * 去除电话号码中不必要的字符
     *
     * @param number
     * @return
     */
    public static final String removeNoNecessaryWordsFromPhoneNumber(String number) {
        // 电话号码，去除不必要的字符
        if (number.startsWith("+86"))
            number = number.substring(3);

        if (!TextUtils.isDigitsOnly(number)) {
            StringBuilder sb = new StringBuilder();
            char[] chars = number.toCharArray();
            for (char c : chars)
                if (Character.isDigit(c)) {
                    sb.append(c);
                }

            number = sb.toString();
        }
        return number;
    }

    public static HashMap<String, String> stringToMap(String... params) {
        HashMap<String, String> map = new HashMap<String, String>();

        if (params != null) {
            for (String paramStr : params) {
                String[] paramArray = paramStr.split("&");
                if (paramArray.length > 1) {
                    map.put(paramArray[0], paramArray[1]);
                }
            }
        }

        return map;
    }

    public static boolean isOpenNetwork(Context context) {
        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connManager.getActiveNetworkInfo() != null) {
            return connManager.getActiveNetworkInfo().isAvailable();
        }
        return false;
    }

    /**
     * @param name
     * @return 从业务名称中删除资费代码
     */
    public static String deleteProdPrcidFromBusinessName(String name) {
        if (TextUtils.isEmpty(name))
            return name;

        int start = name.indexOf('[');
        int end = name.indexOf(']');

        if (start == -1 || end == -1 || start > end)
            return name;
        String temp = name.substring(start, end + 1);
        return name.replace(temp, "");
    }

    public static boolean isChineseMobile(String phoneNum) {
        if (TextUtils.isEmpty(phoneNum.trim()) || phoneNum.trim().length() != 11) {
            return false;
        } else {
            String[] chinaMobile = {"188", "147", "152", "138", "151", "137", "158", "134", "135", "136", "178", "184", "187", "183", "139", "150", "182",
                    "157", "159", "156", "153"};
            String temp = phoneNum.trim().substring(0, 3);
            for (int i = 0; i < chinaMobile.length; i++) {
                if (chinaMobile[i].equals(temp)) {
                    return true;
                }
            }
            return false;
        }
    }

    private static long lastClickTime;

    /**
     * 功能： 防止快速双次点击
     *
     * @return 返回是否可进行二次操作
     * <p>
     * 使用方法：public void onClick(View v) { if (Utils.isFastDoubleClick())
     * {return;}}
     */
    public static boolean isFastDoubleClick() {
        long time = System.currentTimeMillis();
        long timeD = time - lastClickTime;
        if (0 < timeD && timeD < 500) {

            return true;

        }
        lastClickTime = time;
        return false;

    }

    public static String getUniqueID(Context context) {
        String m_szDevIDShort = "35" + Build.BOARD.length() % 10 + Build.BRAND.length() % 10 + Build.CPU_ABI.length() % 10 + Build.DEVICE.length() % 10
                + Build.DISPLAY.length() % 10 + Build.HOST.length() % 10 + Build.ID.length() % 10 + Build.MANUFACTURER.length() % 10 + Build.MODEL.length()
                % 10 + Build.PRODUCT.length() % 10 + Build.TAGS.length() % 10 + Build.TYPE.length() % 10 + Build.USER.length() % 10;
        String uID = m_szDevIDShort + getAppUUID(context);

        // 13 digits
        return m_szDevIDShort;
    }

    public synchronized static String getAppUUID(Context context) {
        String sID = null;
        final String INSTALLATION = "INSTALLATION";
        if (sID == null) {
            File installation = new File(context.getFilesDir(), INSTALLATION);
            try {
                if (!installation.exists())
                    writeInstallationFile(installation);
                sID = readInstallationFile(installation);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return sID;
    }

    private static String readInstallationFile(File installation) throws IOException {
        RandomAccessFile f = new RandomAccessFile(installation, "r");
        byte[] bytes = new byte[(int) f.length()];
        f.readFully(bytes);
        f.close();
        return new String(bytes);
    }

    private static void writeInstallationFile(File installation) throws IOException {
        FileOutputStream out = new FileOutputStream(installation);
        String id = UUID.randomUUID().toString();
        out.write(id.getBytes());
        out.close();
    }

    public static String getCpuID() {
        String str = "", strCPU = "", cpuAddress = "0000000000000000";
        try {
            // 读取CPU信息
            Process pp = Runtime.getRuntime().exec("cat /procuinfo");
            InputStreamReader ir = new InputStreamReader(pp.getInputStream());
            LineNumberReader input = new LineNumberReader(ir);
            // 查找CPU序列号
            for (int i = 1; i < 100; i++) {
                str = input.readLine();
                if (str != null) {
                    // 查找到序列号所在行
                    if (str.indexOf("Serial") > -1) {
                        // 提取序列号
                        strCPU = str.substring(str.indexOf(":") + 1, str.length());
                        // 去空格
                        cpuAddress = strCPU.trim();
                        break;
                    }
                } else {
                    // 文件结尾
                    break;
                }
            }
        } catch (IOException ex) {
            // 赋予默认值
            ex.printStackTrace();
        }
        return cpuAddress;

    }

    public static String getAppsign(Context context) {
        return getSingInfo3(context);
    }

    private static String getAppSign1(Context context) {
        StringBuilder builder = new StringBuilder();
        try {
            /** 通过包管理器获得指定包名包含签名的包信息 **/
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_SIGNATURES);
            /******* 通过返回的包信息获得签名数组 *******/
            Signature[] signatures = packageInfo.signatures;
            /******* 循环遍历签名数组拼接应用签名 *******/
            for (Signature signature : signatures) {
                builder.append(signature.toCharsString());
            }
            /************** 得到应用签名 **************/
            return builder.toString();
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return "未获取到MD5";
    }

    private static String getAppSign2(Context context) {

        InputStream fis;
        byte[] buffer = new byte[1024];
        int numRead = 0;
        MessageDigest md5;
        String test = "";
        try {
            fis = new FileInputStream(context.getPackageResourcePath());
            md5 = MessageDigest.getInstance("MD5");
            while ((numRead = fis.read(buffer)) > 0) {
                md5.update(buffer, 0, numRead);
            }
            fis.close();
            test = toHexString(md5.digest());
        } catch (Exception e) {
            e.printStackTrace();
        }
        test.toString();
        return test.toString();
    }

    private static final char HEX_DIGITS[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    private static String toHexString(byte[] b) {
        StringBuilder sb = new StringBuilder(b.length * 2);
        for (int i = 0; i < b.length; i++) {
            sb.append(HEX_DIGITS[(b[i] & 0xf0) >>> 4]);
            sb.append(HEX_DIGITS[b[i] & 0x0f]);
        }
        return sb.toString();
    }

    private static String getSingInfo3(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_SIGNATURES);
            Signature[] signs = packageInfo.signatures;
            Signature sign = signs[0];
            return parseSignature(sign.toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String parseSignature(byte[] signature) {
        try {
            CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
            X509Certificate cert = (X509Certificate) certFactory.generateCertificate(new ByteArrayInputStream(signature));
            String pubKey = cert.getPublicKey().toString();
            String signNumber = cert.getSerialNumber().toString();
            // System.out.println("pubKey:" + pubKey);
            // System.out.println("signNumber:" + signNumber);
            return pubKey;
        } catch (CertificateException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static int getApkVersion(Context context) {
        try {
            return context.getPackageManager().getPackageInfo(context.getApplicationContext().getPackageName(), 0).versionCode;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static String getApkVersionName(Context context) {
        try {
            return context.getPackageManager().getPackageInfo(context.getApplicationContext().getPackageName(), 0).versionName;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return "0.0";
    }

    public static boolean isApkInstalled(Context context, String string) {
        PackageManager pm = context.getPackageManager();// 通过这个application得到PackageManager
        // 找在launch中注册了的应用 pm.getInstalledApplications 可以返回所有的列表
        Intent intent = pm.getLaunchIntentForPackage(string);
        if (intent == null) {// 这个intent如果为空，那就是代表没有找到指定包名的程序。
            return false;
        }
        return true;
    }

    public static boolean isMobilePhone(String number) {
        return number.matches("^((13[4-9])|(147)|(15[^4,^5,\\D])|(178)|(18[2-4,7-8]))\\d{8}$");
    }

    public static boolean isPhoneNumber(String number) {
        return number.matches("^((13[0-9])|(14[0-9])|(15[0-9])|(17[0-9])|(18[0-9]))\\d{8}$");
    }


    public static void exit(Context context) {
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    public static String getWeekName(String week) {
        Pattern p = Pattern.compile("[^0-7]");
        int num = Integer.parseInt(p.matcher(week).replaceAll(""));
        final String[] weeknames = {"一", "二", "三", "四", "五", "六", "日"};
        return "周" + weeknames[num - 1];
    }


    /**
     * compare two object
     *
     * @param a
     * @param b
     * @return
     */
    public static boolean equals(Object a, Object b) {
        return (a == b) || (a != null && a.equals(b));
    }

    /**
     * 关闭输入法
     *
     * @param context
     */
    public static void closeIME(Activity context) {
        if (context != null && context.getCurrentFocus() != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(context.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    public static byte[] toByteMac(String macString) {
        byte[] mac = new byte[6];
        String[] macOct = macString.split(":");

        for (int i = 0; i < macOct.length; i++) {
            mac[i] = (byte) (Integer.parseInt(macOct[i], 16) & 0xFF);
        }

        return mac;
    }

    public static String getMacString(byte[] mac) {
        StringBuilder strMac = new StringBuilder();
        for (byte oct : mac) {
            strMac.append(String.format("%02X", oct & 0xFF)).append(":");
        }
        strMac.deleteCharAt(strMac.length() - 1);
        return strMac.toString();
    }

    public static boolean isWiFiConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        if (ni != null && ni.getState() == NetworkInfo.State.CONNECTED)
            return true;
        return false;
    }

    public static String getMacAddr() {
        try {
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                if (!nif.getName().equalsIgnoreCase("wlan0")) continue;
                byte[] macBytes = nif.getHardwareAddress();
                if (macBytes == null) {
                    return "";
                }
                StringBuilder res1 = new StringBuilder();
                for (byte b : macBytes) {
                    res1.append(String.format("%02X:", b));
                }
                if (res1.length() > 0) {
                    res1.deleteCharAt(res1.length() - 1);
                }
                return res1.toString();
            }
        } catch (Exception ex) {
        }
        return "02:00:00:00:00:00";
    }

    public static String toStrIp(int ip) {
        final int[] mask = {0x000000FF, 0x0000FF00, 0x00FF0000, 0xFF000000};
        //final int[] mask = { 0xFF000000, 0x00FF0000, 0x0000FF00, 0x000000FF};
        final StringBuilder ipAddress = new StringBuilder();
        for (int i = 0; i < mask.length; i++) {
            //Log.d("www:::", Integer.toString((ip & mask[mask.length - 1 - i]) >>> ((mask.length - 1 - i)* 8)) );
            ipAddress.insert(0, (int) ((ip & mask[mask.length - 1 - i]) >>> ((mask.length - 1 - i) * 8)));
            if (i < mask.length - 1) {
                ipAddress.insert(0, ".");
            }
        }
        return ipAddress.toString();

    }


    /**
     * Created by fuheng on 2017/12/13.
     */

    public static final int toInt(String s) {
        return toInt(s, 0);
    }

    public static final int toInt(String s, int defaultValue) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static double dbm2Distance(double dbm) {
        return Math.pow(Math.E, (dbm + 49.53) / -17.7);
    }

    public static double distance2dbm(double distance) {
        return Math.log(distance) * -17.7 - 49.53;
    }

    /**
     * 修改状态栏为全透明
     *
     * @param activity
     */
    public static void transparencyBar(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = activity.getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                    | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        }
    }

    public static Drawable tintDrawable(Drawable drawable, @ColorInt int color) {
        final Drawable wrappedDrawable = DrawableCompat.wrap(drawable.mutate());
        DrawableCompat.setTint(wrappedDrawable, color);
        return wrappedDrawable;
    }

    public static Drawable tintDrawable(Drawable drawable, int[] colors, int[][] states) {
        ColorStateList colorStateList = new ColorStateList(states, colors);
        StateListDrawable stateListDrawable = new StateListDrawable();
        for (int i = 0; i < states.length; i++) {
            stateListDrawable.addState(states[i], drawable);
        }
        Drawable.ConstantState state = stateListDrawable.getConstantState();
        drawable = DrawableCompat.wrap(state == null ? stateListDrawable : state.newDrawable()).mutate();
        DrawableCompat.setTintList(drawable, colorStateList);
        return drawable;
    }
    //是否连接WIFI

    public static boolean isWifiConnected(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiNetworkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (wifiNetworkInfo.isConnected()) {
            return true;
        }

        return false;
    }

    /**
     * 获取屏幕任务栏高度
     */
    public static int getStatusBarHeight(Activity activity) {
        Rect rectangle = new Rect();
        activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(rectangle);
        System.out.println("WangJ : ==== ~ 状态栏-方法3:" + rectangle.toString());
        return rectangle.top;
    }

    public static short getShort(byte[] data, int offset) {
        short result = data[offset];
        result <<= 8;
        result |= data[offset + 1];
        return result;
    }

    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * @param context
     * @return 判断是否是debug版本
     */
    public static boolean isDebugApp(Context context) {
        try {
            ApplicationInfo info = context.getApplicationInfo();
            return (info.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
        } catch (Exception x) {
            return false;
        }
    }
}
