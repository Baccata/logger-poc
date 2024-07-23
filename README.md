# Typelevel-friendly Logger POC

## Disclaimer

This is not a library. I'm not planning on publishing/maintaining the code under an Maven organisation I control. This is merely a POC to facilitate some discussions around logging interfaces.

Initially discussed here: https://github.com/typelevel/log4cats/discussions/815

## What the F[_] is this ?

This repository in a reification of some ideas related to how the defacto-standard logging interface of the Typelevel ecosystem could be made much better. The current standard in the TL ecosystem is log4cats, and although it has the merit of existing, the core interfaces it expose suffer from several problems that make it not ideal from both an application-writer point of view, but also from a library-writer point of view.

Some of the main problems are :

1. The log4cats `StructuredLogger` interface, which is realistically what everybody should be using when using `log4cats`, has no less than 20 non-orthogonal methods : the only difference between these methods is the parameter-sets that capture the data that gets dispatched by the logger. The sheer number of methods make this logger unfriendly to middleware, which means that users who want to change the behaviour of the logger to enrich the dispatched data with additional informations experience a painful process of having to modify 20 methods, which is tedious and error-prone.

2. The same `StructuredLogger` interface exposes a "context" value of type `Map[String, String]`. This facilitates a loss of information in what gets dispatched to the logging backend, which impacts the ability of engineers to run rich queries to process the logs and distill important information from them. Logging backends (such as AWS Cloudwatch) tend to provide query languages that have operations on integers, arrays, sometime nested objects. Forcing contextual value to "String" really constraints the kinds of queries operators are able to make.

3. Besides being reasonably familiar in semantics to most developers, the methods of `StructuredLogger` do not allow for capturing source-code information (such as file/line number / classnames, etc). This encourages users to effectively instantiate a Slf4j-backed logger PER class where the logger is used in order to get the name of the class in their logs, which basically goes against the best practice of declaring side-effecting dependencies as class parameters to increase testability. It also makes it harder to the developers to wire logging middleware that may be relevant to their applications, as they have to do so in many locations instead of doing it in their main method and injecting a middleware'd logger everywhere it's needed.

These 3 points are the fundamental problems of the log4cats interface, and are the rationale for this POC.

## Why not Scribe ?

[Scribe](https://github.com/outr/scribe) is absolutely amazing. However, even though Scribe allows for reasonably convenient usage in TL applications, Scribe makes it too easy to log imperatively (which is really nice for a lot of things, but not so much in the context of the TL ecosystem). It's an amazing library and a lot of inspiration can be drawn from it, but the pure-FP interface it provides is an afterthought. I'd like to tackle the problem using an interface-centric approach as opposed to implementation-centric.

## Ecosystem-related goals

The likely unrealistic goal of this work would be for log4cats to be amended so that a more fundamental interface would see the light of day. I will call this interface `LoggerKernel` for now, although I suck at naming things and if people are weighing in, it'd be nice.

* The existing log4cats interfaces would continue to exist but would offer to be instantiated on top of a `LoggerKernel`
* A new front-end interface would be provided as an alternative to the existing log4cats interfaces.
* An cross-platform, fs2-io based backend would be provided OOTB, circumventing the need for slf4j
* Two renderers should be provided OOTB : a human-readable, colourful one, and a JSON one

## Design goals

* LoggerKernel should be a SAM type, making it middleware-friendly
* LoggerKernel should be able to expose a front-end expressed in the current log4cats interfaces.
* LoggerKernel should come in zero-dependency, minimalistic module
* LoggerKernel should allow to express the capture of more precise/richer types than `String`
* LoggerKernel should preferably be reasonably agnostic to how the data is stored in memory, allowing for implementations to pick and mix the bits of logs that are important to them.
* LoggerKernel should allow for circumventing un-necessary allocation of string messages by exposing by-name parameters where relevant.

## Project structure

### kernel module

This contains the fundamental proposed interface, which libraries that need loggers should be using. It's zero-dependency (doesn't even depend on cats-core), and only expresses algebra that could potentially be used by library authors who desire to log things (http4s comes to mind)

### interop-log4cats module

This contains interop layer to convert from the current log4cats interface to the LoggerKernel and vice versa. Some information is lost when going
from StructuredLogger to LoggerKernel, hinting that LoggerKernel is more powerful in what data it allows to capture.

### frontend module

This contains a more elaborate interface than the LoggerKernel, that allows to capture source-code information by means of implicit macros provided
by Haoyi's sourcecode library.

### backend module

This contains an fs2-based LoggerKernel implementation that uses a channel as a "multiple producer, single consumer" queue, which connects to a
sink that renders the logs as colourful lines and dumps them to stdout.

### test

Contains a main application to play with.
