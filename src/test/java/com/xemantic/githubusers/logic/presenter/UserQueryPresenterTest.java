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

import com.xemantic.githubusers.logic.event.UserQueryEvent;
import com.xemantic.githubusers.logic.view.UserQueryView;
import io.reactivex.Observable;
import io.reactivex.observers.TestObserver;
import io.reactivex.subjects.PublishSubject;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

/**
 * Test of the {@link UserQueryPresenter}.
 *
 * @author morisil
 */
public class UserQueryPresenterTest {

  @Test
  public void start_view_shouldOnlyBindToView() {
    // given
    PublishSubject<UserQueryEvent> userQueryBus = PublishSubject.create();
    TestObserver<UserQueryEvent> tracker = new TestObserver<>();
    userQueryBus.subscribe(tracker);

    UserQueryView view = mock(UserQueryView.class);
    given(view.observeQueryInput()).willReturn(Observable.empty());

    UserQueryPresenter presenter = new UserQueryPresenter(userQueryBus::onNext);

    // when
    presenter.start(view);

    // then
    then(view).should().observeQueryInput();
    then(view).shouldHaveNoMoreInteractions();
    tracker.assertEmpty();
  }

  @Test
  public void onUserQuery_queryString_shouldPostEventWithQueryString() {
    // given
    PublishSubject<UserQueryEvent> userQueryBus = PublishSubject.create();
    TestObserver<UserQueryEvent> tracker = new TestObserver<>();
    userQueryBus.subscribe(tracker);

    UserQueryView view = mock(UserQueryView.class);
    PublishSubject<String> userQueryTrigger = PublishSubject.create();
    given(view.observeQueryInput()).willReturn(userQueryTrigger);

    UserQueryPresenter presenter = new UserQueryPresenter(userQueryBus::onNext);
    presenter.start(view);
    // when
    userQueryTrigger.onNext("foo");

    // then
    tracker.assertValueCount(1);
    assertThat(tracker.values().get(0).getQuery(), is("foo"));
  }

  @Test
  public void onUserQuery_2subsequentQueriesProvided_shouldPost2EventsWithQueryString() {
    // given
    PublishSubject<UserQueryEvent> userQueryBus = PublishSubject.create();
    TestObserver<UserQueryEvent> tracker = new TestObserver<>();
    userQueryBus.subscribe(tracker);

    UserQueryView view = mock(UserQueryView.class);
    PublishSubject<String> userQueryTrigger = PublishSubject.create();
    given(view.observeQueryInput()).willReturn(userQueryTrigger);

    UserQueryPresenter presenter = new UserQueryPresenter(userQueryBus::onNext);
    presenter.start(view);
    // when
    userQueryTrigger.onNext("foo");
    userQueryTrigger.onNext("foobar");

    // then
    List<UserQueryEvent> events = tracker.values();
    assertThat(events, hasSize(2));
    assertThat(events.get(0).getQuery(), is("foo"));
    assertThat(events.get(1).getQuery(), is("foobar"));
  }

}
