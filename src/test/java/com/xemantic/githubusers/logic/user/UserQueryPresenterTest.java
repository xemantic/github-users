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

import com.xemantic.ankh.shared.event.Sink;
import com.xemantic.githubusers.logic.event.UserQueryEvent;
import io.reactivex.observers.TestObserver;
import io.reactivex.subjects.PublishSubject;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mockito.quality.Strictness;

import java.util.List;

import static com.xemantic.ankh.test.TestEvents.noEvents;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

/**
 * Test of the {@link UserQueryPresenter}.
 *
 * @author morisil
 */
public class UserQueryPresenterTest {

  @Rule
  public MockitoRule mockitoRule = MockitoJUnit.rule().strictness(Strictness.STRICT_STUBS);

  @Mock
  private UserQueryView view;

  @Test
  public void start_view_shouldOnlyBindToView() {
    // given
    TestObserver<UserQueryEvent> userQuery$ = TestObserver.create();
    given(view.queryInput$()).willReturn(noEvents());
    UserQueryPresenter presenter = new UserQueryPresenter(Sink.of(userQuery$));

    // when
    presenter.start(view);

    // then
    then(view).should().queryInput$();
    then(view).shouldHaveNoMoreInteractions();
    userQuery$.assertNoValues();
  }

  @Test
  public void onUserQuery_queryString_shouldPostEventWithQueryString() {
    // given
    TestObserver<UserQueryEvent> userQuery$ = TestObserver.create();
    PublishSubject<String> userQueryIntent = PublishSubject.create();
    given(view.queryInput$()).willReturn(userQueryIntent);
    UserQueryPresenter presenter = new UserQueryPresenter(Sink.of(userQuery$));
    presenter.start(view);

    // when
    userQueryIntent.onNext("foo");

    // then
    userQuery$.assertValueCount(1);
    UserQueryEvent event = userQuery$.values().get(0);
    assertThat(event.getQuery(), is("foo"));
  }

  @Test
  public void onUserQuery_2subsequentQueriesProvided_shouldPost2EventsWithQueryString() {
    // given
    TestObserver<UserQueryEvent> userQuery$ = TestObserver.create();
    PublishSubject<String> userQueryIntents = PublishSubject.create();
    given(view.queryInput$()).willReturn(userQueryIntents);
    UserQueryPresenter presenter = new UserQueryPresenter(Sink.of(userQuery$));
    presenter.start(view);

    // when
    userQueryIntents.onNext("foo");
    userQueryIntents.onNext("foobar");

    // then
    userQuery$.assertValueCount(2);
    List<UserQueryEvent> events = userQuery$.values();
    assertThat(events.get(0).getQuery(), is("foo"));
    assertThat(events.get(1).getQuery(), is("foobar"));
  }

}
