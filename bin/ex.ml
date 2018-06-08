open Seashell
open Lexer
open Lexing

open Type

let transpile = ref false
let no_typecheck = ref false 

let usage = "usage: " ^ Sys.argv.(0) ^ "[-tr]"

let specs = [
  ("-tr", Arg.Set transpile, ": transpile program (rather than interpret)");
  ("-nt", Arg.Set no_typecheck, ": do not typecheck program");
]

let _ =

  Arg.parse
    specs
    (fun s -> raise (Arg.Bad ("Invalid argument " ^ s)))
    usage;

  let lexbuf = Lexing.from_channel stdin in
  let commands = Parser.prog Lexer.token lexbuf in
  try
    if not (!no_typecheck) then
      ignore (check_cmd commands empty_context)
    else ();


    if !transpile then
      let final_env = Eval.eval_command (commands, Eval.empty_env) in
      print_endline (Eval.string_of_env final_env)
    else
      print_endline (Emit.generate_c commands)
    
  with
    TypeError s -> print_endline s
