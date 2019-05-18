package com.example.michi.myapplication;

import com.orbbec.astra.Astra;
import com.orbbec.astra.DepthFrame;
import com.orbbec.astra.DepthStream;
import com.orbbec.astra.ImageStreamMode;
import com.orbbec.astra.PointFrame;
import com.orbbec.astra.PointStream;
import com.orbbec.astra.ReaderFrame;
import com.orbbec.astra.StreamReader;
import com.orbbec.astra.StreamSet;
import com.orbbec.astra.Vector3D;
import com.orbbec.astra.android.AstraAndroidContext;

import android.content.Context;
import android.util.Log;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class OrbbecCamAndroid {
    private static final int updateSleepMS = 100;
    private static final String logTagFrame = "ASTRA_FRAME";
    private static final String logTagPointStream = "ASTRA_POINT";
    private static final String logTagDepthStream = "ASTRA_DEPTH";
    private boolean first_call = true;
    private Context applicationContext;
    private AstraAndroidContext aac;
    private int width;
    private int height;

    public OrbbecCamAndroid(Context applicationContext, int width, int height) {
        this.applicationContext = applicationContext;
        this.width = width;
        this.height = height;

        aac = new AstraAndroidContext(this.applicationContext);
        aac.initialize();
        aac.openAllDevices();
    }

    public ArrayList<Vector3D> get3DVectors() {
        final boolean[] frameFinished = {false};
        final ArrayList<Vector3D> vector3DList = new ArrayList<>();

        // Call depth stream once to set resoultion. Can't set resolution via point stream.
        if (first_call) {
            getDepthData();
            first_call = false;
        }

        try {
            Log.d(logTagPointStream, "Trying to open stream");
            StreamSet streamSet = StreamSet.open();
            StreamReader reader = streamSet.createReader();

            reader.addFrameListener(new StreamReader.FrameListener() {
                public void onFrameReady(StreamReader reader, ReaderFrame frame) {
                    PointFrame pf = PointFrame.get(frame);
                    FloatBuffer buffer = pf.getPointBuffer();

                    if (pf.isValid()) {
                        Log.d(logTagFrame, "frame is valid");
                        Log.d(logTagFrame, "height: " + pf.getHeight());
                        Log.d(logTagFrame, "width: " + pf.getWidth());

                        while (buffer.hasRemaining()) {
                            vector3DList.add(new Vector3D(buffer.get(), buffer.get(), buffer.get() * -1));
                        }
                        frameFinished[0] = true;
                    }
                }
            });

            PointStream pointStream = PointStream.get(reader);
            pointStream.start();

            while (!frameFinished[0]) {
                Astra.update();
                TimeUnit.MILLISECONDS.sleep(updateSleepMS);
            }

            pointStream.stop();
            reader.destroy();
            streamSet.close();
        } catch (Throwable e) {
            Log.e(logTagPointStream, e.toString());
        }

        Log.d(logTagPointStream, "size of list: " + vector3DList.size());

        return vector3DList;
    }

    public ArrayList<Vector3D> getDepthData() {
        final boolean[] frameFinished = {false};
        final ArrayList<Vector3D> vector3DList = new ArrayList<>();

        try {
            Log.d(logTagDepthStream, "Trying to open stream");
            StreamSet streamSet = StreamSet.open();
            StreamReader reader = streamSet.createReader();

            reader.addFrameListener(new StreamReader.FrameListener() {

                public void onFrameReady(StreamReader reader, ReaderFrame frame) {
                    DepthFrame df = DepthFrame.get(frame);
                    ShortBuffer buffer = df.getDepthBuffer();

                    if (df.isValid()) {
                        Log.d(logTagFrame, "frame is valid");
                        Log.d(logTagFrame, "height: " + df.getHeight());
                        Log.d(logTagFrame, "width: " + df.getWidth());

                        while (buffer.hasRemaining()) {
                            //TODO Check if data looks like below data
                            vector3DList.add(new Vector3D(
                                    buffer.get(),
                                    buffer.get(),
                                    buffer.get() * -1));
                        }
                        frameFinished[0] = true;
                    }
                }
            });

            DepthStream depthStream = DepthStream.get(reader);
            depthStream.setMode(new ImageStreamMode(0, width, height, 100, 30));
            depthStream.start();

            while (!frameFinished[0]) {
                Astra.update();
                TimeUnit.MILLISECONDS.sleep(updateSleepMS);
            }

            depthStream.stop();
            reader.destroy();
            streamSet.close();
        } catch (Throwable e) {
            Log.e(logTagDepthStream, e.toString());
        }

        Log.d(logTagDepthStream, "size of list: " + vector3DList.size());

        return vector3DList;
    }

    public void closeCam() {
        aac.terminate();
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }
}