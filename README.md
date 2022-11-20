<a href="https://pinterest.github.io/ktlint/"><img src="https://img.shields.io/badge/code%20style-%E2%9D%A4-FF4081.svg" alt="ktlint"></a>

# kactor

A small proof of concept implementing the actor model using Kotlin coroutines. What's an actor? Actors are one of the first concurrency models created in Computer Science in 1973 by Carl Hewitt, and co.

An actor is a computational entity that receives and processes asynchronous messages to maintain a state. In detail, in response to the reception of a message, an actor can:

- send a finite number of messages to other actors;
- create a finite number of new actors;
- designate the behavior to be used for the next message.

One of the most famous implementation of the actor model is [Akka](https://akka.io/), a toolkit and runtime for building highly concurrent, distributed, and fault-tolerant event-driven applications on the JVM. Akka is based on concurrency built directly on top of thread.

In Kotlin, we have coroutines, which are a form of lightweight threads. Coroutines are a great fit for the actor model, because they are cheap to create, and they can be suspended and resumed at any time. In this project, we will implement the actor model using coroutines.
