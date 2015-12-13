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

import org.trashtier.chan.R;
import org.trashtier.chan.controller.Controller;
import org.trashtier.chan.controller.ControllerTransition;
import org.trashtier.chan.controller.ui.NavigationControllerContainerLayout;
import org.trashtier.chan.ui.theme.ThemeHelper;
import org.trashtier.chan.ui.toolbar.Toolbar;

public class StyledToolbarNavigationController extends ToolbarNavigationController {
    public StyledToolbarNavigationController(Context context) {
        super(context);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        view = inflateRes(R.layout.controller_navigation_toolbar);
        container = (NavigationControllerContainerLayout) view.findViewById(R.id.container);
        container.setNavigationController(this);
        toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        toolbar.setBackgroundColor(ThemeHelper.getInstance().getTheme().primaryColor.color);
        toolbar.setCallback(this);
    }

    @Override
    public void transition(Controller from, Controller to, boolean pushing, ControllerTransition controllerTransition) {
        super.transition(from, to, pushing, controllerTransition);

        if (to != null) {
            DrawerController drawerController = getDrawerController();
            if (drawerController != null) {
                drawerController.setDrawerEnabled(to.navigationItem.hasDrawer);
            }
        }
    }

    @Override
    public void endSwipeTransition(Controller from, Controller to, boolean finish) {
        super.endSwipeTransition(from, to, finish);

        if (finish) {
            DrawerController drawerController = getDrawerController();
            if (drawerController != null) {
                drawerController.setDrawerEnabled(to.navigationItem.hasDrawer);
            }
        }
    }

    @Override
    public boolean onBack() {
        if (super.onBack()) {
            return true;
        } else if (parentController instanceof PopupController && childControllers.size() == 1) {
            ((PopupController) parentController).dismiss();
            return true;
        } else if (splitNavigationController != null && childControllers.size() == 1) {
            if (splitNavigationController.rightController == this) {
                splitNavigationController.setRightController(null);
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    @Override
    public void onMenuClicked() {
        DrawerController drawerController = getDrawerController();
        if (drawerController != null) {
            drawerController.onMenuClicked();
        }
    }

    private DrawerController getDrawerController() {
        if (parentController instanceof DrawerController) {
            return (DrawerController) parentController;
        } else if (splitNavigationController != null) {
            if (splitNavigationController.parentController instanceof DrawerController) {
                return (DrawerController) splitNavigationController.parentController;
            }
        }
        return null;
    }
}
