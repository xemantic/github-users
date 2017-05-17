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
import com.xemantic.githubusers.logic.model.SearchResult;
import com.xemantic.githubusers.logic.model.User;
import com.xemantic.githubusers.logic.service.UserService;
import com.xemantic.githubusers.logic.view.UserListView;
import com.xemantic.githubusers.logic.view.UserView;
import org.junit.Test;
import org.mockito.InOrder;
import rx.Observable;
import rx.Single;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

import java.util.Collections;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

/**
 * Test of the {@link UserListPresenter}.
 *
 * @author morisil
 */
public class UserListPresenterTest {

  @Test
  public void start_noQueryEvent_shouldDoNothingWithView() {
    // given
    EventBus eventBus = new EventBus();

    UserService userService = mock(UserService.class);

    UserListView view = mock(UserListView.class);
    given(view.observeLoadMore()).willReturn(Observable.empty());

    UserListPresenter presenter = new UserListPresenter(
        eventBus,
        userService,
        () -> mock(UserView.class),
        () -> mock(UserPresenter.class),
        Schedulers.immediate(),
        10
    );

    // when
    presenter.start(view);

    // then
    verifyNoMoreInteractions(view, userService);
  }

  @Test
  public void onUserQueryEvent_1user_shouldRetrieveDataAndSetSubView() {
    // given
    EventBus eventBus = new EventBus();

    User user = mock(User.class);
    UserService userService = mock(UserService.class);
    SearchResult result = mock(SearchResult.class);
    given(result.getTotalCount()).willReturn(1);
    given(result.getItems()).willReturn(Collections.singletonList(user));
    given(userService.find("foo", 1, 10)).willReturn(Single.just(result));

    UserView userView = mock(UserView.class);
    given(userView.observeSelection()).willReturn(Observable.empty());

    UserPresenter userPresenter = mock(UserPresenter.class);

    UserListView view = mock(UserListView.class);
    given(view.observeLoadMore()).willReturn(Observable.empty());

    UserListPresenter presenter = new UserListPresenter(
        eventBus,
        userService,
        () -> userView,
        () -> userPresenter,
        Schedulers.immediate(),
        10
    );
    presenter.start(view);

    // when
    eventBus.post(new UserQueryEvent("foo"));

    // then
    verify(userPresenter).start(user, userView);
    InOrder inOrder = inOrder(view, userService);
    inOrder.verify(view).enableLoadMore(false);
    inOrder.verify(view).clear();
    inOrder.verify(view).observeLoadMore();
    inOrder.verify(view).enableLoadMore(false);
    inOrder.verify(userService).find("foo", 1, 10);
    inOrder.verify(view).add(userView);
    inOrder.verify(view).enableLoadMore(false); // the last page
    verifyNoMoreInteractions(view, userService);
  }

  @Test
  public void start_loadMore_shouldRetrieveDataAndSetSubViews() {
    // given
    EventBus eventBus = new EventBus();

    User user1 = mock(User.class);
    User user2 = mock(User.class);
    UserService userService = mock(UserService.class);
    SearchResult result1 = mock(SearchResult.class);
    given(result1.getTotalCount()).willReturn(2);
    given(result1.getItems()).willReturn(Collections.singletonList(user1));
    SearchResult result2 = mock(SearchResult.class);
    given(result2.getTotalCount()).willReturn(2);
    given(result2.getItems()).willReturn(Collections.singletonList(user2));
    given(userService.find("foo", 1, 1)).willReturn(Single.just(result1));
    given(userService.find("foo", 2, 1)).willReturn(Single.just(result2));

    UserView userView = mock(UserView.class);
    given(userView.observeSelection()).willReturn(Observable.empty());

    UserListView view = mock(UserListView.class);
    PublishSubject<Trigger> loadMoreTrigger = PublishSubject.create();
    given(view.observeLoadMore()).willReturn(loadMoreTrigger);

    UserListPresenter presenter = new UserListPresenter(
        eventBus,
        userService,
        () -> userView,
        () -> new UserPresenter(eventBus),
        Schedulers.immediate(),
        1
    );
    presenter.start(view);
    eventBus.post(new UserQueryEvent("foo"));

    // when
    loadMoreTrigger.onNext(Trigger.INSTANCE);

    // then
    InOrder inOrder = inOrder(view, userService);
    inOrder.verify(view).enableLoadMore(false);
    inOrder.verify(view).clear();
    inOrder.verify(view).observeLoadMore();
    inOrder.verify(view).enableLoadMore(false);
    inOrder.verify(userService).find("foo", 1, 1);
    inOrder.verify(view).add(userView);
    inOrder.verify(view).enableLoadMore(true);
    inOrder.verify(view).enableLoadMore(false);
    inOrder.verify(userService).find("foo", 2, 1);
    inOrder.verify(view).add(userView);
    inOrder.verify(view).enableLoadMore(true); // the last page
    verifyNoMoreInteractions(view, userService);
  }

}
