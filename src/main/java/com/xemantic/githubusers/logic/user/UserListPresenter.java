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
import com.xemantic.ankh.shared.presenter.Presenter;
import com.xemantic.ankh.shared.request.Page;
import com.xemantic.githubusers.logic.event.UserQueryEvent;
import com.xemantic.githubusers.logic.model.SearchResult;
import com.xemantic.githubusers.logic.model.User;
import com.xemantic.githubusers.logic.service.UserService;
import io.reactivex.Observable;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import java.util.LinkedList;
import java.util.List;

/**
 * Presenter of the {@link UserListView}.
 *
 * @author morisil
 */
public class UserListPresenter extends Presenter {

  private final Observable<UserQueryEvent> userQuery$;

  private final UserService userService;

  private final Provider<UserView> userViewProvider;

  private final Provider<UserPresenter> userPresenterProvider;

  private final List<UserPresenter> displayedUserPresenters = new LinkedList<>();

  private final int userListPageSize;

  private final int gitHubUserSearchLimit;

  private UserListView view;

  @Inject
  public UserListPresenter(
      Observable<UserQueryEvent> userQuery$,
      UserService userService,
      Provider<UserView> userViewProvider,
      Provider<UserPresenter> userPresenterProvider,
      @Named("userListPageSize") int userListPageSize,
      // "Only the first 1000 search results are available"
      @Named("gitHubUserSearchLimit") int gitHubUserSearchLimit
  ) {
    this.userQuery$ = userQuery$;
    this.userService = userService;
    this.userViewProvider = userViewProvider;
    this.userPresenterProvider = userPresenterProvider;
    this.userListPageSize = userListPageSize;
    this.gitHubUserSearchLimit = gitHubUserSearchLimit;
  }

  /**
   * Starts and initializes the presenter with given {@code view}.
   *
   * @param view the view.
   */
  public void start(UserListView view) {
    this.view = view;
    on(userQuery$
        .filter(event -> ! event.getQuery().trim().isEmpty())  // kick out empty queries
        .map(UserQueryEvent::getQuery)
        .switchMap(this::loadUsers)// will dispose stale subscriptions from previous requests
    ).call(this::handlePage);
  }

  @Override
  public void stop() {
    super.stop();
    clearDisplayedUserPresenters();
  }

  private Observable<Page<SearchResult>> loadUsers(String query) {
    return Page.emitPagesOn(view.loadMoreIntent$())
        .doOnNext(page -> {
          view.enableLoadMore(false);
          if (page == 1) { view.loadingFirstPage(true); }
        })
        .flatMapSingle(page ->
            userService.find(query, page, userListPageSize)
                .map(result -> new Page<>(page, result))
        )
        .retry(throwable -> {
          cleanViewOnError();
          Errors.onError(throwable);
          return true;
        });
  }

  private void cleanViewOnError() {
    view.enableLoadMore(true);
    view.loadingFirstPage(false);
  }

  private void handlePage(Page<SearchResult> page) {
    if (page.getNumber() == 1) {
      view.loadingFirstPage(false);
      view.clear();
      clearDisplayedUserPresenters();
    }
    page.getPayload()
        .getItems()
        .forEach(user -> view.add(newUserView(user)));
    if (!isLast(page)) {
      view.enableLoadMore(true);
    }
  }

  private void clearDisplayedUserPresenters() {
    displayedUserPresenters.forEach(Presenter::stop);
    displayedUserPresenters.clear();
  }

  private boolean isLast(Page<SearchResult> page) {
    int currentCount = (
        ((page.getNumber() - 1) * userListPageSize)
            + page.getPayload().getItems().size()
    );
    return (
        (currentCount >= page.getPayload().getTotalCount()) // there is more
            || (currentCount >= gitHubUserSearchLimit)
    );
  }

  private UserView newUserView(User user) {
    UserView view = userViewProvider.get();
    UserPresenter presenter = userPresenterProvider.get();
    presenter.start(user, view);
    displayedUserPresenters.add(presenter);
    return view;
  }

}
