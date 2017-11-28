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

import io.reactivex.Observable;
import io.reactivex.observers.TestObserver;
import io.reactivex.subjects.PublishSubject;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test of the {@link Trigger}.
 *
 * @author morisil
 */
public class TriggerTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Test
  public void fire_nullObserver_shouldThrowException() {
    // given
    thrown.expect(NullPointerException.class);

    // when
    Trigger.fire(null);

    // then should fail
  }

  @Test
  public void fire_testObserver_shouldEmitOneTrigger() {
    // given
    PublishSubject<Trigger> subject = PublishSubject.create();
    TestObserver<Trigger> observer = subject.test();

    // when
    Trigger.fire(subject);

    // then
    observer
        .assertNoErrors()
        .assertNotComplete()
        .assertValueCount(1)
        .assertValue(Trigger.INSTANCE);
  }

  @Test
  public void noTriggers_shouldReturnEmptyObservable() {
    // when
    Observable<Trigger> observable = Trigger.noTriggers();

    // then
    assertThat(observable).isNotNull();
    assertThat(observable.toList().blockingGet()).isEmpty();
  }

}
