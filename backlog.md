#### Add builtin '+'

This will operate over integers and strings.  The schema for this will need to be

> forall T : Int | String . T -> T -> T


As a result of this the type system will need to be expanded to include an or operator over types.


#### Add builtin '==', '!=', '<', '<=', '>', '>='

This will operate over all types.  The schema for this will be

> forall T : Any . T -> T -> Bool


#### Add builtin '&&', '||'

This will only operate over Bool


#### Add a Unit type

This will include this types only value `()`