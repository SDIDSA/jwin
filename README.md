# jwin
a simple java tool to deploy maven based java apps to windows (planning to support plain java projects in the next version).

## how to use

- select the source folders for your project.
- select the main class.
- select the jdk to be used to compile the java code. (can be automatically detected if you hava javac in your environment variable [path])
- select the jre to be packed and deployed with your app. (an as or more recent version than the selected jdk)
- select the icon (.ico) to be used for the launcher and the installer.
- click "resolve" to resolve dependencies. (only works with the pom.xml dependencies for now, planning to add the option to include plain jars in the next version)
- enter app name, version and publisher.
- click "build" and select the destination folder for your installer.
- wait for the process to finish.
- that's it, find your installer in the selected location.

## how it works

This tool will compile your source code (using the selected jdk) into a temp folder alongside with your resources / dependencies and the jre you selected, the generates a .bat file that runs the compiled code using the copied jre, then converts the bat into an exe using the Bat To Exe CLI, then packs everything with innoSetup CLI using a generated innoSetup script.

## problems and notes

- all the fields are required.
- resources and java code can't exist within the same classpath entry, resources in an entry that contains java code will not be copied (working on a fix).
