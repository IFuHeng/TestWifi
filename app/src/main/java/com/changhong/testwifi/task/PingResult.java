package com.changhong.testwifi.task;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Observer;

public class PingResult {
    String hostname;
    String ip;
    int sendNum;
    int receiveNum;
    int lostNum;
    int lostRate;
    float averageTime;
    float rangeTime;
    float minTime;
    float maxTime;
    int errorCode;
    String errorMsg;

    public PingResult() {
    }

    public PingResult(int errorCode, String errorMsg) {
        this.errorCode = errorCode;
        this.errorMsg = errorMsg;
    }

    @Override
    public String toString() {
        return "PingResult{" +
                "hostname='" + hostname + '\'' +
                ", ip='" + ip + '\'' +
                ", sendNum=" + sendNum +
                ", receiveNum=" + receiveNum +
                ", lostNum=" + lostNum +
                ", lostRate=" + lostRate +
                ", averageTime=" + averageTime +
                ", rangeTime=" + rangeTime +
                ", minTime=" + minTime +
                ", maxTime=" + maxTime +
                ", errorCode=" + errorCode +
                ", errorMsg='" + errorMsg + '\'' +
                '}';
    }

    public void init(BufferedReader bufferedReader, Observer... observers) throws IOException {
        do {
            String temp = bufferedReader.readLine();
            if (temp == null)
                break;
            Log.d(getClass().getSimpleName(), "====~ temp = " + temp);
            if (temp.contains("unknown host")) {
                throw new UnknownHostException(temp);
            } else if (temp.contains("PING")) {
                hostname = readHostName(temp);
                ip = readIP(temp);
            } else if (temp.toLowerCase().contains("transmitted")) {
                sendNum = readPackageSendResult(temp);
                receiveNum = readPackageReceiveResult(temp);
                lostRate = readPackageLostRate(temp);
            } else if (temp.toLowerCase().contains("min/avg/max/mdev")) {
                float[] values = readCostTime(temp);
                minTime = values[0];
                averageTime = values[1];
                maxTime = values[2];
                rangeTime = values[3];
            } else if (temp.toLowerCase().contains("from")) {
                sendNum = getIntValue(temp, "icmp_seq");
                if (temp.toLowerCase().contains("time=")) {
                    float time = Float.parseFloat(getStringValue(temp, "time"));
                    receiveNum++;
                    if (minTime == 0)
                        minTime = time;
                    else
                        minTime = Math.min(minTime, time);
                    maxTime = Math.max(maxTime, time);

                    averageTime = (averageTime * (receiveNum - 1) + time) / receiveNum;//readStepAveraageTime(temp.toLowerCase());

                } else if (temp.toLowerCase().contains("host unreachable")) {

                }

                if (observers != null)
                    for (Observer observer : observers) {
                        observer.update(null, this);
                    }
            }

        } while (true);
    }

    float readStepAveraageTime(String temp) {

        int start = temp.indexOf("icmp_seq=") + 9;
        int end = temp.indexOf(' ', start);
        int step = Integer.parseInt(temp.substring(start, end));

        start = temp.indexOf("time=") + 5;
        end = temp.indexOf(' ', start);
        float cur = Float.parseFloat(temp.substring(start, end));

        float result = averageTime * (step - 1) + cur;
        result /= step;
        return result;
    }

    int readPackageLostRate(String temp) {
        int end = temp.indexOf("% packet loss");
        int start = temp.lastIndexOf(' ', end);
        if (start == -1)
            start = 0;

        String sendNumStr = temp.substring(start, end).trim();
        int sendNum = Integer.parseInt(sendNumStr);
        return sendNum;
    }

    int readPackageSendResult(String temp) {
        int end = temp.indexOf(" packets transmitted");
        int start = temp.lastIndexOf(' ', end - 1);
        if (start == -1)
            start = 0;

        String sendNumStr = temp.substring(start, end).trim();
        int sendNum = Integer.parseInt(sendNumStr);
        return sendNum;
    }

    int readPackageReceiveResult(String temp) {
        int end = temp.indexOf(" received");
        int start = temp.lastIndexOf(' ', end - 1);
        if (start == -1)
            start = 0;

        String sendNumStr = temp.substring(start, end).trim();
        int sendNum = Integer.parseInt(sendNumStr);
        return sendNum;
    }

    String readHostName(String string) {
        int start = string.indexOf(' ') + 1;
        int end = string.indexOf(' ', start);
        return string.substring(start, end);
    }

    String readIP(String string) {
        int start = string.indexOf('(') + 1;
        int end = string.indexOf(')', start);
        return string.substring(start, end);
    }

    int getIntValue(String string, String key) {
        int start = string.indexOf(key) + key.length() + 1;
        int end = string.indexOf(' ', start);
        return Integer.parseInt(string.substring(start, end));
    }

    String getStringValue(String string, String key) {
        int start = string.indexOf(key) + key.length() + 1;
        int end = string.indexOf(' ', start);
        return string.substring(start, end);
    }

    float[] readCostTime(String string) {
        String[] list = string.substring(string.indexOf('=') + 1, string.lastIndexOf(" ms")).trim().split("/");
        float[] result = new float[list.length];
        for (int i = 0; i < list.length; i++) {
            result[i] = Float.parseFloat(list[i]);
        }
        return result;
    }

    public String getHostname() {
        return hostname;
    }

    public String getIp() {
        return ip;
    }

    public int getSendNum() {
        return sendNum;
    }

    public int getReceiveNum() {
        return receiveNum;
    }

    public int getLostNum() {
        return lostNum;
    }

    public int getLostRate() {
        return lostRate;
    }

    public float getAverageTime() {
        return averageTime;
    }

    public float getRangeTime() {
        return rangeTime;
    }

    public float getMinTime() {
        return minTime;
    }

    public float getMaxTime() {
        return maxTime;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public int getLostCount() {
        return sendNum - receiveNum;
    }
}
