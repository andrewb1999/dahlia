---
id: cheatsheet
title: Cheatsheet
---

The next few sections will cover these in details. Feel free to skip this if
nothing makes sense yet!

## Constants

```dahlia
1; // numbers
true; false; // booleans
1.2; // floating point
```

## Let bindings

```dahlia
let x = 1;
// Can optionally provide type
let x: bit<32> = 1;
```

## Declarations

```dahlia
decl a: bit<32>; // number
```

## Arrays

### Global arrays

Arrays defined using `decl` are used as the memory interface for this module.
**Note**: The Vivado backend does not allow for banking interfaces since the
generated hardware is incorrect.

```dahlia
decl arr: bit<32>[10]; // 10 elements each of which is bit<32>
decl barr: bit<32>[10 bank 2]; // An array with two banks
decl marr: bit<32>[10 bank 2][8 bank 4]; // multi-dimensional arrays are supported
```

### Local arrays

Local arrays are either mapped to BRAM or a register file depending on their
size and usage. Read uninitialized memories results in undefined behavior.

```dahlia
let tmp: bit<32>[10];
let tmp_banked: bit<32>[10 bank 5];
```

### Literals

Local array definitions can optionally be initialized. Local memories with
initializers can only have a single dimension.

```dahlia
let tmp: bit<32>[3] = {1, 2, 3};
```

## Records

### Definitions

```dahlia
// Cannot contain arrays
record point {
  x: bit<32>;
  y: bit<32>
}
// Can contain other records
record rect {
  lb: point;
  rt: point
}
```

### Literals

Record literals can only defined in a let binder and need an explicit type.

```dahlia
let p: point = { x = 1; y = 2 }
```

> Note the missing semicolon after `y = 2`

## Binary operations

```dahlia
1 + 2;
a[i] << 1;
```

## Assignment

```dahlia
x := 1;
a[i] := 10;
```

## Conditionals (if)

```dahlia
// Can omit the else
if (x) {
  a[i] := 1
}

if (y) {
  true;
} else {
  false;
}
```

## While loops

```dahlia
while (i > 0) {
  a[i] := 1;
  i := i - 1;
}
```

## For loops

```dahlia
for (let i = 0..10) {
  a[i] := 1;
}
```

```dahlia
// With unrolling factor
for(let i = 0..10) unroll 2 {
  a[i] := 1;
}
```

## Combine blocks and reductions

```dahlia
let sum = 0;
for (let i = 0..10) unroll 2 {
  let x = a[i];
} combine {
  sum += x; // += is a reductions
}
```

## Sequential composition

```dahlia
a[0] := 1;
---
a[1] := 10;
```

## Functions

Functions cannot return values. They can only modify buffers.

### Definitions

```dahlia

def foo(a: bit<32>[10], b: bit<32>[10]) = {
  a[0] := b[0];
}
```

### Application

```dahlia
foo(arr, barr)
```

## Import statements

Import statements can be used to add `#include`s to files and import external
definitions. The imported functions do not have bodies;

```dahlia
import "printer.h" {
  def print_vector(a: bit<32>[10]);
  def run_prog(a: bit<32>[10]);
}
```


## Views

Views are used to represent different memory access patterns.
Once created, they are accessed transparently as arrays and can be used to
define other views.

### Shrink Views
A shrink view reduces the banking of a memory by a integer factor.

```dahlia
let A: float[16 bank 8];
view sh = A[_: bank 4];
for (let i = 0..8) unroll 2 {
  sh[i]; // OK: sh has 2 banks (8 / 4 = 2)
}
```

The leading `_` tells the compiler that this view is only reducing the banking
factor without affecting the indexing logic.

### Suffix Views
These views let you take a suffix of a memory as long as the banking factor of a memory
is a factor of the suffix. To enforce this restriction, the syntax of suffix/prefix is
`M[k * e: bank b]` where `k` must be an integer factor of `b`.

```dahlia
let A: float[6 bank 4];
for (let i = 0..4) {
  view s = A[2 * i: bank 4];
}
```

The `bank 4` refers to a possible shrink view. This allows for terse
expressions of combined suffix and shrink views.

### Shift Views
These are generalized suffix/prefix views that lift the restriction that the banking factor
is a factor of the shifting factor. The syntax for this view is `shfit M[by e]` where `e` can be
any expression.

```dahlia
let A: float[6 bank 4];
for (let i = 0..4) {
  view s = A[i * i: bank 4];
}
```

### Split Views
Split views transform one dimension of a memory into multiple dimensions.
The split factor must divide the size of the dimension that is being split.

```dahlia
let A: float[8];
split split_A = A[by 2]; // split_A has type float[4 bank 2][2 bank 2]
```

## Program Structure

A Dahlia program has the following structure:
```
<includes>
<func defs or record defs>
<decls>
<commands>
```

Any one of these may be omitted but the order between them must be maintained.
