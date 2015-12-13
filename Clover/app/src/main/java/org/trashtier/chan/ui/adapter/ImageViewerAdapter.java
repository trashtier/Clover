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
package org.trashtier.chan.ui.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import org.trashtier.chan.core.model.PostImage;
import org.trashtier.chan.ui.view.MultiImageView;
import org.trashtier.chan.ui.view.ViewPagerAdapter;
import org.trashtier.chan.utils.Logger;

import java.util.ArrayList;
import java.util.List;

public class ImageViewerAdapter extends ViewPagerAdapter {
    private static final String TAG = "ImageViewerAdapter";

    private final Context context;
    private final List<PostImage> images;
    private final MultiImageView.Callback multiImageViewCallback;

    private List<MultiImageView> loadedViews = new ArrayList<>(3);
    private List<ModeChange> pendingModeChanges = new ArrayList<>();

    public ImageViewerAdapter(Context context, List<PostImage> images, MultiImageView.Callback multiImageViewCallback) {
        this.context = context;
        this.images = images;
        this.multiImageViewCallback = multiImageViewCallback;
    }

    @Override
    public View getView(int position, ViewGroup parent) {
        PostImage postImage = images.get(position);
        MultiImageView view = new MultiImageView(context);
        view.bindPostImage(postImage, multiImageViewCallback);

        loadedViews.add(view);

        return view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        super.destroyItem(container, position, object);

        //noinspection SuspiciousMethodCalls
        loadedViews.remove(object);
    }

    @Override
    public int getCount() {
        return images.size();
    }

    @Override
    public void finishUpdate(ViewGroup container) {
        for (ModeChange change : pendingModeChanges) {
            MultiImageView view = find(change.postImage);
            if (view == null) {
                Logger.w(TAG, "finishUpdate setMode view still not found");
            } else {
                view.setMode(change.mode);
            }
        }
        pendingModeChanges.clear();
    }

    public void setMode(final PostImage postImage, MultiImageView.Mode mode) {
        MultiImageView view = find(postImage);
        if (view == null) {
            pendingModeChanges.add(new ModeChange(mode, postImage));
        } else {
            view.setMode(mode);
        }
    }

    public MultiImageView.Mode getMode(PostImage postImage) {
        MultiImageView view = find(postImage);
        if (view == null) {
            Logger.w(TAG, "getMode view not found");
            return null;
        } else {
            return view.getMode();
        }
    }

    public MultiImageView find(PostImage postImage) {
        for (MultiImageView view : loadedViews) {
            if (view.getPostImage() == postImage) {
                return view;
            }
        }
        return null;
    }

    private static class ModeChange {
        public MultiImageView.Mode mode;
        public PostImage postImage;

        private ModeChange(MultiImageView.Mode mode, PostImage postImage) {
            this.mode = mode;
            this.postImage = postImage;
        }
    }
}
