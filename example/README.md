# Lively example project

This is a minimal example project showing [lively](http://github.com/immoh/lively) in action.


## Instructions


1. **Start ClojureScript compiler**

   In terminal:

   ```
   lein cljsbuild auto
   ```

2. **Start the server**

   In another terminal:

   ```
   lein ring server
   ```

   This should automatically open a web browser, if not, navigate to
   [http://localhost:3000/index-dev.html](http://localhost:3000/index-dev.html).


3. **Start hacking**

   Open your favorite editor and start editing ClojureScript files in [src/cljs](http://github.com/immoh/lively/blob/0.1.0/example/src/cljs). Your changes
   are automatically reflected in the browser with no need to reload the page!

   For example, try editing the greeting text in [greet](http://github.com/immoh/lively/blob/0.1.0/example/src/cljs/hello/core.cljs#L5) function.
   After saving the file, click on the greet button.


Copyright © 2014 Immo Heikkinen
