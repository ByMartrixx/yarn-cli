# Yarn CLI
Yarn CLI is a command line tool that integrates mapping related commands from the [Fabric Bot](https://github.com/FabricMC/fabric-discord-bot).
Some code was copied and adapted from the [Fabric Bot](https://github.com/FabricMC/fabric-discord-bot), and is [licensed under the MIT License](/LICENSE-FABRIC-BOT).
This project is also licensed under the [MIT License](https://opensource.org/licenses/MIT).

### Please report any issues/bugs on the [issue tracker](https://github.com/ByMartrixx/yarn-cli/issues)

## Usage
To run this tool: 
1. Download the `-all` jar file, from the latest release, to a folder of your preference
2. Open a terminal inside the folder
3. Type in `java -jar jarfil.jar"`, changing the `jarfile.jar` for the name of the file downloaded in step 1
4. (Optional) create a new text file, write the command from step 3 on it, and save the file with the `.bat`
   extension (On Windows). You can use this to run the tool now
5. After it finishes getting the versions, you can type in `help` to get a list of available commands

The tool uses mappings from the fabric-loom gradle plugin cache - if there are no mappings for the selected
yarn version, it downloads them to the `.yarncli` folder in your user folder (`C:\Users\youruser\.yarncli`
on Windows, `/home/youruser/.yarncli` on Unix)
