![Kotlin Version](https://img.shields.io/badge/Kotlin-1.7.20-blue?style=flat&logo=kotlin)
![GitHub Workflow Status (with branch)](https://img.shields.io/github/actions/workflow/status/rcardin/kactor/ci.yml?branch=main)
![Maven Central](https://img.shields.io/maven-central/v/in.rcard/kactor-core)
![GitHub release (latest by date)](https://img.shields.io/github/v/release/rcardin/kactor)
<a href="https://pinterest.github.io/ktlint/"><img src="https://img.shields.io/badge/code%20style-%E2%9D%A4-FF4081.svg" alt="ktlint"></a>

# kactor 🎭

A small proof of concept implementing the actor model using Kotlin coroutines. What's an actor? Actors are one of the first concurrency models created in Computer Science in 1973 by Carl Hewitt and Co.

## Introduction

An actor is a computational entity that receives and processes asynchronous messages to maintain a state. In detail, in response to the reception of a message, an actor can:

- Create a finite number of new actors;
- Designate the behavior to be used for the following message.
- Send a finite number of messages to other actors;

One of the most famous implementations of the actor model is [Akka](https://akka.io/), a toolkit and runtime for building highly concurrent, distributed, and fault-tolerant event-driven applications on the JVM. Akka is based on concurrency constructed directly on top of threads.

In Kotlin, we have **coroutines**, a form of lightweight threads. Coroutines are an excellent fit for the actor model because they are cheap to create and can be suspended and resumed at any time. In this project, we will implement the actor model using coroutines. In detail, we will implement something strictly inspired by the API offered by the [Akka Typed library](https://doc.akka.io/docs/akka/current/typed/index.html).

### Getting Started

This section gives a brief overview of the main feature of the `kactor` library. For a more detailed explanation, please refer to the following guide sections.

To use the `kactor` library, you need to add the following dependency to your `pom.xml` file:

```xml
<dependency>
  <groupId>in.rcard</groupId>
  <artifactId>kactor-core</artifactId>
  <version>0.0.1</version>
</dependency>
```

First of all, we'll define the messages the actor accepts. Let's say we want to implement a counter. The counter can be incremented. The counter can also be reset to zero. The counter can also be queried for its current value:

```kotlin
object Counter {
    sealed interface Command
    data class Increment(val by: Int) : Command
    object Reset : Command
    data class GetValue(val replyTo: KActorRef<Int>) : Command
}
```

Then, we defined the actor's behavior to respond to the above commands. We use the `receiveMessage` behavior builder, a helper function that allows us to write the actor's behavior as a function of the message received. The `receiveMessage` function takes a lambda as input and returns a `KBehavior<Command>`. The lambda receives in input the messages to process:

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

We functionally manage the actor state, passing it as an input of the following behavior. If there is no change to the behavior, we can use the `same()` function, which returns the same behavior.

In case of a query, we use the actor reference, `KActorRef<Int>`, contained in the message to send the response. The _bang_ function is an alias for the `tell` method, used to send a message to an actor through its reference.

The creation of the above actor requires the definition of an _actor system_ first, which is a particular actor that manages the creation of the context needed by all the other actors. First, we need to define the behavior of the actor system:

```kotlin
object MainActor {
    
    val behavior: KBehavior<Int> = setup { ctx ->
        val counterRef = ctx.spawn("counter", Counter.behavior(0))
        
        counterRef `!` Counter.Increment(40)
        counterRef `!` Counter.Increment(2)
        counterRef `!` Counter.GetValue(ctx.self)
        counterRef `!` Counter.Reset
        counterRef `!` Counter.GetValue(ctx.self)
        
        receiveMessage { msg ->
            ctx.log.info("The counter value is $msg")
            same()
        }
    }
}
```

In the above case, we introduced the `setup` behavior builder, which defines the operation to execute during the actor creation. In detail, we `spawn` the counter actor using the `KActorContext<T>` instance provided to each actor during its creation. The `spawn` method returns a reference to the freshly created actor, which we can use to send messages to it. Moreover, we can retrieve an actor's reference through the context.

Finally, we defined the behavior of the `MainActor`, which is merely logging the retrieved value of the counter. We can access an instance of the logger through the context instance.

With the behavior of the actor system defined, we can create the actor system itself:

```kotlin
suspend fun main(): Unit = coroutineScope {
    kactorSystem(MainActor.behavior)
}
```

The `kactorSystem` function is an extension of the `CoroutineScope` class. So, we need to create a scope first, which is done using the `coroutineScope` suspending function. The execution of the above program will produce an output similar to the following.

```
15:20:56.618 [DefaultDispatcher-worker-3] [{kactor=kactor-system}] INFO  kactor-system - The counter value is 42
15:20:56.624 [DefaultDispatcher-worker-3] [{kactor=kactor-system}] INFO  kactor-system - The counter value is 0
```

## Main Types

The whole library is based on top of only a few types. In detail, the main types are:

* `KBehavior<T>`: It represents an actor's behavior that accepts type `T` messages. The behavior defines how the actor reacts to external messages.
* `KActor<T>`: It represents an actor that accepts type `T` messages. It's behavior is entirely defined by a `KBehavior<T>` object.
* `KActorRef<T>`: It represents a reference to an actor. Through it, it's possible to send messages to actors.
* `KActorContext<T>`: It gives access to some critical features to actors. Each actor owns a context. For example, through it, the actor can access the reference to itself, the logger, and its name.

## Actor Behavior

An actor's behavior is a function that defines how an actor should react to a message. In detail, an actor's behavior is a function that takes a message and returns a new behavior. The new behavior will be used to process the next message. We represent an actor's behavior with the KBehavior<T> type. The type parameter `T` represents the messages the actor can process.

The only way to create an instance of the `KBehavior<T> type is through the available builders. Let's see which are the ones available.

### The `receiveMessage` Builder

The `receiveMessage` builder is the most basic one. It allows us to define an actor's behavior as a function of the message received. The function passed to the builder must return a new behavior that will be used to process the next message:

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

The `setup` builder is helpful to perform some initialization operations before the actor starts processing messages. For example, we can use it to create other actors, initialize the actor's state, etc.

It allows to access the instance of the `KactorContext<T>` of an actor:

```kotlin
object MainActor {

    val behavior: KBehavior<Int> = setup { ctx ->
        val counterRef = ctx.spawn("counter", Counter.behavior(0))
// ...
    }
}
```

Through the context, it's possible to create new actors, to access the reference and the name of the actor itself, and to a logger:

```kotlin
ctx.log.info("Getting the value of the counter")
counterRef `!` Counter.GetValue(ctx.self)
```

The function in input to the `setup` builder must return a behavior. So, usually, we use one of the other builders to define the returned behavior, such as the `receiveMessage` or the `receive` builders.

### The `receive` Builder

If we need to define the behavior used to process incoming messages and we also need to access the feature available in the context, we can use the `receive` builder:

```kotlin
object HelloWorldActor {
    
    data class SayHello(val name: String, val replyTo: KActorRef<ReplyReceived>)
    
    val behavior: KBehavior<SayHello> =
        receive { ctx, msg ->
            ctx.log.info("Hello ${msg.name}!")
            msg.replyTo `!` ReplyReceived
            same()
        }
}
```

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

It's impossible to create an actor's primary behavior using the `same` builder directly. Doing it will result in an exception at runtime.

### The `stopped` Builder

Sometimes, after processing, we need to stop an actor. In this case, we can use the `stopped` builder, which creates a behavior that tells the actor system to stop the actor:

```kotlin
object MainActor {

    object Start

    fun behavior(): KBehavior<Start> =
        setup { ctx ->
            val routeRef = ctx.router("greeter", 1000, Greeter.behavior)
            repeat(1000) {
                routeRef `!` Greeter.Greet(it)
            }
            stopped()
        }
}
```

In the above example, we stop the actor after creating a router actor and sending 1000 messages.

⚠️ Feature still under development: Children actors are not stopped when the parent stops ⚠️

## Create an Actor

Creating an actor is relatively easy. However, there are two different kinds of actors: The actor system and all the other actors. The actor system is the root of the actor hierarchy. It is the first actor that is created. We can create an actor system using the `kactorSystem` actor builder:

```kotlin
coroutineScope {
    val mainKActorRef = kactorSystem(MainActor.behavior())
}
``` 

Since the builder is an extension function of a `CoroutineScope`, we need a scope to create an actor system. To create an actor, we need a behavior (more in the following sections). The creation of the actor system returns a reference to the created actor, and we can use such reference to send messages to the new, freshly created actor.

Once an actor system is defined, we can _spawn_ other actors. To do so, we can use the `spawn` actor builder:

```kotlin
val behavior = setup { ctx -> 
    val helloWorldActorRef = ctx.spawn("kactor_$i", HelloWorldActor.behavior)
    // ...
}
```

The `spawn` builder is the `KactorContext<T>` extension function. As we saw in previous sections, we can retrieve an actor context using the `setup` and the `receive` behavior builders. In the above example, we used the `setup` builder. Every actor other than the actor system has a name passed as a parameter during actor creation.

Many times, an actor uses some resources that must be released when the actor stops. To do so, we can use the `finally` input lambda of the `spawn` builder:

```kotlin
val behavior: KBehavior<Start> = receive { ctx, _ ->
    val res = Resource("my-resource")
    val kRef = ctx.spawn(
        "resKactor",
        ResourceKActor.behavior(res),
        finally = { res.close() }
    )
    kRef `!` ResourceKActor.UseIt
    same()
}
```

The actor system guarantees that the `finally` lambda is called when the actor stops, both in response to an error or voluntarily. The `finally` block receives the `Throwable` that caused the actor to stop, if any:

```kotlin
fun <T> KActorContext<*>.spawn(
    name: String,
    behavior: KBehavior<T>,
    finally: ((ex: Throwable?) -> Unit)? = null
): KActorRef<T>
```

In the `finally` block, we can pattern match the `Throwable` to understand what happened and react accordingly.

## Sending Messages to an Actor

The only way to send messages to an actor is through its reference. A reference to an actor has the type `KActorRef<T>`, where `T` is the type of messages the actor can process. We can obtain a reference to an actor during its creation:

```kotlin
val helloWorldActorRef: KActorRef<SayHello> = ctx.spawn("kactor_$i", HelloWorldActor.behavior)
```

Moreover, every actor context stores an actor reference to the actor itself:

```kotlin
SayHello("Actor $i", ctx.self)
```

Sending a message to an actor through its reference is relatively easy. We can use the `fun tell(msg: T)` function or the function `fun `!`(msg: T)`, which is an alias for `tell`:

```kotlin
helloWorldActorRef.tell(SayHello("Actor $i", ctx.self))
// ..or..
helloWorldActorRef `!` SayHello("Actor $i", ctx.self)
```

A common best practice is defining a hierarchy of types representing the messages an actor can process. In this way, we can use the type system to ensure that we are sending the right message to the right actor:

```kotlin
sealed interface Command
data class Increment(val by: Int) : Command
object Reset : Command
data class GetValue(val replyTo: KActorRef<Int>) : Command
```

### The `ask` Pattern

The tell pattern we used so far models a communication where the sender does not expect any response from the receiver. We can say it's _fire and forget_. However, sometimes, we must send an actor a message and wait for a response. In this case, we can use the `ask` pattern. The `ask` pattern is a way to send a message to an actor and wait for a response:

```kotlin
suspend fun askPattern() = coroutineScope {
    val tellerActor = kactorSystem(TellerActor.behavior)

    val deferred: Deferred<Answer> = ask(tellerActor) { ref ->
        TellerActor.Question(ref)
    }

    println("The answer is: ${deferred.await().msg}")
}
```

Since the library defines the `ask` function as an extension method of the `CoroutinScope` type, we have an instance of a scope to run the function.

The first parameter of the `ask` function refers to the actor that will receive the message. The second parameter is a function that takes as input a reference to the actor that will send the request (the library implicitly creates this actor) and returns the message to send.

The output of the `ask` function is a `Deferred<T>`, where `T` is the type of the response.

### Scheduling Messages to Self

Sometimes, we must schedule a message to be sent to the actor at given time intervals. To do so, we need a timer scheduler. The library provides a simple timer scheduler that can be used to schedule messages to self. To use the timers scheduler, we can use the `withTimers` builder:

```kotlin
fun behavior(): KBehavior<Tick> = setup { _ ->
    withTimers { timers ->
        timers.startSingleTimer(TimerKey, Tick, 1.seconds)
        processTick(0, timers)
    }
}
```

The `withTimers` builder takes as input a function that takes as input a reference to the timer's scheduler and returns a behavior. So, we can define, start, and cancel how many we want through the timers' reference.

To start a new timer, we must use the `startSingleTimer` function:

```kotlin
suspend fun <K : Any> startSingleTimer(timerKey: K, msg: T, delayTime: Duration)
```

The first parameter is the key of the timer, and it's used to identify the timer. The second parameter is the message sent to the actor when the timer expires. The third parameter is the delay time. The timer will be started when the `startSingleTimer` function is called, and the message will be sent to the actor at fixed intervals every time the delay expires.

When we don't need the timer anymore, we can cancel it using the `cancel` function:

```kotlin
fun <K : Any> cancel(timerKey: K)
```

For example, the example below shows how to handle a timer cancellation:

```kotlin
private fun processTick(counter: Int, timers: TimerScheduler<Tick>): KBehavior<Tick> =
    receive { ctx, _ ->
        ctx.log.info("Another second passed")
        if (counter == 10) {
            timers.cancel(TimerKey)
            stopped()
        } else {
            processTick(counter + 1, timers)
        }
    }
```

### The Router Pattern

Sometimes, we need to scale the number of actors of the same type horizontally. In detail, what we need is a *router*. A router is a particular actor that handles incoming messages using a pool of coroutines. The coroutines in the pool read the messages using a FIFO algorithm.

We can define a router actor quite easily using the `router` function on the `KContext` object:

```kotlin
object MainActor {

    object Start

    fun behavior(): KBehavior<Start> =
        setup { ctx ->
            val routeRef = ctx.router("greeter", 1000, Greeter.behavior)
            repeat(1000) {
                routeRef `!` Greeter.Greet(it)
            }
            stopped()
        }
}
```

The first parameter of the `router` function is the router's name, the second is the size of the pool, and the third is the behavior handling incoming messages. The `Greeter` behavior is defined as a standard actor behavior in the above example.

Be aware that all the coroutines in the pool are created when the router is created.

## Blocking Behaviors

The `kactor` library is heavenly based on Kotlin coroutines. In such an environment, blocking a thread is not a good practice since the effect is that the thread is not available for other coroutines. However, sometimes we need to stop a thread. For example, we can have a behavior that reads from a file, and we need to wait for the file to be read. In this case, we can use the `blocking` behavior builder:

```kotlin
fun behavior(): KBehavior<Start> = receive { ctx, _ ->
    val fileReader = ctx.spawn("fileReader", blocking(FileReader.behavior))
    fileReader `!` FileReader.ReadFile("/file.txt")
    same()
}
```

If we surround a behavior with the `blocking` builder, we are configuring the coroutine executing it to use the `Dispatchers.IO` dispatcher. This dispatcher is backed by a thread pool optimized for blocking operations. In this way, we can stop a thread without blocking other coroutines.

## Logging

The library provides a logging facility that can be used to log messages from actors. The logging facility is based on the Slf4j library, backed by the logback library. To use log, we need to import at least the following dependency in the `pom.xml` file:

```xml
<dependency>
  <groupId>ch.qos.logback</groupId>
  <artifactId>logback-classic</artifactId>
  <version>${logback.version}</version>
</dependency>
```

Every actor can access its logger using the `log` property of the actor context:

```kotlin
object HelloWorldActor {
    data class SayHello(val name: String, val replyTo: KActorRef<ReplyReceived>)

    val behavior: KBehavior<SayHello> = receive { ctx, msg ->
            ctx.log.info("Hello ${msg.name}!")
            msg.replyTo `!` ReplyReceived
            same()
    }
}
```

The `log` property is an instance of the `org.slf4j.Logger` interface, and it logs information using the following pattern:

```
%d [%thread] [{%mdc}] %-5level %logger{36} - %msg%n
```

Every actor put its name with the `kactor` key inside the `MDC` map. An example of a log message is the following:

```
2022-12-04 15:20:35,850 [DefaultDispatcher-worker-2] [{kactor=kactor_0}] INFO  kactor_0 - Hello Actor 0!
```

## Acknowledgements

The `kactor` library would not have been possible without the great book [Kotlin Coroutines Deep Dive](https://kt.academy/book/coroutines) by Marcin Moskała. The book is a great resource to learn about Kotlin coroutines and a must-read for every Kotlin developer.

## Disclosure

I know that in the `kotlinx-coroutines-core` library, a coroutine builder called `actor<T>` uses `Channel`s to build a representation of an actor. However, I need to learn coroutines. So, why don't we reimplement an actor model from scratch? 😜

Anyway, the library is intended to be used outside of production. It is just a proof of concept.