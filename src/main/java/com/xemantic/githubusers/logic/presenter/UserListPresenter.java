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
import com.xemantic.githubusers.logic.eventbus.EventBus;
import com.xemantic.githubusers.logic.eventbus.Trigger;
import com.xemantic.githubusers.logic.event.UserQueryEvent;
import com.xemantic.githubusers.logic.model.SearchResult;
import com.xemantic.githubusers.logic.model.User;
import com.xemantic.githubusers.logic.service.UserService;
import com.xemantic.githubusers.logic.view.UserListView;
import com.xemantic.githubusers.logic.view.UserView;
import rx.Single;
import rx.Subscription;
import rx.plugins.RxJavaHooks;
import rx.subjects.PublishSubject;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import java.util.List;

/**
 * Presenter of the {@link UserListView}.
 *
 * @author morisil
 */
public class UserListPresenter {

  private final EventBus eventBus;

  private final UserService userService;

  private final ErrorAnalyzer errorAnalyzer;

  private final Provider<UserView> userViewProvider;

  private final Provider<UserPresenter> userPresenterProvider;

  private final int userListPageSize;

  private final int gitHubUserSearchLimit;

  /*
   * we don't care about synchronization of currentPage and query
   * because these fields are supposed to be always mutated from the
   * rendering thread of each platform (UI rendering is single-threaded everywhere)
   */
  private int currentPage = 0;

  private String query;

  private UserListView view;

  private Subscription userQuerySubscription;

  private Subscription requestSubscription;

  @Inject
  public UserListPresenter(
      EventBus eventBus,
      UserService userService,
      ErrorAnalyzer errorAnalyzer,
      Provider<UserView> userViewProvider,
      Provider<UserPresenter> userPresenterProvider,
      @Named("userListPageSize") int userListPageSize,
      // "Only the first 1000 search results are available"
      @Named("gitHubUserSearchLimit") int gitHubUserSearchLimit
  ) {
    this.eventBus = eventBus;
    this.userService = userService;
    this.errorAnalyzer = errorAnalyzer;
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
    userQuerySubscription = eventBus.observe(UserQueryEvent.class)
        .filter(event -> ! event.getQuery().trim().isEmpty()) // kick out empty queries
        .subscribe(event -> handleNewQuery(event.getQuery()));
  }

  /**
   * Stops this presenter. This method is supposed to be called
   * when it is required to cancel all the pending HTTP requests, and
   * unsubscribe from the {@link EventBus}. for example when user is navigating
   * to another {@code Activity} on Android platform.
   */
  public void stop() {
    requestSubscription.unsubscribe();
    userQuerySubscription.unsubscribe();
  }

  private void handleNewQuery(String query) {

    prepareNewRequestStream(query);

    PublishSubject<Trigger> firstTrigger = PublishSubject.create();

    requestSubscription = view.observeLoadMore()
        .mergeWith(firstTrigger)
        .doOnNext(e -> afterLoadMoreEvent())
        .flatMapSingle(e -> requestPage())
        .doOnNext(this::handleResult)
        .doOnError(this::handleError)
        .retry((integer, throwable) -> errorAnalyzer.isRecoverable(throwable))
        .subscribe();

    firstTrigger.onNext(Trigger.INSTANCE);
    /*
      This needs some explanation, in the original code the whole request
      observable was started with Observable.just(Trigger.INSTANCE),
      to push initial trigger, and then it was merged with the view.observeLoadMore().
      But it didn't work as expected with the .retry() logic which is
      resubscribing to the whole already defined observable therefore it was also
      emitting the first trigger. For this reason I used PublishSubject to make the
      initial trigger conditional and use it only when new query arrives.
     */
  }

  private void prepareNewRequestStream(String query) {
    this.query = query;
    if (requestSubscription != null) {
      requestSubscription.unsubscribe();
    }
    currentPage = 0;
  }

  private void afterLoadMoreEvent() {
    view.enableLoadMore(false);
    currentPage++;
  }

  private Single<SearchResult> requestPage() {
    return userService.find(query, currentPage, userListPageSize);
  }

  private void handleResult(SearchResult result) {
    if (currentPage == 1) {
      view.clear();
    }
    addUsers(result.getItems());
    if (shouldEnableLoadMore(result)) {
      view.enableLoadMore(true);
    }
  }

  private boolean shouldEnableLoadMore(SearchResult result) {
    int currentCount = (
        ((currentPage - 1) * userListPageSize)
            + result.getItems().size()
    );
    return (
        (currentCount < result.getTotalCount()) // there is more
            && (currentCount < gitHubUserSearchLimit)
    );
  }

  private void handleError(Throwable e) {
    currentPage--;
    view.enableLoadMore(true); // give it a chance to retry
    RxJavaHooks.onError(e); // just to log unexpected exception, is it possible to do it globally for all doOnErrors?
  }

  private void addUsers(List<User> users) {
    for (User user : users) {
      view.add(newUserView(user));
    }
  }

  private UserView newUserView(User user) {
    UserView view = userViewProvider.get();
    UserPresenter presenter = userPresenterProvider.get();
    presenter.start(user, view);
    return view;
  }

}
