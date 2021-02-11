package io.github.bymartrixx.yarncli;

import static io.github.bymartrixx.yarncli.OutputUtil.*;
import static io.github.bymartrixx.yarncli.MappingsManager.NS_INTERMEDIARY;
import static io.github.bymartrixx.yarncli.MappingsManager.NS_NAMED;
import static io.github.bymartrixx.yarncli.MappingsManager.NS_OFFICIAL;

import io.github.bymartrixx.yarncli.object.MappingsResult;
import io.github.bymartrixx.yarncli.object.MinecraftLatest;
import net.fabricmc.mapping.tree.ClassDef;
import net.fabricmc.mapping.tree.Descriptored;
import net.fabricmc.mapping.tree.MethodDef;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;

public class YarnCli {
    private static final String[] SPINNING_PROGRESS_BAR_FRAMES = {
            "[      ]",
            "[      ]",
            "[=     ]",
            "[==    ]",
            "[===   ]",
            "[====  ]",
            "[ ==== ]",
            "[  ====]",
            "[   ===]",
            "[    ==]",
            "[     =]",
            "[      ]",
    };
    public static final Path appPath = Paths.get(System.getProperty("user.home"), ".yarncli");
    public static final Path loomPath = Paths.get(System.getProperty("user.home"), ".gradle", "caches", "fabric-loom", "mappings");

    private static final String prompt = "yarncli> ";
    private static MappingsManager mappingsManager;

    public static void main(String[] args) {
        try {
            // Create app directory
            Files.createDirectories(appPath);
        } catch (Exception ignored) {}

        try {
            // Init mappings manager
            mappingsManager = new MappingsManager();

            // Prepare user input handler
            Terminal terminal = TerminalBuilder.builder().system(true).build();
            terminal.enterRawMode();
            LineReader reader = LineReaderBuilder.builder().terminal(terminal).build();

            // Get latest versions
            SpinningProgressBar spinningProgressBar = new SpinningProgressBar("Getting latest Minecraft versions... ", 50, SPINNING_PROGRESS_BAR_FRAMES);
            spinningProgressBar.startSpinning();
            MinecraftLatest minecraftLatest = mappingsManager.getLatestMinecraftVersions();
            spinningProgressBar.stopSpinning("[ done ]\n");

            // Setup mappingsManager
            spinningProgressBar = new SpinningProgressBar("Getting Yarn versions... ", 50, SPINNING_PROGRESS_BAR_FRAMES);
            spinningProgressBar.startSpinning();
            mappingsManager.cacheMappings(minecraftLatest.release, minecraftLatest.snapshot);
            spinningProgressBar.stopSpinning("[ done ]\n");

            printVersions(minecraftLatest);
            println("Use \"help\" to get a list of commands");

            String line;
            while (true) {
                line = "";
                line = reader.readLine(String.format("\033[32m%s\033[0m", prompt));
                line = line.trim();
                terminal.flush();

                if (line.equalsIgnoreCase("quit") || line.equalsIgnoreCase("exit")) {
                    break;
                }

                if (!line.equals("")) {
                    executeCommand(line);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void printVersions(MinecraftLatest latest) {
        String versionsFormat = "\tRelease: %s%n\tSnapshot: %s%n";

        String mcVersions = String.format(versionsFormat, latest.release, latest.snapshot);

        String yarnRelease = mappingsManager.yarnVersions.getOrDefault(latest.release, "No Yarn mappings found for " + latest.release);
        String yarnSnaphot = mappingsManager.yarnVersions.getOrDefault(latest.snapshot, "No Yarn mappings found for " + latest.snapshot);
        String yarnVersions = String.format(versionsFormat, yarnRelease, yarnSnaphot);

        printf("Using Minecraft versions: %n%s%n", mcVersions);
        printf("Using Yarn versions: %n%s%n", yarnVersions);
    }

    public static void executeCommand(String line) {
        String[] args = line.split("\\s");
        String command = args[0];
        switch (command.toLowerCase(Locale.ROOT)) {
            case "help":
                help();
                break;
            case "class":
            case "yc":
            case "c":
                if (args.length < 2) {
                    println("You must provide a class name!");
                } else {
                    List<MappingsResult> mappingsData;
                    try {
                        mappingsData = mappingsManager.getClassMappings(args[1]);
                    } catch (Exception e) {
                        red();
                        println("There was an error trying to process the request!");
                        reset();

                        e.printStackTrace();
                        break;
                    }

                    if (mappingsData == null) {
                        red();
                        println("Unable to find Yarn mappings for " + mappingsManager.selectedVersion);
                        reset();
                        break;
                    } else if (mappingsData.isEmpty()) {
                        yellow();
                        println("Unable to find any matching class names");
                        reset();
                        break;
                    }

                    formatPrint(mappingsData);
                }
                break;
            case "field":
            case "yf":
            case "f":
                if (args.length < 2) {
                    println("You must provide a field name!");
                } else {
                    List<MappingsResult> mappingsData;
                    try {
                        mappingsData = mappingsManager.getFieldMappings(args[1]);
                    } catch (Exception e) {
                        red();
                        println("There was an error trying to process the request!");
                        reset();

                        e.printStackTrace();
                        break;
                    }

                    if (mappingsData == null) {
                        red();
                        println("Unable to find Yarn mappings for " + mappingsManager.selectedVersion);
                        reset();
                        break;
                    } else if (mappingsData.isEmpty()) {
                        yellow();
                        println("Unable to find any matching field names");
                        reset();
                        break;
                    }

                    formatPrint(mappingsData);
                }
                break;
            case "method":
            case "ym":
            case "m":
                if (args.length < 2) {
                    println("You must provide a method name!");
                } else {
                    List<MappingsResult> mappingsData;
                    try {
                        mappingsData = mappingsManager.getMethodMappings(args[1]);
                    } catch (Exception e) {
                        red();
                        println("There was an error trying to process the request!");
                        reset();

                        e.printStackTrace();
                        break;
                    }

                    if (mappingsData == null) {
                        red();
                        println("Unable to find Yarn mappings for " + mappingsManager.selectedVersion);
                        reset();
                        break;
                    } else if (mappingsData.isEmpty()) {
                        yellow();
                        println("Unable to find any matching method names");
                        reset();
                        break;
                    }

                    formatPrint(mappingsData);
                }
                break;
            case "version":
                String version;
                try {
                    version = args[1];
                } catch (ArrayIndexOutOfBoundsException e) {
                    version = "release";
                }

                mappingsManager.selectVersion(version);
                break;
            default:
                printf("Unknown command \"%s\"%n", command);
                break;
        }
    }

    public static void help() {
        String[] help = {
                "List of available commands:",
                "\tclass <class>       Retrieve mappings for a given class name",
                "\tyc <class>          -> class <class>",
                "\tc <class>           -> class <class>",
                "\tfield <field>       Retrieve mappings for a given field name",
                "\tyf <field>          -> field <field>",
                "\tf <field>           -> field <field>",
                "\tmethod <method>     Retrieve mappings for a given method name",
                "\tym <method>         -> method <method>",
                "\tm <method>          -> method <method>",
                "\tversion [<version>] Change the Minecraft version.",
                "\t                      Use \"release\" for latest release and",
                "\t                      \"snapshot\" for latest snapshot. If",
                "\t                      unspecified default to latest release.",
                "\thelp                Show this message",
                "\th                   -> help",
                "\texit                Exit the program",
                "\tquit                -> exit",
        };

        for (String s : help) {
            println(s);
        }
    }

    public static void formatPrint(List<MappingsResult> results) {
        MappingResultsPrinter printer = result -> {
            if (result == null) {
                return;
            }

            ClassDef classDef = result.classDef;
            Descriptored member = result.member;

            underline(); bold();
            print("Class names");
            reset();
            print("\n\n");

            printf("\tOfficial > %s%n", classDef.getName(NS_OFFICIAL));
            printf("\tIntermediary > %s%n", classDef.getName(NS_INTERMEDIARY));
            printf("\tYarn > %s%n%n", classDef.getName(NS_NAMED));

            underline(); bold();
            if (member == null) {
                print("Access widener");
                reset();
                print("\n\n");

                printf("\taccessible\tclass\t%s%n", classDef.getName(NS_NAMED));
            } else {
                print("Member names");
                reset();
                print("\n\n");

                printf("\tOfficial > %s%n", member.getName(NS_OFFICIAL));
                printf("\tIntermediary > %s%n", member.getName(NS_INTERMEDIARY));
                printf("\tYarn > %s%n%n", member.getName(NS_NAMED));

                String type = (member instanceof MethodDef) ? "method" : "field";

                underline(); bold();
                print("Descriptor");
                reset();
                print("\n\n");

                printf("\t%s%n%n", member.getDescriptor(NS_NAMED));

                underline(); bold();
                print("Access widener");
                reset();
                print("\n\n");

                printf("\taccessible\t%s\t%s\t%s\t%s%n%n", type, classDef.getName(NS_NAMED), member.getName(NS_NAMED), member.getDescriptor(NS_NAMED));
            }
        };

        underline(); bold();
        printf("Minecraft %s / %d %s", mappingsManager.selectedVersion, results.size(), results.size() > 1 ? "results" : "result");
        reset();
        print("\n\n");

        for (MappingsResult result : results) {
            printer.print(result);
        }
    }

    static class SpinningProgressBar extends Thread {
        private final String msg;
        private final int delay;
        private final String[] frames;

        private boolean spinning;
        private int frameNumber = 0;

        SpinningProgressBar(String msg, int delay, String ... frames) {
            this.msg = msg;
            this.delay = delay;
            this.frames = frames;
        }

        public void startSpinning() {
            this.spinning = true;
            this.start();
        }

        public void stopSpinning(String doneMsg) {
            this.spinning = false;
            try {
                this.join();
            } catch (InterruptedException ignored) {}
            eraseCursorLine();
            cursorHorizontalAbsolute(1);
            yellow();
            OutputUtil.print(this.msg);
            reset();
            OutputUtil.print(doneMsg);
        }

        @Override
        public void run() {
            hideCursor();
            while (this.spinning) {
                eraseCursorLine();
                cursorHorizontalAbsolute(1);
                yellow();
                OutputUtil.print(this.msg);
                reset();
                OutputUtil.print(this.frames[this.frameNumber]);
                this.frameNumber = (this.frameNumber + 1) % this.frames.length;

                try {
                    Thread.sleep(this.delay);
                } catch (InterruptedException ignored) {}
            }
            showCursor();
        }
    }

    @FunctionalInterface
    interface MappingResultsPrinter {
        void print(MappingsResult result);
    }
}
