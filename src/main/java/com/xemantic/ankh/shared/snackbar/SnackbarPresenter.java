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

package com.xemantic.ankh.shared.snackbar;

import com.xemantic.ankh.shared.event.SnackbarMessageEvent;
import io.reactivex.Observable;

import javax.inject.Inject;

/**
 * Presenter of the {@link SnackbarView}.
 *
 * @author morisil
 */
public class SnackbarPresenter {

  private final Observable<SnackbarMessageEvent> snackbarMessage$;

  @Inject
  public SnackbarPresenter(Observable<SnackbarMessageEvent> snackbarMessage$) {
    this.snackbarMessage$ = snackbarMessage$;
  }

  public void start(SnackbarView view) {
    snackbarMessage$.map(SnackbarMessageEvent::getMessage)
        .subscribe(view::show);
  }

}
