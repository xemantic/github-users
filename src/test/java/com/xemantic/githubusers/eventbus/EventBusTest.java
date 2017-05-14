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

package com.xemantic.githubusers.eventbus;

import org.junit.Test;
import rx.observers.TestSubscriber;
import rx.schedulers.Schedulers;

/**
 * Test of the {@link EventBus}.
 *
 * @author morisil
 */
public class EventBusTest {

  // happy path
  @Test
  public void post_1event_shouldReceiveIt() {
    // given
    TestSubscriber<EventA> subscriber = TestSubscriber.create();
    EventBus bus = new EventBus();
    bus.observe(EventA.class)
        .subscribeOn(Schedulers.immediate())
        .subscribe(subscriber);
    EventA eventA = new EventA();

    // when
    bus.post(eventA);

    // then
    subscriber.assertValues(eventA);
    subscriber.assertNoErrors();
  }

  @Test
  public void post_1eventWhenSubscribedAnd1WhenNot_shouldReceiveOnly1event() {
    // given
    TestSubscriber<EventA> subscriber = TestSubscriber.create();
    EventBus bus = new EventBus();
    bus.observe(EventA.class)
        .subscribeOn(Schedulers.immediate())
        .subscribe(subscriber);
    EventA eventA1 = new EventA();
    EventA eventA2 = new EventA();

    // when
    bus.post(eventA1);
    subscriber.unsubscribe();
    bus.post(eventA2);

    // then
    subscriber.assertValues(eventA1);
    subscriber.assertNoErrors();
  }

  @Test
  public void post_subscribedTo2DifferentEventTypesAnd3DifferentEventsPosted_shouldReceiveOnly2Events() {
    // given
    TestSubscriber<EventA> subscriberA = TestSubscriber.create();
    TestSubscriber<EventB> subscriberB = TestSubscriber.create();
    EventBus bus = new EventBus();
    bus.observe(EventA.class)
        .subscribeOn(Schedulers.immediate())
        .subscribe(subscriberA);
    bus.observe(EventB.class)
        .subscribeOn(Schedulers.immediate())
        .subscribe(subscriberB);
    EventA eventA1 = new EventA();
    EventA eventA2 = new EventA();
    EventB eventB = new EventB();
    EventC eventC = new EventC();

    // when
    bus.post(eventA1);
    bus.post(eventB);
    bus.post(eventC);
    bus.post(eventA2);

    // then
    subscriberA.assertValues(eventA1, eventA2);
    subscriberA.assertNoErrors();
    subscriberB.assertValues(eventB);
    subscriberB.assertNoErrors();
  }

  @Test(expected = NullPointerException.class)
  public void post_nullEvent_shouldThrowException() {
    // given
    EventBus bus = new EventBus();

    // when
    bus.post(null);

    // then exception will be thrown
  }

  @Test
  public void post_objectAsEventAndNoSubscriptions_shouldDoNothing() {
    // given
    EventBus bus = new EventBus();
    Object event = new Object();

    // when
    bus.post(event);

    // then nothing will happen
  }

  @Test(expected = NullPointerException.class)
  public void observe_nullEventType_throwException() {
    // given
    EventBus bus = new EventBus();

    // when
    bus.observe(null);

    // then exception will be thrown
  }



  // test events
  private static class EventA { /* no implementation */ }

  private static class EventB { /* no implementation */ }

  private static class EventC { /* no implementation */ }

}
