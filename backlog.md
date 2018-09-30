# Primary Features

* Add case construct with patterns

* Add support for patterns on let declarations

* Add where



# Enhancements

* Add location to scheme and type - this will enable better error reporting

* Fix issue with the parser when creating a type signature.  It currently parsers
  
  > ```haskell
  > singleton : a -> List a 
  > singleton a =
  >   Cons a Nil
  > ```
  
  erroneously as
  
  > ```haskell
  > singleton : a -> List
  > a singleton a =
  >   Cons a Nil
  > ```
   
  During development this can be fixed by placing parenthesis around the `List a`.