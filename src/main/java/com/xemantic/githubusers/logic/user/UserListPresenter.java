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

import com.xemantic.ankh.shared.error.Errors;
import com.xemantic.ankh.shared.event.Trigger;
import com.xemantic.ankh.shared.presenter.Presenter;
import com.xemantic.githubusers.logic.event.UserQueryEvent;
import com.xemantic.githubusers.logic.model.SearchResult;
import com.xemantic.githubusers.logic.model.User;
import com.xemantic.githubusers.logic.service.UserService;
import io.reactivex.Observable;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.LinkedList;
import java.util.List;

/**
 * Presenter of the {@link UserListView}.
 *
 * @author morisil
 */
public class UserListPresenter extends Presenter {

  private final UserPresenterFactory userPresenterFactory;

  private final List<UserPresenter> activeUserPresenters = new LinkedList<>();

  @Inject
  public UserListPresenter(
      UserListView view,
      Observable<UserQueryEvent> userQuery$,
      UserService userService,
      UserPresenterFactory userPresenterFactory,
      @Named("userListPageSize") int pageSize,
      // API docs: "Only the first 1000 search results are available"
      @Named("gitHubUserSearchLimit") int userSearchLimit
  ) {

    this.userPresenterFactory = userPresenterFactory;

    register(
        // task: transform stream of search queries into stream of users
        // the challenge: delayed request paging happens in-between
        userQuery$
            .map(UserQueryEvent::getQuery)
            .filter(query -> !query.trim().isEmpty())  // kick out empty queries
            .switchMap(query -> {
                  Observable<Trigger> oneTime = Trigger.oneTime();
                  return Observable.range(1, Integer.MAX_VALUE) // paging
                      .concatMap(page ->
                          oneTime // it will always attempt to populate the first page on start
                              .mergeWith(view.loadMoreIntent$())
                              .take(1)
                              .doOnNext(trigger -> {
                                view.enableLoadMore(false);
                                if (page == 1) {
                                  // users from previous query will be still displayed for a while
                                  view.loadingFirstPage(true);
                                }
                              })
                              .flatMapSingle(trigger ->
                                  userService.find(query, page, pageSize)
                              )
                              .doOnNext(result -> {
                                if (hasNext(page, result, pageSize, userSearchLimit)) {
                                  view.enableLoadMore(true);
                                }
                                if (page == 1) {
                                  clearOnFirstPage(view);
                                }
                              })
                              .retry(throwable -> {
                                Errors.onError(throwable);
                                view.enableLoadMore(true);
                                if (page == 1) {
                                  clearOnFirstPage(view);
                                }
                                return true;
                              })
                      );
                },
                1 // one page to prefetch
            )
            .flatMapIterable(SearchResult::getItems)
            .doOnNext(user -> view.add(newUserView(user)))
    );
  }

  @Override
  public void stop() {
    super.stop();
    clearActiveUserPresenters();
  }

  private void clearOnFirstPage(UserListView view) {
    view.loadingFirstPage(false);
    view.clear();
    clearActiveUserPresenters();
  }

  private void clearActiveUserPresenters() {
    for (Presenter presenter : activeUserPresenters) {
      presenter.stop();
    }
    activeUserPresenters.clear();
  }

  private static boolean hasNext(
      int page,
      SearchResult result,
      int pageSize,
      int elementLimit
  ) {
    int currentCount = (
        ((page - 1) * pageSize)
            + result.getItems().size()
    );
    return (
        (currentCount < result.getTotalCount())
            && (currentCount < elementLimit)
    );
  }

  private UserView newUserView(User user) {
    UserPresenter presenter = userPresenterFactory.create(user);
    activeUserPresenters.add(presenter);
    presenter.start();
    return presenter.getView();
  }

}
