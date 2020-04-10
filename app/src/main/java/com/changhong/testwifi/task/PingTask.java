package com.changhong.testwifi.task;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.UnknownHostException;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.TimeoutException;

/**
 * @author fuheng
 * 异步处理ping
 */
public class PingTask extends AsyncTask<BaseBeen<Integer, String>, BaseBeen<String, PingResult>, PingResult> implements Observer {

    private OnCallback<PingResult, BaseBeen<String, PingResult>> callback;

    private long timeOutTime = 2000;
    private static long TIME_OUT_WAIT_FOR_STREAM_READY = 60000;
    private static final float DEFAULT_I_VALUE = 0.2f;

    private float mIValue = DEFAULT_I_VALUE;


    public PingTask execute(String url, int times, OnCallback<PingResult, BaseBeen<String, PingResult>> callback) {
        this.callback = callback;
        executeOnExecutor(THREAD_POOL_EXECUTOR, new BaseBeen(times, url));
        return this;
    }

    public PingTask execute(String url, int times, float i, OnCallback<PingResult, BaseBeen<String, PingResult>> callback) {
        this.callback = callback;
        mIValue = i;
        executeOnExecutor(THREAD_POOL_EXECUTOR, new BaseBeen(times, url));
        return this;
    }

    @Override
    protected PingResult doInBackground(BaseBeen<Integer, String>... params) {
        Log.d(getClass().getSimpleName(), "====~doInBackground ");
        Process p = null;
        BufferedReader stdInput = null;
        try {
            sleep(1000);
            String cmd = "ping -c " + params[0].t1 + " -s 32 -i 0.2 " + params[0].t2;

            p = Runtime.getRuntime().exec(cmd);

            stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));

            PingResult result = analysis(stdInput);
            return result;
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return new PingResult(1, e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            return new PingResult(2, e.getMessage());
        } catch (TimeoutException e) {
            e.printStackTrace();
            return new PingResult(3, e.getMessage());
        } finally {
            try {
                if (stdInput != null)
                    stdInput.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
            if (p != null)
                try {
                    p.destroy();
                } catch (Exception e) {
                    e.printStackTrace();
                }
        }
    }

    @Override
    protected void onPostExecute(PingResult result) {
        if (callback != null)
            callback.onCallback(result);
        super.onPostExecute(result);
    }

    @Override
    protected void onProgressUpdate(BaseBeen<String, PingResult>... values) {
        if (callback != null)
            callback.onStep(values[0]);
        super.onProgressUpdate(values);
    }

    @Override
    public void update(Observable o, Object arg) {
        publishProgress((BaseBeen<String, PingResult>) arg);
    }


    private PingResult analysis(BufferedReader bufferedReader) throws IOException, TimeoutException {
        PingResult pingResult = new PingResult();
        long startTime = System.currentTimeMillis();
        while (!bufferedReader.ready()) {
            sleep(500);
            if (System.currentTimeMillis() - startTime > TIME_OUT_WAIT_FOR_STREAM_READY) {
                throw new TimeoutException("Wait for imputStream over " + TIME_OUT_WAIT_FOR_STREAM_READY + " millisecounds");
            }
        }
        do {
            String temp = bufferedReader.readLine();
            Log.d(getClass().getSimpleName(), "====~ temp = " + temp);
            if (temp == null)
                break;

            if (temp.contains("unknown host")) {
                throw new UnknownHostException(temp);
            } else if (temp.contains("PING")) {
                pingResult.hostname = pingResult.readHostName(temp);
                pingResult.ip = pingResult.readIP(temp);
                publishProgress(new BaseBeen(temp, pingResult));
            } else if (temp.toLowerCase().contains("transmitted")) {
                pingResult.sendNum = pingResult.readPackageSendResult(temp);
                pingResult.receiveNum = pingResult.readPackageReceiveResult(temp);
                pingResult.lostRate = pingResult.readPackageLostRate(temp);
                publishProgress(new BaseBeen(temp, pingResult));
            } else if (temp.toLowerCase().contains("min/avg/max/mdev")) {
                float[] values = pingResult.readCostTime(temp);
                pingResult.minTime = values[0];
                pingResult.averageTime = values[1];
                pingResult.maxTime = values[2];
                pingResult.rangeTime = values[3];
                publishProgress(new BaseBeen(temp, pingResult));
            } else if (temp.toLowerCase().contains("from")) {
                pingResult.sendNum = pingResult.getIntValue(temp, "icmp_seq");
                if (temp.toLowerCase().contains("time=")) {
                    float time = Float.parseFloat(pingResult.getStringValue(temp, "time"));
                    pingResult.receiveNum++;
                    if (pingResult.minTime == 0)
                        pingResult.minTime = time;
                    else
                        pingResult.minTime = Math.min(pingResult.minTime, time);
                    pingResult.maxTime = Math.max(pingResult.maxTime, time);

                    pingResult.averageTime = (pingResult.averageTime * (pingResult.receiveNum - 1) + time) / pingResult.receiveNum;//readStepAveraageTime(temp.toLowerCase());

                } else if (temp.toLowerCase().contains("host unreachable")) {

                }
                publishProgress(new BaseBeen(temp, pingResult));
            }
        } while (!isCancelled());
        return pingResult;
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
