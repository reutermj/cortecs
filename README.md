# cortecs

Cortecs is an in development, declarative/functional programming language designed to enable the ergonomic development of applications, particularly games and game engines, using the Entity Component System (ECS) architectural style. Cortecs is a language server first programming language; all design and implementation decisions are made to support an efficient and effective language server using the Language Server Protocol (LSP) and Visual Studio Code. 

Current status of the Cortecs langauge server
* Incremental LL(1) parsing using recursive decent with operator precedent parsing (Pratt parsing)
* Full program, incremental type inference based on Edlira Kuci's thesis "Co-Contextual Type Systems: Contextless Deductive Reasoning for Correct Incremental Type Checking"
* Entity types (prototyped on non-main branch) based on Daan Leijen's paper "Extensible records with scoped labels"
