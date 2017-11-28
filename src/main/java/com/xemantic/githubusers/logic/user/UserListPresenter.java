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
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Presenter of the {@link UserListView}.
 *
 * @author morisil
 */
public class UserListPresenter extends Presenter {

  private final UserListView view;

  private final UserPresenterFactory userPresenterFactory;

  private final List<UserPresenter> displayedUserPresenters = new LinkedList<>();

  private final int userListPageSize;

  private final int gitHubUserSearchLimit;

  @Inject
  public UserListPresenter(
      UserListView view,
      Observable<UserQueryEvent> userQuery$,
      UserService userService,
      UserPresenterFactory userPresenterFactory,
      @Named("userListPageSize") int userListPageSize,
      // "Only the first 1000 search results are available"
      @Named("gitHubUserSearchLimit") int gitHubUserSearchLimit
  ) {

    this.view = view;
    this.userPresenterFactory = userPresenterFactory;
    this.userListPageSize = userListPageSize;
    this.gitHubUserSearchLimit = gitHubUserSearchLimit;

    register(userQuery$
        .filter(event -> ! event.getQuery().trim().isEmpty())  // kick out empty queries
        .map(UserQueryEvent::getQuery)
        .switchMap(query -> { // switchMap will dispose stale subscriptions from previous request
          AtomicInteger pager = new AtomicInteger(1);
          return Trigger.oneTime().mergeWith(view.loadMoreIntent$())
              .doOnNext(trigger -> indicateRequest(pager.get()))
              .flatMapSingle(trigger ->
                  userService.find(query, pager.get(), userListPageSize)
              )
              .doOnNext(result -> {
                handlePage(pager.get(), result);
                pager.incrementAndGet();
              })
              .retry(throwable -> { // retry here because of the switchMap, we want to stay on the same page
                cleanViewOnError();
                Errors.onError(throwable);
                return true;
              });
        })
    );
  }

  @Override
  public void stop() {
    super.stop();
    clearDisplayedUserPresenters();
  }

  private void indicateRequest(int page) {
    view.enableLoadMore(false);
    if (page == 1) { view.loadingFirstPage(true); }
  }

  private void cleanViewOnError() {
    view.enableLoadMore(true);
    view.loadingFirstPage(false);
  }

  private void handlePage(int page, SearchResult result) {
    if (page == 1) {
      view.loadingFirstPage(false);
      view.clear();
      clearDisplayedUserPresenters();
    }
    for (User user : result.getItems()) {
      view.add(newUserView(user));
    }
    if (!isLast(page, result)) {
      view.enableLoadMore(true);
    }
  }

  private void clearDisplayedUserPresenters() {
    for (Presenter presenter : displayedUserPresenters) {
      presenter.stop();
    }
    displayedUserPresenters.clear();
  }

  private boolean isLast(int page, SearchResult result) {
    int currentCount = (
        ((page - 1) * userListPageSize)
            + result.getItems().size()
    );
    return (
        (currentCount >= result.getTotalCount())
            || (currentCount >= gitHubUserSearchLimit)
    );
  }

  private UserView newUserView(User user) {
    UserPresenter presenter = userPresenterFactory.create(user);
    displayedUserPresenters.add(presenter);
    presenter.start();
    return presenter.getView();
  }

}
