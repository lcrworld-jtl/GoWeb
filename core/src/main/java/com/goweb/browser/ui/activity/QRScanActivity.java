package com.goweb.browser.ui.activity;

import android.Manifest;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.goweb.browser.R;
import com.goweb.browser.ui.dialog.CustomDialog;

import java.io.ByteArrayOutputStream;
import java.util.List;

public class QRScanActivity extends Activity implements SurfaceHolder.Callback, Camera.PreviewCallback {

    public static final String RESULT_QR_CODE = "qr_result";
    private static final int PERMISSION_CAMERA = 2001;

    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private Camera camera;
    private boolean isPreviewing = false;
    private Handler handler = new Handler(Looper.getMainLooper());
    private boolean processing = false;
    private View scanLine;
    private float scanLineY = 0f;
    private boolean animateDown = true;
    private TextView statusText;
    private int scanAttempts = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_scan);

        surfaceView = findViewById(R.id.surface_view);
        scanLine = findViewById(R.id.scan_line);
        statusText = findViewById(R.id.hint_text);
        ImageView backBtn = findViewById(R.id.btn_back);

        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);

        backBtn.setOnClickListener(v -> {
            releaseCamera();
            setResult(RESULT_CANCELED);
            finish();
        });

        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                    new CustomDialog.Builder(this)
                            .setTitle(getString(R.string.camera_permission_title))
                            .setMessage(getString(R.string.camera_permission_rationale))
                            .setPositiveButton(getString(R.string.btn_allow), () ->
                                    requestPermissions(new String[]{Manifest.permission.CAMERA}, PERMISSION_CAMERA))
                            .setNegativeButton(getString(R.string.btn_deny), () -> {
                                showManualInputOption();
                            })
                            .show();
                } else {
                    requestPermissions(new String[]{Manifest.permission.CAMERA}, PERMISSION_CAMERA);
                }
            }
        }

        startScanAnimation();

        handler.postDelayed(() -> {
            if (!isFinishing() && scanAttempts < 1) {
                showManualInputOption();
            }
        }, 5000);
    }

    private void showManualInputOption() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(24, 24, 24, 16);

        TextView hint = new TextView(this);
        hint.setText(R.string.qr_manual_hint);
        hint.setTextSize(13);
        hint.setTextColor(0xFF888888);
        layout.addView(hint);

        EditText input = new EditText(this);
        input.setHint(R.string.qr_input_hint);
        input.setSingleLine();
        input.setPadding(0, 16, 0, 16);
        layout.addView(input);

        LinearLayout btnRow = new LinearLayout(this);
        btnRow.setOrientation(LinearLayout.HORIZONTAL);
        btnRow.setGravity(Gravity.END);
        btnRow.setPadding(0, 8, 0, 0);

        TextView pasteBtn = new TextView(this);
        pasteBtn.setText(R.string.paste_from_clipboard);
        pasteBtn.setTextColor(0xFF2d6a4f);
        pasteBtn.setTextSize(14);
        pasteBtn.setPadding(16, 8, 16, 8);
        pasteBtn.setOnClickListener(v -> {
            ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            if (cm != null && cm.hasPrimaryClip()) {
                ClipData.Item item = cm.getPrimaryClip().getItemAt(0);
                if (item != null && item.getText() != null) {
                    input.setText(item.getText().toString());
                }
            }
        });
        btnRow.addView(pasteBtn);

        TextView goBtn = new TextView(this);
        goBtn.setText(R.string.go);
        goBtn.setTextColor(0xFF2d6a4f);
        goBtn.setTextSize(14);
        goBtn.setPadding(16, 8, 16, 8);
        goBtn.setOnClickListener(v -> {
            String text = input.getText().toString().trim();
            if (!text.isEmpty()) {
                onQRDetected(text);
            } else {
                Toast.makeText(this, R.string.url_empty, Toast.LENGTH_SHORT).show();
            }
        });
        btnRow.addView(goBtn);

        layout.addView(btnRow);

        new CustomDialog.Builder(this)
                .setTitle(getString(R.string.qr_scan_title))
                .setView(layout)
                .setNegativeButton(getString(R.string.cancel), null)
                .setCancelable(true)
                .show();
    }

    private void startScanAnimation() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isFinishing()) return;
                if (animateDown) {
                    scanLineY += 8f;
                    if (scanLineY >= 400f) animateDown = false;
                } else {
                    scanLineY -= 8f;
                    if (scanLineY <= 0f) animateDown = true;
                }
                scanLine.setTranslationY(scanLineY);
                handler.postDelayed(this, 16);
            }
        }, 16);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PERMISSION_CAMERA) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initCamera();
            } else {
                Toast.makeText(this, R.string.camera_permission_denied, Toast.LENGTH_SHORT).show();
                showManualInputOption();
            }
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        initCamera();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (isPreviewing) stopPreview();
        startPreview();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        releaseCamera();
    }

    @SuppressWarnings("deprecation")
    private void initCamera() {
        if (Build.VERSION.SDK_INT >= 23 && checkSelfPermission(Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        try {
            camera = Camera.open();
            if (camera == null) {
                Toast.makeText(this, R.string.camera_open_failed, Toast.LENGTH_SHORT).show();
                showManualInputOption();
                return;
            }
            camera.setDisplayOrientation(90);

            Camera.Parameters params = camera.getParameters();
            List<Camera.Size> sizes = params.getSupportedPreviewSizes();
            if (sizes != null && !sizes.isEmpty()) {
                Camera.Size best = sizes.get(0);
                for (Camera.Size s : sizes) {
                    if (s.width * s.height > best.width * best.height &&
                            s.width <= 1920 && s.height <= 1080) {
                        best = s;
                    }
                }
                params.setPreviewSize(best.width, best.height);
            }

            List<String> focusModes = params.getSupportedFocusModes();
            if (focusModes != null) {
                if (focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                    params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
                } else if (focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
                    params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                }
            }

            if (params.isAutoExposureLockSupported())
                params.setAutoExposureLock(false);
            if (params.isAutoWhiteBalanceLockSupported())
                params.setAutoWhiteBalanceLock(false);

            camera.setParameters(params);
        } catch (Exception e) {
            Toast.makeText(this, R.string.camera_denied, Toast.LENGTH_SHORT).show();
            showManualInputOption();
        }
    }

    @SuppressWarnings("deprecation")
    private void startPreview() {
        if (camera == null) return;
        try {
            camera.setPreviewDisplay(surfaceHolder);
            camera.setPreviewCallback(this);
            camera.startPreview();
            isPreviewing = true;
            try { camera.autoFocus(null); } catch (Exception ignored) {}
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void stopPreview() {
        if (camera != null && isPreviewing) {
            try {
                camera.stopPreview();
                camera.setPreviewCallback(null);
            } catch (Exception e) {}
            isPreviewing = false;
        }
    }

    private void releaseCamera() {
        if (camera != null) {
            stopPreview();
            try { camera.release(); } catch (Exception e) {}
            camera = null;
        }
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        if (processing) return;
        processing = true;
        scanAttempts++;

        Camera.Parameters params = camera.getParameters();
        Camera.Size size = params.getPreviewSize();

        handler.post(() -> {
            try {
                String result = decodeQR(data, size.width, size.height);
                if (result != null && !result.isEmpty()) {
                    onQRDetected(result);
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            processing = false;
        });
    }

    private String decodeQR(byte[] data, int width, int height) {
        try {
            YuvImage yuvImage = new YuvImage(data, ImageFormat.NV21, width, height, null);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            yuvImage.compressToJpeg(new Rect(0, 0, width, height), 70, out);
            byte[] jpegData = out.toByteArray();
            Bitmap bitmap = BitmapFactory.decodeByteArray(jpegData, 0, jpegData.length);
            if (bitmap == null) return null;

            int w = bitmap.getWidth();
            int h = bitmap.getHeight();
            int[] pixels = new int[w * h];
            bitmap.getPixels(pixels, 0, w, 0, 0, w, h);

            byte[] binary = new byte[w * h];
            int totalGray = 0;
            for (int i = 0; i < pixels.length; i++) {
                int gray = ((pixels[i] >> 16) & 0xFF) * 30 +
                        ((pixels[i] >> 8) & 0xFF) * 59 +
                        (pixels[i] & 0xFF) * 11;
                totalGray += gray;
                binary[i] = (byte) gray;
            }
            int avgGray = totalGray / pixels.length;
            for (int i = 0; i < binary.length; i++) {
                binary[i] = (byte) ((binary[i] & 0xFF) < avgGray ? 0 : 1);
            }

            String result = findQRCode(binary, w, h);
            bitmap.recycle();
            out.close();
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private String findQRCode(byte[] binary, int width, int height) {
        int step = 4;
        for (int y = 0; y < height; y += step) {
            int state = 0;
            int runLength = 0;
            for (int x = 0; x < width; x++) {
                byte pixel = binary[y * width + x];
                if (state == 0 && pixel == 0) {
                    state = 1; runLength = 1;
                } else if (state == 1) {
                    if (pixel == 0) runLength++;
                    else { state = 2; runLength = 1; }
                } else if (state == 2) {
                    if (pixel == 1) runLength++;
                    else {
                        if (runLength >= 3) { state = 3; runLength = 1; }
                        else { state = 1; runLength = 1; }
                    }
                } else if (state == 3) {
                    if (pixel == 0) runLength++;
                    else { state = 4; runLength = 1; }
                } else if (state == 4) {
                    if (pixel == 1) runLength++;
                    else {
                        if (runLength >= 1) {
                            String decoded = tryExtractUrl(binary, width, height, x, y);
                            if (decoded != null) return decoded;
                        }
                        state = 0; runLength = 0;
                    }
                }
            }
        }
        return null;
    }

    private String tryExtractUrl(byte[] binary, int width, int height, int endX, int y) {
        return null;
    }

    private void onQRDetected(String result) {
        releaseCamera();
        Intent intent = new Intent();
        intent.putExtra(RESULT_QR_CODE, result);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopPreview();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (camera != null && !isPreviewing) startPreview();
        else if (camera == null && surfaceHolder.getSurface() != null) initCamera();
    }

    @Override
    protected void onDestroy() {
        releaseCamera();
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        releaseCamera();
        setResult(RESULT_CANCELED);
        super.onBackPressed();
    }
}
