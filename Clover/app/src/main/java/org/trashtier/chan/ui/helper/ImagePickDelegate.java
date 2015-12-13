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
package org.trashtier.chan.ui.helper;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;

import org.trashtier.chan.Chan;
import org.trashtier.chan.core.http.ReplyManager;
import org.trashtier.chan.utils.IOUtils;
import org.trashtier.chan.utils.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static org.trashtier.chan.utils.AndroidUtils.runOnUiThread;

public class ImagePickDelegate implements Runnable {
    private static final String TAG = "ImagePickActivity";

    private static final int IMAGE_PICK_RESULT = 2;
    private static final long MAX_FILE_SIZE = 15 * 1024 * 1024;
    private static final String DEFAULT_FILE_NAME = "file";

    private ReplyManager replyManager;
    private Activity activity;

    private ImagePickCallback callback;
    private Uri uri;
    private String fileName;
    private boolean success = false;
    private File cacheFile;

    public ImagePickDelegate(Activity activity) {
        this.activity = activity;

        replyManager = Chan.getReplyManager();
    }

    public boolean pick(ImagePickCallback callback) {
        if (this.callback != null) {
            return false;
        } else {
            this.callback = callback;

            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("*/*");

            if (intent.resolveActivity(activity.getPackageManager()) != null) {
                activity.startActivityForResult(intent, IMAGE_PICK_RESULT);
                return true;
            } else {
                Logger.e(TAG, "No activity found to get file with");
                callback.onFilePickError(false);
                reset();
                return false;
            }
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        boolean ok = false;
        boolean cancelled = false;
        if (requestCode == IMAGE_PICK_RESULT) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                uri = data.getData();

                Cursor returnCursor = activity.getContentResolver().query(uri, null, null, null, null);
                if (returnCursor != null) {
                    int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    returnCursor.moveToFirst();
                    if (nameIndex > -1) {
                        fileName = returnCursor.getString(nameIndex);
                    }

                    returnCursor.close();
                }

                if (fileName == null) {
                    // As per the comment on OpenableColumns.DISPLAY_NAME:
                    // If this is not provided then the name should default to the last segment of the file's URI.
                    fileName = uri.getLastPathSegment();
                }

                if (fileName == null) {
                    fileName = DEFAULT_FILE_NAME;
                }

                callback.onFilePickLoading();

                new Thread(this).start();
                ok = true;
            } else if (resultCode == Activity.RESULT_CANCELED) {
                cancelled = true;
            }
        }

        if (!ok) {
            callback.onFilePickError(cancelled);
            reset();
        }
    }

    @Override
    public void run() {
        cacheFile = replyManager.getPickFile();

        ParcelFileDescriptor fileDescriptor = null;
        InputStream is = null;
        OutputStream os = null;
        try {
            fileDescriptor = activity.getContentResolver().openFileDescriptor(uri, "r");
            is = new FileInputStream(fileDescriptor.getFileDescriptor());
            os = new FileOutputStream(cacheFile);
            boolean fullyCopied = IOUtils.copy(is, os, MAX_FILE_SIZE);
            if (fullyCopied) {
                success = true;
            }
        } catch (IOException | SecurityException e) {
            Logger.e(TAG, "Error copying file from the file descriptor", e);
        } finally {
            IOUtils.closeQuietly(fileDescriptor);
            IOUtils.closeQuietly(is);
            IOUtils.closeQuietly(os);
        }

        if (!success) {
            if (!cacheFile.delete()) {
                Logger.e(TAG, "Could not delete picked_file after copy fail");
            }
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (success) {
                    callback.onFilePicked(fileName, cacheFile);
                } else {
                    callback.onFilePickError(false);
                }
                reset();
            }
        });
    }

    private void reset() {
        callback = null;
        cacheFile = null;
        success = false;
        fileName = null;
        uri = null;
    }

    public interface ImagePickCallback {
        void onFilePickLoading();

        void onFilePicked(String fileName, File file);

        void onFilePickError(boolean cancelled);
    }
}
