package com.changhong.wifimng.task;

import android.os.AsyncTask;


public abstract class GenericTask extends AsyncTask<TaskParams, Object, TaskResult> {
    private Exception exception;
    private int id = -1;
    private TaskListener mListener = null;

    abstract protected TaskResult _doInBackground(TaskParams... params);

    public void setListener(TaskListener taskListener) {
        mListener = taskListener;
    }

    public TaskListener getListener() {
        return mListener;
    }

    public void doPublishProgress(Object... values) {
        super.publishProgress(values);
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();

        if (mListener != null) {
            mListener.onCancelled(this);
        }

    }

    @Override
    protected void onPostExecute(TaskResult result) {
        super.onPostExecute(result);

        if (mListener != null) {
            try {
                mListener.onPostExecute(this, result);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        if (mListener != null) {
            try {
                mListener.onPreExecute(this);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onProgressUpdate(Object... values) {
        super.onProgressUpdate(values);

        if (mListener != null) {
            if (values != null && values.length > 0) {
                try {
                    mListener.onProgressUpdate(this, values[0]);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    protected TaskResult doInBackground(TaskParams... params) {
        return _doInBackground(params);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Exception getException() {
        return exception;
    }

    public void setException(Exception exception) {
        this.exception = exception;
    }

    protected void sleep(long delay) {
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
