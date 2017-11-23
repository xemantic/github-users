[![Build Status](https://api.travis-ci.org/xemantic/github-users.svg?branch=master)](https://travis-ci.org/xemantic/github-users)

# About github-users

Lists GitHub users. Minimal app demonstrating cross-platform app development
(Web, Android, iOS) where core logic is shared and
[transpiled](https://en.wikipedia.org/wiki/Source-to-source_compiler)
from Java to JavaScript and Objective-C.

# Demo

This project provides only source code of the shared logic. Here is
the web version:

https://github-users-web.appspot.com/

See also: 

* [github-users-web](https://github.com/xemantic/github-users-web)
* [github-users-android](https://github.com/xemantic/github-users-android)
* [github-users-ios](https://github.com/xemantic/github-users-ios)

# Why I started such a project?

Google claims that applications like [Google Inbox](https://www.google.com/inbox/)
can [share up to 70% of client code between all the platforms](https://gmail.googleblog.com/2014/11/going-under-hood-of-inbox.html).
It's significant achievement when taking into account:

* reduced Total Cost of Ownership with only one code base to maintain
* naturally synchronized development process across all the teams
(backend, presentation logic, frontend, mobile)
* the same features released for all the platforms
* and the time-to-market for new value is shorter

Just to name a few. Usually cross-platform development tools fall into these categories:

* abstraction over native UI components and IO operations - specialized API
accessible from programming language of choice which is either interpreted
or compiled for specific platform (possibilities are limited by what the API has to offer)
* the use of HTML+CSS on all platforms (discarding the possible advantages offered by
native solutions on mobile devices) 

But there is a third way, where only presentation logic code is shared and UI rendering
and IO stays native. Where iOS, Web and Android developers can customize the app in every
detail.

This approach seems to be the most demanding one in terms of software architecture. Common business
logic cannot longer be expressed in purely technical terms of low-level events, requests,
responses or threads. Mouse clicks or touch events are becoming a
stream of semantically defined user intents like "select element". HTTP requests to remote services
are becoming a streams of domain data being provided asynchronously in any moment.
Concurrency is handled by declaring *what* to do leaving *when*
and *how* to reactive framework. Such an approach, even though more challenging conceptually,
is worth the effort. Abstracting app logic from specific
platform brings much better overall architecture which pays off in the future when
the application grows.

As there is no blueprint from Google on how to build applications like Google Inbox, I decided
to use my whole experience to "reverse engineer" possible approach and provide such a minimal project.
I hope to push it even further in terms of reactive
programming on top of RxJava as it is quite popular on Android, there is a GWT port, and apparently
it is possible to transpile the whole library to Objective-C.

It does not matter so much what this application is doing and if it is useful at all.
I did not want to provide any backend component and struggle with deployment. Therefore I decided
to display data loaded from one of public APIs available on the Internet and
[GitHub users search API](https://developer.github.com/v3/search/#search-users)
will serve as a good example.

# Use case

As application user I want to submit query to search for GitHub users 
so that relevant user list will be displayed.

# Architecture

This project provides conceptual presentation logic without actual UI code bound to any platform.

For the platform specific code see:

* [github-users-web](https://github.com/xemantic/github-users-web)
* [github-users-android](https://github.com/xemantic/github-users-android)
* [github-users-ios](https://github.com/xemantic/github-users-ios)

Technically it is a library containing Java code which will be transpiled either
to JavaScript ([GWT](http://www.gwtproject.org/)) or to
Objective-C ([J2ObjC](https://developers.google.com/j2objc/))
code for Web and iOS platform respectively. In case of
Android platform the Java code can be used directly.

## Dependencies

Only minimal set of Java 8 classes is used plus:

* [javax-inject](http://javax-inject.github.io/javax-inject/) - JSR-330 Dependency Injection
* [RxJava](https://github.com/ReactiveX/RxJava) - Reactive Extensions for the JVM
* [junit](http://junit.org/junit4/) - JUnit is a simple framework to write repeatable tests
* [mockito](http://site.mockito.org/) - Tasty mocking framework for unit tests in Java
* [AssertJ](http://joel-costigliola.github.io/assertj/) - Fluent assertions for java 

These popular dependencies will either have emulation on all the platforms or
be transpiled to the native code.

## Model-View-Presenter

Basically view is dumb and can be mocked while presenter has testable logic.

See [Model-View-Presenter article](https://en.wikipedia.org/wiki/Model%E2%80%93view%E2%80%93presenter)
article on Wikipedia.

## Reactive paradigm

RxJava is a crucial component of this solution providing:
 
* event distribution mechanism decoupling presenters and therefore also associated visual components
* handling of asynchronous responses from remote web services
* abstracting the way how UI events are streamed to the presenter logic

## Events

Application events are defined in the
[com.xemantic.githubusers.logic.event](src/main/java/com/xemantic/githubusers/logic/event/)
package. Thanks to being separated from the rest of application logic, they can be
easily used to decouple components (presenter logic).

Event distribution is based on event channels where:

* publisher is injected with [Sink](src/main/java/com/xemantic/githubusers/logic/event/Sink.java)
* subscriber is injected with `Observable`

Note: the original design was based on the EventBus concept, but @ibaca pointed out that thanks
to typed injections, it is possible to eliminate explicit EventBus completely.

### Emitting Events

```java
public class FooPresenter {
  
  private final Sink<BarEvent> barSink;
  
  @Inject
  public FooPresenter(Sink<BarEvent> barSink) {
    this.barSink = barSink;    
  }
  
  public void handleMyAction() {
    barSink.publish(new BarEvent("bar"));
  }
  
}
```

Note: several `Sink`s of different event types can be injected.

### Receiving Events
 
```java
public class BuzzPresenter {
  
  private final Observable<BarEvent> barEvent$;
  
  @Inject
  public BuzzPresenter(Observable<BarEvent> barEvent$) {
    this.barEvent$ = barEvent$;    
  }
  
  public void start() {
    barEvent$.subscribe(e -> log(e.getPayload()));
  }
  
}
```

Note: several `Observable`s of different event types can be injected. 

### Application Events vs Platform UI Events

The only events defined in this project are application events. Platform events specific
to UI will always come out of View interfaces and their `observe` + _Intent_ methods.

Many UI events, like specific user intent received via click or touch event, will not
carry any payload. They will be just marked with
[Trigger](src/main/java/com/xemantic/githubusers/logic/event/Trigger.java) as an event
type.

### Presenter Lifecycle and Events

When presenter is started it will usually:

* subscribe to general application events
* subscribe to events generated by UI actions

The presenter logic defines how to react to these events, it might:

* display something on view
* call external service
* change internal presenter state
* publish general application events to be received by decoupled event consumers

Most of these operations are easily testable with mocked view.
 
## Service Access Layer

### Service

Is provided exclusively by interfaces [UserSearchService](src/main/java/com/xemantic/githubusers/logic/service/UserService.java)
which returns `Observable` (technically `Single`) of [SearchResult](src/main/java/com/xemantic/githubusers/logic/model/SearchResult.java)
holding also the list of [User](src/main/java/com/xemantic/githubusers/logic/model/User.java)s.

Services can be implemented using:

* [Retrofit + RxJava](http://square.github.io/retrofit/) on Android
* [AutoREST for GWT](https://github.com/intendia-oss/autorest)

### Model

The structure of `SearchResult` interface reflects
[JSON structure of GitHub API response](https://developer.github.com/v3/search/#search-users).

When implementing these entities various methods might be used like

* GSON/jackson json parser for android
* `@JsInterop` annotations for GWT

## View

See [com.xemantic.githubusers.logic.view](src/main/java/com/xemantic/githubusers/logic/view)
package.

## Presenter

See [com.xemantic.githubusers.logic.presenter](src/main/java/com/xemantic/githubusers/logic/presenter)
package.

Expectations for these presenters are visible in their
[test cases](src/test/java/com/xemantic/githubusers/presenter)
which account for most code in this project.

## Testing

By following MVP principles all the views are prepared in the way they can be mocked and
assumptions can be made against their state in the unit tests. Ready presenters are
coming with full test coverage and test cases can be transpiled as well to be run again on
the target platform. See example
[UserPresenterTest](src/test/java/com/xemantic/githubusers/presenter/UserPresenterTest.java).

Note: don not use `inOrder.verifyNoMoreInteractions()`, for some reason
much more meaningful message comes from `verify(all, the, mocks, again)`

# User Experience design

The [Material Design](https://material.io/guidelines/) will be used on all the platforms
with help of [Material Components](https://material.io/components/).

# Versioning

This project is following [Semantic Versioning](http://semver.org/) scheme
with 3 decimal numbers separated by dots. All 3 version numbers
(major, minor, bugfix) should be always present, which implies that for
major and minor releases bugfix version will be set to 0.
