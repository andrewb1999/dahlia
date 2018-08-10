# Seashell

[![CircleCI](https://circleci.com/gh/cucapra/seashell.svg?style=svg)](https://circleci.com/gh/cucapra/seashell)

Seashell provides a type system that makes programming FPGAs easier. To learn about how Seashell works and where it's headed, check out these links:
  - [Notes about Seashell](https://capra.cs.cornell.edu/seashell/docs/index.html)
  - [Internal Seashell Wiki](https://github.com/cucapra/seashell/wiki)
  - [Seashell Roadmap](https://github.com/cucapra/seashell/wiki/Project-Roadmap)
  - [Vim Syntax Highlighting](https://github.com/tedbauer/seashell.vim)

## Setup/Use

Requires [Dune](https://github.com/ocaml/dune) and [Menhir](http://gallium.inria.fr/~fpottier/menhir/) (in addition to OCaml and OPAM):

	$ opam install dune
	$ opam install menhir

Then for setup and use, build and install `seac` compiler:

    $ dune build
    $ dune install

Also can use the makefile with `make build`

Now you can generate HLS programs from your Seashell programs.
For example:

    $ seac < examples/float.ss > float.c
