Aardappel Programming Environment, July 2000.

User Manual

Wouter van Oortmerssen


Installation
============
The Aardappel Editor is written in Java, and the compiler generates
Java applications so you will need a Java SDK or JRE installed on your
system (a Java enabled browser won't do). Get it from:

http://java.sun.com/jdk/

To install AardEd itself simply unzip it to any place you like (with
directories). If you have Java correctly installed you should be able
to run AardEd using the included aarded.bat (windows) or aarded.sh
(any unix) scripts. You may need to modify these to use "javaw" or
"jre" instead of "java", depending on what kind of Java you have
installed.


Using AardEd
============
Note well: this text is there to provide all practical information not
included in my thesis, as such it does not explain the language. To be
able to use AardEd sensibly you are advised to read atleast chapters 3
and 6 of my thesis, available from:

http://strlen.com/aardappel/

A few sample Aardappel projects have been included with AardEd that
you can load, browse through, and execute, to get a feel for the
system. Use "open" from the project menu and go to the "projects"
folder in the top directory. Load "project.ap" from any of its
subdirectories. "factorial" or "qsort" are good starting points.

To create your own project, simply create a new directory below the
projects dir (or anywhere else) and use "new" from the project menu to
save a "project.ap" file there. Then add new bags & modules to your
project and edit them. To save, use "save all" from the menu, which
saves all bags & modules and your project file. Quitting and
restarting AardEd will automatically load the last project you were
working on.


Editing programs
================
Again, most should become appearent from what is in the thesis and
some experimenting, but here are some general hints & tips to get
started.

The best way to create any tree is drag and drop: start at the root
and drag/replace children as you go. If you drag trees from another
rule they will be copied, if you drag them from pattern to expression
part you will create a placeholder. If you want to copy something
from another window, use copy & paste.

An alternative method to create trees is typing the name of the
root atom in the string widget at the top of the window. If it's
an existing tree with rules for it, Aarded will fill in the
children for you. To add children to a tree, use the the right
mouse button menu (ctrl-A).


Creating your own icons
=======================
You can associate a graphical representation with any atom you use in
your programs.  Simply create the picture in a paint program, save it
in gif format using transparent background if applicable, and put it
the "atomimages" subdirectory under <name>.gif, where <name> is equal
to the atom you want replaced (for operator symbols use their ascii
value). AardEd will automatically scan these pictures when it starts
up and render your atoms accordingly.

Similarly if certain icons confuse you, all that you need to do is
remove them from that directory (currently the only icons I made are
some operators, "lambda", and a couple for the qsort example).


Running an Aardappel program separate from AardEd
=================================================
To give compiled Aardappel programs to other people without
the environment, simply create a zip or jar that includes
the following files:

CompiledAardappel.class
AardappelRuntime*.class
r/*.class

and maybe a script file that runs

java CompiledAardappel

CompiledAardappel.class is simply the last compiled expression, so
make sure you select "compile standalone" on the appropriate top level
expression / bag before you copy these files.


Distributing programs over a network
====================================
To set up a machine as a server that can help out in Aardappel
computations, it needs to have a copy of these files:

AardappelRuntime*.class
r/*.class

AardEd uses RMI to communicate with the servers, so you need to have
"rmiregistry" running. This should be part of any Java installation.

You can then launch the server as follows:

java r/AardappelServer localhost

If everything went ok, this will then tell you that it is ready to
participate in computations. On your client machine you can now run
your Aardappel program as standalone and specify the ip addresses /
names of any servers on the command line:

java CompiledAardappel server1 server2 ...

if everything went ok your program should now run distributed over all
available servers and the client!

You can also start distributed computations directly from AardEd, but
depending on the way your system is set up there may be problems with
that. The infrastructure for it is in place, look at the source for
Compiler.compile() if you are interested.

In most cases the above is too simple to be true, and trying to run a
distributed computation out of the box will result in various obscure
security exceptions being thrown. RMI is subject to the
RMISecurityManager, which under Java 1.2 or greater gets its
permissions from the java policy files (they don't exist on 1.1, so it
should be much easier to run the distributed runtime system on that),
which in most default installations don't allow enough for the runtime
system to work.

To fix this requires you fiddle with the policy files in your Java
installation. You can take the lazy route and put something like
(replace <path> with where you installed AardEd):

grant codeBase "file://<path>/aarded/-" {
	permission java.security.AllPermission;
};

at the top of java.policy (depending on your system, the one in
jre/lib/security, or you can create one in your home directory or in
the aarded directory, in which case you may need to point to it from
the java.security file). Alternatively you can read up on Java
security and try to grant permissions more precisely :)


Not implemented functions
=========================
from the menu: "deploy" (use compile standalone in the treeview
editors instead).


Source code
===========
This release comes with full sourcecode, which you can browse out of
mere interest or enhance yourself.

One of the first thing you may want to add if you intend to write some
Aardappel programs are more builtin functions: the basic ones are
there but there are bound to be ones missing. To do this, add a
function to the existing ones in AardappelRuntime.java, like:

public static Rval builtin_rule_myfunction_2(Rval a, Rval b) { ... };

(the 2 is the number of arguments). Then in the constructor in
Compiler.java add a line like:

builtin("myfunction",2);

Now you can use trees with root atom "myfunction" and two children and
have this function evaluated instead. Look at other builtin functions
to see how to access an Rval. Caveat: if you add functions that have
side effects you will have to use AardappelServer.marshall() to make
sure they are executed on the client, if you want them to behave
correctly in a distributed program (look at "plot" for how to do
this).

General guide to the source code files:

the runtime system consists of AardappelRuntime.java + all the files
in the "r" package, most of which implement various runtime
representations of Aardappel values. Most interesting is the bag,
which implements Linda (see RbagTS.java)

The project editor is in Mainwin.java and Project.java, and the
graphical editor is in Treeview.java, all the subclasses of Code.java
(Atom/Int/Bag/Tree/Rules), and the various *info.java classes.

The compiler is in Compiler.java, Codegen.java (a generic interface to
the jas package) and Sym.java (contains the functionality for
compiling all rules with a certain root atom).

most code should prove to be easily modifyable and maintainable.


Future versions, legality status etc.
=====================================
I would like to improve AardEd, especially now it finally got
somewhere: all raw functionality is in, but it could do with a lot of
improvements. I can't make any promises however, since I have taken on
a quite demanding job, so don't come bug me for new releases or fixes
of certain problems.

Program & source are released under the Apache license v2. If you make
an improved version of AardEd I would love to hear about it.


Wouter van Oortmerssen



