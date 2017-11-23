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

import com.xemantic.ankh.shared.event.Trigger;
import com.xemantic.githubusers.logic.event.UserQueryEvent;
import com.xemantic.githubusers.logic.model.SearchResult;
import com.xemantic.githubusers.logic.model.User;
import com.xemantic.githubusers.logic.service.UserService;
import com.xemantic.ankh.test.ExpectedUncaughtException;
import io.reactivex.Single;
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
import java.util.stream.IntStream;

import static com.xemantic.ankh.shared.event.Trigger.fire;
import static com.xemantic.ankh.shared.event.Trigger.noTriggers;
import static org.assertj.core.api.Assertions.assertThat;
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
  public ExpectedUncaughtException uncaughtThrown = ExpectedUncaughtException.none();

  private static final int DEFAULT_PAGE_SIZE = 100; // max for GitHub API

  private static final int DEFAULT_USER_SEARCH_LIMIT = 1000; // max for GitHub API

  @Mock
  private UserListView view;

  @Mock
  private UserService userService;

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
        () -> mock(UserView.class),
        () -> mock(UserPresenter.class),
        DEFAULT_PAGE_SIZE,
        DEFAULT_USER_SEARCH_LIMIT
    );

    // when
    presenter.start(view);

    // then
    verifyNoMoreInteractions(view, userService);
  }

  @Test
  public void onUserQueryEvent_emptyQueryString_shouldDoNothingWithView() {
    // given
    PublishSubject<UserQueryEvent> userQuery$ = PublishSubject.create();
    UserListPresenter presenter = new UserListPresenter(
        userQuery$,
        userService,
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
    verifyNoMoreInteractions(view, userService);
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

    given(view.loadMoreIntent$()).willReturn(noTriggers());

    UserListPresenter presenter = new UserListPresenter(
        userQuery$,
        userService,
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
    inOrder.verify(view).loadingFirstPage(true);
    inOrder.verify(userService).find("foo", 1, DEFAULT_PAGE_SIZE);
    inOrder.verify(view).loadingFirstPage(false);
    inOrder.verify(view).clear();
    inOrder.verify(userPresenter).start(user, userView);
    inOrder.verify(view).add(userView);
    // load more stays disabled as it's the last page of results.
    verifyNoMoreInteractions(view, userService, userPresenter);
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

    given(view.loadMoreIntent$()).willReturn(noTriggers());

    UserListPresenter presenter = new UserListPresenter(
        userQuery$,
        userService,
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
    inOrder.verify(view).loadingFirstPage(true);
    inOrder.verify(userService).find("foo", 1, pageSize);
    inOrder.verify(view).loadingFirstPage(false);
    inOrder.verify(view).clear();
    inOrder.verify(userPresenter).start(user, userView);
    inOrder.verify(view).add(userView);
    inOrder.verify(view).enableLoadMore(true);
    verifyNoMoreInteractions(view, userService, userPresenter);
  }

  @Test
  public void onLoadMore_when1pageIsAlreadyDisplayedAnd2UsersInTotal_shouldRequestAndDisplayNextPageAndDisableLoadMore() throws InterruptedException {
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
        userViewProvider,
        userPresenterProvider,
        pageSize,
        DEFAULT_USER_SEARCH_LIMIT
    );
    presenter.start(view);
    userQuery$.onNext(new UserQueryEvent("foo"));

    // when
    fire(loadMoreIntent);

    // then
    InOrder inOrder = inOrder(
        view,
        userService,
        userPresenter1,
        userPresenter2
    );
    inOrder.verify(view).loadMoreIntent$();
    inOrder.verify(view).enableLoadMore(false);
    inOrder.verify(view).loadingFirstPage(true);
    inOrder.verify(userService).find("foo", 1, 1);
    inOrder.verify(view).loadingFirstPage(false);
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
        userPresenter1,
        userPresenter2
    );
  }

  @Test
  public void onLoadAll1000Users_when1001UsersFoundInTotal_shouldRequestAndDisplay1000UsersIn10PagesAndThenDisableLoadMore() {
    // given
    int totalCount = 1001;  // higher than max limit of 1000
    PublishSubject<UserQueryEvent> userQuery$ = PublishSubject.create();

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
        () -> userView,
        () -> mock(UserPresenter.class),
        DEFAULT_PAGE_SIZE,
        DEFAULT_USER_SEARCH_LIMIT
    );
    presenter.start(view);

    // when
    userQuery$.onNext(new UserQueryEvent("foo"));
    IntStream.rangeClosed(2, 10).forEach(i -> fire(loadMoreIntent));

    // then
    verify(view).loadingFirstPage(true);
    verify(view).loadingFirstPage(false);
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
    verifyNoMoreInteractions(view, userService);
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

    given(view.loadMoreIntent$()).willReturn(noTriggers());

    UserView userView = mock(UserView.class);

    UserListPresenter presenter = new UserListPresenter(
        userQuery$,
        userService,
        () -> userView,
        () -> mock(UserPresenter.class),
        DEFAULT_PAGE_SIZE,
        DEFAULT_USER_SEARCH_LIMIT
    );

    // intermediate state check
    assertThat(userQuery$.hasObservers()).isFalse();
    assertThat(response1$.hasObservers()).isFalse();
    assertThat(response2$.hasObservers()).isFalse();

    presenter.start(view);

    // intermediate state check
    assertThat(userQuery$.hasObservers()).isTrue();
    assertThat(response1$.hasObservers()).isFalse();
    assertThat(response2$.hasObservers()).isFalse();

    userQuery$.onNext(event1);

    // intermediate state check
    assertThat(userQuery$.hasObservers()).isTrue();
    assertThat(response1$.hasObservers()).isTrue();
    assertThat(response2$.hasObservers()).isFalse();

    // when
    userQuery$.onNext(event2); // ping
    response2$.onNext(result); // pong

    // intermediate state check
    assertThat(userQuery$.hasObservers()).isTrue();
    assertThat(response1$.hasObservers()).isFalse();
    assertThat(response2$.hasObservers()).isTrue();

    response2$.onComplete();

    // then
    assertThat(userQuery$.hasObservers()).isTrue();
    // request1 is eventually unsubscribed, which implies pending request is cancelled
    assertThat(response1$.hasObservers()).isFalse();
    assertThat(response2$.hasObservers()).isFalse();

    InOrder inOrder = inOrder(view, userService);
    inOrder.verify(view).loadMoreIntent$();
    inOrder.verify(view).enableLoadMore(false);
    inOrder.verify(view).loadingFirstPage(true);
    inOrder.verify(userService).find("foo", 1, DEFAULT_PAGE_SIZE);
    inOrder.verify(view).loadMoreIntent$();
    inOrder.verify(view).enableLoadMore(false);
    inOrder.verify(view).loadingFirstPage(true);
    inOrder.verify(userService).find("bar", 1, DEFAULT_PAGE_SIZE);
    inOrder.verify(view).loadingFirstPage(false);
    inOrder.verify(view).clear();
    inOrder.verify(view).add(userView);
    verifyNoMoreInteractions(view, userService);
  }

  // use case - for example 500 internal server error happens remotely
  @Test
  public void onErrorIn1StRequest_errorIsNotRecoverable_shouldTryAgainWhenLoadMoreIsTriggered() throws Exception {
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

    PublishSubject<Trigger> loadMoreIntent = PublishSubject.create();
    given(view.loadMoreIntent$()).willReturn(loadMoreIntent);

    UserView userView = mock(UserView.class);

    UserListPresenter presenter = new UserListPresenter(
        userQuery$,
        userService,
        () -> userView,
        () -> mock(UserPresenter.class),
        pageSize,
        DEFAULT_USER_SEARCH_LIMIT
    );
    presenter.start(view);
    userQuery$.onNext(new UserQueryEvent("foo")); // will generate error in request

    // when
    fire(loadMoreIntent);

    // then
    InOrder inOrder = inOrder(view, userService);
    inOrder.verify(view).loadMoreIntent$();
    inOrder.verify(view).enableLoadMore(false);
    inOrder.verify(view).loadingFirstPage(true);
    inOrder.verify(userService).find("foo", 1, pageSize);
    inOrder.verify(view).enableLoadMore(true);
    inOrder.verify(view).loadingFirstPage(false);
    inOrder.verify(view).enableLoadMore(false);
    inOrder.verify(view).loadingFirstPage(true);
    inOrder.verify(userService).find("foo", 1, pageSize);
    inOrder.verify(view).loadingFirstPage(false);
    inOrder.verify(view).clear();
    inOrder.verify(view).add(userView);
    verifyNoMoreInteractions(view, userService);

    uncaughtThrown.expect(RuntimeException.class);
    uncaughtThrown.expectMessage("bar");
  }

  // use case - for example 403 - GitHub req/minute limit reached reached
  @Test
  public void onErrorIn3RdRequest_errorIsNotRecoverable_shouldTryAgainWhenLoadMoreIsTriggered() throws Exception {
    // given
    @SuppressWarnings("ThrowableNotThrown")
    RuntimeException error = new RuntimeException("API rate limit exceeded");
    int totalCount = 3; // we want to keep "load more" enabled at the end
    int pageSize = 1;
    PublishSubject<UserQueryEvent> userQuery$ = PublishSubject.create();

    User user1 = mock(User.class);
    SearchResult result1 = mock(SearchResult.class);
    given(result1.getTotalCount()).willReturn(totalCount);
    given(result1.getItems()).willReturn(Collections.singletonList(user1));

    User user2 = mock(User.class);
    SearchResult result2 = mock(SearchResult.class);
    given(result2.getTotalCount()).willReturn(totalCount);
    given(result2.getItems()).willReturn(Collections.singletonList(user2));

    given(userService.find(anyString(), anyInt(), anyInt()))
        .willReturn(Single.just(result1))
        .willReturn(Single.error(error))
        .willReturn(Single.just(result2));

    PublishSubject<Trigger> loadMoreIntent = PublishSubject.create();
    given(view.loadMoreIntent$()).willReturn(loadMoreIntent);

    UserView userView = mock(UserView.class);

    UserListPresenter presenter = new UserListPresenter(
        userQuery$,
        userService,
        () -> userView,
        () -> mock(UserPresenter.class),
        pageSize,
        DEFAULT_USER_SEARCH_LIMIT
    );
    presenter.start(view);
    userQuery$.onNext(new UserQueryEvent("foo")); // will generate error in request
    fire(loadMoreIntent);

    // when
    fire(loadMoreIntent);

    // then
    InOrder inOrder = inOrder(view, userService);
    inOrder.verify(view).loadMoreIntent$();
    inOrder.verify(view).enableLoadMore(false);
    inOrder.verify(view).loadingFirstPage(true);
    inOrder.verify(userService).find("foo", 1, pageSize);
    inOrder.verify(view).loadingFirstPage(false);
    inOrder.verify(view).clear();
    inOrder.verify(view).add(userView);
    inOrder.verify(view).enableLoadMore(true);
    inOrder.verify(view).enableLoadMore(false);
    inOrder.verify(userService).find("foo", 2, pageSize);
    inOrder.verify(view).enableLoadMore(true);
    inOrder.verify(view).loadingFirstPage(false);
    inOrder.verify(view).enableLoadMore(false);
    inOrder.verify(userService).find("foo", 2, pageSize);
    inOrder.verify(view).add(userView);
    inOrder.verify(view).enableLoadMore(true); // still 3rd page to be shown
    verifyNoMoreInteractions(view, userService);

    uncaughtThrown.expect(RuntimeException.class);
    uncaughtThrown.expectMessage("API rate limit exceeded");
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
        () -> mock(UserView.class),
        () -> mock(UserPresenter.class),
        DEFAULT_PAGE_SIZE,
        DEFAULT_USER_SEARCH_LIMIT
    );
    presenter.start(view);
    userQuery$.onNext(event);

    // intermediate check
    assertThat(userQuery$.hasObservers()).isTrue();
    assertThat(request$.hasObservers()).isTrue();
    assertThat(loadMoreIntent.hasObservers()).isTrue();

    // when
    presenter.stop();

    // then
    InOrder inOrder = inOrder(view, userService);
    inOrder.verify(view).loadMoreIntent$();
    inOrder.verify(view).enableLoadMore(false);
    inOrder.verify(view).loadingFirstPage(true);
    inOrder.verify(userService).find("foo", 1, DEFAULT_PAGE_SIZE);
    verifyNoMoreInteractions(view, userService);

    assertThat(userQuery$.hasObservers()).isFalse();
    assertThat(request$.hasObservers()).isFalse();
    assertThat(loadMoreIntent.hasObservers()).isFalse();
  }

  @Test
  public void stop_afterDisplaying1StPage_shouldUnsubscribeFromEventBusCancelRequestStopAllChilderenPresentersAndUnbindView() {
    // given
    int totalCount = 1;
    int pageSize = 1;

    UserQueryEvent event = new UserQueryEvent("foo");
    PublishSubject<UserQueryEvent> userQuery$ = PublishSubject.create();
    PublishSubject<SearchResult> request$ = PublishSubject.create();

    User user = mock(User.class);
    SearchResult result = mock(SearchResult.class);
    given(result.getTotalCount()).willReturn(totalCount);
    given(result.getItems()).willReturn(Collections.singletonList(user));

    given(userService.find("foo", 1, pageSize)).willReturn(Single.just(result));

    PublishSubject<Trigger> loadMoreIntent = PublishSubject.create();
    given(view.loadMoreIntent$()).willReturn(loadMoreIntent);

    UserPresenter userPresenter = mock(UserPresenter.class);
    given(userPresenterProvider.get()).willReturn(userPresenter);

    UserView userView = mock(UserView.class);

    UserListPresenter presenter = new UserListPresenter(
        userQuery$,
        userService,
        () -> userView,
        userPresenterProvider,
        pageSize,
        DEFAULT_USER_SEARCH_LIMIT
    );
    presenter.start(view);
    userQuery$.onNext(event);

    // intermediate check
    assertThat(userQuery$.hasObservers()).isTrue();
    assertThat(loadMoreIntent.hasObservers()).isTrue();
    assertThat(request$.hasObservers()).isFalse(); // already finished

    // when
    presenter.stop();

    // then
    InOrder inOrder = inOrder(view, userService, userPresenter);
    inOrder.verify(view).loadMoreIntent$();
    inOrder.verify(view).enableLoadMore(false);
    inOrder.verify(view).loadingFirstPage(true);
    inOrder.verify(userService).find("foo", 1, pageSize);
    inOrder.verify(view).loadingFirstPage(false);
    inOrder.verify(view).clear();
    inOrder.verify(userPresenter).start(user, userView);
    inOrder.verify(view).add(userView);
    inOrder.verify(userPresenter).stop(); // essence of this test - child presenter is stopped as well
    verifyNoMoreInteractions(view, userService, userPresenter);

    assertThat(userQuery$.hasObservers()).isFalse();
    assertThat(loadMoreIntent.hasObservers()).isFalse();
    assertThat(request$.hasObservers()).isFalse();
  }

}
