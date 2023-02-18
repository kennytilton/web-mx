# Running and Developing Standalone Examples

It may be more convenient to develop right in the Web/MX codebase.

## Running examples
To run one of examples seen in the `examples` sub-directory, say `ticktock.cljs`, enter this command at the root `web-mx` directory:

```bash
clojure -M -m figwheel.main --build ticktock --repl
```

Or define this dispatcher in your shell startup:

```
figo () {
    echo "figwheel building and running $1"
    clojure -M -m figwheel.main --build $1 --repl
}
```
...and then `figo ticktock`. Use ^C ^D to kill the test.

## Creating your own example
Just duplicate these three files from one of the examples:
* src/examples/ticktock.cljs
* ticktock.cljs.edn
* resources/public/ticktock.html
* if needed, resources/public/css/ticktock.css

The tricky part is finding and replacing the many internal "ticktock" strings with the name of your own example. I always miss  a couple.
