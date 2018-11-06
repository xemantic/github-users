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

package com.xemantic.githubusers.logic.user;

import com.xemantic.ankh.shared.event.Sink;
import com.xemantic.ankh.shared.presenter.Presenter;
import com.xemantic.githubusers.logic.event.UserQueryEvent;

import javax.inject.Inject;

/**
 * Presenter of the {@link UserQueryView}.
 *
 * @author morisil
 */
public class UserQueryPresenter extends Presenter {

  @Inject
  public UserQueryPresenter(
      UserQueryView view,
      Sink<UserQueryEvent> userQuerySink
  ) {
    super(
        view.queryInput$().doOnNext(query ->
            userQuerySink.publish(new UserQueryEvent(query))
        )
    );
  }

}
