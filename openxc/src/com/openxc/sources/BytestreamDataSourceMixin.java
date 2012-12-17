package com.openxc.sources;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import android.util.Log;

import com.google.common.primitives.Bytes;

/**
 * A "mixin" of sorts to be used with object composition, this contains
 * functionality common to data sources that received streams of bytes.
 */
public class BytestreamDataSourceMixin {
    private final static String TAG = "BytestreamDataSourceMixin";
    private double mBytesReceived = 0;
    private double mLastLoggedTransferStatsAtByte = 0;
    private final long mStartTime = System.nanoTime();
    private BufferedReader mReader;
    private BlockingQueue<Byte> mCharacterQueue =
        new ArrayBlockingQueue<Byte>(512000);


    public BytestreamDataSourceMixin() {
         mReader = new BufferedReader(new InputStreamReader(
                     new StringBackedInputStream()));
    }

    /**
     * Add additional bytes to the buffer from the data source.
     *
     * @param bytes an array of bytes received from the interface.
     * @param length number of bytes received, and thus the amount that should
     *      be read from the array.
     */
    public void receive(byte[] bytes, int length) {
        try {
            mCharacterQueue.addAll(
                    Bytes.asList(Arrays.copyOfRange(bytes, 0, length)));
        } catch(IllegalStateException e) {
            Log.w(TAG, "Dropping incoming bytes, queue is full");
            return;
        }

        mBytesReceived += length;

        logTransferStats();
    }

    private class StringBackedInputStream extends InputStream {
        public int read() {
            try {
                return mCharacterQueue.take();
            } catch(InterruptedException e) {
                Log.d(TAG, "Interrupted while waiting for a new character");
                return 0;
            }
        }

        public int read(byte[] buffer, int offset, int length) {
            List<Byte> dump = new ArrayList<Byte>();
            try {
                // use take() so we block until new elements are added
                dump.add(mCharacterQueue.take());
            } catch(InterruptedException e) {
                Log.d(TAG, "Interrupted while waiting for new characters");
                return -1;
            }

            mCharacterQueue.drainTo(dump, length - offset - 1);
            byte[] result = Bytes.toArray(dump);
            System.arraycopy(result, 0, buffer, offset, result.length);
            return result.length;
        }
    }

    public String readLine() {
        try {
            return mReader.readLine();
        } catch(IOException e) {
            return null;
        }
    }

    private void logTransferStats() {
        // log the transfer stats roughly every 1MB
        if(mBytesReceived > mLastLoggedTransferStatsAtByte + 1024 * 1024) {
            mLastLoggedTransferStatsAtByte = mBytesReceived;
            SourceLogger.logTransferStats(TAG, mStartTime, System.nanoTime(),
                    mBytesReceived);
        }
    }

}
