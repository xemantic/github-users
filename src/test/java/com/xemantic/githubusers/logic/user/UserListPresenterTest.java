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

import com.xemantic.githubusers.logic.event.UserSelectedEvent;
import com.xemantic.ankh.shared.event.Trigger;
import com.xemantic.githubusers.logic.event.UserQueryEvent;
import com.xemantic.githubusers.logic.model.SearchResult;
import com.xemantic.githubusers.logic.model.User;
import com.xemantic.githubusers.logic.service.UserService;
import com.xemantic.ankh.test.ExpectedRxJavaError;
import io.reactivex.Single;
import io.reactivex.functions.BiPredicate;
import io.reactivex.subjects.PublishSubject;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mockito.quality.Strictness;

import javax.inject.Provider;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;

import static com.xemantic.ankh.test.TestEvents.noEvents;
import static com.xemantic.ankh.test.TestEvents.trigger;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

/**
 * Test of the {@link UserListPresenter}.
 *
 * @author morisil
 */
public class UserListPresenterTest {

  @Rule
  public MockitoRule mockitoRule = MockitoJUnit.rule().strictness(Strictness.STRICT_STUBS);

  @Rule
  public ExpectedRxJavaError expectedError = ExpectedRxJavaError.none();

  private static final int DEFAULT_PAGE_SIZE = 100; // max for GitHub API

  private static final int DEFAULT_USER_SEARCH_LIMIT = 1000; // max for GitHub API

  @Mock
  private UserListView view;

  @Mock
  private UserService userService;

  @Mock
  private BiPredicate<Integer, Throwable> errorAnalyzer;

  @Mock
  private Provider<UserView> userViewProvider;

  @Mock
  private Provider<UserPresenter> userPresenterProvider;

  @Test
  public void start_noInput_shouldDoNothingWithViewAndServices() {
    // given
    PublishSubject<UserQueryEvent> userQuery$ = PublishSubject.create();
    UserListPresenter presenter = new UserListPresenter(
        userQuery$,
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
    PublishSubject<UserQueryEvent> userQuery$ = PublishSubject.create();
    UserListPresenter presenter = new UserListPresenter(
        userQuery$,
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
    userQuery$.onNext(event);

    // then
    verifyNoMoreInteractions(view, userService, errorAnalyzer);
  }

  @Test
  public void onUserQueryEvent_1UserToBeFound_shouldRetrieveAndDisplay1UserAndDisableLoadMore() {
    // given
    int totalCount = 1;
    PublishSubject<UserQueryEvent> userQuery$ = PublishSubject.create();

    User user = mock(User.class);
    SearchResult result = mock(SearchResult.class);
    given(result.getTotalCount()).willReturn(totalCount);
    given(result.getItems()).willReturn(Collections.singletonList(user));
    given(userService.find("foo", 1, DEFAULT_PAGE_SIZE)).willReturn(Single.just(result));

    UserView userView = mock(UserView.class);
    UserPresenter userPresenter = mock(UserPresenter.class);

    given(view.loadMoreIntent$()).willReturn(noEvents());

    UserListPresenter presenter = new UserListPresenter(
        userQuery$,
        userService,
        errorAnalyzer,
        () -> userView,
        () -> userPresenter,
        DEFAULT_PAGE_SIZE,
        DEFAULT_USER_SEARCH_LIMIT
    );
    presenter.start(view);

    // when
    userQuery$.onNext(new UserQueryEvent("foo"));

    // then
    InOrder inOrder = inOrder(view, userService, userPresenter);
    inOrder.verify(view).loadMoreIntent$();
    inOrder.verify(view).enableLoadMore(false);
    inOrder.verify(userService).find("foo", 1, DEFAULT_PAGE_SIZE);
    inOrder.verify(view).clear();
    inOrder.verify(userPresenter).start(user, userView);
    inOrder.verify(view).add(userView);
    // load more stays disabled as it's the last page of results.
    verifyNoMoreInteractions(view, userService, errorAnalyzer, userPresenter);
  }

  @Test
  public void onUserQueryEvent_2UsersToBeFoundWhenPageSizeIs1_shouldRetrieveAndDisplay1UserAndEnableLoadMore() {
    // given
    int pageSize = 1;
    int totalCount = 2;
    PublishSubject<UserQueryEvent> userQuery$ = PublishSubject.create();

    User user = mock(User.class);
    SearchResult result = mock(SearchResult.class);
    given(result.getTotalCount()).willReturn(totalCount);
    given(result.getItems()).willReturn(Collections.singletonList(user));
    given(userService.find("foo", 1, pageSize)).willReturn(Single.just(result));

    UserView userView = mock(UserView.class);
    UserPresenter userPresenter = mock(UserPresenter.class);

    given(view.loadMoreIntent$()).willReturn(noEvents());

    UserListPresenter presenter = new UserListPresenter(
        userQuery$,
        userService,
        errorAnalyzer,
        () -> userView,
        () -> userPresenter,
        pageSize,
        DEFAULT_USER_SEARCH_LIMIT
    );
    presenter.start(view);

    // when
    userQuery$.onNext(new UserQueryEvent("foo"));

    // then
    InOrder inOrder = inOrder(view, userService, userPresenter);
    inOrder.verify(view).loadMoreIntent$();
    inOrder.verify(view).enableLoadMore(false);
    inOrder.verify(userService).find("foo", 1, pageSize);
    inOrder.verify(view).clear();
    inOrder.verify(userPresenter).start(user, userView);
    inOrder.verify(view).add(userView);
    inOrder.verify(view).enableLoadMore(true);
    verifyNoMoreInteractions(view, userService, errorAnalyzer, userPresenter);
  }

  @Test
  public void onLoadMore_when1pageIsAlreadyDisplayedAnd2UsersInTotal_shouldRequestAndDisplayNextPageAndDisableLoadMore() {
    // given
    int pageSize = 1;
    int totalCount = 2;
    PublishSubject<UserQueryEvent> userQuery$ = PublishSubject.create();

    User user1 = mock(User.class);
    User user2 = mock(User.class);
    SearchResult result1 = mock(SearchResult.class);
    given(result1.getTotalCount()).willReturn(totalCount);
    given(result1.getItems()).willReturn(Collections.singletonList(user1));

    SearchResult result2 = mock(SearchResult.class);
    given(result2.getTotalCount()).willReturn(totalCount);
    given(result2.getItems()).willReturn(Collections.singletonList(user2));
    given(userService.find(anyString(), anyInt(), anyInt()))
        .willReturn(Single.just(result1))
        .willReturn(Single.just(result2));

    UserView userView1 = mock(UserView.class);
    UserPresenter userPresenter1 = mock(UserPresenter.class);
    UserView userView2 = mock(UserView.class);
    UserPresenter userPresenter2 = mock(UserPresenter.class);

    given(userViewProvider.get()).willReturn(userView1, userView2);
    given(userPresenterProvider.get()).willReturn(userPresenter1, userPresenter2);

    PublishSubject<Trigger> loadMoreIntent = PublishSubject.create();
    given(view.loadMoreIntent$()).willReturn(loadMoreIntent);

    UserListPresenter presenter = new UserListPresenter(
        userQuery$,
        userService,
        errorAnalyzer,
        userViewProvider,
        userPresenterProvider,
        pageSize,
        DEFAULT_USER_SEARCH_LIMIT
    );
    presenter.start(view);
    userQuery$.onNext(new UserQueryEvent("foo"));

    // when
    trigger(loadMoreIntent);

    // then
    InOrder inOrder = inOrder(
        view,
        userService,
        userViewProvider,
        userPresenterProvider,
        userPresenter1,
        userPresenter2
    );
    inOrder.verify(view).loadMoreIntent$();
    inOrder.verify(view).enableLoadMore(false);
    inOrder.verify(userService).find("foo", 1, 1);
    inOrder.verify(view).clear();
    inOrder.verify(userPresenter1).start(user1, userView1);
    inOrder.verify(view).add(userView1);
    inOrder.verify(view).enableLoadMore(true);
    inOrder.verify(view).enableLoadMore(false);
    inOrder.verify(userService).find("foo", 2, 1);
    inOrder.verify(userPresenter2).start(user2, userView2);
    inOrder.verify(view).add(userView2);
    verifyNoMoreInteractions(
        view,
        userService,
        errorAnalyzer,
        userViewProvider,
        userPresenterProvider,
        userPresenter1,
        userPresenter2);
  }

  @Test
  public void onLoadAll1000Users_when1001UsersFoundInTotal_shouldRequestAndDisplay1000UsersIn10PagesAndThenDisableLoadMore() {
    // given
    int totalCount = 1001;  // higher than max limit of 1000
    PublishSubject<UserQueryEvent> userQuery$ = PublishSubject.create();
    PublishSubject<UserSelectedEvent> userSelectedBus = PublishSubject.create();

    User user = mock(User.class);
    SearchResult result = mock(SearchResult.class);
    given(result.getTotalCount()).willReturn(totalCount);
    given(result.getItems()).willReturn(Collections.nCopies(DEFAULT_PAGE_SIZE, user));
    given(userService.find(eq("foo"), anyInt(), eq(DEFAULT_PAGE_SIZE))).willReturn(Single.just(result));

    UserView userView = mock(UserView.class);

    PublishSubject<Trigger> loadMoreIntent = PublishSubject.create();
    given(view.loadMoreIntent$()).willReturn(loadMoreIntent);

    UserListPresenter presenter = new UserListPresenter(
        userQuery$,
        userService,
        errorAnalyzer,
        () -> userView,
        () -> mock(UserPresenter.class),
        DEFAULT_PAGE_SIZE,
        DEFAULT_USER_SEARCH_LIMIT
    );
    presenter.start(view);

    // when
    userQuery$.onNext(new UserQueryEvent("foo"));
    for (int i = 2; i <= 10; i++) { // starting with 2 as the page 1 is loaded immediately after query event
      trigger(loadMoreIntent);
    }

    // then
    verify(view).loadMoreIntent$();
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
    int totalCount = 1;
    UserQueryEvent event1 = new UserQueryEvent("foo");
    UserQueryEvent event2 = new UserQueryEvent("bar");
    PublishSubject<UserQueryEvent> userQuery$ = PublishSubject.create();
    PublishSubject<SearchResult> response1$ = PublishSubject.create();
    PublishSubject<SearchResult> response2$ = PublishSubject.create();

    User user = mock(User.class);
    SearchResult result = mock(SearchResult.class);
    given(result.getTotalCount()).willReturn(totalCount);
    given(result.getItems()).willReturn(Collections.singletonList(user));

    given(userService.find(anyString(), anyInt(), anyInt()))
        .willReturn(response1$.singleOrError())
        .willReturn(response2$.singleOrError());

    given(view.loadMoreIntent$()).willReturn(noEvents());

    UserView userView = mock(UserView.class);

    UserListPresenter presenter = new UserListPresenter(
        userQuery$,
        userService,
        errorAnalyzer,
        () -> userView,
        () -> mock(UserPresenter.class),
        DEFAULT_PAGE_SIZE,
        DEFAULT_USER_SEARCH_LIMIT
    );

    // intermediate state check
    assertThat(userQuery$.hasObservers(), is(false));
    assertThat(response1$.hasObservers(), is(false));
    assertThat(response2$.hasObservers(), is(false));

    presenter.start(view);

    // intermediate state check
    assertThat(userQuery$.hasObservers(), is(true));
    assertThat(response1$.hasObservers(), is(false));
    assertThat(response2$.hasObservers(), is(false));

    userQuery$.onNext(event1);

    // intermediate state check
    assertThat(userQuery$.hasObservers(), is(true));
    assertThat(response1$.hasObservers(), is(true));
    assertThat(response2$.hasObservers(), is(false));

    // when
    userQuery$.onNext(event2); // ping
    response2$.onNext(result); // pong

    // intermediate state check
    assertThat(userQuery$.hasObservers(), is(true));
    assertThat(response1$.hasObservers(), is(false));
    assertThat(response2$.hasObservers(), is(true));

    response2$.onComplete();

    // then
    assertThat(userQuery$.hasObservers(), is(true));
    // request1 is eventually unsubscribed, which implies pending request is cancelled
    assertThat(response1$.hasObservers(), is(false));
    assertThat(response2$.hasObservers(), is(false));

    InOrder inOrder = inOrder(view, userService);
    inOrder.verify(view).loadMoreIntent$();
    inOrder.verify(view).enableLoadMore(false);
    inOrder.verify(userService).find("foo", 1, DEFAULT_PAGE_SIZE);
    inOrder.verify(view).loadMoreIntent$();
    inOrder.verify(view).enableLoadMore(false);
    inOrder.verify(userService).find("bar", 1, DEFAULT_PAGE_SIZE);
    inOrder.verify(view).clear();
    inOrder.verify(view).add(userView);

    verifyNoMoreInteractions(view, userService, errorAnalyzer);
  }

  // TODO in the future this test case should be handled by service access layer itself (exponential backoff)
  @Test
  public void onErrorInRequest_errorIsRecoverable_shouldRetryRequest() throws Exception {
    // given
    @SuppressWarnings("ThrowableNotThrown")
    RuntimeException error = new RuntimeException();
    int totalCount = 1;
    int pageSize = 1;
    PublishSubject<UserQueryEvent> userQuery$ = PublishSubject.create();

    User user = mock(User.class);
    SearchResult result = mock(SearchResult.class);
    given(result.getTotalCount()).willReturn(totalCount);
    given(result.getItems()).willReturn(Collections.singletonList(user));

    AtomicInteger requestCounter = new AtomicInteger(0);
    given(userService.find(anyString(), anyInt(), anyInt()))
        .willReturn(
            Single.just(result)
              .doOnEvent((searchResult, throwable) -> {
                  if (requestCounter.incrementAndGet() == 1) { // first attempt
                    throw error;
                  }
              })
        );

    given(errorAnalyzer.test(any(Integer.class), any(Throwable.class))).willReturn(true);

    given(view.loadMoreIntent$()).willReturn(noEvents());

    UserView userView = mock(UserView.class);

    UserListPresenter presenter = new UserListPresenter(
        userQuery$,
        userService,
        errorAnalyzer,
        () -> userView,
        () -> mock(UserPresenter.class),
        pageSize,
        DEFAULT_USER_SEARCH_LIMIT
    );
    presenter.start(view);

    // when
    userQuery$.onNext(new UserQueryEvent("foo")); // will generate error in request

    // then
    InOrder inOrder = inOrder(view, userService, errorAnalyzer);
    inOrder.verify(view).loadMoreIntent$();
    inOrder.verify(view).enableLoadMore(false);
    inOrder.verify(userService).find("foo", 1, pageSize);
    inOrder.verify(errorAnalyzer).test(1, error); // 1 - first attempt, will retry
    inOrder.verify(view).clear();
    inOrder.verify(view).add(userView);
    // no load more
    verifyNoMoreInteractions(view, userService, errorAnalyzer);
  }

  @Test
  public void onErrorInRequest_errorIsNotRecoverable_shouldTryAgainWhenLoadMoreIsTriggered() throws Exception {
    // given
    @SuppressWarnings("ThrowableNotThrown")
    RuntimeException error = new RuntimeException("bar");
    int totalCount = 1;
    int pageSize = 1;
    PublishSubject<UserQueryEvent> userQuery$ = PublishSubject.create();

    User user = mock(User.class);
    SearchResult result = mock(SearchResult.class);
    given(result.getTotalCount()).willReturn(totalCount);
    given(result.getItems()).willReturn(Collections.singletonList(user));

    given(userService.find(anyString(), anyInt(), anyInt()))
        .willReturn(Single.error(error))
        .willReturn(Single.just(result));

    given(errorAnalyzer.test(any(Integer.class), any(Throwable.class))).willReturn(false);

    PublishSubject<Trigger> loadMoreIntent = PublishSubject.create();
    given(view.loadMoreIntent$()).willReturn(loadMoreIntent);

    UserView userView = mock(UserView.class);

    UserListPresenter presenter = new UserListPresenter(
        userQuery$,
        userService,
        errorAnalyzer,
        () -> userView,
        () -> mock(UserPresenter.class),
        pageSize,
        DEFAULT_USER_SEARCH_LIMIT
    );
    presenter.start(view);
    userQuery$.onNext(new UserQueryEvent("foo")); // will generate error in request

    // when
    trigger(loadMoreIntent);

    // then
    InOrder inOrder = inOrder(view, userService, errorAnalyzer);
    inOrder.verify(view).loadMoreIntent$();
    inOrder.verify(view).enableLoadMore(false);
    inOrder.verify(userService).find("foo", 1, pageSize);
    inOrder.verify(view).enableLoadMore(true);
    inOrder.verify(view).enableLoadMore(false);
    inOrder.verify(userService).find("foo", 1, pageSize);
    inOrder.verify(view).clear();
    inOrder.verify(view).add(userView);
    // no load more
    verifyNoMoreInteractions(view, userService, errorAnalyzer);

    expectedError.expect(RuntimeException.class);
    expectedError.expectMessage("bar");
  }

  @Test
  public void stop_whileHandlingRequest_shouldUnsubscribeFromEventBusCancelRequestAndUnbindView() {
    // given
    UserQueryEvent event = new UserQueryEvent("foo");
    PublishSubject<UserQueryEvent> userQuery$ = PublishSubject.create();
    PublishSubject<SearchResult> request$ = PublishSubject.create();

    given(userService.find("foo", 1, DEFAULT_PAGE_SIZE)).willReturn(request$.singleOrError());

    PublishSubject<Trigger> loadMoreIntent = PublishSubject.create();
    given(view.loadMoreIntent$()).willReturn(loadMoreIntent);

    UserListPresenter presenter = new UserListPresenter(
        userQuery$,
        userService,
        errorAnalyzer,
        () -> mock(UserView.class),
        () -> mock(UserPresenter.class),
        DEFAULT_PAGE_SIZE,
        DEFAULT_USER_SEARCH_LIMIT
    );
    presenter.start(view);
    userQuery$.onNext(event);

    // intermediate check
    assertThat(request$.hasObservers(), is(true));
    assertThat(loadMoreIntent.hasObservers(), is(true));

    // when
    presenter.stop();

    // then
    InOrder inOrder = inOrder(view, userService);
    inOrder.verify(view).loadMoreIntent$();
    inOrder.verify(view).enableLoadMore(false);
    inOrder.verify(userService).find("foo", 1, DEFAULT_PAGE_SIZE);
    verifyNoMoreInteractions(view, userService, errorAnalyzer);

    assertThat(userQuery$.hasObservers(), is(false));
    assertThat(request$.hasObservers(), is(false));
    assertThat(loadMoreIntent.hasObservers(), is(false));
  }

}
