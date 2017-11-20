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

package com.xemantic.ankh.shared.event;

import io.reactivex.Observer;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mockito.quality.Strictness;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

/**
 * Test of the {@link Sink}.
 *
 * @author morisil
 */
public class SinkTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Rule
  public MockitoRule mockitoRule = MockitoJUnit.rule().strictness(Strictness.STRICT_STUBS);

  @Mock
  private Observer<Object> observer;

  @Test
  public void of_observer_shouldCreateSinkInstance() {
    // given observer

    // when
    Sink sink = Sink.of(observer);

    // then
    assertThat(sink, notNullValue());
    verifyNoMoreInteractions(observer);
  }

  @Test
  public void publish_event_shouldAdaptObserverToSink() {
    // given
    Object event = new Object();
    Sink<Object> sink = Sink.of(observer);

    // when
    sink.publish(event);

    // then
    verify(observer).onNext(event);
    verifyNoMoreInteractions(observer);
  }

  @Test
  @SuppressWarnings("ResultOfMethodCallIgnored")
  public void of_nullObserver_shouldThrowException() {
    // given
    observer = null;
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("observer cannot be null");

    // when
    Sink.of(observer);

    // then exception should be thrown
  }

}
