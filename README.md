# yapping
Yet Another Properties Parser in Gjava

## A data-based language
### every node resolves to a type
```
# properties: a list of sections
yapPrimitives:   # begin properties
   123           # int
   123.456       # decimal
   1 3/4         # quotient
   myName        # name
   `regex~flags` # regex
   "name"        # name (literal)
yapConstructs:   # (another section)
   name1 = 1     # pair
   2 = name2     # pair
   (item1, 0)    # list
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
    !com.user.TriangleMeshPlugin
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
   !com.user.package.Plugin   # used to reference a java resource as a plugin
section: 
   # all of the above items from import can be referenced from here
   # however this does not resolve to an actual 
   
   :com.user.package.Plugin.environment1: doSomething; # works - describes a full path to the environment 
   :Plugin.environment1: nothing ;     # need a full path or imported environment 
   :environment1: doSomething ;        # equivalent to first line
   :com.user.package.AnotherPlugin: nothing ; # this was not imported, and thus does not use the environment
   :import: !com.user.package.AnotherPlugin in: :com.user.package.AnotherPlugin.myPlugin: something; ;
        # now the reference works
```

Syntax
```
import: ( @[filepath] | ![javaPath] | { [mapValues] }(. [scope] )* )*
```

Essentially, data can be viewed as a path structure like 
```
com -> user -> plugins -> JavaPlugin1 -> environment1
                                      -> environment2
    -> yapping -> JavaPlugin1 -> environment1
                              -> environment2
```
Using an import on one of these will expose both the absolute path we imported and the variables
after the path we imported. 

For example, 
```
import: !com      # {user = ... , yapping = ... }
import: !com.user # {plugins = { JavaPlugin1 = {environment1 = .., environment2 = ..} } }
import: user      # same as above, as user is defined from the first line
```
The import acts on any map or environment type. For map types it will assign the string keys defined 
by the map into the current context referencing the assigned values. 

Importing an environment will cause the environment to be applied to all nodes after the import completes. 
For example, 
```
import: com.user.JavaPlugin1.environment1
        environment1 will be applied here
aSection: 
        environment1 will also be applied here
```
#### Using multiple environments
We may use multiple environments at the same time, this is done by defining a list
of environments to be used as a section.

```
import @yapping/vars.yah # allows us to set variables
      !com.user.Plugin1
      twoEnvironments = (environment1, environment2)
twoEnvironments: 
    now we are using both envionments
```
this is equivalent to 
```
import !com.user.Plugin1
environment1: 
   :environment2:
    now we are using both environments 
   ;
```
in future iterations we will permit lists in the section head like so
```
import !com.user.Plugin1
(environment1, environment2) : 
    now we are using both environments
```

#### Importing and regexes

We can use imports to change the way in which we import. For example, 
we are allowed to use a regex on a path:
```
import: # this allows the following line to work
        @yapping/regex.yah
        !com.`(\w+\.)+`JavaPlugin`\d`.environment1 # form is `[regex]~[flags]` 
        # {user.plugins.JavaPlugin1.environment1 = .., yapping.JavaPlugin1.environment1 = ..}
```
Note that the result of a regex import is a map. However here, the keys are paths relative to 
the first non-regex path of our base. The keys of the map are defined as if they were imported 
and map to their respective values. 

```
import: @yapping/regex.yah
        !com.`(\w+\.)+`JavaPlugin`\d`
        # {user.plugins.JavaPlugin1 = environment1, yapping.JavaPlugin1 = environment1}
```
We can use these regexes to import multiple environments
```
import
    @yapping/vars.yah           # set and retrieve variables
    @yapping/scope.yah          # get scope of items outside of import
    @yapping/singleton-list.yah # unboxes (a) -> a
    @yapping/regex.yah
    environments = (!com.`(\w+\.)+`JavaPlugin`\d`.environment1).vals
environments: 
    # now using (user.plugins.JavaPlugin1.environment1, yapping.JavaPlugin1.environment1)
```

#### Import scopes
Using an import will bring all defined variables of a path into the immediate scope. 
Environments imported this way may be referenced using the 
`environment: args` syntax. One may still reference an environment via its absolute
path, but we are required to import it beforehand.

The scope ends at the end of the properties block, end of the list, or end of a map where the import block is defined.

```
sectionExample: 
   :import: !com.user.Plugin
            in: :environment: this works;
   environment: this works 
   ;
   :Plugin: this does nothing ;
   :import: !com.user.Plugin
             in: :environment: this works;
                 :Plugin.enviroment: this does nothing;
                 :com.user.Plugin.environment; this works;
    environment: this does nothing
   ; 
listExample: 
   (Plugin: this does nothing, import: com.user.Plugin, environment: this works)
   (environment: this does nothing)
   :environment: this also does nothing;
pairExample: 
   :import: com.user.Plugin; = :environment: this works;
   x = environment: this does nothing;
   :com.user.Plugin.environment: this does nothing; # not imported in this scope
mapExample: {
   x = environment: this does nothing,  
   import: com.user.Plugin = (),
   x = environment: this does something, 
   () = import: com.user.Plugin2,
   x = environment2: this also does something
}
contextExample: 
    :import: !com.user.Plugin.environment
        now everything after this import has the context of environment
    ; 
    no longer in environment
    import: !com.user.Plugin.environment
    now: everthing till end of file is using environment   
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
   !com.user.Plugin
   !com.user.TriangleDataModel
export: 
   # plugin just references a single environment defined for the plugin
   plugin = Plugin.myEnvironment
   # triangles is the collection of all environments under TriangleDataModel
   triangles = TriangleDataModel
   # we can also export data
   scale = 1.0
```
Then it can be referenced later
```
# mydatamodel.yah
import: 
    @mydefinition.yah
    @vars.yah
plugin: 
    heres something myEnvironment matches
    scale also resolves here
triangles.vertices: 
    t0 = (0, 0, 0)
    t1 = (0, scale, 0)
    t2 = (0, scale, scale)
    t3 = (scale, 0, 0)
triangles.mesh: 
    (t0, t1, t2)
    (t3, t1, t0)
```
We can have files export environment functionality to the top level
```
# mydefinition2.yah
import: 
   @user/unusedimport.yap
   x = @yapping/vars.yah
   y = !com.user.SomePlugin
export: 
   x
   y.someEnvironment
   !com.user.MyPlugin.myEnvironment
```
This will export `vars.yah`, `SomePlugin.someEnvironment`, and `MyPlugin.myEnvironment` as top-level
environments that will automatically be applied on import
```
# myscript.yap
import: 
    @mydefinitions2.yah
something: 
    # using three environments from mydefinitions2
```

One can prevent automatically using environments by providing a name
```
import: 
    x = @yapping/vars.yah
aSection: 
   y = 3      # literally y = 3 pair
   (y, y, y)  # literally (y, y, y)
x: 
   y = 3      # sets y to 3
   (y, y, y)  # equal to (3, 3, 3)
```

### Plugin overview: `args`
Args allows us to pass data to another yap script, to a plugin on entering an environment, or to a plugin on import.
The `args` environment is matched to a top-level section node labeled `args:`

Here's an example
```
# myplugin.yah
import: @yapping/getargs.yah
argsmap: 
     {
        FLAG1 = @yapping/regex.yah
        FLAG2 = @yapping/vars.yah
     }
export: 
   argsmap.result
```
This will cause `argsmap.result` to evaluate to a different list depending if `FLAG1` or `FLAG2` is 
present as an argument, or none, or both. 
```
# mypassargs.yap
import:
   @yapping/setargs.yah # modifies environment so we can pass args to imports
   w = :setargs: @myplugin.yah FLAG1;
   x = :setargs: @myplugin.yah FLAG2 FLAG1;
   @myplugin.yah ABC FLAG1  # setargs is not required but necessary if we want to set the import to a variable
   z = @myplugin.yah        # unless if we have no args afterwards
```

Here we add additional imports from the header based on the args passed in.
We can also use these args to define variables, pass around data, or even get data we can reference in java.

### Plugin overview: `vars`
Allows us to define and reference variables. Additional semantics to come...

### Plugin overview: `scope`
Allows us to access scopes of variables and values. Additional semantics to come...

### Plugin overview: `regex`
Regexes have the form 
```
`regex~flags`
```
The regex can be used for pattern matching (in tandem with the `args` plugin) or for multiple scope
resolutions (in tandem with the `import` or `scope`). 

Todo - add functionality for calling and matching against regexes. Have a plugin specifically 
for pattern matching (possibly in `conditions` file) 

### Plugin overview: `base`
Base is our go-to entrypoint for most files, as it defines `vars` and `scope`. It also optionally 
exports `export`, `regex`, `singleton-list`, `setargs`, `getargs`. 

It may also optionally define `strings`, `math`, and `conditions`, which contain basic non-turing complete functionality. 

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




   
