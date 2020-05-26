package com.example.flat.Common;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.example.flat.Model.User;

import java.text.DecimalFormat;
import java.util.Calendar;

public class Common {

    public static final String DELETE = "Delete";
    public static final String UPDATE = "Update" ;
    public static User currentUser;

    public static final String USER_KEY = "User" ;
    public static final String PWD_KEY = "Password" ;

    public static boolean isConnectedToInternet(Context context){
        ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if(connectivityManager != null ){
            NetworkInfo[] info = connectivityManager.getAllNetworkInfo();
            if(info!=null){
                for(int i=0; i<info.length;i++){
                    if(info[i].getState() == NetworkInfo.State.CONNECTED)
                        return true;
                }
            }
        }
        return false;
    }

    public static String convertCodeToStatus(boolean code) {
        if(code){
            return "YES";
        }else{
            return "NO";
        }
    }

    public static String convertCodeToStatus2(String code) {
        if(code.equals("0"))
            return "Pending";
        else if(code.equals("1"))
            return "Received";
        else if(code.equals("2"))
            return "Confirm";
        else
            return "Not Applicable";
    }

    public static String convertCodeToPayment(String code) {
        if(code.equals("0"))
            return "Cash";
        else if(code.equals("1"))
            return "Paytm/Online";
        else
            return "Not Applicable";
    }

    public static String convertCodeToMonth(String code){
        if(code.equals("01")){
            return "JAN";
        } else if(code.equals("02")){
            return "FEB";
        }else if(code.equals("03")){
            return "MAR";
        }else if(code.equals("04")){
            return "APR";
        }else if(code.equals("05")){
            return "MAY";
        }else if(code.equals("06")){
            return "JUN";
        }else if(code.equals("07")){
            return "JUL";
        }else if(code.equals("08")){
            return "AUG";
        }else if(code.equals("09")){
            return "SEP";
        }else if(code.equals("10")){
            return "OCT";
        }else if(code.equals("11")){
            return "NOV";
        }else {
            return "DEC";
        }
    }

    public static String getKeyFormat(int thisMonth, int thisYear) {
        String key = String.format(new DecimalFormat("00").format(thisMonth)) + new DecimalFormat("0000").format(thisYear);
        return key;
    }

    public static int getCurrentYear() {
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);
        return currentYear;
    }

    public static int getCurrentMonth() {
        Calendar calendar = Calendar.getInstance();
        int currentMonth = calendar.get(Calendar.MONTH);
        return currentMonth;
    }
}
