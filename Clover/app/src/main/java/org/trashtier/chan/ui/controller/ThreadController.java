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
package org.trashtier.chan.ui.controller;

import android.content.Context;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.KeyEvent;
import android.view.LayoutInflater;

import org.trashtier.chan.Chan;
import org.trashtier.chan.R;
import org.trashtier.chan.chan.ChanUrls;
import org.trashtier.chan.controller.Controller;
import org.trashtier.chan.core.manager.FilterEngine;
import org.trashtier.chan.core.model.Filter;
import org.trashtier.chan.core.model.Loadable;
import org.trashtier.chan.core.model.Pin;
import org.trashtier.chan.core.model.PostImage;
import org.trashtier.chan.ui.helper.RefreshUIMessage;
import org.trashtier.chan.ui.layout.ThreadLayout;
import org.trashtier.chan.ui.toolbar.Toolbar;
import org.trashtier.chan.ui.view.ThumbnailView;
import org.trashtier.chan.utils.Logger;

import java.util.List;

import de.greenrobot.event.EventBus;

import static org.trashtier.chan.utils.AndroidUtils.dp;

public abstract class ThreadController extends Controller implements ThreadLayout.ThreadLayoutCallback, ImageViewerController.PreviewCallback, SwipeRefreshLayout.OnRefreshListener, ToolbarNavigationController.ToolbarSearchCallback, NfcAdapter.CreateNdefMessageCallback {
    private static final String TAG = "ThreadController";

    protected ThreadLayout threadLayout;
    private SwipeRefreshLayout swipeRefreshLayout;

    public ThreadController(Context context) {
        super(context);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        EventBus.getDefault().register(this);

        navigationItem.collapseToolbar = true;

        threadLayout = (ThreadLayout) LayoutInflater.from(context).inflate(R.layout.layout_thread, null);
        threadLayout.setCallback(this);

        swipeRefreshLayout = new SwipeRefreshLayout(context) {
            @Override
            public boolean canChildScrollUp() {
                return threadLayout.canChildScrollUp();
            }
        };
        swipeRefreshLayout.addView(threadLayout);

        swipeRefreshLayout.setOnRefreshListener(this);

        if (navigationItem.collapseToolbar) {
            int toolbarHeight = getToolbar().getToolbarHeight();
            swipeRefreshLayout.setProgressViewOffset(false, toolbarHeight - dp(40), toolbarHeight + dp(64 - 40));
        }

        view = swipeRefreshLayout;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        threadLayout.getPresenter().unbindLoadable();

        EventBus.getDefault().unregister(this);
    }

    public abstract void openPin(Pin pin);

    /*
     * Used to save instance state
     */
    public Loadable getLoadable() {
        return threadLayout.getPresenter().getLoadable();
    }

    public void selectPost(int post) {
        threadLayout.getPresenter().selectPost(post);
    }

    @Override
    public boolean onBack() {
        return threadLayout.onBack();
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return threadLayout.sendKeyEvent(event) || super.dispatchKeyEvent(event);
    }

    public void onEvent(Chan.ForegroundChangedMessage message) {
        threadLayout.getPresenter().onForegroundChanged(message.inForeground);
    }

    public void onEvent(RefreshUIMessage message) {
        threadLayout.getPresenter().requestData();
    }

    @Override
    public void onRefresh() {
        threadLayout.refreshFromSwipe();
    }

    @Override
    public NdefMessage createNdefMessage(NfcEvent event) {
        Loadable loadable = getLoadable();
        String url = null;
        NdefMessage message = null;

        if (loadable != null) {
            if (loadable.isThreadMode()) {
                url = ChanUrls.getThreadUrlDesktop(loadable.board, loadable.no);
            } else if (loadable.isCatalogMode()) {
                url = ChanUrls.getCatalogUrlDesktop(loadable.board);
            }
        }

        if (url != null) {
            try {
                Logger.d(TAG, "Pushing url " + url + " to android beam");
                NdefRecord record = NdefRecord.createUri(url);
                message = new NdefMessage(new NdefRecord[]{record});
            } catch (IllegalArgumentException e) {
                Logger.e(TAG, "NdefMessage create error", e);
            }
        }

        return message;
    }

    public void presentRepliesController(Controller controller) {
        presentController(controller);
    }

    @Override
    public void showImages(List<PostImage> images, int index, Loadable loadable, final ThumbnailView thumbnail) {
        // Just ignore the showImages request when the image is not loaded
        if (thumbnail.getBitmap() != null) {
            final ImageViewerNavigationController imageViewerNavigationController = new ImageViewerNavigationController(context);
            presentController(imageViewerNavigationController, false);
            imageViewerNavigationController.showImages(images, index, loadable, this);
        }
    }

    @Override
    public ThumbnailView getPreviewImageTransitionView(ImageViewerController imageViewerController, PostImage postImage) {
        return threadLayout.getThumbnail(postImage);
    }

    public void onPreviewCreate(ImageViewerController imageViewerController) {
//        presentingImageView.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onPreviewDestroy(ImageViewerController imageViewerController) {
//        presentingImageView.setVisibility(View.VISIBLE);
//        presentingImageView = null;
    }

    public void scrollToImage(PostImage postImage) {
        threadLayout.getPresenter().scrollToImage(postImage, true);
    }

    @Override
    public void onShowPosts() {
    }

    @Override
    public void hideSwipeRefreshLayout() {
        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public Toolbar getToolbar() {
        return ((ToolbarNavigationController) navigationController).getToolbar();
    }

    @Override
    public void onSearchVisibilityChanged(boolean visible) {
        threadLayout.getPresenter().onSearchVisibilityChanged(visible);
    }

    @Override
    public void onSearchEntered(String entered) {
        threadLayout.getPresenter().onSearchEntered(entered);
    }

    @Override
    public void openFilterForTripcode(String tripcode) {
        FiltersController filtersController = new FiltersController(context);
        if (splitNavigationController != null) {
            splitNavigationController.pushController(filtersController);
        } else {
            navigationController.pushController(filtersController);
        }
        Filter filter = new Filter();
        filter.type = FilterEngine.FilterType.TRIPCODE.id;
        filter.pattern = tripcode;
        filtersController.showFilterDialog(filter);
    }
}
