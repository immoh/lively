# Lively [![Build Status](https://travis-ci.org/immoh/lively.svg?branch=travis)](https://travis-ci.org/immoh/lively)

ClojureScript live coding with ease

Lively monitors compiled JavaScript files for changes and reloads them when they
change, so that you don't have to. This creates a development environment where you can edit ClojureScript code and see the changes
immediately in your browser without needing to refresh the page.

Lively does not compile ClojureScript. (I recommend using [lein-cljsbuild](https://github.com/emezeske/lein-cljsbuild) for that.)

Lively does not provide web server for serving JavaScript files.


## Installation

Add the following Leiningen dependency:

```clojure
[lively "0.2.1"]
```

## Usage

Prerequisites:

* ClojureScript version 0.0-2202 or newer
* ClojureScript is compiled with `:optimizations` set to `:none`
* JavaScript files are loaded over HTTP

Simply include call to `lively/start` somewhere in your ClojureScript codebase, passing the location of the main JavaScript file
(this is the value of `src` attribute of the `script` tag loading the file in your HTML markup):

```clojure
(ns your.app
  (:require [lively.core :as lively]))

(lively/start "/js/hello.js")
```

Call to `start` is idempotent, it is safe to call it multiple times.

The followig options can be passed as an optional options map:

* `:polling-rate`: Milliseconds to sleep between polls. Defaults to 1000.
* `:on-reload`: Callback function to call after files have been reloaded.

For example:

```clojure
(lively/start "/js/hello.js" {:polling-rate 500
                              :on-reload    (fn [] (.log js/console "Reloaded!"))})
```

## Examples

* Minimalistic example project can be found in [example](https://github.com/immoh/lively/tree/master/example) directory.
* [Lively Snake Demo](https://github.com/immoh/lively-snake-demo) showcases implementing a snake game using Lively


## How does it work?

Lively monitors changes in JavaScript files by making consecutive HEAD requests to the server.
When ClojureScript files are compiled, the main JavaScript file is always is generated again and this change is
noticed by Lively. Lively finds out which namespaces have changed by making HEAD requests for each namespace-specific
JavaScript file and reloads ones that have changed.


## License

Copyright © 2014-2015 Immo Heikkinen

Distributed under the Eclipse Public License, the same as Clojure.
