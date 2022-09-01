- Unused methods
  - Should be operational, if you want to get a little more strict check to see if all inherited methods are unused and remove them + the parent method
  - hierarchy tree is scuffed maybe
- Multipliers
  - identification is mostly working
  - Constant folding work around required
- Try catch blocks
- Opaque predicates
- Dummy and unused parameters
- ~~Block sorting~~
- for method calls that use the same parameter every time (constant parameter) they can be inlined
  or changed to a constant field reference?
- Source compatible naming including the ability to rename from a file consisting of mappings
- ~~Access modifiers~~
- ~~redundant gotos~~
- dead/unreachable code
- Exploits for moving members to their original classes, maybe ordering too.
  See https://github.com/alexanderhenne/osrs-exploits/blob/master/src/main/java/com/uniquepassive/osrsexploits/GamepackMethodOriginalClassFinder.java
- Strahler number (obber reuses local vars over and over)
- copy propagation
- Invert if statements
- Remove unused local vars
- convert ints to character literals where appropriate
- Remove empty classes? After moving methods to their original classes and moving fields there
  should be some
- source level: cleaner scopes and branching (favour early return over else)
- ~~Statement ordering (via AST). If statements, artihmetic operations, operation precedence in
  general.~~
- source level: transform loops i.e for each/for i > while etc
- hexadecimal in specific numbers?
- increment preference
- redundant casts
- ternarys
- transform classes into enums if possible
- transform certain casts into generics like `List` -> `List<Node>`
- generify rs classes like NodeDeque
- anything else that makes source code cleaner?