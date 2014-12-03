# clj-restmachine

A simple clone of Basho's Webmachine.

## Installation

Add the following dependency to your `project.clj` file:

    [clj-restmachine "0.1.0-SNAPSHOT"]

## Documentation

A resource is can be created by implementing the protocol Resource in clj-restmachine.type.  To make implementing rest repetitive there's also a default-resource,  that tries to put sensible defaults for Resources.

To create a resource you need a type for the Resource:

    (deftype MyResource [])

That type needs to implement the `clj-restmachine.type.Resource` protocol.  You can add all functions,  or you can extend `clj-restmachine.type.default-resource`:

```
    (extend MyResource
      clj-restmachine.type/Resource
	  (merge clj-restmachine.type/default-resource
	    {:create-response (fn [_ req] {:status 200
                                       :body "hello world"})}))
```

Once your resources have been implemented,  you need to provide a routing to the correct resources.  The `clj-restmachine.routing/route` function implements routing to the correct Resource.

The routing table is a seq of vectors with as first argument the matcher,  and as second argument the resource.  A matcher is a seq of elements that can be either a string or a regular expression pattern.  The resource that is associated with the first matcher will be called with the resource.

```
    (route (list [["hello"] (MyResource.)]) request)
```

This will execute the flow in `clj-restmachine.flow` and return a response with the assigned status code and body.
		 

## License

Copyright © 2014 Brecht Hoflack

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
