/*
 * Clover - 4chan browser https://github.com/Floens/Clover/
 * Copyright (C) 2014  Floens
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.trashtier.chan.core.saver;

import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;

import org.trashtier.chan.Chan;
import org.trashtier.chan.core.cache.FileCache;
import org.trashtier.chan.core.model.PostImage;
import org.trashtier.chan.utils.AndroidUtils;
import org.trashtier.chan.utils.IOUtils;
import org.trashtier.chan.utils.ImageDecoder;
import org.trashtier.chan.utils.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static org.trashtier.chan.utils.AndroidUtils.dp;
import static org.trashtier.chan.utils.AndroidUtils.getAppContext;

public class ImageSaveTask implements Runnable, FileCache.DownloadedCallback {
    private static final String TAG = "ImageSaveTask";

    private PostImage postImage;
    private ImageSaveTaskCallback callback;
    private File destination;
    private boolean share;
    private boolean makeBitmap;
    private Bitmap bitmap;
    private boolean showToast;

    private boolean success = false;

    public ImageSaveTask(PostImage postImage) {
        this.postImage = postImage;
    }

    public void setCallback(ImageSaveTaskCallback callback) {
        this.callback = callback;
    }

    public PostImage getPostImage() {
        return postImage;
    }

    public void setDestination(File destination) {
        this.destination = destination;
    }

    public File getDestination() {
        return destination;
    }

    public void setShare(boolean share) {
        this.share = share;
    }

    public void setMakeBitmap(boolean makeBitmap) {
        this.makeBitmap = makeBitmap;
    }

    public boolean isMakeBitmap() {
        return makeBitmap;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setShowToast(boolean showToast) {
        this.showToast = showToast;
    }

    public boolean isShowToast() {
        return showToast;
    }

    @Override
    public void run() {
        try {
            if (destination.exists()) {
                onDestination();
            } else {
                FileCache.FileCacheDownloader fileCacheDownloader = Chan.getFileCache().downloadFile(postImage.imageUrl, this);
                // If the fileCacheDownloader is null then the callbacks were already executed here,
                // else wait for the download to finish to avoid that the next task is immediately executed.
                if (fileCacheDownloader != null) {
                    // If the file is now downloading
                    fileCacheDownloader.getFuture().get();
                }
            }
        } catch (InterruptedException e) {
            onInterrupted();
        } catch (Exception e) {
            Logger.e(TAG, "Uncaught exception", e);
        } finally {
            postFinished(success);
        }
    }

    @Override
    public void onProgress(long downloaded, long total, boolean done) {
    }

    @Override
    public void onFail(boolean notFound) {
    }

    @Override
    public void onSuccess(File file) {
        copyToDestination(file);
        onDestination();
    }

    private void onInterrupted() {
        if (destination.exists()) {
            if (!destination.delete()) {
                Logger.e(TAG, "Could not delete destination after an interrupt");
            }
        }
    }

    private void onDestination() {
        scanDestination();
        if (makeBitmap) {
            bitmap = ImageDecoder.decodeFile(destination, dp(512), dp(256));
        }
    }

    private void copyToDestination(File source) {
        InputStream is = null;
        OutputStream os = null;
        try {
            File parent = destination.getParentFile();
            if (!parent.mkdirs() && !parent.isDirectory()) {
                throw new IOException("Could not create parent directory");
            }

            if (destination.isDirectory()) {
                throw new IOException("Destination file is already a directory");
            }

            is = new FileInputStream(source);
            os = new FileOutputStream(destination);
            IOUtils.copy(is, os);

            success = true;
        } catch (IOException e) {
            Logger.e(TAG, "Error writing to file", e);
        } finally {
            IOUtils.closeQuietly(is);
            IOUtils.closeQuietly(os);
        }
    }

    private void scanDestination() {
        MediaScannerConnection.scanFile(getAppContext(), new String[]{destination.getAbsolutePath()}, null, new MediaScannerConnection.OnScanCompletedListener() {
            @Override
            public void onScanCompleted(String path, final Uri uri) {
                // Runs on a binder thread
                AndroidUtils.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        afterScan(uri);
                    }
                });
            }
        });
    }

    private void afterScan(final Uri uri) {
        Logger.d(TAG, "Media scan succeeded: " + uri);

        if (share) {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("image/*");
            intent.putExtra(Intent.EXTRA_STREAM, uri);
            AndroidUtils.openIntent(intent);
        }
    }

    private void postFinished(final boolean success) {
        AndroidUtils.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                callback.imageSaveTaskFinished(ImageSaveTask.this, success);
            }
        });
    }

    public interface ImageSaveTaskCallback {
        void imageSaveTaskFinished(ImageSaveTask task, boolean success);
    }
}
