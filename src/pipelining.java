import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * 
 */

/**
 * @author Ryan Murphy, Nachiket Chauhan
 *
 */
public class pipelining {

    public static pipelining INSTANCE = new pipelining();

    // The list of instructions
    private ArrayList<Instruction> instructions = new ArrayList<Instruction>();

    // The list of registers. Check the handout for which index is which.
    private ArrayList<Integer> registers = new ArrayList<Integer>(10);
    // Stores which instructions are in which stage.
    private Instruction IF, ID, EX, MEM, WB;
    // The mode is only used for deciding the algorithm, so in theory it could
    // be a boolean.
    private int mode, pc = 0;

    // I'm doing this so that I don't have to worry about what is static and
    // what isn't, it really doesn't do anything beneficial
    public static void main(String[] args) {
	try {
	    INSTANCE.run(args);
	} catch (FileNotFoundException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
    }

    public void run(String[] args) throws FileNotFoundException {
	File firstFile = new File(args[0]);
	File secondFile = new File(args[1]);
	mode = Integer.parseInt(args[2]);

	registers.set(0, 0);
	Scanner scan = new Scanner(firstFile);

	// Add each instruction to the array of instructions. This might be
	// superfluous but fuck it.
	while (scan.hasNext()) {
	    instructions.add(new Instruction(scan.nextLine().trim()));
	}

	scan.close();
	scan = new Scanner(secondFile);

	while (scan.hasNext()) {
	    // I don't know what to do here
	}

	scan.close();

	if (mode == 1)
	    runMode1();
	else
	    runMode2();

    }

    private void runMode1() {
	// Do stuff
    }

    private void runMode2() {
	// Do stuff
    }

    private void printpc() {
	System.out.println("PC = " + pc);
    }

    // This is going to represent each command in the program. They'll each
    // handle their own flags maybe? Havn't decided yet.
    private class Instruction {

	// The command and then whatever else is after the command.
	private String command, data;

	// Each of the registers. There are going to be at max three, but not
	// all of them have to be initialized.
	private int reg1, reg2, reg3;
	
	// The immediate
	private int immediate;

	public Instruction(String instruction) {
	    Scanner scan = new Scanner(instruction);
	    String printCheck = scan.next();
	    if (printCheck.equalsIgnoreCase("PRINTPC")
		    || printCheck.equalsIgnoreCase("PRINTREG")
		    || printCheck.equalsIgnoreCase("PRINTEX"))
		command = printCheck;
	    else
		command = scan.next();
	    data = scan.nextLine().trim();

	    // TODO: parse which registers are going to be used by this
	    // instruction and put them in int form. How are we going to
	    // implement stuff like 100($T1)?
	}

	public String toString() {
	    return command + " " + data;
	}
    }
}
