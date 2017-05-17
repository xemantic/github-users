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

import com.xemantic.githubusers.logic.event.Trigger;
import com.xemantic.githubusers.logic.event.UserQueryEvent;
import com.xemantic.githubusers.logic.eventbus.EventBus;
import com.xemantic.githubusers.logic.model.User;
import com.xemantic.githubusers.logic.service.UserService;
import com.xemantic.githubusers.logic.view.UserListView;
import com.xemantic.githubusers.logic.view.UserView;
import rx.Observable;
import rx.Scheduler;
import rx.Subscription;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import java.util.List;

/**
 * @author morisil
 */
public class UserListPresenter {

  private final UserService userService;

  private final Provider<UserView> userViewProvider;

  private final Provider<UserPresenter> userPresenterProvider;

  private final Scheduler renderingScheduler;

  private final int perPage;

  private int lastPage = 0;

  private UserListView view;

  private EventBus eventBus;

  private Subscription userQuerySubscription;

  private Subscription requestSubscription;

  private String query;

  @Inject
  public UserListPresenter(
      EventBus eventBus,
      UserService userService,
      Provider<UserView> userViewProvider,
      Provider<UserPresenter> userPresenterProvider,
      Scheduler renderingScheduler,
      @Named("userListPageSize") int perPage
  ) {
    this.eventBus = eventBus;
    this.userService = userService;
    this.userViewProvider = userViewProvider;
    this.userPresenterProvider = userPresenterProvider;
    this.renderingScheduler = renderingScheduler;
    this.perPage = perPage;
  }

  public void start(UserListView view) {
    this.view = view;
    userQuerySubscription = eventBus.observe(UserQueryEvent.class)
        .subscribeOn(renderingScheduler)
        .subscribe(event -> handleNewQuery(event.getQuery()));
  }

  private void handleNewQuery(String query) {
    view.enableLoadMore(false);
    view.clear();

    resetRequest(query);

    requestSubscription = Observable.just(Trigger.INSTANCE)
        .concatWith(view.observeLoadMore())
        .doOnNext(e -> view.enableLoadMore(false))
        .flatMapSingle(e -> userService.find(query, ++lastPage, perPage)) // maybe we should have special requestScheduler here?
        .doOnNext(result -> {
          addUsers(result.getItems());
          view.enableLoadMore((lastPage * perPage) <= result.getTotalCount());
        })
        .doOnError(e -> {
          lastPage--;
          view.enableLoadMore(true);
        })
        .subscribeOn(renderingScheduler)
        .subscribe();
  }

  private void resetRequest(String query) {
    if (requestSubscription != null) {
      requestSubscription.unsubscribe();
    }
    this.query = query;
    lastPage = 0;
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

  public void destroy() {
    requestSubscription.unsubscribe();
    userQuerySubscription.unsubscribe();
  }

}
