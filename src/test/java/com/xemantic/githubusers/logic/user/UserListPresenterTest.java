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

  private InOrder inOrder;

  @Test
  public void start_noInput_shouldDoNothingWithViewAndServices() {
    // given
    PublishSubject<UserQueryEvent> userQuery$ = PublishSubject.create();
    UserListPresenter presenter = new UserListPresenter(
        view,
        userQuery$,
        userService,
        mock(UserPresenterFactory.class),
        DEFAULT_PAGE_SIZE,
        DEFAULT_USER_SEARCH_LIMIT
    );

    // when
    presenter.start();

    // then
    verifyZeroInteractions(view, userService);
  }

  @Test
  public void onUserQueryEvent_emptyQueryString_shouldDoNothingWithView() {
    // given
    PublishSubject<UserQueryEvent> userQuery$ = PublishSubject.create();
    UserListPresenter presenter = new UserListPresenter(
        view,
        userQuery$,
        userService,
        mock(UserPresenterFactory.class),
        DEFAULT_PAGE_SIZE,
        DEFAULT_USER_SEARCH_LIMIT
    );
    presenter.start();
    UserQueryEvent event = new UserQueryEvent(" "); // empty string

    // when
    userQuery$.onNext(event);

    // then
    verifyZeroInteractions(view, userService);
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
    given(userPresenter.getView()).willReturn(userView);

    UserPresenterFactory userPresenterFactory = mock(UserPresenterFactory.class);
    given(userPresenterFactory.create(user)).willReturn(userPresenter);

    given(view.loadMoreIntent$()).willReturn(noTriggers());

    UserListPresenter presenter = new UserListPresenter(
        view,
        userQuery$,
        userService,
        userPresenterFactory,
        DEFAULT_PAGE_SIZE,
        DEFAULT_USER_SEARCH_LIMIT
    );
    presenter.start();

    // when
    userQuery$.onNext(new UserQueryEvent("foo"));

    // then
    inOrder = inOrder(view, userService, userPresenter);
    $(view).loadMoreIntent$();
    $(view).enableLoadMore(false);
    $(view).loadingFirstPage(true);
    $(userService).find("foo", 1, DEFAULT_PAGE_SIZE);
    $(view).loadingFirstPage(false);
    $(view).clear();
    $(userPresenter).start();
    $(view).add(userView);
    $(view).loadMoreIntent$();
    // load more stays disabled as it's the last page of results.
    verifyNoMoreInteractions(view, userService, userPresenter);
    inOrder.verifyNoMoreInteractions();
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
    given(userPresenter.getView()).willReturn(userView);
    UserPresenterFactory userPresenterFactory = mock(UserPresenterFactory.class);
    given(userPresenterFactory.create(user)).willReturn(userPresenter);

    given(view.loadMoreIntent$()).willReturn(noTriggers());

    UserListPresenter presenter = new UserListPresenter(
        view,
        userQuery$,
        userService,
        userPresenterFactory,
        pageSize,
        DEFAULT_USER_SEARCH_LIMIT
    );
    presenter.start();

    // when
    userQuery$.onNext(new UserQueryEvent("foo"));

    // then
    inOrder = inOrder(view, userService, userPresenter);
    $(view).loadMoreIntent$();
    $(view).enableLoadMore(false);
    $(view).loadingFirstPage(true);
    $(userService).find("foo", 1, pageSize);
    $(view).enableLoadMore(true);
    $(view).loadingFirstPage(false);
    $(view).clear();
    $(userPresenter).start();
    $(view).add(userView);
    $(view).loadMoreIntent$();
    verifyNoMoreInteractions(view, userService, userPresenter);
    inOrder.verifyNoMoreInteractions();
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
    given(userPresenter1.getView()).willReturn(userView1);
    given(userPresenter2.getView()).willReturn(userView2);
    UserPresenterFactory userPresenterFactory = mock(UserPresenterFactory.class);
    given(userPresenterFactory.create(any())).willReturn(userPresenter1, userPresenter2);

    PublishSubject<Trigger> loadMoreIntent = PublishSubject.create();
    given(view.loadMoreIntent$()).willReturn(loadMoreIntent);

    UserListPresenter presenter = new UserListPresenter(
        view,
        userQuery$,
        userService,
        userPresenterFactory,
        pageSize,
        DEFAULT_USER_SEARCH_LIMIT
    );
    presenter.start();
    userQuery$.onNext(new UserQueryEvent("foo"));

    // when
    fire(loadMoreIntent);

    // then
    inOrder = inOrder(
        view,
        userService,
        userPresenter1,
        userPresenter2
    );
    $(view).loadMoreIntent$();
    $(view).enableLoadMore(false);
    $(view).loadingFirstPage(true);
    $(userService).find("foo", 1, 1);
    $(view).enableLoadMore(true);
    $(view).loadingFirstPage(false);
    $(view).clear();
    $(userPresenter1).start();
    $(view).add(userView1);
    $(view).enableLoadMore(false);
    $(userService).find("foo", 2, 1);
    $(userPresenter2).start();
    $(view).add(userView2);
    $(view).loadMoreIntent$();
    verifyNoMoreInteractions(
        view,
        userService,
        userPresenter1,
        userPresenter2
    );
    inOrder.verifyNoMoreInteractions();
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
        view,
        userQuery$,
        userService,
        mockUserPresenterFactory(user, userView),
        DEFAULT_PAGE_SIZE,
        DEFAULT_USER_SEARCH_LIMIT
    );
    presenter.start();

    // when
    userQuery$.onNext(new UserQueryEvent("foo"));
    IntStream.rangeClosed(2, 10).forEach(i -> fire(loadMoreIntent));

    // then
    verify(view).loadingFirstPage(true);
    verify(view).loadingFirstPage(false);
    verify(view, times(11)).loadMoreIntent$();
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
        view,
        userQuery$,
        userService,
        mockUserPresenterFactory(user, userView),
        DEFAULT_PAGE_SIZE,
        DEFAULT_USER_SEARCH_LIMIT
    );

    // intermediate state check
    assertThat(userQuery$.hasObservers()).isFalse();
    assertThat(response1$.hasObservers()).isFalse();
    assertThat(response2$.hasObservers()).isFalse();

    presenter.start();

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

    inOrder = inOrder(view, userService);
    $(view).loadMoreIntent$();
    $(view).enableLoadMore(false);
    $(view).loadingFirstPage(true);
    $(userService).find("foo", 1, DEFAULT_PAGE_SIZE);
    $(view).enableLoadMore(false);
    $(view).loadingFirstPage(true);
    $(userService).find("bar", 1, DEFAULT_PAGE_SIZE);
    $(view).loadingFirstPage(false);
    $(view).clear();
    $(view).add(userView);
    $(view).loadMoreIntent$();
    verifyNoMoreInteractions(view, userService);
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  public void onUserQueryEvent_whenPageOfAnotherQueryIsDisplayed_shouldClearOldUserViewsAndStopAssociatedPresenters() {
    // given
    int totalCount = 1;
    UserQueryEvent event1 = new UserQueryEvent("foo");
    UserQueryEvent event2 = new UserQueryEvent("bar");
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

    given(view.loadMoreIntent$()).willReturn(noTriggers());

    UserView userView1 = mock(UserView.class);
    UserPresenter userPresenter1 = mock(UserPresenter.class);
    UserView userView2 = mock(UserView.class);
    UserPresenter userPresenter2 = mock(UserPresenter.class);
    given(userPresenter1.getView()).willReturn(userView1);
    given(userPresenter2.getView()).willReturn(userView2);
    UserPresenterFactory userPresenterFactory = mock(UserPresenterFactory.class);
    given(userPresenterFactory.create(any())).willReturn(userPresenter1, userPresenter2);

    UserListPresenter presenter = new UserListPresenter(
        view,
        userQuery$,
        userService,
        userPresenterFactory,
        DEFAULT_PAGE_SIZE,
        DEFAULT_USER_SEARCH_LIMIT
    );
    presenter.start();
    userQuery$.onNext(event1);

    // when
    userQuery$.onNext(event2);

    // then
    inOrder = inOrder(view, userService, userPresenter1, userPresenter2);
    $(view).loadMoreIntent$();
    $(view).enableLoadMore(false);
    $(view).loadingFirstPage(true);
    $(userService).find("foo", 1, DEFAULT_PAGE_SIZE);
    $(view).loadingFirstPage(false);
    $(view).clear();
    $(userPresenter1).start();
    $(view).add(userView1);
    $(view).enableLoadMore(false);
    $(view).loadingFirstPage(true);
    $(userService).find("bar", 1, DEFAULT_PAGE_SIZE);
    $(view).loadingFirstPage(false);
    $(view).clear();
    $(userPresenter1).stop(); // stops the first presenter
    $(userPresenter2).start();
    $(view).add(userView2);
    $(view).loadMoreIntent$();
    verifyNoMoreInteractions(view, userService, userPresenter1, userPresenter2);
    inOrder.verifyNoMoreInteractions();
  }

  // use case - for example 500 internal server error happens remotely
  @Test
  public void onErrorIn1StRequest_errorIsNotRecoverable_shouldTryAgainWhenLoadMoreIsTriggered() {
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
        view,
        userQuery$,
        userService,
        mockUserPresenterFactory(user, userView),
        pageSize,
        DEFAULT_USER_SEARCH_LIMIT
    );
    presenter.start();
    userQuery$.onNext(new UserQueryEvent("foo")); // will generate error in request

    // when
    fire(loadMoreIntent);

    // then
    inOrder = inOrder(view, userService);
    $(view).loadMoreIntent$();
    $(view).enableLoadMore(false);
    $(view).loadingFirstPage(true);
    $(userService).find("foo", 1, pageSize);
    $(view).enableLoadMore(true);
    $(view).loadingFirstPage(false);
    $(view).clear();
    $(view).enableLoadMore(false);
    $(view).loadingFirstPage(true);
    $(userService).find("foo", 1, pageSize);
    $(view).loadingFirstPage(false);
    $(view).clear();
    $(view).add(userView);
    $(view).loadMoreIntent$();
    verifyNoMoreInteractions(view, userService);
    inOrder.verifyNoMoreInteractions();

    uncaughtThrown.expect(RuntimeException.class);
    uncaughtThrown.expectMessage("bar");
  }

  // use case - for example 403 - GitHub req/minute limit reached reached
  @Test
  public void onErrorIn3RdRequest_errorIsNotRecoverable_shouldTryAgainWhenLoadMoreIsTriggered() {
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
    UserPresenter userPresenter = mock(UserPresenter.class);
    given(userPresenter.getView()).willReturn(userView);
    UserPresenterFactory userPresenterFactory = mock(UserPresenterFactory.class);
    given(userPresenterFactory.create(any())).willReturn(userPresenter);

    UserListPresenter presenter = new UserListPresenter(
        view,
        userQuery$,
        userService,
        userPresenterFactory,
        pageSize,
        DEFAULT_USER_SEARCH_LIMIT
    );
    presenter.start();
    userQuery$.onNext(new UserQueryEvent("foo")); // will generate error in request
    fire(loadMoreIntent);

    // when
    fire(loadMoreIntent);

    // then
    inOrder = inOrder(view, userService);
    $(view).loadMoreIntent$();
    $(view).enableLoadMore(false);
    $(view).loadingFirstPage(true);
    $(userService).find("foo", 1, pageSize);
    $(view).enableLoadMore(true);
    $(view).loadingFirstPage(false);
    $(view).clear();
    $(view).add(userView);
    $(view).enableLoadMore(false);
    $(userService).find("foo", 2, pageSize);
    $(view).enableLoadMore(true);
    $(view).enableLoadMore(false);
    $(userService).find("foo", 2, pageSize);
    $(view).enableLoadMore(true); // still 3rd page to be shown
    $(view).add(userView);
    $(view).loadMoreIntent$();
    verifyNoMoreInteractions(view, userService);
    inOrder.verifyNoMoreInteractions();

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
        view,
        userQuery$,
        userService,
        mock(UserPresenterFactory.class),
        DEFAULT_PAGE_SIZE,
        DEFAULT_USER_SEARCH_LIMIT
    );
    presenter.start();
    userQuery$.onNext(event);

    // intermediate check
    assertThat(userQuery$.hasObservers()).isTrue();
    assertThat(request$.hasObservers()).isTrue();

    // when
    presenter.stop();

    // then
    inOrder = inOrder(view, userService);
    $(view).loadMoreIntent$();
    $(view).enableLoadMore(false);
    $(view).loadingFirstPage(true);
    $(userService).find("foo", 1, DEFAULT_PAGE_SIZE);
    verifyNoMoreInteractions(view, userService);
    inOrder.verifyNoMoreInteractions();

    assertThat(userQuery$.hasObservers()).isFalse();
    assertThat(request$.hasObservers()).isFalse();
    assertThat(loadMoreIntent.hasObservers()).isFalse();
  }

  @Test
  public void stop_afterDisplaying1StPage_shouldUnsubscribeFromEventBusCancelRequestStopAllChildPresentersAndUnbindView() {
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

    UserView userView = mock(UserView.class);
    UserPresenter userPresenter = mock(UserPresenter.class);
    given(userPresenter.getView()).willReturn(userView);
    UserPresenterFactory userPresenterFactory = mock(UserPresenterFactory.class);
    given(userPresenterFactory.create(any())).willReturn(userPresenter);

    UserListPresenter presenter = new UserListPresenter(
        view,
        userQuery$,
        userService,
        userPresenterFactory,
        pageSize,
        DEFAULT_USER_SEARCH_LIMIT
    );
    presenter.start();
    userQuery$.onNext(event);

    // intermediate check
    assertThat(userQuery$.hasObservers()).isTrue();
    assertThat(loadMoreIntent.hasObservers()).isTrue();
    assertThat(request$.hasObservers()).isFalse(); // already finished

    // when
    presenter.stop();

    // then

    inOrder = inOrder(view, userService, userPresenter);
    $(view).loadMoreIntent$();
    $(view).enableLoadMore(false);
    $(view).loadingFirstPage(true);
    $(userService).find("foo", 1, pageSize);
    $(view).loadingFirstPage(false);
    $(view).clear();
    $(userPresenter).start();
    $(view).add(userView);
    $(userPresenter).stop(); // essence of this test - child presenter is stopped as well
    verifyNoMoreInteractions(view, userService, userPresenter);
    inOrder.verifyNoMoreInteractions();

    assertThat(userQuery$.hasObservers()).isFalse();
    assertThat(loadMoreIntent.hasObservers()).isFalse();
    assertThat(request$.hasObservers()).isFalse();
  }

  private UserPresenterFactory mockUserPresenterFactory(User user, UserView userView) {
    UserPresenter userPresenter = mock(UserPresenter.class);
    given(userPresenter.getView()).willReturn(userView);
    UserPresenterFactory userPresenterFactory = mock(UserPresenterFactory.class);
    given(userPresenterFactory.create(user)).willReturn(userPresenter);
    return userPresenterFactory;
  }

  private <T> T $(T mock) {
    return inOrder.verify(mock);
  }

}
