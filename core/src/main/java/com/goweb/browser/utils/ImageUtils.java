package com.goweb.browser.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ImageUtils {

    public static Bitmap compressBitmap(Bitmap bitmap, int quality) {
        if (bitmap == null) return null;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out);
        byte[] data = out.toByteArray();
        return BitmapFactory.decodeByteArray(data, 0, data.length);
    }

    public static Bitmap scaleBitmap(Bitmap bitmap, int maxWidth, int maxHeight) {
        if (bitmap == null) return null;
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        float scale = Math.min((float) maxWidth / width, (float) maxHeight / height);
        if (scale >= 1.0f) return bitmap;
        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);
        return Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
    }

    public static Bitmap cropCenter(Bitmap bitmap, int targetWidth, int targetHeight) {
        if (bitmap == null) return null;
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        if (width < targetWidth || height < targetHeight) return bitmap;
        int x = (width - targetWidth) / 2;
        int y = (height - targetHeight) / 2;
        return Bitmap.createBitmap(bitmap, x, y, targetWidth, targetHeight);
    }

    public static Bitmap createRoundedBitmap(Bitmap bitmap, float radius) {
        if (bitmap == null) return null;
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        RectF rect = new RectF(0, 0, bitmap.getWidth(), bitmap.getHeight());
        canvas.drawRoundRect(rect, radius, radius, paint);
        paint.setXfermode(new android.graphics.PorterDuffXfermode(android.graphics.PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, 0, 0, paint);
        return output;
    }

    public static Bitmap createCircleBitmap(Bitmap bitmap) {
        if (bitmap == null) return null;
        int size = Math.min(bitmap.getWidth(), bitmap.getHeight());
        Bitmap output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint);
        paint.setXfermode(new android.graphics.PorterDuffXfermode(android.graphics.PorterDuff.Mode.SRC_IN));
        int x = (bitmap.getWidth() - size) / 2;
        int y = (bitmap.getHeight() - size) / 2;
        canvas.drawBitmap(bitmap, x, y, paint);
        return output;
    }

    public static Bitmap generateSiteIcon(String domain, int size) {
        if (domain == null || domain.isEmpty()) domain = "?";
        String letter = domain.substring(0, 1).toUpperCase();

        Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        Paint bgPaint = new Paint();
        bgPaint.setAntiAlias(true);
        bgPaint.setColor(getColorForDomain(domain));
        canvas.drawCircle(size / 2f, size / 2f, size / 2f, bgPaint);

        Paint textPaint = new Paint();
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(size * 0.45f);
        textPaint.setAntiAlias(true);
        textPaint.setTextAlign(Paint.Align.CENTER);

        Rect textBounds = new Rect();
        textPaint.getTextBounds(letter, 0, letter.length(), textBounds);
        float textY = size / 2f + textBounds.height() / 2f;
        canvas.drawText(letter, size / 2f, textY, textPaint);

        return bitmap;
    }

    public static int getColorForDomain(String domain) {
        if (domain == null || domain.isEmpty()) return 0xFF2d6a4f;
        int hash = domain.hashCode();
        int[] colors = {
                0xFFE53935, 0xFFD81B60, 0xFF8E24AA, 0xFF5E35B1,
                0xFF3949AB, 0xFF1E88E5, 0xFF039BE5, 0xFF00ACC1,
                0xFF00897B, 0xFF43A047, 0xFF7CB342, 0xFFC0CA33,
                0xFFFDD835, 0xFFFFB300, 0xFFFB8C00, 0xFFF4511E
        };
        return colors[Math.abs(hash) % colors.length];
    }

    public static boolean saveBitmapToFile(Bitmap bitmap, File file) {
        if (bitmap == null || file == null) return false;
        try {
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static Bitmap loadBitmapFromFile(String path) {
        if (path == null || path.isEmpty()) return null;
        try {
            return BitmapFactory.decodeFile(path);
        } catch (Exception e) {
            return null;
        }
    }

    public static Bitmap loadBitmapFromUri(Context context, Uri uri) {
        if (context == null || uri == null) return null;
        try {
            InputStream is = context.getContentResolver().openInputStream(uri);
            if (is != null) {
                Bitmap bitmap = BitmapFactory.decodeStream(is);
                is.close();
                return bitmap;
            }
        } catch (Exception e) {
        }
        return null;
    }

    public static String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format(Locale.US, "%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format(Locale.US, "%.1f MB", bytes / (1024.0 * 1024.0));
        return String.format(Locale.US, "%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0));
    }

    public static String formatDate(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    public static String formatRelativeTime(long timestamp) {
        long now = System.currentTimeMillis();
        long diff = now - timestamp;
        if (diff < 60000) return "Just now";
        if (diff < 3600000) return (diff / 60000) + " min ago";
        if (diff < 86400000) return (diff / 3600000) + " hours ago";
        if (diff < 604800000) return (diff / 86400000) + " days ago";
        return formatDate(timestamp);
    }

    public static Bitmap addWatermark(Bitmap bitmap, String text) {
        if (bitmap == null) return null;
        Bitmap output = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(output);
        Paint paint = new Paint();
        paint.setColor(Color.argb(80, 255, 255, 255));
        paint.setTextSize(24f);
        paint.setAntiAlias(true);
        paint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText(text, bitmap.getWidth() - 20, bitmap.getHeight() - 20, paint);
        return output;
    }

    public static Bitmap rotateBitmap(Bitmap bitmap, int degrees) {
        if (bitmap == null) return null;
        Matrix matrix = new Matrix();
        matrix.postRotate(degrees);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    public static Bitmap flipBitmap(Bitmap bitmap, boolean horizontal, boolean vertical) {
        if (bitmap == null) return null;
        Matrix matrix = new Matrix();
        matrix.preScale(horizontal ? -1 : 1, vertical ? -1 : 1);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        int height = options.outHeight;
        int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            int halfHeight = height / 2;
            int halfWidth = width / 2;
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    public static Bitmap decodeSampledBitmapFromFile(String path, int reqWidth, int reqHeight) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(path, options);
    }

    public static boolean copyFile(File src, File dst) {
        try {
            FileInputStream fis = new FileInputStream(src);
            FileOutputStream fos = new FileOutputStream(dst);
            byte[] buffer = new byte[8192];
            int len;
            while ((len = fis.read(buffer)) > 0) {
                fos.write(buffer, 0, len);
            }
            fis.close();
            fos.flush();
            fos.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static String getMimeType(String fileName) {
        if (fileName == null) return "*/*";
        String lower = fileName.toLowerCase();
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".gif")) return "image/gif";
        if (lower.endsWith(".webp")) return "image/webp";
        if (lower.endsWith(".bmp")) return "image/bmp";
        if (lower.endsWith(".svg")) return "image/svg+xml";
        if (lower.endsWith(".pdf")) return "application/pdf";
        if (lower.endsWith(".mp4")) return "video/mp4";
        if (lower.endsWith(".mp3")) return "audio/mpeg";
        if (lower.endsWith(".apk")) return "application/vnd.android.package-archive";
        if (lower.endsWith(".zip")) return "application/zip";
        if (lower.endsWith(".txt")) return "text/plain";
        if (lower.endsWith(".html") || lower.endsWith(".htm")) return "text/html";
        if (lower.endsWith(".css")) return "text/css";
        if (lower.endsWith(".js")) return "application/javascript";
        if (lower.endsWith(".json")) return "application/json";
        if (lower.endsWith(".xml")) return "application/xml";
        if (lower.endsWith(".doc")) return "application/msword";
        if (lower.endsWith(".docx")) return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        if (lower.endsWith(".xls")) return "application/vnd.ms-excel";
        if (lower.endsWith(".xlsx")) return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        if (lower.endsWith(".ppt")) return "application/vnd.ms-powerpoint";
        if (lower.endsWith(".pptx")) return "application/vnd.openxmlformats-officedocument.presentationml.presentation";
        return "*/*";
    }

    public static int getFileIconResource(String fileName) {
        if (fileName == null) return 0;
        String lower = fileName.toLowerCase();
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png") ||
                lower.endsWith(".gif") || lower.endsWith(".webp") || lower.endsWith(".bmp")) {
            return com.goweb.browser.R.drawable.ic_file;
        }
        if (lower.endsWith(".mp4") || lower.endsWith(".avi") || lower.endsWith(".mkv") ||
                lower.endsWith(".mov") || lower.endsWith(".wmv") || lower.endsWith(".flv")) {
            return com.goweb.browser.R.drawable.ic_file;
        }
        if (lower.endsWith(".mp3") || lower.endsWith(".wav") || lower.endsWith(".flac") ||
                lower.endsWith(".aac") || lower.endsWith(".ogg") || lower.endsWith(".wma")) {
            return com.goweb.browser.R.drawable.ic_file;
        }
        return com.goweb.browser.R.drawable.ic_file;
    }
}
