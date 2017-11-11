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

public class DrawerPresenter extends Presenter<DrawerView> {

  @Inject DrawerPresenter(
      DrawerView view,
      @Named("projectGitHubUrl") String projectUrl,
      Sink<SnackbarMessageEvent> snackbarMessageSink,
      UrlOpener urlOpener
  ) {
    super(view);

    register(view.observeOpenDrawerIntent().doOnNext(t -> view.openDrawer(true)));

    register(view.observeReadAboutIntent()
        .map(ev -> new SnackbarMessageEvent("To be implemented soon"))
        .doOnNext(snackbarMessageSink::publish));

    register(view.observeOpenProjectOnGitHubIntent().doOnNext(t -> urlOpener.openUrl(projectUrl)));

    register(view.observeSelectLanguageIntent()
        .map(ev -> new SnackbarMessageEvent("To be implemented soon"))
        .doOnNext(snackbarMessageSink::publish));
  }

}
