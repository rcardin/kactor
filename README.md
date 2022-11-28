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

### Getting Started

This section gives a brief overview of the main feature of the `kactor` library. For a more detailed explanation, please refer to the following sections.

First of all, we'll define the messages the actor accepts. Let's say we want to implement a counter. The counter can be incremented. The counter can also be reset to zero. The counter can also be queried for its current value:

```kotlin
object Counter {
    sealed interface Command
    data class Increment(val by: Int) : Command
    object Reset : Command
    data class GetValue(val replyTo: KActorRef<Int>) : Command
}
```

Then, we defined the behavior the actor must use to respond to the above commands. We use the `receiveMessage` behavior builder, which is a helper function that allows us to write the behavior of the actor as a function of the message received. The `receiveMessage` function takes a function as input, and returns a `KBehavior<Command>`. The function receives in input the next message to process: 

```kotlin
object Counter {
    fun behavior(currentValue: Int): KBehavior<Command> = receiveMessage { msg ->
        when (msg) {
            is Counter.Increment -> behavior(currentValue + msg.by)
            is Counter.Reset -> behavior(0)
            is Counter.GetValue -> {
                msg.replyTo `!` currentValue
                same()
            }
        }
    }
}
```

We manage the actor state in a functional way, passing it as an input of the next behavior to have. In case no change to the behavior is required, we can use the `same()` function, which returns the same behavior as before.

In case of a query, we use the actor reference, `KActorRef<Int>`, contained in the message to send the response. The _bang_ function is an alias for the `tell` method, used to send a message to an actor through its reference.

The actual creation of the above actor requires the definition of an _actor system_ first, which is a special actor that manages the creation of the context needed by all the other actors. First, we need to define the behavior of the actor system:

```kotlin
object MainActor {
    
    val behavior: KBehavior<Int> = setup { ctx ->
        val counterRef = ctx.spawn("counter", Counter.behavior(0))
        
        counterRef `!` Counter.Increment(40)
        counterRef `!` Counter.Increment(2)
        counterRef `!` Counter.GetValue(ctx.actorRef)
        counterRef `!` Counter.Reset
        counterRef `!` Counter.GetValue(ctx.actorRef)
        
        receiveMessage { msg ->
            ctx.log.info("The counter value is $msg")
            same()
        }
    }
}
```

In the above case, we introduced the `setup` behavior builder, which defines the operation to execute during the actor creation. In detail, we `spawn` the counter actor, using the `KActorContext<T>` instance provided to each actor during the creation. The `spawn` method returns a reference the freshly new created actor. which we can use to send messages to it. Moreover, through the context, we can retrieve a reference to the actor itself. 

Finally, we defined the behavior of the `MainActor`, which is merely logging the retrieved value of the counter. We can access to an instance of the logger through the context instance.

With the behavior of the actor system defined, we can create the actor system itself:

```kotlin
suspend fun main(): Unit = coroutineScope {
    kactorSystem(MainActor.behavior)
}
```

The `kactorSystem` function is an extension function of the `CoroutineScope` class. So, we need to create a scope first, which is done using the `coroutineScope` suspending function. The execution of the above program will produce an output similar to the following

```
15:20:56.618 [DefaultDispatcher-worker-3] [{kactor=kactor-system}] INFO  kactor-system - The counter value is 42
15:20:56.624 [DefaultDispatcher-worker-3] [{kactor=kactor-system}] INFO  kactor-system - The counter value is 0
```

## Main Types

The whole library is based on top of only few types. In details, the main types are:

* `KBehavior<T>`: It represents the behavior of an actor that accepts messages of type `T`. The behavior defines how the actor reacts to the external messages.
* `KActor<T>`: It represents an actor that accepts messages of type `T`. It's behavior is fully defined by a `KBehavior<T>` object.
* `KActorRef<T>`: It represents a reference to an actor. Through it, it's possible to send messages to actors.
* `KActorContext<T>`: It gives access to some important feature to actors. Each actor owns a context. For example, through it, the actor can access to the reference to itself, to the logger, to its name.

## Actor Behavior

An actor behavior is a function that defines how an actor should react to a message. In detail, an actor behavior is a function that takes a message and returns a new behavior. The new behavior will be used to process the next message. We represent an actor behavior with the type `KBehavior<T>`. The type parameter `T` represents the type of the messages that the actor can process.

The only way to create an instance of the type `KBehavior<T>` is through the available builders. Let's see which are the ones available.

### The `receiveMessage` Builder

The `receiveMessage` builder is the most basic one. It allows to define the behavior of an actor as a function of the message received. The function passed to the builder must return a new behavior. The new behavior will be used to process the next message. 

```kotlin
object Counter {
    // ...
    fun behavior(currentValue: Int): KBehavior<Command> = receiveMessage { msg ->
        when (msg) {
            is Increment -> behavior(currentValue + msg.by)
            is Reset -> behavior(0)
            // ...
        }
    }
}
```


### The `setup` Builder

The `setup` builder is useful to perform some initialization operations before the actor starts processing messages. For example, we can use it to create other actors, to initialize the state of the actor, and so on.

It allows to access the instance of the `KactorContext<T>` of an actor:

```kotlin
object MainActor {

    val behavior: KBehavior<Int> = setup { ctx ->
        val counterRef = ctx.spawn("counter", Counter.behavior(0))
// ...
    }
}
```

Through the context, it's possible to create new actors, to access to the reference and the name of the actor itself, and to a logger:

```kotlin
ctx.log.info("Getting the value of the counter")
counterRef `!` Counter.GetValue(ctx.actorRef)
```

The function in input to the `setup` builder must return a behavior. So, usually, we use one of the other builders to to define the returned behavior. 

### The `same` Builder

The `same` builder is a builder that returns the same behavior as before. It's useful when the actor does not need to change its behavior:

```kotlin
object Counter {
    // ...
    fun behavior(currentValue: Int): KBehavior<Command> = receiveMessage { msg ->
        when (msg) {
            // ...
            is GetValue -> {
                msg.replyTo `!` currentValue
                same()  // No change to the behavior is required
            }
        }
    }
}
```

It's not possible to create the main behavior of an actor using the `same` builder directly. Doing it will result in an exception at runtime.


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

## Disclosure

I know that in the `kotlinx-coroutines-core`library there is a coroutine builder called `actor<T>` that uses `Channel`s to build a representation of an actor. However, I need to learn coroutines. So, why don't reimplement an actor model from scratch? 😜

Anyway, the library is not intended to be used in production. It is just a proof of concept.