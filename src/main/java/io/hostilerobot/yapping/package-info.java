package io.hostilerobot.yapping;

/**
 * package overview:
 *   ast - contains data related to the syntax tree in a yapping format
 *   parser - contains functions related to generating a syntax tree from a charsequence
 *   parser.advancer - contains functional structures for advancing through a charsequence with state for parsing
 *   util - various utilities
 *
 * todo -
 *   Have parsing implementations that allow us to read in files (or inputstreams) and handle the data
 *   options:
 *       ToDataStructure - converts from charsequence to (bare( ast
 *
 *       (no tostringmap for the time being. might be an implementation of pluginparser)
 *       ToStringMap(ToDataStructure, Options) - converts from chars to a string map
 *              Options - COLLECT_CLASH(Predicate) | OVERWRITE_CLASH(Predicate) | MERGE_CASH(Predicate)
 *              section:
 *                 item1 = a
 *                 item2 = b
 *              section:
 *                 item2 = a
 *                 item2 = c
 *                 item3 = a
 *              => section.item1 = a (list)
 *                 section.item2 = b
 *                 section.item2 = c
 *                 section.item2 = c
 *                 section.item3 = a
 *              => section.item2 = c (OVERWRITE on all)
 *                 section.item3 = a
 *              => section.item1 = a (Merge on all)
 *                 section.item2 = c
 *                 section.item3 = a
 *              => 0.section.item1 = a (Collect on all)
 *                 0.section.item2 = b
 *                 1.section.0.item2 = a
 *                 1.section.1.item2 = c
 *                 1.section.item3 = a
 *              (a, b, c)
 *              => 0 = a
 *                 1 = b
 *                 2 = c
 *
 *       PluginParser(Plugins, Options, ToDataStructure)
 *          -- base plugins can be Imports, Base, Math, etc. Imports handles the import sections. Base handles looking up names with values
 *
 *          todo - use brackets to symbolize lookups.
 *                  myfile.triangles[5 3/4].
 *                  import [my name with spaces] = @[my file with spaces].yah
 *                  [my name with spaces].triangles
 *                  import @[my yah file].yah # use @ to symbolize yah file vs java defined properties format
 *                  [my yah file].triangles
 *                  import com.user.Matrices
 *                  Matrices.determinant[ file1.matrix1 ] # pass matrix1 like an argument
 *                  import mat = com.user.Matrices
 *                  mat.determinant[ ((0, 1, 2), (3, 4, 5), (6, 7, 8)) ]
 *
 *       what we want:
 *       import :
 *           @yapping/base.yah # defines setting variables, etc. Will just import relevant java classes
 *           @yapping/math.yah # "yet another header". defines a structure that will be merged in
 *           @yapping/debug.yah # use (debug, value) to print values
 *           com.user.YATriangleProperties # defines vertices, triangles environments (after this point)
 *       vertices:
 *          v0 = (0, 0) v1 = (3 3/4, -3/2), v2 = (3 3/4, 3/2)
 *       triangles: (mesh1,
 *          t1 = (v0, v1, v2),
 *          t2 = (v0, v2, v3))
 *          (mesh2,
 *          t1 = (add, mesh1.t1, mesh1.t2)
 *          t2 = mesh1.t1
 *          (import: @myfile.yah, myfile.triangle))
 *
 *      what we want:
 *          each java class defines a set of environments. Environments "match" against a node to know whether or not they accept it
 *          environments may or may not be sticky (e.g. matching with parent node implies matching with child nodes)
 *          multiple environments may be on a node at a single time, but closest scope goes first.
 *          environment applies a transformation to the node, resulting in another node structure or to some other Object.
 *          the result of the node may also produce more than one named Lookup objects, which can get values or apply arguments to the node.
 *          the name is included in the variable scope.
 *          We also may provide a "merge" function, which deals with name clashes. Essentially given two nodes under the same name (name, A, B) -> M where M is merged. Can be overwriting,
 *             can be set union, can be making a new name for the entries.
 *          mynode.abc -> lookup abc, abc always treated as a string literal
 *          mynode[abc] -> lookup abc. We parse abc as normal with the environment XXX - we should probably just use [abc] as a primitive only
 *                         lookup has a defined list of matching arguments for a given environment, so it could be possible to use the second form
 *                         (mymesh.getabove, 3 3/4) vs mymesh.getabove[3 3/4]
 *                         (glueparts: SPACE, my, var, with, spaces)
 *                         (glueparts: SPACE LITERAL, my, var, with, spaces) -- reset variables so inner parts are literal
 *                         translated = (for: i=0, i<mymesh.triangles.length, in:
 *                              (get: mymesh.triangles, i).x + 1
 *                              i = i + 1)
 *
*                          glueparts: SPACE LITERAL (my, var, with, spaces);
 *                          add = fun: (x, y) (add: ...)
 *                          myadd = fun: x y (add: (x, y));
 *
 *                          myadd2 = fun: w x y (add: (w, myadd2: x y))
 *                                   fun: x y (add: (x, y))
 *                                   fun: y (y);
 *                          myadd2: 1 2 3 4 5 6;
 *
 *
 *
 *                         myadd = (fun: x y, (add, x, y))
 *                         add12 = myadd:12; # or maybe (myadd:, 12) or (myadd, 12)
 *                         22 = add12: 10 ;
 *
 *
 *
 *      some environemnts will merge with its parent (like base or math, which merge with the parent)
 *
 *
 *
 */