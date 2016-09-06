package me.himanshusoni.quantumflux.model.loader.support;

import android.content.Context;
import android.database.Cursor;
import android.os.Handler;
import android.os.Message;
import android.view.View;

import java.lang.ref.SoftReference;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import me.himanshusoni.quantumflux.logger.QuantumFluxLog;
import me.himanshusoni.quantumflux.model.util.QuantumFluxCursor;

public abstract class QuantumFluxAsyncCursorAdaptor<T, K> extends QuantumFluxCursorAdaptor<T, K> {

    private final Lock mLock = new ReentrantLock();
    private final Condition mCondition = mLock.newCondition();
    private final Queue<T> mLoaderQueue = new LinkedList<T>();
    private final LoaderThreadHandler mLoaderThreadHandler = new LoaderThreadHandler(this);
    private LoaderThread mLoaderThread;

    public QuantumFluxAsyncCursorAdaptor(Context context, int layoutId) {
        super(context, layoutId);
    }

    public QuantumFluxAsyncCursorAdaptor(Context context, Cursor c, int layoutId) {
        super(context, c, layoutId);
    }

    public QuantumFluxAsyncCursorAdaptor(Context context, Cursor c, int layoutId, int flags) {
        super(context, c, layoutId, flags);
    }

    @Override
    public void changeCursor(Cursor cursor) {
        dispose();
        super.changeCursor(cursor);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        startLoaderThreadIfStopped();
        mLock.lock();
        T inflate = ((QuantumFluxCursor<T>) getCursor()).inflate();
        if (!mLoaderQueue.contains(inflate))
            mLoaderQueue.offer(inflate);
        mCondition.signal();
        mLock.unlock();
        super.bindView(view, context, cursor);
    }

    private void dispose() {
        mLock.lock();
        if (mLoaderThread != null) {
            mLoaderThread.isCancelled = true;
            mLoaderThread = null;
        }
        mCondition.signal();
        mLock.unlock();
    }

    private void startLoaderThreadIfStopped() {
        if (mLoaderThread == null) {
            mLoaderThread = new LoaderThread();
            mLoaderThread.start();
        }
    }

    public abstract boolean loadAsyncInformation(T information);

    private class LoaderThread extends Thread {
        volatile boolean isCancelled = false;
        QuantumFluxCursor<T> mCursor = (QuantumFluxCursor<T>) getCursor();

        @Override
        public void run() {

            setName(QuantumFluxAsyncCursorAdaptor.class.getSimpleName() + "_Loader");
            try {
                while (isStillValid()) {

                    mLock.lock();
                    while (mLoaderQueue.peek() == null && isStillValid()) {
                        mCondition.await();
                    }

                    if (!isStillValid()) {
                        mLock.unlock();
                        break;
                    }

                    T item = mLoaderQueue.poll();
                    mLock.unlock();
                    if (loadAsyncInformation(item))
                        mLoaderThreadHandler.sendEmptyMessage(0);
                }
            } catch (InterruptedException e) {
                QuantumFluxLog.e("Failed while waiting for items", e);
            }
        }

        private boolean isStillValid() {
            return mCursor != null && !mCursor.isClosed() && !isCancelled;
        }
    }

    private static class LoaderThreadHandler extends Handler {

        private final SoftReference<QuantumFluxCursorAdaptor> adaptorReference;

        LoaderThreadHandler(QuantumFluxCursorAdaptor adaptor) {
            this.adaptorReference = new SoftReference<QuantumFluxCursorAdaptor>(adaptor);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            QuantumFluxCursorAdaptor adaptor = adaptorReference.get();
            if (adaptor != null) adaptor.notifyDataSetChanged();
        }
    }
}
