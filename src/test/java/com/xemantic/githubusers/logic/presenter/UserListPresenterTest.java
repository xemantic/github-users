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
import com.xemantic.githubusers.logic.eventbus.DefaultEventBus;
import com.xemantic.githubusers.logic.eventbus.EventBus;
import com.xemantic.githubusers.logic.eventbus.Trigger;
import com.xemantic.githubusers.logic.event.UserQueryEvent;
import com.xemantic.githubusers.logic.model.SearchResult;
import com.xemantic.githubusers.logic.model.User;
import com.xemantic.githubusers.logic.service.UserService;
import com.xemantic.githubusers.logic.view.UserListView;
import com.xemantic.githubusers.logic.view.UserView;
import org.junit.Test;
import org.mockito.InOrder;
import rx.Observable;
import rx.Single;
import rx.subjects.PublishSubject;

import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

/**
 * Test of the {@link UserListPresenter}.
 *
 * @author morisil
 */
public class UserListPresenterTest {

  private static final int DEFAULT_PAGE_SIZE = 100; // max for GitHub API

  private static final int DEFAULT_USER_SEARCH_LIMIT = 1000; // max for GitHub API

  @Test
  public void start_noInput_shouldDoNothingWithViewAndServices() {
    // given
    EventBus eventBus = new DefaultEventBus();

    UserService userService = mock(UserService.class);
    ErrorAnalyzer errorAnalyzer = mock(ErrorAnalyzer.class);

    UserListView view = mock(UserListView.class);
    given(view.observeLoadMore()).willReturn(Observable.empty());

    UserListPresenter presenter = new UserListPresenter(
        eventBus,
        userService,
        errorAnalyzer,
        () -> mock(UserView.class),
        () -> mock(UserPresenter.class),
        DEFAULT_PAGE_SIZE,
        DEFAULT_USER_SEARCH_LIMIT
    );
    // when
    presenter.start(view);

    // then
    verifyNoMoreInteractions(view, userService, errorAnalyzer);
  }

  @Test
  public void onUserQueryEvent_emptyQueryString_shouldDoNothingWithView() {
    // given
    EventBus eventBus = new DefaultEventBus();

    UserService userService = mock(UserService.class);
    ErrorAnalyzer errorAnalyzer = mock(ErrorAnalyzer.class);

    UserListView view = mock(UserListView.class);
    given(view.observeLoadMore()).willReturn(Observable.empty());

    UserListPresenter presenter = new UserListPresenter(
        eventBus,
        userService,
        errorAnalyzer,
        () -> mock(UserView.class),
        () -> mock(UserPresenter.class),
        DEFAULT_PAGE_SIZE,
        DEFAULT_USER_SEARCH_LIMIT
    );
    presenter.start(view);
    UserQueryEvent event = new UserQueryEvent(" "); // empty string

    // when
    eventBus.post(event);

    // then
    verifyNoMoreInteractions(view, userService, errorAnalyzer);
  }

  @Test
  public void onUserQueryEvent_1UserToBeFound_shouldRetrieveAndDisplay1UserAndDisableLoadMore() {
    // given
    int totalCount = 1;
    EventBus eventBus = new DefaultEventBus();

    User user = mock(User.class);
    UserService userService = mock(UserService.class);
    SearchResult result = mock(SearchResult.class);
    given(result.getTotalCount()).willReturn(totalCount);
    given(result.getItems()).willReturn(Collections.singletonList(user));
    given(userService.find("foo", 1, DEFAULT_PAGE_SIZE)).willReturn(Single.just(result));

    ErrorAnalyzer errorAnalyzer = mock(ErrorAnalyzer.class);

    UserView userView = mock(UserView.class);
    given(userView.observeSelection()).willReturn(Observable.empty());

    UserPresenter userPresenter = mock(UserPresenter.class);

    UserListView view = mock(UserListView.class);
    given(view.observeLoadMore()).willReturn(Observable.empty());

    UserListPresenter presenter = new UserListPresenter(
        eventBus,
        userService,
        errorAnalyzer,
        () -> userView,
        () -> userPresenter,
        DEFAULT_PAGE_SIZE,
        DEFAULT_USER_SEARCH_LIMIT
    );
    presenter.start(view);

    // when
    eventBus.post(new UserQueryEvent("foo"));

    // then
    verify(userPresenter).start(user, userView);
    InOrder inOrder = inOrder(view, userService);
    inOrder.verify(view).observeLoadMore();
    inOrder.verify(view).enableLoadMore(false);
    inOrder.verify(userService).find("foo", 1, DEFAULT_PAGE_SIZE);
    inOrder.verify(view).clear();
    inOrder.verify(view).add(userView);
    // load more stays disabled as it's the last page of results.
    verifyNoMoreInteractions(view, userService, errorAnalyzer);
  }

  @Test
  public void onUserQueryEvent_2UsersToBeFoundWhenPageSizeIs1_shouldRetrieveAndDisplay1UserAndEnableLoadMore() {
    // given
    int pageSize = 1;
    int totalCount = 2;
    EventBus eventBus = new DefaultEventBus();

    User user = mock(User.class);
    UserService userService = mock(UserService.class);
    SearchResult result = mock(SearchResult.class);
    given(result.getTotalCount()).willReturn(totalCount);
    given(result.getItems()).willReturn(Collections.singletonList(user));
    given(userService.find("foo", 1, pageSize)).willReturn(Single.just(result));

    ErrorAnalyzer errorAnalyzer = mock(ErrorAnalyzer.class);

    UserView userView = mock(UserView.class);
    given(userView.observeSelection()).willReturn(Observable.empty());

    UserPresenter userPresenter = mock(UserPresenter.class);

    UserListView view = mock(UserListView.class);
    given(view.observeLoadMore()).willReturn(Observable.empty());

    UserListPresenter presenter = new UserListPresenter(
        eventBus,
        userService,
        errorAnalyzer,
        () -> userView,
        () -> userPresenter,
        pageSize,
        DEFAULT_USER_SEARCH_LIMIT
    );
    presenter.start(view);

    // when
    eventBus.post(new UserQueryEvent("foo"));

    // then
    verify(userPresenter).start(user, userView);
    InOrder inOrder = inOrder(view, userService);
    inOrder.verify(view).observeLoadMore();
    inOrder.verify(view).enableLoadMore(false);
    inOrder.verify(userService).find("foo", 1, pageSize);
    inOrder.verify(view).clear();
    inOrder.verify(view).add(userView);
    inOrder.verify(view).enableLoadMore(true);
    verifyNoMoreInteractions(view, userService, errorAnalyzer);
  }

  @Test
  public void onLoadMore_when1pageIsAlreadyDisplayedAnd2UsersInTotal_shouldRequestAndDisplayNextPageAndDisableLoadMore() {
    // given
    int pageSize = 1;
    int totalCount = 2;
    EventBus eventBus = new DefaultEventBus();

    User user1 = mock(User.class);
    User user2 = mock(User.class);
    UserService userService = mock(UserService.class);
    SearchResult result1 = mock(SearchResult.class);
    given(result1.getTotalCount()).willReturn(totalCount);
    given(result1.getItems()).willReturn(Collections.singletonList(user1));

    SearchResult result2 = mock(SearchResult.class);
    given(result2.getTotalCount()).willReturn(totalCount);
    given(result2.getItems()).willReturn(Collections.singletonList(user2));
    given(userService.find("foo", 1, pageSize)).willReturn(Single.just(result1));
    given(userService.find("foo", 2, pageSize)).willReturn(Single.just(result2));

    ErrorAnalyzer errorAnalyzer = mock(ErrorAnalyzer.class);

    UserView userView = mock(UserView.class);
    given(userView.observeSelection()).willReturn(Observable.empty());

    UserListView view = mock(UserListView.class);
    PublishSubject<Trigger> loadMoreTrigger = PublishSubject.create();
    given(view.observeLoadMore()).willReturn(loadMoreTrigger);

    UserListPresenter presenter = new UserListPresenter(
        eventBus,
        userService,
        errorAnalyzer,
        () -> userView,
        () -> new UserPresenter(eventBus),
        pageSize,
        DEFAULT_USER_SEARCH_LIMIT
    );
    presenter.start(view);
    eventBus.post(new UserQueryEvent("foo"));

    // when
    loadMoreTrigger.onNext(Trigger.INSTANCE);

    // then
    InOrder inOrder = inOrder(view, userService);
    inOrder.verify(view).observeLoadMore();
    inOrder.verify(view).enableLoadMore(false);
    inOrder.verify(userService).find("foo", 1, 1);
    inOrder.verify(view).clear();
    inOrder.verify(view).add(userView);
    inOrder.verify(view).enableLoadMore(true);
    inOrder.verify(view).enableLoadMore(false);
    inOrder.verify(userService).find("foo", 2, 1);
    inOrder.verify(view).add(userView);
    verifyNoMoreInteractions(view, userService, errorAnalyzer);
  }

  @Test
  public void onLoadAll1000Users_when1001UsersFoundInTotal_shouldRequestAndDisplay1000UsersIn10PagesAndThenDisableLoadMore() {
    // given
    int totalCount = 1001;  // higher than max limit of 1000
    EventBus eventBus = new DefaultEventBus();

    User user = mock(User.class);
    UserService userService = mock(UserService.class);
    SearchResult result = mock(SearchResult.class);
    given(result.getTotalCount()).willReturn(totalCount);
    given(result.getItems()).willReturn(Collections.nCopies(DEFAULT_PAGE_SIZE, user));
    given(userService.find(eq("foo"), anyInt(), eq(DEFAULT_PAGE_SIZE))).willReturn(Single.just(result));

    ErrorAnalyzer errorAnalyzer = mock(ErrorAnalyzer.class);

    UserView userView = mock(UserView.class);
    given(userView.observeSelection()).willReturn(Observable.empty());

    UserListView view = mock(UserListView.class);
    PublishSubject<Trigger> loadMoreTrigger = PublishSubject.create();
    given(view.observeLoadMore()).willReturn(loadMoreTrigger);

    UserListPresenter presenter = new UserListPresenter(
        eventBus,
        userService,
        errorAnalyzer,
        () -> userView,
        () -> new UserPresenter(eventBus),
        DEFAULT_PAGE_SIZE,
        DEFAULT_USER_SEARCH_LIMIT
    );
    presenter.start(view);

    // when
    eventBus.post(new UserQueryEvent("foo"));
    for (int i = 2; i <= 10; i++) { // starting with 2 as the page 1 is loaded immediately after query event
      loadMoreTrigger.onNext(Trigger.INSTANCE);
    }

    // then
    verify(view).observeLoadMore();
    verify(view).clear();
    verify(userService, times(10)).find(eq("foo"), anyInt(), eq(DEFAULT_PAGE_SIZE));
    verify(view, times(1000)).add(userView);
    verify(view, times(10)).enableLoadMore(false);
    verify(view, times(9)).enableLoadMore(true);
    /*
     * it would be better to actually check the latest operation if it leaves loadMore disabled,
     * but there are no good mockito matchers, maybe should be reworked to use ArgumentCaptor instead
     */
    verifyNoMoreInteractions(view, userService, errorAnalyzer);
  }

  @Test
  public void onUserQueryEvent_whenAlreadyHandlingOneQueryAndRequest_shouldCancelOldRequest() {
    // given
    UserQueryEvent event1 = new UserQueryEvent("foo");
    UserQueryEvent event2 = new UserQueryEvent("bar");
    PublishSubject<UserQueryEvent> events$ = PublishSubject.create();
    EventBus eventBus = mock(EventBus.class);
    given(eventBus.observe(UserQueryEvent.class)).willReturn(events$);
    doAnswer(invocation -> {events$.onNext(event1); return null;})
        .when(eventBus).post(event1);
    doAnswer(invocation -> {events$.onNext(event2); return null;})
        .when(eventBus).post(event2);

    PublishSubject<SearchResult> request1$ = PublishSubject.create();
    PublishSubject<SearchResult> request2$ = PublishSubject.create();

    UserService userService = mock(UserService.class);
    given(userService.find("foo", 1, DEFAULT_PAGE_SIZE)).willReturn(request1$.toSingle());
    given(userService.find("bar", 1, DEFAULT_PAGE_SIZE)).willReturn(request2$.toSingle());

    ErrorAnalyzer errorAnalyzer = mock(ErrorAnalyzer.class);

    UserListView view = mock(UserListView.class);
    PublishSubject<Trigger> loadMoreTrigger = PublishSubject.create();
    given(view.observeLoadMore()).willReturn(loadMoreTrigger);

    UserListPresenter presenter = new UserListPresenter(
        eventBus,
        userService,
        errorAnalyzer,
        () -> mock(UserView.class),
        () -> new UserPresenter(eventBus),
        DEFAULT_PAGE_SIZE,
        DEFAULT_USER_SEARCH_LIMIT
    );

    // intermediate state check
    assertThat(events$.hasObservers(), is(false));
    assertThat(request1$.hasObservers(), is(false));
    assertThat(request2$.hasObservers(), is(false));

    presenter.start(view);

    // intermediate state check
    assertThat(events$.hasObservers(), is(true));
    assertThat(request1$.hasObservers(), is(false));
    assertThat(request2$.hasObservers(), is(false));

    eventBus.post(event1);

    // intermediate state check
    assertThat(events$.hasObservers(), is(true));
    assertThat(request1$.hasObservers(), is(true));
    assertThat(request2$.hasObservers(), is(false));

    // when
    eventBus.post(event2);

    // then
    InOrder inOrder = inOrder(eventBus, userService);
    inOrder.verify(eventBus).observe(UserQueryEvent.class);
    inOrder.verify(eventBus).post(event1);
    inOrder.verify(userService).find("foo", 1, DEFAULT_PAGE_SIZE);
    inOrder.verify(eventBus).post(event2);
    inOrder.verify(userService).find("bar", 1, DEFAULT_PAGE_SIZE);

    assertThat(events$.hasObservers(), is(true));
    // request1 is eventually unsubscribed, which implies pending request is cancelled
    assertThat(request1$.hasObservers(), is(false));
    assertThat(request2$.hasObservers(), is(true));

    verifyNoMoreInteractions(eventBus, userService, errorAnalyzer);
  }

  @Test
  public void onErrorInRequest_errorIsRecoverable_shouldRecoverWhenLoadMoreIsTriggered() {
    // given
    @SuppressWarnings("ThrowableNotThrown")
    RuntimeException error = new RuntimeException();
    int totalCount = 2;
    int pageSize = 2;
    EventBus eventBus = new DefaultEventBus();

    User user = mock(User.class);
    UserService userService = mock(UserService.class);
    SearchResult result = mock(SearchResult.class);
    given(result.getTotalCount()).willReturn(totalCount);
    given(result.getItems()).willReturn(Collections.singletonList(user));
    given(userService.find("foo", 1, pageSize))
        .willThrow(error)
        .willReturn(Single.just(result));

    ErrorAnalyzer errorAnalyzer = mock(ErrorAnalyzer.class);
    given(errorAnalyzer.isRecoverable(any(Throwable.class))).willReturn(true);

    UserListView view = mock(UserListView.class);
    PublishSubject<Trigger> loadMoreTrigger = PublishSubject.create();
    given(view.observeLoadMore()).willReturn(loadMoreTrigger);

    UserView userView = mock(UserView.class);

    UserListPresenter presenter = new UserListPresenter(
        eventBus,
        userService,
        errorAnalyzer,
        () -> userView,
        () -> mock(UserPresenter.class),
        pageSize,
        DEFAULT_USER_SEARCH_LIMIT
    );
    presenter.start(view);
    eventBus.post(new UserQueryEvent("foo"));

    // when - after error
    loadMoreTrigger.onNext(Trigger.INSTANCE);

    // then
    InOrder inOrder = inOrder(view, userService, errorAnalyzer);
    inOrder.verify(view).observeLoadMore();
    inOrder.verify(view).enableLoadMore(false);
    inOrder.verify(userService).find("foo", 1, pageSize);
    inOrder.verify(view).enableLoadMore(true); // always re enable in case of error
    inOrder.verify(errorAnalyzer).isRecoverable(error); // will resubscribe

    // and now assumptions after loadMoreTrigger was called
    inOrder.verify(view).enableLoadMore(false);
    inOrder.verify(userService).find("foo", 1, pageSize); // the same request
    inOrder.verify(view).clear();
    inOrder.verify(view).add(userView);
    inOrder.verify(view).enableLoadMore(true); // one more user to load
    verifyNoMoreInteractions(view, userService, errorAnalyzer);
  }

  @Test
  public void onErrorInRequest_errorIsNonRecoverable_shouldDoNothingOnLoadMoreAndRethrow() {
    // given
    @SuppressWarnings("ThrowableNotThrown")
    RuntimeException error = new RuntimeException();

    EventBus eventBus = new DefaultEventBus();

    UserService userService = mock(UserService.class);
    given(userService.find("foo", 1, DEFAULT_PAGE_SIZE)).willThrow(error);

    ErrorAnalyzer errorAnalyzer = mock(ErrorAnalyzer.class);
    given(errorAnalyzer.isRecoverable(any(Throwable.class))).willReturn(false);

    UserListView view = mock(UserListView.class);
    PublishSubject<Trigger> loadMoreTrigger = PublishSubject.create();
    given(view.observeLoadMore()).willReturn(loadMoreTrigger);

    UserListPresenter presenter = new UserListPresenter(
        eventBus,
        userService,
        errorAnalyzer,
        () -> mock(UserView.class),
        () -> mock(UserPresenter.class),
        DEFAULT_PAGE_SIZE,
        DEFAULT_USER_SEARCH_LIMIT
    );
    presenter.start(view);

    // when
    RuntimeException thrown;
    try {
      eventBus.post(new UserQueryEvent("foo"));
      throw new AssertionError("Should throw exception");
    } catch (RuntimeException e) {
      thrown = e;
    }

    loadMoreTrigger.onNext(Trigger.INSTANCE);

    // then
    InOrder inOrder = inOrder(view, userService, errorAnalyzer);
    inOrder.verify(view).observeLoadMore();
    inOrder.verify(view).enableLoadMore(false);
    inOrder.verify(userService).find("foo", 1, DEFAULT_PAGE_SIZE);
    inOrder.verify(view).enableLoadMore(true); // always re enable in case of error
    inOrder.verify(errorAnalyzer).isRecoverable(error);
    verifyNoMoreInteractions(view, userService, errorAnalyzer);

    assertThat(thrown.getCause(), equalTo(error));
  }

  @Test
  public void stop_whileHandlingRequest_shouldUnsubscribeFromEventBusCancelRequestAndUnbindView() {
    // given
    UserQueryEvent event = new UserQueryEvent("foo");
    PublishSubject<UserQueryEvent> events$ = PublishSubject.create();
    EventBus eventBus = mock(EventBus.class);
    given(eventBus.observe(UserQueryEvent.class)).willReturn(events$);
    doAnswer(invocation -> {events$.onNext(event); return null;})
        .when(eventBus).post(event);

    PublishSubject<SearchResult> request$ = PublishSubject.create();

    UserService userService = mock(UserService.class);
    given(userService.find("foo", 1, DEFAULT_PAGE_SIZE)).willReturn(request$.toSingle());

    ErrorAnalyzer errorAnalyzer = mock(ErrorAnalyzer.class);

    UserListView view = mock(UserListView.class);
    PublishSubject<Trigger> loadMoreTrigger = PublishSubject.create();
    given(view.observeLoadMore()).willReturn(loadMoreTrigger);

    UserListPresenter presenter = new UserListPresenter(
        eventBus,
        userService,
        errorAnalyzer,
        () -> mock(UserView.class),
        () -> new UserPresenter(eventBus),
        DEFAULT_PAGE_SIZE,
        DEFAULT_USER_SEARCH_LIMIT
    );
    presenter.start(view);
    eventBus.post(event);

    // intermediate check
    assertThat(request$.hasObservers(), is(true));

    // when
    presenter.stop();

    // then
    InOrder inOrder = inOrder(eventBus, userService);
    inOrder.verify(eventBus).observe(UserQueryEvent.class);
    inOrder.verify(eventBus).post(event);
    inOrder.verify(userService).find("foo", 1, DEFAULT_PAGE_SIZE);
    verifyNoMoreInteractions(eventBus, userService, errorAnalyzer);

    assertThat(events$.hasObservers(), is(false));
    assertThat(request$.hasObservers(), is(false));
    assertThat(loadMoreTrigger.hasObservers(), is(false));
  }

}
