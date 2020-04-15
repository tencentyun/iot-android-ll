package com.qrcode.utils;

/**
 * Created by yangyu on 17/10/18.
 */

import android.annotation.TargetApi;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Build;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Utility methods for configuring the Android camera.
 *
 * @author Sean Owen
 */
@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
public final class CameraConfigurationUtils {

    private static final String TAG = "CameraConfiguration";

    private static final Pattern SEMICOLON = Pattern.compile(";");

    private static final int MIN_PREVIEW_PIXELS = 480 * 320; // normal screen
    private static final float MAX_EXPOSURE_COMPENSATION = 1.5f;
    private static final float MIN_EXPOSURE_COMPENSATION = 0.0f;
    private static final double MAX_ASPECT_DISTORTION = 0.15;
    private static final int MIN_FPS = 10;
    private static final int MAX_FPS = 20;
    private static final int AREA_PER_1000 = 400;

    private CameraConfigurationUtils() {
    }

    public static void setFocus(Camera.Parameters parameters,
                                boolean autoFocus,
                                boolean disableContinuous,
                                boolean safeMode) {
        List<String> supportedFocusModes = parameters.getSupportedFocusModes();
        String focusMode = null;
        if (autoFocus) {
            if (safeMode || disableContinuous) {
                focusMode = findSettableValue("focus mode",
                        supportedFocusModes,
                        Camera.Parameters.FOCUS_MODE_AUTO);
            } else {
                focusMode = findSettableValue("focus mode",
                        supportedFocusModes,
                        Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE,
                        Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO,
                        Camera.Parameters.FOCUS_MODE_AUTO);
            }
        }
        // Maybe selected auto-focus but not available, so fall through here:
        if (!safeMode && focusMode == null) {
            focusMode = findSettableValue("focus mode",
                    supportedFocusModes,
                    Camera.Parameters.FOCUS_MODE_MACRO,
                    Camera.Parameters.FOCUS_MODE_EDOF);
        }
        if (focusMode != null) {
            if (focusMode.equals(parameters.getFocusMode())) {
                Log.i(TAG, "Focus mode already set to " + focusMode);
            } else {
                parameters.setFocusMode(focusMode);
            }
        }
    }

    public static void setTorch(Camera.Parameters parameters, boolean on) {
        List<String> supportedFlashModes = parameters.getSupportedFlashModes();
        String flashMode;
        if (on) {
            flashMode = findSettableValue("flash mode",
                    supportedFlashModes,
                    Camera.Parameters.FLASH_MODE_TORCH,
                    Camera.Parameters.FLASH_MODE_ON);
        } else {
            flashMode = findSettableValue("flash mode",
                    supportedFlashModes,
                    Camera.Parameters.FLASH_MODE_OFF);
        }
        if (flashMode != null) {
            if (flashMode.equals(parameters.getFlashMode())) {
                Log.i(TAG, "Flash mode already set to " + flashMode);
            } else {
                Log.i(TAG, "Setting flash mode to " + flashMode);
                parameters.setFlashMode(flashMode);
            }
        }
    }


    public static void setFocusArea(Camera.Parameters parameters) {
        if (parameters.getMaxNumFocusAreas() > 0) {
            Log.i(TAG, "Old focus areas: " + toString(parameters.getFocusAreas()));
            List<Camera.Area> middleArea = buildMiddleArea(AREA_PER_1000);
            Log.i(TAG, "Setting focus area to : " + toString(middleArea));
            parameters.setFocusAreas(middleArea);
        } else {
            Log.i(TAG, "Device does not support focus areas");
        }
    }

    public static void setMetering(Camera.Parameters parameters) {
        if (parameters.getMaxNumMeteringAreas() > 0) {
            Log.i(TAG, "Old metering areas: " + parameters.getMeteringAreas());
            List<Camera.Area> middleArea = buildMiddleArea(AREA_PER_1000);
            Log.i(TAG, "Setting metering area to : " + toString(middleArea));
            parameters.setMeteringAreas(middleArea);
        } else {
            Log.i(TAG, "Device does not support metering areas");
        }
    }

    private static List<Camera.Area> buildMiddleArea(int areaPer1000) {
        return Collections.singletonList(
                new Camera.Area(new Rect(-areaPer1000, -areaPer1000, areaPer1000, areaPer1000), 1));
    }

    public static void setVideoStabilization(Camera.Parameters parameters) {
        if (parameters.isVideoStabilizationSupported()) {
            if (parameters.getVideoStabilization()) {
                Log.i(TAG, "Video stabilization already enabled");
            } else {
                Log.i(TAG, "Enabling video stabilization...");
                parameters.setVideoStabilization(true);
            }
        } else {
            Log.i(TAG, "This device does not support video stabilization");
        }
    }

    public static void setBarcodeSceneMode(Camera.Parameters parameters) {
        if (Camera.Parameters.SCENE_MODE_BARCODE.equals(parameters.getSceneMode())) {
            Log.i(TAG, "Barcode scene mode already set");
            return;
        }
        String sceneMode = findSettableValue("scene mode",
                parameters.getSupportedSceneModes(),
                Camera.Parameters.SCENE_MODE_BARCODE);
        if (sceneMode != null) {
            parameters.setSceneMode(sceneMode);
        }
    }

    public static void setZoom(Camera.Parameters parameters, double targetZoomRatio) {
        if (parameters.isZoomSupported()) {
            Integer zoom = indexOfClosestZoom(parameters, targetZoomRatio);
            if (zoom == null) {
                return;
            }
            if (parameters.getZoom() == zoom) {
                Log.i(TAG, "Zoom is already set to " + zoom);
            } else {
                Log.i(TAG, "Setting zoom to " + zoom);
                parameters.setZoom(zoom);
            }
        } else {
            Log.i(TAG, "Zoom is not supported");
        }
    }

    private static Integer indexOfClosestZoom(Camera.Parameters parameters, double targetZoomRatio) {
        List<Integer> ratios = parameters.getZoomRatios();
        Log.i(TAG, "Zoom ratios: " + ratios);
        int maxZoom = parameters.getMaxZoom();
        if (ratios == null || ratios.isEmpty() || ratios.size() != maxZoom + 1) {
            Log.w(TAG, "Invalid zoom ratios!");
            return null;
        }
        double target100 = 100.0 * targetZoomRatio;
        double smallestDiff = Double.POSITIVE_INFINITY;
        int closestIndex = 0;
        for (int i = 0; i < ratios.size(); i++) {
            double diff = Math.abs(ratios.get(i) - target100);
            if (diff < smallestDiff) {
                smallestDiff = diff;
                closestIndex = i;
            }
        }
        Log.i(TAG, "Chose zoom ratio of " + (ratios.get(closestIndex) / 100.0));
        return closestIndex;
    }

    public static void setInvertColor(Camera.Parameters parameters) {
        if (Camera.Parameters.EFFECT_NEGATIVE.equals(parameters.getColorEffect())) {
            Log.i(TAG, "Negative effect already set");
            return;
        }
        String colorMode = findSettableValue("color effect",
                parameters.getSupportedColorEffects(),
                Camera.Parameters.EFFECT_NEGATIVE);
        if (colorMode != null) {
            parameters.setColorEffect(colorMode);
        }
    }

    public static Point findBestPreviewSizeValue(Camera.Parameters parameters, Point screenResolution) {

        List<Camera.Size> rawSupportedSizes = parameters.getSupportedPreviewSizes();
        if (rawSupportedSizes == null) {
            Log.w(TAG, "Device returned no supported preview sizes; using default");
            Camera.Size defaultSize = parameters.getPreviewSize();
            if (defaultSize == null) {
                throw new IllegalStateException("Parameters contained no preview size!");
            }
            return new Point(defaultSize.width, defaultSize.height);
        }

        // Sort by size, descending
        List<Camera.Size> supportedPreviewSizes = new ArrayList<>(rawSupportedSizes);
        Collections.sort(supportedPreviewSizes, new Comparator<Camera.Size>() {
            @Override
            public int compare(Camera.Size a, Camera.Size b) {
                int aPixels = a.height * a.width;
                int bPixels = b.height * b.width;
                if (bPixels < aPixels) {
                    return -1;
                }
                if (bPixels > aPixels) {
                    return 1;
                }
                return 0;
            }
        });

        if (Log.isLoggable(TAG, Log.INFO)) {
            StringBuilder previewSizesString = new StringBuilder();
            for (Camera.Size supportedPreviewSize : supportedPreviewSizes) {
                previewSizesString.append(supportedPreviewSize.width).append('x')
                        .append(supportedPreviewSize.height).append(' ');
            }
            Log.i(TAG, "Supported preview sizes: " + previewSizesString);
        }

        double screenAspectRatio = screenResolution.x / (double) screenResolution.y;

        // Remove sizes that are unsuitable
        Iterator<Camera.Size> it = supportedPreviewSizes.iterator();
        while (it.hasNext()) {
            Camera.Size supportedPreviewSize = it.next();
            int realWidth = supportedPreviewSize.width;
            int realHeight = supportedPreviewSize.height;
            if (realWidth * realHeight < MIN_PREVIEW_PIXELS) {
                it.remove();
                continue;
            }

            boolean isCandidatePortrait = realWidth < realHeight;
            int maybeFlippedWidth = isCandidatePortrait ? realHeight : realWidth;
            int maybeFlippedHeight = isCandidatePortrait ? realWidth : realHeight;
            double aspectRatio = maybeFlippedWidth / (double) maybeFlippedHeight;
            double distortion = Math.abs(aspectRatio - screenAspectRatio);
            if (distortion > MAX_ASPECT_DISTORTION) {
                it.remove();
                continue;
            }

            if (maybeFlippedWidth == screenResolution.x && maybeFlippedHeight == screenResolution.y) {
                Point exactPoint = new Point(realWidth, realHeight);
                Log.i(TAG, "Found preview size exactly matching screen size: " + exactPoint);
                return exactPoint;
            }
        }

        // If no exact match, use largest preview size. This was not a great idea on older devices because
        // of the additional computation needed. We're likely to get here on newer Android 4+ devices, where
        // the CPU is much more powerful.
        if (!supportedPreviewSizes.isEmpty()) {
            Camera.Size largestPreview = supportedPreviewSizes.get(0);
            Point largestSize = new Point(largestPreview.width, largestPreview.height);
            Log.i(TAG, "Using largest suitable preview size: " + largestSize);
            return largestSize;
        }

        // If there is nothing at all suitable, return current preview size
        Camera.Size defaultPreview = parameters.getPreviewSize();
        if (defaultPreview == null) {
            throw new IllegalStateException("Parameters contained no preview size!");
        }
        Point defaultSize = new Point(defaultPreview.width, defaultPreview.height);
        Log.i(TAG, "No suitable preview sizes, using default: " + defaultSize);
        return defaultSize;
    }

    private static String findSettableValue(String name,
                                            Collection<String> supportedValues,
                                            String... desiredValues) {
        Log.i(TAG, "Requesting " + name + " value from among: " + Arrays.toString(desiredValues));
        Log.i(TAG, "Supported " + name + " values: " + supportedValues);
        if (supportedValues != null) {
            for (String desiredValue : desiredValues) {
                if (supportedValues.contains(desiredValue)) {
                    Log.i(TAG, "Can set " + name + " to: " + desiredValue);
                    return desiredValue;
                }
            }
        }
        Log.i(TAG, "No supported values match");
        return null;
    }

    private static String toString(Collection<int[]> arrays) {
        if (arrays == null || arrays.isEmpty()) {
            return "[]";
        }
        StringBuilder buffer = new StringBuilder();
        buffer.append('[');
        Iterator<int[]> it = arrays.iterator();
        while (it.hasNext()) {
            buffer.append(Arrays.toString(it.next()));
            if (it.hasNext()) {
                buffer.append(", ");
            }
        }
        buffer.append(']');
        return buffer.toString();
    }

    private static String toString(Iterable<Camera.Area> areas) {
        if (areas == null) {
            return null;
        }
        StringBuilder result = new StringBuilder();
        for (Camera.Area area : areas) {
            result.append(area.rect).append(':').append(area.weight).append(' ');
        }
        return result.toString();
    }

    public static String collectStats(Camera.Parameters parameters) {
        return collectStats(parameters.flatten());
    }

    public static String collectStats(CharSequence flattenedParams) {
        StringBuilder result = new StringBuilder(1000);

        result.append("BOARD=").append(Build.BOARD).append('\n');
        result.append("BRAND=").append(Build.BRAND).append('\n');
        result.append("CPU_ABI=").append(Build.CPU_ABI).append('\n');
        result.append("DEVICE=").append(Build.DEVICE).append('\n');
        result.append("DISPLAY=").append(Build.DISPLAY).append('\n');
        result.append("FINGERPRINT=").append(Build.FINGERPRINT).append('\n');
        result.append("HOST=").append(Build.HOST).append('\n');
        result.append("ID=").append(Build.ID).append('\n');
        result.append("MANUFACTURER=").append(Build.MANUFACTURER).append('\n');
        result.append("MODEL=").append(Build.MODEL).append('\n');
        result.append("PRODUCT=").append(Build.PRODUCT).append('\n');
        result.append("TAGS=").append(Build.TAGS).append('\n');
        result.append("TIME=").append(Build.TIME).append('\n');
        result.append("TYPE=").append(Build.TYPE).append('\n');
        result.append("USER=").append(Build.USER).append('\n');
        result.append("VERSION.CODENAME=").append(Build.VERSION.CODENAME).append('\n');
        result.append("VERSION.INCREMENTAL=").append(Build.VERSION.INCREMENTAL).append('\n');
        result.append("VERSION.RELEASE=").append(Build.VERSION.RELEASE).append('\n');
        result.append("VERSION.SDK_INT=").append(Build.VERSION.SDK_INT).append('\n');

        if (flattenedParams != null) {
            String[] params = SEMICOLON.split(flattenedParams);
            Arrays.sort(params);
            for (String param : params) {
                result.append(param).append('\n');
            }
        }

        return result.toString();
    }

}
