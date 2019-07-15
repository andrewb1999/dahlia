package fuselang.backend

import Cpp._

import fuselang.common._
import Syntax._
import Configuration._
import CompilerError._

private class IntelBackend extends CppLike {

  def unroll(n: Int): Doc = n match {
           case 1 => emptyDoc
           case n => value(s"#pragma unroll $n\n")
     }


  def bank(id: Id, banks: List[Int]): String = banks.zipWithIndex.foldLeft(""){
    case (acc, (bank, dim)) =>
      if (bank != 1) {
        //s"${acc}"__attribute__((xcl_array_partition(block, <factor>, 1)))
        s"${acc}\n#pragma HLS ARRAY_PARTITION variable=$id factor=$bank dim=${dim + 1}"
      } else {
        acc
      }
  }

  def bankPragmas(decls: List[Decl]): List[Doc] = decls
    .collect({ case Decl(id, typ: TArray) => bank(id, typ.dims.map(_._2)) })
    .withFilter(s => s != "")
    .map(s => value(s))

  def emitFor(cmd: CFor): Doc =
    unroll(cmd.range.u) <+>
    "for" <> emitRange(cmd.range) <+> scope {
      cmd.par <>
      (if (cmd.combine != CEmpty) line <> text("// combiner:") <@> cmd.combine
       else emptyDoc)
    }

  def emitFuncHeader(func: FuncDef): Doc = {
    vsep(bankPragmas(func.args))
  }

  def emitArrayDecl(ta: TArray, id: Id) =
    emitType(ta.typ) <+> id <> generateDims(ta.dims)

  def generateDims(dims: List[(Int, Int)]): Doc =
    ssep(dims.map(d => brackets(value(d._1))), emptyDoc)
  
  def emitType(typ: Type) = typ match {
    case _:TVoid => "void"
    case _:TBool | _:TIndex | _:TStaticInt | _:TSizedInt => "int"
    case _:TFloat => "float"
    case _:TDouble => "double"
    case TArray(typ, dims) => dims.foldLeft(emitType(typ))({ case (acc, _) => "buffer" <> angles(acc) })
    case TRecType(n, _) => n
    case _:TFun => throw Impossible("Cannot emit function types")
    case TAlias(n) => n
  }


  def emitProg(p: Prog, c: Config): String = {
    val layout =
      vsep(p.includes.map(emitInclude)) <@>
      vsep(p.defs.map(emitDef)) <@>
      "__kernel " <> emitFunc(FuncDef(Id(c.kernelName), p.decls, Some(p.cmd)))

    super.pretty(layout).layout
  }
}

private class IntelBackendHeader extends IntelBackend {
  override def emitCmd(c: Command): Doc = emptyDoc

  override def emitFunc = { case FuncDef(id, args, _) =>
    val as = hsep(args.map(d => emitDecl(d.id, d.typ)), comma)
    "void" <+> id <> parens(as) <> semi
  }

  override def emitProg(p: Prog, c: Config) = {
    val declarations =
      vsep(p.includes.map(emitInclude) ++ p.defs.map(emitDef)) <@>
      emitFunc(FuncDef(Id(c.kernelName), p.decls, None))

    super.pretty(declarations).layout
  }
}

case object IntelBackend extends Backend {
  def emitProg(p: Prog, c: Config) = c.header match {
    case true => (new IntelBackendHeader()).emitProg(p, c)
    case false => (new IntelBackend()).emitProg(p, c)
  }
  val canGenerateHeader = true
}