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
   \regex\      # regex
   "name"       # name (literal)
yapConstructs:  # (another section)
   name1 = 1    # pair
   2 = name2    # pair
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
                        is: {concat: "neg" vName = neg: vItem}
    vertices: (negateAll: boxMesh.vertices);.vertices
```
This is a `Type-0` script, where there may be possible turing completeness. Note that we can still parse 
this file as `Type-3`, which will just result in a structure of nodes. 

User plugins are specified with their relevant complexity, `Type-0`, `Type-1`, etc.
From the user's setup we may specify a series of initial plugins and a target complexity. 

If there is any attempt to import any plugin that's at a higher complexity than the parser's target, 
then an exception will be thrown and parsing will stop. E.g. a `Type-3` script attempting to use a
`Type-0` plugin is not allowed, as it may make our script turing complete.

#### Note on yapping grammar complexity
At its base, parsing _yapping_ is a context-sensitive `Type-1` grammar. When we refer to a plugin's complexity however, 
this refers to the transformations that take place on the node after it is parsed. 

For example, a user may make a `Type-3` plugin that just matches individual nodes and transforms them into a data 
structure usable by the user's java program. Or they could write a `Type-2` plugin that matches individual nodes
in a context-free manner. Or, they could write a `Type-1` plugin that uses context and variables. Finally, they 
could write a `Type-0` processor that would recognize nodes as control flow. 

### Plugin overview: `import`
Since import itself is a plugin, we have to specify it manually on the java-side. After this is done, `import` will 
transform the AST among variables in scope
```
import: 
   @my/file/path/myimport.yap # we use @ symbol to reference a yap script
   @my/file/path/header.yah   # we use yah as header files, which only contain other imports or data
   com.user.package.Plugin    # used to reference a java resource as a plugin
section: 
   # all of the above items from import can be referenced from here
   # however this does not resolve to an actual 
    
   :Plugin.environment1: doSomething ; # references an environment from the plugin 
   :environment1: doSomething ;        # equivalent to above
   :com.user.package.AnotherPlugin: nothing ; # this was not imported, and thus does not use the environment
   :import: com.user.package.AnotherPlugin in: :com.user.package.AnotherPlugin: something; ;
        # now the reference works
```

#### Import scopes
Using an import will bring all defined _environments_ into the immediate scope. Environments are referenced using the 
`environment: args` syntax.

The scope ends at the end of the properties block, end of the list, or end of a map where the import block is defined.

```
sectionExample: 
   :import: com.user.Plugin
            in: :environment: this works;
   environment: this works 
   ;
   :Plugin: this does nothing ;
   :import: com.user.Plugin.environment
             in: :environment: this works;
                 :Plugin.enviroment: this does nothing;
    environment:
   ; 
listExample: 
   (Plugin: this does nothing, import: com.user.Plugin, Plugin: this works)
   (Plugin: this does nothing)
   :Plugin: this also does nothing;
pairExample: 
   :import: com.user.Plugin; = :Plugin: this works;
   x = Plugin: this does nothing;
   :Plugin: this does nothing;
mapExample: {
   x = Plugin: this does nothing,  
   import: com.user.Plugin = (),
   x = Plugin: this does something, 
   () = import: com.user.Plugin2,
   x = Plugin2: this also does something
}
```
#### Plugin environments
When using the base `import` plugin, environments can only be used and referenced using the `section:` syntax, where the
section matches a name to an environment we imported. The environment for `import` is matched to an arbitrary section
titled `import:`. Note that a plugin contains a set of environments, and if a set of environments is defined on the
given name, all will be applied in order.

An `import`, at its base, exposes variables defined at a context level. These can then be used by the yap script using
the `section:` syntax to enter an environment. At its base, we don't provide any more functionality by default. However, 
this is the only plugin that needs to be specified ahead of time on the java side in order for us to extend the language.
Additional functionality during importing can be provided using the `args`, `export`, and `vars` plugin.

### Plugin overview: `export`
Allows us to export data from a `yap` file so it is defined as a variable when referenced in another file.

Here's an example
```
# mydefinition.yah
import:
   @yapping/export.yah
   com.user.Plugin
   com.user.TriangleDataModel
export: 
   # plugin just references a single environment defined for the plugin
   plugin = Plugin.myEnvironment
   # triangles is the collection of all environments under TriangleDataModel
   triangles = TriangleDataModel
```
Then it can be referenced later
```
# mydatamodel.yah
import: 
    @mydefinition.yah
plugin: 
    heres something myEnvironment matches
triangles.vertices: 
    (0, 0, 0)
    (0, 1, 0)
    (0, 1, 1)
    (1, 0, 0)
triangles.mesh: 
    (0, 1, 2)
    (3, 1, 0)
```

### Plugin overview: `args`
Args allows us to pass data to another yap script, to a plugin on entering an environment, or to a plugin on import.
The `args` environment is matched to a top-level section node labeled `args:`

Here's an example
```
import: args
args: 
     {
        FLAG1 = VAL1
        FLAG2 = VAL2
        (FLAG1, FLAG3) = VAL3
        () = VAL4
     }
export: 


import:
   @yapping/passargs.yah # modifies environment so we can pass args to imports
   @yapping/base.yah VARS # modifies environment so we can utilize variables
   plugin =  
```


### Note on `:section:`
We permit defining properties in the form `:section1: section2: ;` 
When we encounter the first colon `:`, we enter a new properties. 
We then find `section1:` to define the first section. 
Then we find `section2:` to define the final section. 
Finally, we find `;`, which ends the current properties.

Note that the first `:` or last `;` is not required. For example, 
`x = section: abc;` will create properties with one `section`, then end the properties. 
The following example defines ways we can define a section without ambiguity.
```
outerSection: 
    x = section: abc; 
    x = :section: abc; # equivalent to above
                       # without ';' rest of file would be in this section.
    (section: abc, 
        :section: abc, 
        :section: abc;,
        section: abc;) # these four are equivalent
    :section: abc; # both : and ; are required to disambiguate from outerSection
    
    :section: abc; = x # both : and ; are required for this to be a key, or else it would resolve to below
    :section: abc = x  # NOT the same as above - resolves to :section: (abc = x) ;
    
    {section: abc = x, 
     :section: abc = x,
     section: abc; = x,
     :section: abc; = x} # these four are equivalent, as we look for the '=' sign to split the map into pairs before
                         # parsing its key/value
                         
     {section: abc = def; # we do at least require ; here, or else we'd match against the first '=' sign.
         = section: abc = def}
     
```




   
