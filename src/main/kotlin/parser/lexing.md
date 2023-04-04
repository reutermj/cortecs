Keywords: function | if | let | return

Variable Names: [a-z_][a-zA-Z_0-9]*

Type Names: [A-Z][a-zA-Z_0-9]*

Numbers: \d+[fFdDbBsSlL]? | ?\d+\.\d*[fFdD]? | \d*.\d+[fFdD]?
* Unary '+' and '-' tokens in front of a number is lexed as a separate token
  * +123 -> '+' '123'
  * +.123 -> '+' '.123'
  * -0.123 -> '-' '0.123'
  * -123. -> '-' '123.'
* Precision specifier are lexed as part of the token and are defined as:
  * 'b' and 'B' for I8: 123b -> '123b', 123B -> '123B'
  * 's' and 'S' for I16: 123s -> '123s', 123S -> '123S'
  * unspecified for I32: 123 -> '123'
  * 's' and 'S' for I64: 123l -> '123l', 123L -> '123L'
  * unspecified and 'f' and 'F' for F32: 123f -> '123f', 123.F -> '123.F', 123.0 -> '123.0'
  * 'd' and 'D' for F64: 123d -> '123d', 123.D -> '123.D', 123.0D -> '123.0D'
* Other characters following a number are lexed as a separate token
  * 123t -> '123' 't'
  * 123bt -> '123b' 't'
  * 0.123t -> '0.123' 't'
  * 0.123ft -> '0.123f' 't'

Bad Stuff: All other strings of characters