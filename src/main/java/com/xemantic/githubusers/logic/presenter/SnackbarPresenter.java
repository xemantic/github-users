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

import com.xemantic.githubusers.logic.event.SnackbarMessageEvent;
import com.xemantic.githubusers.logic.view.SnackbarView;
import io.reactivex.Observable;

import javax.inject.Inject;

public class SnackbarPresenter extends Presenter<SnackbarView> {

  @Inject SnackbarPresenter(
      SnackbarView view,
      Observable<SnackbarMessageEvent> snackbarMessage$
  ) {
    super(view);

    register(snackbarMessage$.doOnNext(e -> view.show(e.getMessage())));
  }

}
