#### Add type signatures on let declarations

Allow let declarations to be qualified


#### Add the keyword "typealias"

This will allow schemas to be referenced
 

#### Add builtin '*'

This will only operate over integers


#### Add builtin '/'

This will need to be a traditional division and therefore is able to throw an exception if an attempt is made to divide
by 0.  At a later point this will need to replaced with something that'll return a result of something similar.


#### Add builtin '+'

This will operate over integers and strings.  The schema for this will need to be

> forall T : Int | String . T -> T -> T


As a result this the type system will need to be expanded to include an or operator over types.


#### Add builtin '==', '!=', '<', '<=', '>', '>='

This will operate over all types.  The schema for this will be

> forall T : Any . T -> T -> Bool


#### Add builtin '&&', '||'

This will only operate over Bool
