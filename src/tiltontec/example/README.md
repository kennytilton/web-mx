# Running and Developing Standalone Examples

It may be more convenient to develop right in the Web/MX codebase.

## Running examples
To run one of examples seen in the `examples` sub-directory, say `ticktock.cljs`, enter this command at the root `web-mx` directory:

```bash
lein fig -- --build ticktock --repl
```

Or define this dispatcher in your shell startup:

```
figrun () {
    echo "figwheel building and running $1"
    lein fig -- --build $1 --repl
}
```
...and then `figrun ticktock`.

## Creating your own example
Just duplicate these three files from one of the examples:
* src/examples/ticktock.cljs
* ticktock.cljs.edn
* resources/public/ticktock.html
