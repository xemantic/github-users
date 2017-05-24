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

package com.xemantic.githubusers.logic.eventbus;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

/**
 * Test of the {@link EventTracker}
 *
 * @author morisil
 */
public class EventTrackerTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  // happy path
  @Test
  public void getEvents_eventsInOrder_shouldReturnOrderedEventLists() {
    // given
    EventTracker tracker = new EventTracker(String.class, Integer.class);
    EventBus eventBus = new DefaultEventBus();
    tracker.attach(eventBus);
    eventBus.post("foo");
    eventBus.post(42);
    eventBus.post("bar");
    eventBus.post(43);
    eventBus.post("buzz");
    eventBus.post(true); // not tracked

    // when
    List<String> strings = tracker.getEvents(String.class);
    List<Integer> integers = tracker.getEvents(Integer.class);
    List<Boolean> booleans = tracker.getEvents(Boolean.class);

    // then
    assertThat(strings, contains("foo", "bar", "buzz"));
    assertThat(integers, contains(42, 43));
    assertThat(booleans, empty());
  }

  // happy path 2 - this scenario is useful in testing
  @Test
  public void assertOnlyOne_andOnlyOneEventOfThisTypeWasPosted_shouldReturnEvent() {
    // given
    EventTracker tracker = new EventTracker(String.class);
    EventBus eventBus = new DefaultEventBus();
    tracker.attach(eventBus);
    eventBus.post("foo");

    // when
    String event = tracker.assertOnlyOne(String.class);

    // then
    assertThat(event, is("foo"));
  }

  @Test
  public void new_noEventTypesToTrack_shouldTrackNothing() {
    // given EventTracker class

    // when
    EventTracker tracker = new EventTracker();

    // then
    assertThat(tracker.getTrackedEventTypes(), empty());
  }

  @Test(expected = NullPointerException.class)
  public void new_nullEventTypes_shouldThrowException() {
    // given EventTracker class

    // when
    new EventTracker((Set<Class<?>>) null);

    // then exception should be thrown
  }

  @Test(expected = NullPointerException.class)
  public void new_nullVarArgs_shouldThrowException() {
    // given EventTracker class

    // when
    new EventTracker((Class<?>[]) null);

    // then exception should be thrown
  }

  @Test(expected = IllegalArgumentException.class)
  public void new_nullVarArg_shouldThrowException() {
    // given EventTracker class

    // when
    new EventTracker((Class<?>) null);

    // then exception should be thrown
  }

  @Test(expected = IllegalArgumentException.class)
  public void new_nullInCollectionOfEventTypes_shouldThrowException() {
    // given EventTracker class

    // when
    new EventTracker(Collections.singleton(null));

    // then exception should be thrown
  }

  @Test
  public void new_trackStrings_shouldTrackStrings() {
    // given
    Class<?> eventType = String.class;

    // when
    EventTracker tracker = new EventTracker(eventType);

    // then
    assertThat(tracker.getTrackedEventTypes(), contains(String.class));
  }

  @Test
  public void new_trackStringsInCollection_shouldTrackStrings() {
    // given
    Set<Class<?>> eventTypes = Collections.singleton(String.class);

    // when
    EventTracker tracker = new EventTracker(eventTypes);

    // then
    assertThat(tracker.getTrackedEventTypes(), contains(String.class));
  }

  @Test
  public void new_attackOnInternalState_shouldStillTrackEvents() {
    // given
    Set<Class<?>> eventTypes = new HashSet<>();
    eventTypes.add(String.class);
    EventTracker tracker = new EventTracker(eventTypes);

    // when
    eventTypes.clear();

    // then
    assertThat(tracker.getTrackedEventTypes(), contains(String.class));
  }

  @Test
  public void getTrackedEventTypes_stringAndIntegerRegistered_shouldTrackStringsAndIntegers() {
    // given
    EventTracker tracker = new EventTracker(String.class, Integer.class);

    // when
    Set<Class<?>> eventTypes = tracker.getTrackedEventTypes();

    // then
    assertThat(eventTypes, containsInAnyOrder(String.class, Integer.class));
  }

  @Test(expected = NullPointerException.class)
  public void attach_nullEventBus_shouldThrowException() {
    // given
    EventTracker tracker = new EventTracker();

    // when
    tracker.attach(null);

    // then exception should be thrown
  }

  @Test
  public void attach_reattachDifferentEventBus_shouldStopListeningToTheFirstOne() {
    // given
    EventTracker tracker = new EventTracker(String.class);
    EventBus eventBus1 = new DefaultEventBus();
    EventBus eventBus2 = new DefaultEventBus();
    tracker.attach(eventBus1);
    eventBus1.post("foo");
    eventBus2.post("bar");

    // when
    tracker.attach(eventBus2);
    eventBus1.post("buzz");
    eventBus2.post("qux");

    // then
    assertThat(tracker.getEvents(String.class), contains("foo", "qux"));
  }

  @Test
  public void detach_somethingAlreadySent_shouldStopListeningToEventsPostedLater() {
    // given
    EventTracker tracker = new EventTracker(String.class);
    EventBus eventBus = new DefaultEventBus();
    tracker.attach(eventBus);
    eventBus.post("foo");

    // when
    tracker.detach();
    eventBus.post("bar");

    // then
    assertThat(tracker.getEvents(String.class), contains("foo"));
  }

  @Test
  public void getEvents_untrackedEventType_shouldReturnEmptyList() {
    // given
    EventTracker tracker = new EventTracker(Integer.class);
    EventBus eventBus = new DefaultEventBus();
    tracker.attach(eventBus);
    eventBus.post("bar");

    // when
    List<String> events = tracker.getEvents(String.class);

    // then
    assertThat(events, empty());
  }

  @Test(expected = UnsupportedOperationException.class)
  public void getEvents_tryToAttackStateByModifyingResult_shouldThrowException() {
    // given
    EventTracker tracker = new EventTracker(String.class);
    EventBus eventBus = new DefaultEventBus();
    tracker.attach(eventBus);
    eventBus.post("foo");
    List<String> events = tracker.getEvents(String.class);

    // when
    events.clear();

    // then exception should be thrown
  }

  @Test(expected = NullPointerException.class)
  public void getEvents_nullEventType_shouldThrowException() {
    // given
    EventTracker tracker = new EventTracker();

    // when
    tracker.getEvents(null);

    // then exception should be thrown
  }

  @Test
  public void assertOnlyOne_nothingWasPosted_shouldThrowError() {
    // given
    EventTracker tracker = new EventTracker(String.class);
    EventBus eventBus = new DefaultEventBus();
    tracker.attach(eventBus);

    thrown.expect(AssertionError.class);
    thrown.expectMessage("No event of type java.lang.String was posted to EventBus");

    // when
    tracker.assertOnlyOne(String.class);

    // then error should be thrown
  }

  @Test
  public void assertOnlyOne_2eventsPosted_shouldThrowError() {
    // given
    EventTracker tracker = new EventTracker(String.class);
    EventBus eventBus = new DefaultEventBus();
    tracker.attach(eventBus);
    eventBus.post("foo");
    eventBus.post("bar");

    thrown.expect(AssertionError.class);
    thrown.expectMessage("More than 1 event of type java.lang.String was posted to EventBus: " + 2);

    // when
    tracker.assertOnlyOne(String.class);

    // then error should be thrown
  }

  @Test(expected = NullPointerException.class)
  public void assertOnlyOne_nullEventType_shouldThrowException() {
    // given
    EventTracker tracker = new EventTracker();

    // when
    tracker.assertOnlyOne(null);

    // then exception should be thrown
  }

  @Test
  public void clearEvents_whenOneStringEventTracked_shouldClearEvents() {
    // given
    EventTracker tracker = new EventTracker(String.class);
    EventBus eventBus = new DefaultEventBus();
    tracker.attach(eventBus);
    eventBus.post("foo");

    // when
    tracker.clearEvents();

    // then
    assertThat(tracker.getEvents(String.class), empty());
  }

}
