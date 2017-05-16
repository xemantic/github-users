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

package com.xemantic.githubusers.logic.eventbus;

import rx.Subscription;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Tracks events posted to the {@link EventBus}. Can attach to any {@link EventBus}
 * instance and then intercept and store all the events of preconfigured types
 * allowing further inspection which is useful in tests.
 *
 * @author morisil
 */
public class EventTracker {

  private final Set<Class<?>> eventTypes;

  private final Map<Class<?>, List<Object>> eventMap = new HashMap<>();

  private List<Subscription> subscriptions = Collections.emptyList();

  /**
   * Creates event tracker instance tracking given {@code eventTypes}.
   *
   * @param eventTypes the enumeration of event classes.
   */
  public EventTracker(Class<?> ... eventTypes) {
    this(Arrays.asList(eventTypes));
  }

  /**
   * Creates event tracker instance tracking given {@code eventTypes}.
   *
   * @param eventTypes the collection of event classes.
   * @throws IllegalArgumentException if any of the {@code eventTypes} is {@code null}.
   */
  public EventTracker(Collection<Class<?>> eventTypes) {
    Objects.requireNonNull(eventTypes);
    for (Class<?> type : eventTypes) {
      if (type == null) {
        throw new IllegalArgumentException("eventType cannot be null");
      }
    }
    this.eventTypes = Collections.unmodifiableSet(new HashSet<>(eventTypes)); // defensive copy
  }

  /**
   * Returns set of tracked event types.
   *
   * @return immutable set of classes representing event types.
   */
  public Set<Class<?>> getTrackedEventTypes() {
    return eventTypes;
  }

  /**
   * Attaches this tracker to given {@code eventBus}.
   * <p>
   *   Note: it will detach from any other already subscribed event bus.
   * </p>
   *
   * @param eventBus the eventBus instance to track.
   * @see #detach()
   */
  public void attach(EventBus eventBus) {
    Objects.requireNonNull(eventBus);
    detach();
    subscriptions = eventTypes.stream()
        .map(eventType ->
            eventBus.observe(eventType)
                .subscribe(event -> saveEvent(eventType, event))
        ).collect(Collectors.toList());
  }

  /**
   * Detaches this tracker from the {@link EventBus} instance.
   * Another event bus can be attached again.
   *
   * @see #attach(EventBus)
   */
  public void detach() {
    for (Subscription subscription : subscriptions) {
      subscription.unsubscribe();
    }
  }

  /**
   * Returns events of given {@code eventType} which were tracked by this tracker.
   *
   * @param eventType the event type class.
   * @param <T> the expected type generic of events.
   * @return the typed list of events.
   */
  @SuppressWarnings("unchecked")
  public <T> List<T> getEvents(Class<T> eventType) {
    Objects.requireNonNull(eventType);
    return (List<T>) Optional.ofNullable(eventMap.get(eventType))
        .map(Collections::unmodifiableList)
        .orElse(Collections.emptyList());
  }

  /**
   * Asserts that there is only one tracked event of given {@code eventType}
   * and returns it.
   *
   * @param eventType the event type class.
   * @param <T> the expected type generic of the event.
   * @return the event instance.
   */
  public <T> T assertOnlyOne(Class<T> eventType) {
    Objects.requireNonNull(eventType);
    List<T> events = getEvents(eventType);
    if (events.isEmpty()) {
      throw new AssertionError(
          "No event of type "
              + eventType.getName()
              + " was posted to EventBus");
    }
    if (events.size() > 1) {
      throw new AssertionError(
          "More than 1 event of type "
              + eventType.getName()
              + " was posted to EventBus: "
              + events.size());
    }
    return events.get(0);
  }

  /**
   * Clears all the events tracked by this tracker.
   */
  public void clearEvents() {
    eventMap.clear();
  }

  private void saveEvent(Class<?> eventType, Object event) {
    eventMap.computeIfAbsent(eventType, type -> new LinkedList<>()).add(event);
  }

}
