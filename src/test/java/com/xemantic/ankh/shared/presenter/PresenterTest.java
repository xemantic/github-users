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
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mockito.quality.Strictness;

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
  private Consumer<String> consumer;

  @Test
  public void lifecycle_noEventRegistrations_shouldCreateInstanceSupportingLifecycle() {
    // given
    class TestPresenter extends Presenter {
      private TestPresenter() {
        super();
      }
    }
    TestPresenter presenter = new TestPresenter();

    // when
    presenter.start();
    presenter.stop();

    // then no exceptions should happen
  }


  @Test
  public void eventPublished_beforeStart_shouldIgnoreEvent() {
    // given
    PublishSubject<String> subject = PublishSubject.create();
    class TestPresenter extends Presenter {
      private TestPresenter() {
        super(subject.doOnNext(consumer));
      }
    }
    TestPresenter presenter = new TestPresenter();

    // when
    subject.onNext("foo");

    // then
    verifyZeroInteractions(consumer);
  }

  @Test
  public void eventPublished_afterStart_shouldReceiveEvent() throws Exception {
    // given
    PublishSubject<String> subject = PublishSubject.create();
    class TestPresenter extends Presenter {
      private TestPresenter() {
        super(subject.doOnNext(consumer));
      }
    }
    TestPresenter presenter = new TestPresenter();
    presenter.start();

    // when
    subject.onNext("foo");

    // then
    verify(consumer).accept("foo");
    verifyNoMoreInteractions(consumer);
  }

  @Test
  public void eventPublished_afterStop_shouldIgnoreEvent() throws Exception {
    // given
    PublishSubject<String> subject = PublishSubject.create();
    class TestPresenter extends Presenter {
      private TestPresenter() {
        super(subject.doOnNext(consumer));
      }
    }
    TestPresenter presenter = new TestPresenter();
    presenter.start();
    presenter.stop();

    // when
    subject.onNext("foo");

    // then
    verifyZeroInteractions(consumer);
  }

  @Test
  public void start_actionToHappenOnStart_shouldExecuteAction() throws Exception {
    // given
    class TestPresenter extends Presenter {
      private TestPresenter() {
        onStart(() -> consumer.accept("foo"));
      }
    }
    TestPresenter presenter = new TestPresenter();

    // when
    presenter.start();

    // then
    verify(consumer).accept("foo");
    verifyNoMoreInteractions(consumer);
  }

  @Test
  public void eventPublished_multipleRegistrations_shouldSubscribeAll() throws Exception {
    // given
    PublishSubject<String> subject = PublishSubject.create();
    class TestPresenter extends Presenter {
      private TestPresenter() {
        super(
            subject.map(value -> value + "1").doOnNext(consumer),
            subject.map(value -> value + "2").doOnNext(consumer)
        );
        onStart(() -> consumer.accept("bar3"));
      }
    }
    TestPresenter presenter = new TestPresenter();
    presenter.start();

    // when
    subject.onNext("foo");

    // then
    InOrder inOrder = inOrder(consumer);
    inOrder.verify(consumer).accept("bar3");
    inOrder.verify(consumer).accept("foo1");
    inOrder.verify(consumer).accept("foo2");
    verifyNoMoreInteractions(consumer);
  }

  @Test
  public void start_errorInOnStartAction_shouldFailToStart() throws Exception {
    // given
    class TestPresenter extends Presenter {
      private TestPresenter() {
        onStart(() -> { throw new Exception(); });
      }
    }
    TestPresenter presenter = new TestPresenter();
    thrown.expect(RuntimeException.class);
    thrown.expectMessage("Could not start presenter");

    // when
    presenter.start();

    // then should fail
  }

  @Test
  public void start_errorWhileObservingEvents_shouldStaySubscribedAndHandleNextEvents() throws Exception {
    // given
    PublishSubject<String> subject = PublishSubject.create();
    class TestPresenter extends Presenter {
      private TestPresenter() {
        super(
          subject
              .doOnNext(value -> { if (value.equals("error")) { throw new Exception("bar"); } })
              .doOnNext(consumer)
        );
      }
    }
    TestPresenter presenter = new TestPresenter();
    presenter.start();
    uncaughtThrown.expect(Exception.class);
    uncaughtThrown.expectMessage("bar");

    // when
    subject.onNext("error");
    subject.onNext("foo");

    // then
    verify(consumer).accept("foo");
    verifyNoMoreInteractions(consumer);
  }

}
