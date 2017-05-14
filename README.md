# About github-users

Lists GitHub users. Minimal app demonstrating cross-platform development
(Web, Android, iOS) on top of Java to JavaScript and Java to Objective-C
[transpilers](https://en.wikipedia.org/wiki/Source-to-source_compiler).

# Demo

Please rather take a look at frontend flavors:

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
* the same features release for all the platforms
* and the time-to-market for new value is shorter

Just to name a few.
Unlike in other cross-platform development methodologies, here UI developers are given full
control over look and feel of their applications. I also believe that such an approach,
abstracting application presentation logic from specific platform, brings much better overall
architecture which pays off in the future.

As there is no blueprint from Google on how to build applications like Google Inbox, I decided
to use my whole experience to "reverse engineer" possible approach and provide such a minimal project.
I hope to push it even further in terms of reactive
programming on top of RxJava as it is quite popular on Android, there is GWT port, and apparently
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

Technically its a library containing Java code which will be transpiled either
to JavaScript or to Objective-C code for Web and iOS platform respectively. In case of
Android platform the Java code can be used directly.

## Dependencies

Only minimal set of Java 8 classes is used plus:

* [javax-inject](http://javax-inject.github.io/javax-inject/) - JSR-330 Dependency Injection
* [RxJava](https://github.com/ReactiveX/RxJava) - Reactive Extensions for the JVM
* [junit](http://junit.org/junit4/) - JUnit is a simple framework to write repeatable tests
* [mockito](http://site.mockito.org/) - Tasty mocking framework for unit tests in Java
* [hamcrest](http://hamcrest.org/JavaHamcrest/) - Matchers that can be combined to create flexible expressions of intent 

These popular dependencies will either have emulation on all the platforms or
transpile to the native code.

## Model-View-Presenter

Basically view is dumb and can be mocked while presenter has testable logic.

https://en.wikipedia.org/wiki/Model%E2%80%93view%E2%80%93presenter

## Reactive paradigm

RxJava is a crucial component of this solution and supports:
 
* general EventBus which decouples presenters and therefore visual components
* handling of asynchronous responses from remote web services
* abstracting the way how UI events are streamed to the presenter logic

## EventBus

The singleton EventBus allows indirect communication of the presenters
resolving traditional issue of direct coupling and component nesting in the UI code.

    UserQueryPresenter -- UserQueryEvent -----> +----------+
    UserListPresenter <-- UserQueryEvent ------ | EventBus |
    UserPresenter ------- UserSelectedEvent --> +----------+

Any other component might subscribe to EventBus in the frontend implementation
to receive `UserSelectedEvent` and redirect view to GitHub profile in platform-specific way.

## Testing

By following MVP principles all the views are prepared in the way they can be mocked and
assumptions can be made against their state in the unit tests. Ready presenters are
coming with full test coverage and test cases can be transpiled as well to be run again on
the target platform.
