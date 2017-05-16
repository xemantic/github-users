/*
 * github-users - lists GitHub users. Minimal app demonstrating
 * cross-platform development (Web, Android, iOS) on top of
 * Java to JavaScript and Java to Objective-C transpilers.
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
import com.xemantic.githubusers.logic.eventbus.EventBus;
import com.xemantic.githubusers.logic.eventbus.EventTracker;
import com.xemantic.githubusers.logic.view.UserQueryView;
import org.junit.Test;
import rx.Observable;
import rx.subjects.PublishSubject;

import java.util.List;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

/**
 * Test of the {@link UserQueryPresenter}.
 *
 * @author morisil
 */
public class UserQueryPresenterTest {

  @Test
  public void start_view_shouldOnlyBindToView() {
    // given
    EventBus eventBus = new EventBus();
    EventTracker tracker = new EventTracker(UserQueryEvent.class);
    tracker.attach(eventBus);

    UserQueryView view = mock(UserQueryView.class);
    given(view.observeQueryInput()).willReturn(Observable.empty());

    UserQueryPresenter presenter = new UserQueryPresenter(eventBus);

    // when
    presenter.start(view);

    // then
    verify(view).observeQueryInput();
    verifyNoMoreInteractions(view);
    assertThat(tracker.getEvents(UserQueryEvent.class), empty());
  }

  @Test
  public void onUserQuery_queryString_shouldPostEventWithQueryString() {
    // given
    EventBus eventBus = new EventBus();
    EventTracker tracker = new EventTracker(UserQueryEvent.class);
    tracker.attach(eventBus);

    UserQueryView view = mock(UserQueryView.class);
    PublishSubject<String> userQueryTrigger = PublishSubject.create();
    given(view.observeQueryInput()).willReturn(userQueryTrigger);

    UserQueryPresenter presenter = new UserQueryPresenter(eventBus);
    presenter.start(view);
    // when
    userQueryTrigger.onNext("foo");

    // then
    UserQueryEvent event = tracker.assertOnlyOne(UserQueryEvent.class);
    assertThat(event.getQuery(), is("foo"));
  }

  @Test
  public void onUserQuery_2subsequentQueriesProvided_shouldPost2EventsWithQueryString() {
    // given
    EventBus eventBus = new EventBus();
    EventTracker tracker = new EventTracker(UserQueryEvent.class);
    tracker.attach(eventBus);

    UserQueryView view = mock(UserQueryView.class);
    PublishSubject<String> userQueryTrigger = PublishSubject.create();
    given(view.observeQueryInput()).willReturn(userQueryTrigger);

    UserQueryPresenter presenter = new UserQueryPresenter(eventBus);
    presenter.start(view);
    // when
    userQueryTrigger.onNext("foo");
    userQueryTrigger.onNext("foobar");

    // then
    List<UserQueryEvent> events = tracker.getEvents(UserQueryEvent.class);
    assertThat(events, hasSize(2));
    assertThat(events.get(0).getQuery(), is("foo"));
    assertThat(events.get(1).getQuery(), is("foobar"));
  }

}
