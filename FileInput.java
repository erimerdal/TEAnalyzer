import java.lang.Object;
import java.util.Scanner;
public class FileInput 
{
  public static void main(String[] args) throws Exception
  {

    System.out.println("Welcome to Transposable Element Analyzer. ");
    System.out.println("For Help, Please Enter --help to see instructions. ");
    System.out.println("Enter Your Command Below: ");
    
    
    // This String can be taken by output.
    System.out.print("-> ");
    Scanner scanner = new Scanner(System.in);
    String choice;
    // geneRead reads the bed data by using FileReader Class's Constructor.
        
        choice = scanner.nextLine();
	
        FileReader geneRead1 = new FileReader(choice);
   }
}
