import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;
import javax.swing.*;

/// Main program entry point.
/**
 * The Main class glues together all of the pieces of the program.  It is responsible
 * for parsing command-line input and creating the displays.  Use the --help switch
 * to get more information about the command line arguments.
 * 
 */
public class Main
{
    /// Prints the commandline instructions.
    public static void helpPrinter()
    {
        System.out.println("  Command Line Parameters are as follows:");
        System.out.println("    \"--help\" : You're looking at it");
        System.out.println("    \"-w [int]\" : Set the width of the terrain map");
        System.out.println("      Example: -w 500");
        System.out.println("    \"-h [int]\" : Set the height of the terrain map");
        System.out.println("      Example: -h 500");
        System.out.println("    \"-seed [int]\" : Set the seed value for generating the terrain map");
        System.out.println("      Example: -seed 3");
        System.out.println("    \"-roughness [0-7]\" : Set the roughness of the terrain map");
        System.out.println("      Example: -roughness 4");
        System.out.println("    \"-movement [cem]\" : Set the type of movement [c: Chess, e: Euclidean, m: Manhattan]");
        System.out.println("      Example: -movement c");
        System.out.println("    \"-contour\" : Displays the order of revealed nodes");
        System.out.println("    \"-chaotic\" : Slightly corrupts the terrain every one second");
        System.out.println("    All other arguments are algorithms to be run on the generated map");
        System.out.println("Example: java Main -w 500 -h 500 Dijkstra AStar");
    }

    /// Displays an image in a new frame.
    /**
     * @param im The image to display.
     * @param title The title of the new window.
     */
    public static void createDisplayWindow(final BufferedImage im, final String title)
    {
        // Set up a frame and a panel that paints this image.
        final JFrame frame = new JFrame("Path Finder: " + title);
        final JPanel panel = new JPanel()
        {
            @Override
            public void paintComponent(final Graphics g)
            {
                super.paintComponent(g);
                g.drawImage(im, 0, 0, getWidth(), getHeight(), null);
            }
        };
        
        panel.setPreferredSize(new Dimension(im.getWidth(), im.getHeight()));
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.add(panel);
        frame.pack();
        frame.setVisible(true);
    }

    /// Main program entry point.
    public static void main(String[] args)
    {        
        ArrayList<AIModule> aiList = new ArrayList<AIModule>();
        TerrainGenerator terraGen = null;
        boolean flexible = true;
        boolean showContour = false;
        boolean chaotic = false;
        int width = 500;
        int height = 500;
        int roughness = 3;
        long seed = System.currentTimeMillis();
        TerrainMap.MovementType moveType = TerrainMap.MovementType.Chess;

        // Parse through the command line arguements
        int i = 0;
        try
        {
            while(i < args.length)
            {
                // Width switch.
                if(args[i].equalsIgnoreCase("-w"))
                {
                    if(!flexible)
                        continue;
                    
                    width = Integer.parseInt(args[i + 1]);
                    if(width <= 0)
                    {
                        throw new IllegalArgumentException("Widths must be nonnegative.");
                    }
                }
                // Height switch
                else if(args[i].equalsIgnoreCase("-h"))
                {
                    if(!flexible)
                        continue;
                    
                    height = Integer.parseInt(args[i + 1]);
                    if(width <= 0)
                    {
                        throw new IllegalArgumentException("Heights must be nonnegative.");
                    }
                }
                // Random seed switch
                else if(args[i].equalsIgnoreCase("-seed"))
                {
                    seed = Integer.parseInt(args[i + 1]);
                }
                // Roughness switch.
                else if(args[i].equalsIgnoreCase("-roughness"))
                {
                    roughness = Integer.parseInt(args[i + 1]);
                    if(roughness < 0 || roughness >= 8)
                        throw new IllegalArgumentException("Roughness must be between 0 and 7.");
                }
                // Movement switch.
                else if(args[i].equalsIgnoreCase("-movement"))
                {
                    switch(args[i + 1].charAt(0))
                    {
                        case 'c': moveType = TerrainMap.MovementType.Chess; break;
                        case 'e': moveType = TerrainMap.MovementType.Euclidean; break;
                        case 'm': moveType = TerrainMap.MovementType.Manhattan; break;
                        default: throw new IllegalArgumentException("Unrecognized movement type.");
                    }
                }
                // Contour switch.
                else if(args[i].equalsIgnoreCase("-contour"))
                {
                    showContour = true;
                    i--;
                }
                // Chaos switch.
                else if(args[i].equalsIgnoreCase("-chaotic"))
                {
                    chaotic = true;
                    i--;
                }
                // Help switch
                else if(args[i].equalsIgnoreCase("--help"))
                {
                    helpPrinter();
                    System.exit(0);
                }
                // If none of these, then we should treat it as a class to load.
                else
                {
                    aiList.add((AIModule) Class.forName(args[i]).getDeclaredConstructor().newInstance());
                    i--;
                }
                i += 2;
            }
        }
        catch(ClassNotFoundException cnf)
        {
            System.err.println("AI Not Found: " + args[i]);
            System.exit(1);
        }
        catch(IndexOutOfBoundsException ioob)
        {
            System.err.println("Invalid Arguements: " + args[i]);
            System.exit(2);
        }
        catch(NumberFormatException e)
        {
            System.err.println("Invalid Integer: " + args[i]);
            System.exit(3);
        }
        catch(Exception e)
        {
            System.err.println("Unknown Error");
            System.exit(4);
        }
        
        terraGen = new PerlinTerrainGenerator(width, height, roughness, seed);
        
        // For each module we've loaded, create a new map and run the appropriate
        // AI module on that map.
        for(AIModule ai : aiList)
        {
            final TerrainMap map = new TerrainMap(width, height, terraGen, moveType, chaotic);
            final long startTime = System.currentTimeMillis();
            final double cost = map.findPath(ai);
            final long endTime = System.currentTimeMillis();

            System.out.println(ai.getClass().getName());
            System.out.println("PathCost, " + cost + ", Uncovered, " + map.getNumVisited() + ", TimeTaken, " + (endTime - startTime));

            createDisplayWindow(map.createImage(), ai.getClass().getName());
            if(showContour)
                createDisplayWindow(map.createContourImage(), ai.getClass().getName());
        }
    }
}