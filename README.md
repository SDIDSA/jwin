# jwin
a simple java tool to deploy java apps to windows (supports plain java projects and maven projects for now).

## how to use

- select the source folders for your project.
- select the main class.
- select the jdk to be used to compile the java code. (can be automatically detected if you hava javac in your environment variable [path])
- select the jre to be packed and deployed with your app. (or generate it with jlink if your jdk has it)
- select the icon (.ico) to be used for the launcher and the installer.
- click "resolve" to resolve maven dependencies or "add jars" to add your dependencies manually.
- enter app name, version and publisher.
- save your project so you can use it later.
- click "build" and select the destination folder for your installer.
- wait for the process to finish.
- that's it, find your installer in the selected location.

## how it works

This tool will compile your source code (using the selected jdk) into a temp folder alongside with your resources / dependencies and the jre you selected, then generates a .bat file that runs the compiled code using the copied jre, then converts the bat into an exe using the Bat To Exe CLI, then packs everything with innoSetup CLI using a generated innoSetup script.

## problems and notes

- you have to resolve dependencies before generating a java runtime with jlink.
- all the fields are required.
