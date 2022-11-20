<a href="https://pinterest.github.io/ktlint/"><img src="https://img.shields.io/badge/code%20style-%E2%9D%A4-FF4081.svg" alt="ktlint"></a>

# kactor

A small proof of concept implementing the actor model using Kotlin coroutines. What's an actor? Actors are one of the first concurrency models created in Computer Science in 1973 by Carl Hewitt, and co.

## Introduction

An actor is a computational entity that receives and processes asynchronous messages to maintain a state. In detail, in response to the reception of a message, an actor can:

- create a finite number of new actors;
- designate the behavior to be used for the next message.
- send a finite number of messages to other actors;

One of the most famous implementation of the actor model is [Akka](https://akka.io/), a toolkit and runtime for building highly concurrent, distributed, and fault-tolerant event-driven applications on the JVM. Akka is based on concurrency built directly on top of thread.

In Kotlin, we have **coroutines**, which are a form of lightweight threads. Coroutines are a great fit for the actor model, because they are cheap to create, and they can be suspended and resumed at any time. In this project, we will implement the actor model using coroutines. In detail, we will implement something strictly inspired to the API offered by the [Akka Typed library](https://doc.akka.io/docs/akka/current/typed/index.html).

## Create an Actor

Creating an actor is quite easy. However, there are two different kind of actors: The actor system and all the other actors. The actor system is the root of the actor hierarchy. It is the first actor that is created. We can create an actor system using the `kactorSystem` actor builder:

```kotlin
coroutineScope {
    val mainKActorRef = kactorSystem(MainActor.behavior())
}
``` 

Since the builder is an extension function of a `CoroutineScope`, we need a scope to create an actor system. To create an actor we need a behavior (more in the following sections). The creation of the actor system returns a reference to the created actor. As we will see, we can use such reference to send messages to the actor.

Once we have an actor system defined, we can _spawn_ other actors. To do so, we can use the `spawn` actor builder:

```kotlin
object ReplyReceived
val behavior = setup { ctx -> 
    val helloWorldActorRef = ctx.spawn("kactor_$i", HelloWorldActor.behavior)
}
```

The `spawn` builder is defined as an extension function of a `KactorContext<T>`. The `KactorContext<T>` is the context of an actor, giving access to a lot of functionalities. We can obtain a context using some of the behavior builders. In the above example, we used the `setup` builder.

## Actor Behavior

An actor behavior is a function that defines how an actor should react to a message. In detail, an actor behavior is a function that takes a message and returns a new behavior. The new behavior will be used to process the next message. All the actor builders we have seen so far require a behavior in input.

We represent an actor behavior with the type `KBehavior<T>`. The type parameter `T` represents the type of the messages that the actor can process.

We can create a behavior using one of the available builders. Let's see which are available in the library.

### The `setup` Builder

The first behavior builder we see is the `setup` builder. It allows to configure all the aspect of the behavior of an actor that are not strictly related to the processing of a message. In the example we have seen before, we used the `setup` builder to create an actor that spawns another actor. The `setup` builder takes a function that takes a `KactorContext<T>` and returns a `KActorBehavior<T>`. 

## Disclosure

I know that in the `kotlinx-coroutines-core`library there is a coroutine builder called `actor<T>` that uses `Channel`s to build a representation of an actor. However, I need to learn coroutines. So, why don't reimplement an actor model from scratch? 😜

Anyway, the library is not intended to be used in production. It is just a proof of concept.