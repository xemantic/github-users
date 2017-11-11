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

import com.xemantic.githubusers.logic.error.ErrorAnalyzer;
import com.xemantic.githubusers.logic.event.UserQueryEvent;
import com.xemantic.githubusers.logic.model.SearchResult;
import com.xemantic.githubusers.logic.model.User;
import com.xemantic.githubusers.logic.service.UserService;
import com.xemantic.githubusers.logic.view.UserListView;
import com.xemantic.githubusers.logic.view.UserView;
import io.reactivex.Observable;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import java.util.HashMap;
import java.util.Map;

public class UserListPresenter extends Presenter<UserListView> {

  @Inject UserListPresenter(
      UserListView view,
      Observable<UserQueryEvent> userQuery$,
      UserService userService,
      ErrorAnalyzer errorAnalyzer,
      Provider<UserView> userViewProvider,
      Provider<UserPresenter> userPresenterProvider,
      @Named("userListPageSize") int userListPageSize,
      // "Only the first 1000 search results are available"
      @Named("gitHubUserSearchLimit") int gitHubUserSearchLimit
  ) {
    super(view);

    register(userQuery$.switchMap(event -> {
      if (event.getQuery().trim().isEmpty()) {
        return Observable.empty(); // kick out empty queries, but here so switchMap cancel previous query
      }

      return view.observeLoadMore()
          .scanWith(() -> new Page(1, userListPageSize, gitHubUserSearchLimit), (page, n) -> page.next())
          .flatMap(page -> {
            view.enableLoadMore(false);
            return userService.find(event.getQuery(), page.num, page.size)
                .retry((cnt, ex) -> errorAnalyzer.isRecoverable(ex))
                .doOnSuccess(result -> {
                  if (page.num == 1) view.clear();
                  if (page.hasMore(result)) view.enableLoadMore(true);

                  for (User user : result.getItems()) {
                    view.add(newUserView(user, userPresenterProvider.get(), userViewProvider.get()));
                  }
                }).toObservable();
          });
    }));
  }

  private static UserView newUserView(User user, UserPresenter presenter, UserView view) {
    Map<String, Object> data = new HashMap<>();
    data.put("user", user);
    presenter.request(data);
    return view;
  }

  public static class Page {
    public int num = 0;
    public final int size;
    public final int limit;

    public Page(int num, int size, int limit) {
      this.num = num;
      this.size = size;
      this.limit = limit;
    }

    public Page next() {
      return new Page(num + 1, size, limit);
    }

    public boolean hasMore(SearchResult result) {
      int currentCount = (num - 1) * size + result.getItems().size();
      return currentCount < result.getTotalCount() /* there is more*/ && currentCount < limit;
    }
  }

}
