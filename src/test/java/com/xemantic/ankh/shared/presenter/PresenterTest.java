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

package com.xemantic.ankh.shared.presenter;

import com.xemantic.ankh.test.ExpectedUncaughtException;
import io.reactivex.functions.Consumer;
import io.reactivex.subjects.PublishSubject;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mockito.quality.Strictness;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Test of the {@link Presenter}.
 *
 * @author morisil
 */
public class PresenterTest {

  @Rule
  public MockitoRule mockitoRule = MockitoJUnit.rule().strictness(Strictness.STRICT_STUBS);

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Rule
  public ExpectedUncaughtException uncaughtThrown = ExpectedUncaughtException.none();

  @Mock
  private Consumer<String> subscriber;

  @Test
  public void on_observable_shouldReturnCallDefiner() {
    // given
    PublishSubject<String> subject = PublishSubject.create();
    TestPresenter presenter = new TestPresenter();

    // when
    Presenter.CallDefiner<String> definer = presenter.on(subject);

    // then
    assertThat(definer).isNotNull();
  }

  @Test
  public void on_null_shouldFail() {
    // given
    TestPresenter presenter = new TestPresenter();
    thrown.expect(NullPointerException.class);

    // when
    presenter.on(null);

    // then exception should be thrown
  }

  @Test
  public void onCall_observablePublishesEvent_shouldExecuteSubscriberCall() throws Exception {
    // given
    PublishSubject<String> subject = PublishSubject.create();
    TestPresenter presenter = new TestPresenter();
    presenter.on(subject).call(subscriber);

    // when
    subject.onNext("foo");

    // then
    verify(subscriber).accept("foo");
    verifyNoMoreInteractions(subscriber);
  }

  @Test
  public void onEvent_presenterStopped_shouldIgnoreSubsequentEvent() {
    // given
    PublishSubject<String> subject = PublishSubject.create();
    TestPresenter presenter = new TestPresenter();
    presenter.on(subject).call(subscriber);
    presenter.stop();

    // when
    subject.onNext("foo");

    // then
    verifyZeroInteractions(subscriber);
  }

  @Test
  public void onEvent_errorInSubscription_shouldStaySubscribedAndHandleNextEvents() throws Exception {
    // given
    List<String> received = new LinkedList<>();
    AtomicInteger counter = new AtomicInteger(0);
    PublishSubject<String> subject = PublishSubject.create();
    TestPresenter presenter = new TestPresenter();
    doAnswer(invocation -> {
      if (counter.incrementAndGet() == 1) {
        throw new RuntimeException("buzz");
      }
      received.add(invocation.getArgument(0));
      return null;
    }).when(subscriber).accept(anyString());
    presenter.on(subject).call(subscriber);

    // when
    subject.onNext("foo"); // throws exception
    subject.onNext("bar");

    // then
    verify(subscriber).accept("foo");
    verify(subscriber).accept("bar");
    verifyNoMoreInteractions(subscriber);
    assertThat(received).containsExactly("bar");
    uncaughtThrown.expect(RuntimeException.class);
    uncaughtThrown.expectMessage("buzz");
  }

  @Test
  public void onEvent_errorInSubscribedObservable_shouldStaySubscribedAndHandleNextEvents() throws Exception {
    // given
    List<String> received = new LinkedList<>();
    AtomicInteger counter = new AtomicInteger(0);
    PublishSubject<String> subject = PublishSubject.create();
    TestPresenter presenter = new TestPresenter();
    presenter
        .on(
            subject.doOnNext(value -> {
              if (counter.incrementAndGet() == 1) {
                throw new Exception("buzz");
              }
              received.add(value);
            })
        )
        .call(subscriber);

    // when
    subject.onNext("foo"); // throws exception
    subject.onNext("bar");

    // then
    verify(subscriber).accept("bar");
    verifyNoMoreInteractions(subscriber);
    assertThat(received).containsExactly("bar");
    uncaughtThrown.expect(Exception.class);
    uncaughtThrown.expectMessage("buzz");
  }

  private static class TestPresenter extends Presenter { /* nothing to override */ }

}
