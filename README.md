# yapping
Yet Another Properties Parser in Gjava

## A data-based language
### every node resolves to a type
```
# properties: a list of sections
yapPrimitives:  # begin properties
   123          # int
   123.456      # decimal
   1 3/4        # quotient
   myName       # name
yapConstructs:  # (another section)
   name1 = 1    # map
   2 = name2
   (item1, 0)   # list
   {myName1 = name1, myName2 = name2} # map
   # properties - we may disambiguate properties within properties
   # by surrounding it with ":" sections... ";"
   :sectionName:  
       sectionInfo
    anotherSection: 
   ; 
combineConstructs:
   name1 = name2 = 1  # equivalent to {name1 = {name2 = 1}}
   (l, r) = (1, 2)
   {a = b, c = d} = (section: 1 2 section: 3 4)
       # pair( pair(a, b), pair(c,d), list( properties(section(1 2), section(3 4)) ) )
```
### Choose your grammar complexity: Chomsky would be proud

We may define plugins that will match and transform nodes in the data structure.
The example above defines a `Type-3` language as it doesn't require any plugins.

However, we may define `Type-[1-3]` plugins that will match and transform existing nodes
in the data structure. This is specified by the library user to determine the initial set of
plugins that we may use. A script may not start using and importing plugins unless if the java
implementation specifies that it can. Here's an example:

```
import:
    @base.yah # setting variables and scopes. @ symbol specifies we're using a file over a java class
    boxMesh = @meshes/box.yap
    com.user.TriangleMeshPlugin
vertices:
    # creates a list of vertices that start with "neg" and have negated values from boxMesh
    :import: @functional.yah @math.yah @strings.yah # import fun:, neg:, and concat:
    definitions:
       negateAll = fun: {vName = vItem} rest
                        is: {concat: "neg" vName = neg: vItem} :negateAll: x;
                   fun: {vName = vItem}
                        is: {concat: "neg" vName = neg: vItem};
    vertices: (negateAll: boxMesh.vertices);.vertices

```
This is a `Type-0` script, where there may be possible turing completeness. Note that we can still parse 
this file as `Type-3`, which will just result in a structure of nodes. 

User plugins are specified with their relevant complexity, `Type-0`, `Type-1`, etc.
From the user's setup we may specify a series of initial plugins and a target complexity. 

If there is any attempt to import any plugin that's at a higher complexity than the parser's target, 
then an exception will be thrown and parsing will stop. E.g. a `Type-3` script attempting to use a
`Type-0` plugin is not allowed, as it may make our script turing complete.
   
