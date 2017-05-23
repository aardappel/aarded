public class AardEd { static public void main(String[] args) { new MainWin(); } }

/*
todo's:

- specifity ordering doesn't work well with repeated vars in patterns
- var in local rule pattern from parent causes verify error (->PAT PRE)
- fix 2-level free variable problem in genexp(Tree t) !!!
- compute stack requirements more precisely: fixed 100 for stack
  is what made it run out??? nope doesnt make a difference.
- nullpointer exception on mandel program on very specific sizes (50x50)
- NIL as a placeholder cdr dissapears
- red markings often over-accurate

- freezes on failed IN-op? [FIXED]

rmi:
- if in CompiledAardappel.main(args) args.length>0 then load
  the classfile back in as data for rmi

from editing:
- easy list operations
- current way of handling examples is clumsy
- more control structures: using local rules for all is tiresome.
  ifthenelse, or, and
- arity for builtins
- arity for other stuff
- easy common trees like lists
- fix focus of string input
- display enormous trees by truncating them
- filling in example from rule that does linda-in also copies the in
- allow to change head using alt with typing also?

editing:
- make copy-paste similar to drag in that it has a source so examples
  can be properly pasted? modify clone() to allow copying of examples?
- wrap operation for tree/bag/local rules.
- make a nil tail of cons editable
  - examples for builtins
  - examples for data-only trees from rules?

- concurrency:
  * bag items functions that get free vars + number
  * specially generated function: gets num + array of free vars and calls fun
  * thread references current bag
  * impl of all blocked
    * bag has own implementation of blocked threads, keeps them
      hanging around with tiny monitors each
    IMPORTANT: semantics slightly different: all blocked doesnt necessary mean
    the end of evaluation, as the blocked trees may be subtrees
  * distributed considerations?

codegen
- local rules can be compiled to code twice if within shared exp (see preexp rules).
- make tree/0 behave as atom everywhere
- sharing -> has to perculate to top level? what if it contains local refs? see filter.
  complicated! has to function as a free var, to all callers
- ensure correct stacksize set for methods generated
- registerfv: allow free vars in global fun if its a bagexp?

- hit area of cons brackets is rather small
- reduce language: remove
  * shared areas?
  * vars in vars? (not catered for in codegen atm).
  * head dragging restricted?

- doesnt make example out of dragged head
- dragging from rules in local rules to exp shares and shouldnt
- scrolling bug: scrolling down then resizing doesnt take into accouny the extra space

- absolute path doesnt work?
- doing an addload sometimes freezes the system? -> File-Ex
- cant do variable atoms
- overwrite doesnt "unshare"
- sharing happens between diff patterns in local rules.
- easy way of "emcapsulating" an existing tree (rather than cut-paste)


- (lambda({ f }) = f(1)) { (lambda(x) = { x } x) }
  tries to put 1 into bag that doesnt exist anymore!
  problem: bag is supposed to be in normal form, we can't allow
  stuff to be put into it -> multiple bag refs!
  [SOLVED]
- clicks often deselect straight away: amounts to drag
  clicking on pattern after dragging vars makes it no var: again: dragging
  dragging to same atom should be detected and work as select

- current Finditer searches whole tree, needs to be restricted to screen
  or to 1 rule or something (mind midx etc)
- xor mode?
- upstream drag increases refcount. maybe have a iterator that recomputes refcounts?
  -> properly decrease children refcounts on refc == 0

code gen:
- trees with 1 child
- 1st pass: what about code that isnt reached by iterating children

- bags as envs can be tracked by forked stacks of them. even simpler:
  given that each bag item is evaluated by a new thread, and the parent
  bag waits, the new thread just needs to know the parent bag. any local
  bags it makes it passes on to its sub threads etc.

- move children up & down
- lists & syntax
- tuples


ops: single/multiple copies on refc boundaries:
select: depends what op next
drop / replace by typing: single -> finditer
                        : multiple -> with modifier!
fold / foldroot: has to be multiple?
decouple: single


iters:       coords all

finditer     x
selectiter   x      o



COMPILER:
- filter out tree/0 early
- allow vars in vars (nested pats)
- optimize where var = arg
- double occurrences of vars in patterns

*/

