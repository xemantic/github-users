/*
 * github-users - lists GitHub users. Minimal app demonstrating
 * cross-platform app development (Web, Android, iOS) where core
 * logic is shared and transpiled from Java to JavaScript and
 * Objective-C. This project delivers core application logic.
 *
 * Copyright (C) 2017  Kazimierz Pogoda
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

package com.xemantic.githubusers.logic.presenter;

import com.xemantic.githubusers.logic.driver.UrlOpener;
import com.xemantic.githubusers.logic.event.Sink;
import com.xemantic.githubusers.logic.event.SnackbarMessageEvent;
import com.xemantic.githubusers.logic.view.DrawerView;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Presenter of the {@link DrawerView}.
 *
 * @author morisil
 */
public class DrawerPresenter {

  private final String projectUrl;

  private final Sink<SnackbarMessageEvent> snackbarMessageSink;

  private final UrlOpener urlOpener;

  @Inject
  public DrawerPresenter(
      @Named("projectGitHubUrl") String projectUrl,
      Sink<SnackbarMessageEvent> snackbarMessageSink,
      UrlOpener urlOpener) {

    this.projectUrl = projectUrl;
    this.snackbarMessageSink = snackbarMessageSink;
    this.urlOpener = urlOpener;
  }

  public void start(DrawerView view) {
    view.observeOpenDrawerIntent().subscribe(t -> view.openDrawer(true));
    view.observeReadAboutIntent().subscribe(t -> snackbarMessageSink.publish(
        new SnackbarMessageEvent("To be implemented soon"))
    );
    view.observeOpenProjectOnGitHubIntent().subscribe(t -> urlOpener.openUrl(projectUrl));
    view.observeSelectLanguageIntent().subscribe(t -> snackbarMessageSink.publish(
        new SnackbarMessageEvent("To be implemented soon"))
    );
  }

}
