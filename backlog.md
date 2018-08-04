#### Add builtin '==', '!=', '<', '<=', '>', '>='

This will operate over all types.  The schema for this will be

> forall T : Any . T -> T -> Bool


#### Add builtin '&&', '||'

This will only operate over Bool


#### Add a Unit type

This will include this types only value `()`