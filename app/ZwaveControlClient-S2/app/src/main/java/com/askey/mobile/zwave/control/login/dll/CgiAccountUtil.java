package com.askey.mobile.zwave.control.login.dll;

import com.askey.mobile.zwave.control.util.Logg;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * 项目名称：ZwaveControlClient
 * 类描述：
 * 创建人：skysoft  charles.bai
 * 创建时间：2018/1/5 10:54
 * 修改人：skysoft
 * 修改时间：2018/1/5 10:54
 * 修改备注：
 */
public class CgiAccountUtil {
    private static final String LOG_TAG = CgiAccountUtil.class.getSimpleName();


    public static String saveAccountToLocal(String serverIp, String emailText, String passwordText) {
        String rec_string = "";//返回值
//        String address = "http://host:9999/settingCGI.fcgi";//URL地址
        Logg.i(LOG_TAG,"==saveAccountToLocal=serverIp="+serverIp);
        String address =  "http://"+serverIp+":9999/rtCGI.fcgi";
//        String address =  "http://"+"192.168.1.21"+":9999/rtCGI.fcgi";
        Logg.i(LOG_TAG,"==saveAccountToLocal=address="+address);
        String commString = getCommStr(emailText,passwordText);//参数字符串
        Logg.i(LOG_TAG, "===saveAccountToLocal===commString=="+commString);
        URL url = null;
        HttpURLConnection urlConn = null;
        try {
            /* 得到url地址的URL类 */
            url = new URL(address);
            /* 获得打开需要发送的url连接 */
            urlConn = (HttpURLConnection) url.openConnection();
            /* 设置连接超时时间 */
            urlConn.setConnectTimeout(30000);
            /* 设置读取响应超时时间 */
            urlConn.setReadTimeout(30000);
            /* 设置post发送方式 */
            urlConn.setRequestMethod("POST");
            /* 发送commString */
            urlConn.setDoOutput(true);
            urlConn.setDoInput(true);
            OutputStream out = urlConn.getOutputStream();
            out.write(commString.getBytes());
            out.flush();
            out.close();
            /* 发送完毕 获取返回流，解析流数据 */
            BufferedReader rd = new BufferedReader(new InputStreamReader(urlConn.getInputStream(), "UTF-8"));
            StringBuffer sb = new StringBuffer();
            int ch;
            while ((ch = rd.read()) > -1) {
                sb.append((char) ch);
            }
            rec_string = sb.toString().trim();
            /* 解析完毕关闭输入流 */
            rd.close();
        } catch (Exception e) {
            /* 异常处理 */
            Logg.e(LOG_TAG,"saveAccountToLocal-> Exception="+e.getMessage());
            return  "";
        } finally {
            if (urlConn != null) {
                /* 关闭URL连接 */
                urlConn.disconnect();
            }
        }
        /* 返回响应内容 */
        return rec_string;
    }


    public static String goLocalLogin(String serverIp, String emailText, String passwordText) {
        String rec_string = "";//返回值
//        String address = "http://host:9999/settingCGI.fcgi";//URL地址
        Logg.i(LOG_TAG,"==goLocalLogin=serverIp="+serverIp);
        String address =  "http://"+serverIp+":9999/rtCGI.fcgi";
//        String address =  "http://"+"192.168.1.21"+":9999/rtCGI.fcgi";
        Logg.i(LOG_TAG,"==goLocalLogin=address="+address);
        String commString = getCGILocalLoginCommStr(emailText,passwordText);//参数字符串
        Logg.i(LOG_TAG, "===goLocalLogin===commString=="+commString);
        URL url = null;
        HttpURLConnection urlConn = null;
        try {
            /* 得到url地址的URL类 */
            url = new URL(address);
            /* 获得打开需要发送的url连接 */
            urlConn = (HttpURLConnection) url.openConnection();
            /* 设置连接超时时间 */
            urlConn.setConnectTimeout(30000);
            /* 设置读取响应超时时间 */
            urlConn.setReadTimeout(30000);
            /* 设置post发送方式 */
            urlConn.setRequestMethod("POST");
            /* 发送commString */
            urlConn.setDoOutput(true);
            urlConn.setDoInput(true);
            OutputStream out = urlConn.getOutputStream();
            out.write(commString.getBytes());
            out.flush();
            out.close();
            /* 发送完毕 获取返回流，解析流数据 */
            BufferedReader rd = new BufferedReader(new InputStreamReader(urlConn.getInputStream(), "UTF-8"));
            StringBuffer sb = new StringBuffer();
            int ch;
            while ((ch = rd.read()) > -1) {
                sb.append((char) ch);
            }
            rec_string = sb.toString().trim();
            /* 解析完毕关闭输入流 */
            rd.close();
        } catch (Exception e) {
            /* 异常处理 */
            Logg.e(LOG_TAG,"goLocalLogin-> Exception="+e.getMessage());
            return "";
        } finally {
            if (urlConn != null) {
                /* 关闭URL连接 */
                urlConn.disconnect();
            }
        }
        /* 返回响应内容 */
        return rec_string;
    }

    //    {
//        "Type":2,
//            "CmdID":18,
//            "Data":
//        {
//            "ID":"String",
//                "Password":"String"
//        }
//    }
    public static String getCGILocalLoginCommStr(String emailText, String passwordText) {
        String result = "";
        JSONObject dataStr = new JSONObject();
        JSONObject parameter = new JSONObject();
        try {
            dataStr.put("ID", emailText);
            dataStr.put("Password", passwordText);
            parameter.put("Type", 2);
            parameter.put("CmdID", 18);
            parameter.put("Data", dataStr);
        } catch (JSONException e) {
            e.printStackTrace();
            Logg.e(LOG_TAG,"getCommStr-> JSONException="+e.getMessage());
        }

        return parameter.toString();
    }


    //    {
//        "Type":2,
//            "CmdID":17,
//            "Data":
//        {
//            "Create":1,
//                "ID":"String",
//                "Password":"String"
//        }
//    }
    private static String getCommStr(String emailText, String passwordText) {
        String result = "";
        JSONObject dataStr = new JSONObject();
        JSONObject parameter = new JSONObject();
        try {
            dataStr.put("Create", 1);
            dataStr.put("ID", emailText);
            dataStr.put("Password", passwordText);
            parameter.put("Type", 2);
            parameter.put("CmdID", 17);
            parameter.put("Data", dataStr);
        } catch (JSONException e) {
            e.printStackTrace();
            Logg.e(LOG_TAG,"getCommStr-> JSONException="+e.getMessage());
        }

        return parameter.toString();
    }



}
