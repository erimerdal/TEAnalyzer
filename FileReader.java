// File: FileReader.java
// Name: Erim Erdal
// Date: 18/07/17
// Desc: A bioinformatics tool for analyzing Transcription Factors and 
// 	 giving outputs accordingly.
// Usage: Program gives output files as txt and graphs in order to help
//	  out the user ease their analyzes.


import java.io.File;
import java.util.Scanner;
import java.lang.Runtime;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.io.PrintWriter;
import java.io.FileNotFoundException;

public class FileReader {

  // Integer: Is used in giving ArrayList a start point.
  int countTemp = 1;
  // Boolean: For checking if gene is entered in ArrayList before.
  boolean teFlag = false;
  // Integer Array: Used for counting TE's.
  int[] countedTe;
  // String ArrayList: To count Genes and their corresponding TE's in bedTools part.
  ArrayList<String> teList = new ArrayList<String>(); 
  // String: Resulted is the String which is written into results.bed.
  String resulted;
  // String: This output of results is hard coded.
  String document = "results.bed";
  // String: Pheno Data's name that user entered.
  String phenoData;
  // String: Gene information file's name that user entered.
  String geneInfo;
  // String: Helps to store names of Bams that user entered and counts them.
  String countBams[];
  // String: Helps us create a structure to print in UpDownInfo.txt.
  String upDownPrint;
  // String Array: Will look at changing TE values in BAM files and will insert to experiment or control accordingly.
  String experimentBams[];
  String controlBams[];
  // String Array: Will look at pheno groups.
  String groupPheno[];
  // String: Result of counts after calling multicov.
  String multiResult;
  // Integer Array: Counts types (UP,DOWN,NON-DE) among BED files.
  int[] geneTypes;

  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  public FileReader(String choice) throws Exception
  {
    
    Scanner bedReader = new Scanner(System.in);
    choice.toLowerCase();
    Scanner choicescan = new Scanner(choice);
    // Should specifically define what order the input user will give.

    while(choicescan.hasNext())
    {
    System.out.println(choicescan.next());
    }    
    while(! (choice.equals("--quit")))
    {
    // Display help chart if user enters --help.
    if(choice.equals("--help"))
    {
     // Gives the user brief information about the program and its uses.
     System.out.println("EXPLANATION");
     System.out.println("___________");
     System.out.println("This tool takes two BED files. First one should be the BED file of genes, second BED file should be Transposable Element's. ");
     System.out.println("File can both work in symmetric and asymmetric window lengths. So you can enter the left and right strand different or same."); 
     System.out.println("Tool will give you an output file named UpDownInfo.txt in your the Destination you give which will include the information about overlaps between Genes and Transposable Elements, also individual and total counts of UP, DOWN and Non-Differentially Expressed Genes.");
     System.out.println("OPTIONS");
     System.out.println("_______");
     System.out.println("--TEanalyzer --genes.bed --te.bed");
     System.out.println("--TEanalyzer --genes.bed --te.bed --geneinfo.txt");
     System.out.println("--TEanalyzer --genes.bed --te.bed --geneinfo.txt --phenodata.txt");
     System.out.println("Also you should give the directory to create the output file UpDownCount.");
     System.out.println("--TEanalyzer --genes.bed --te.bed --o /home/user/Desktop");
     System.out.println("You should also add right and left strand lengths, depending on asymmetric or" + " symmetric search.");
     System.out.println("--TEanalyzer --genes.bed --te.bed --geneinfo.txt --a --l 1000 --r 2000");
     System.out.println("--TEanalyzer --genes.bed --te.bed --geneinfo.txt --s 1000");
     System.out.println("");
     System.out.print("Find More Detailed Explanation in : https://github.com/erimerdal/TEAnalyzer/blob/master/Information.txt ");
      
    }  // End if 
    // Should start interpreting user's input.

    else
    {
      
      System.out.println("In order to be able to use Bedtools Function, you must put your 2" + 
" bed files to desktop and specify their name below. Also the window length.");
      
      System.out.print("1- Enter TE file's name and extension: ");
      String firstFile = bedReader.next();
     
      
      System.out.print("2- Enter GENE file's name and extension: ");
      String secondFile = bedReader.next();
      
      System.out.print("3- Enter PhenoData file's name and extension: ");
      phenoData = bedReader.next();

      System.out.print("4- Enter GeneInformation file's name and extension: ");
      geneInfo = bedReader.next();
      
      System.out.print("You should also define your windows. It can be either asymmetric on left or right depending on your strand type / (-) or (+) /, or it can be a " +
"symmetric window equal on both sides. To create an asymmetric window and enter both left and right length (Any of them can be 0), Enter \"A\", or \"a\". To create a symmetric " +
"window please enter \"S\" or \"s\": ");

      String symChoice = bedReader.next();

      if(symChoice.equals("S") || symChoice.equals("s"))
      {
        System.out.print("Enter your symmetric width: ");
	int width = bedReader.nextInt();
        teList = equalWindow(firstFile,secondFile,width);
	
      }
      if(symChoice.equals("A") || symChoice.equals("a"))
      {
        System.out.print("Enter your left strand width: ");
        int leftWidth = bedReader.nextInt();
        System.out.print("Enter your right strand width: ");
        int rightWidth = bedReader.nextInt();
        teList = asymWindow(firstFile,secondFile,leftWidth,rightWidth);
      }
       //String bamFiles[] = new String[4];
       //bamFiles = getFromPhenoData(phenoData);
       //callSamTools(bamFiles);
       //multiResult = callMultiCov(bamFiles, document);
	
    }
    System.out.print("-> ");
    Scanner choiceScan = new Scanner(System.in);
    choice = choiceScan.nextLine();
    } // End while.
    System.out.println("Goodbye.");
  };
   
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  public int[] counterTEequal (ArrayList<String> arrayName, String fileFirst, String fileSecond, int window) throws Exception
  {
	// Construct geneTypes in 3 different groups.
	// 0 - Up
	// 1 - Down
	// 2 - Non-De
	// Since every TE will have a UP DOWN or NON-DE count, we create 3 times the TE size.
	// (3*count-3) start, (3*count-1) end of each TE.
	int counterForGene = 0;
	geneTypes = new int[( arrayName.size() * 3 )];
	// We need to store an array of TE names, check new TE if it is listed before before incrementing counter.
	String[] teNames = new String[arrayName.size()];
	// Initializing an array to count the genes's corresponding TE's and their numbers.
	int[] teCount = new int[arrayName.size()];
	
	final Process p = Runtime.getRuntime().exec("bedtools window -a " + fileFirst + " -b " + fileSecond + " -w " + window);
        // We need another scanner in order to read the gene information file.
	File infoFile = new File(geneInfo);
	try{
        Scanner infoScan = new Scanner(infoFile);
	boolean isNameThere = false;

           Scanner geneScan = new Scanner(new InputStreamReader(p.getInputStream()));
              try{
              while((geneScan.hasNextLine()))
              {    
	            geneScan.next();
                    geneScan.next();
                    geneScan.next();
                    geneScan.next();
                    String teName = geneScan.next();
		    geneScan.next();
                    geneScan.next();
                    geneScan.next();
                    geneScan.next();
		    geneScan.next();
                    geneScan.next();
                    String geneName = geneScan.next();

		    // Increment counter for next TE.
		    boolean shouldIncrement = true;
		    for(int i = 0; i < teNames.length; i++)
		    {
			if(teName.equals(teNames[i]))
			{
		    		shouldIncrement = false;
			}
		    }
		    
		    if(shouldIncrement)
		    {
			teNames[counterForGene] = teName;
			counterForGene++;
		    }
		    

		    while(infoScan.hasNextLine())
		    {   
			String infoName = infoScan.next();
			String typeName = infoScan.next();
			if(geneName.equals(infoName))
		        {
			    if(typeName.equals("UP"))
			    {
				geneTypes[((counterForGene-1)*3)]++;
			    }
			    else if(typeName.equals("DOWN"))
   			    {
				geneTypes[((counterForGene-1)*3)+1]++;
			    }
			    isNameThere = true;
                        }
		        
			
			infoScan.nextLine();
		    }
		    if(isNameThere != true)
		    {
		 	geneTypes[((counterForGene-1)*3)+2]++;
				
		    }
		    
		    
		    // Restart boolean value.
		    isNameThere = false;
		    shouldIncrement = true;
		    // Restart infoScan for next TE.
		    infoScan = new Scanner(infoFile);
		    // Proceed to next line.
                    geneScan.nextLine();
		    
              }
             
              }catch(NoSuchElementException e)
              {System.out.println("Hello Im an exception.");}
	}catch(FileNotFoundException excep)
        {System.out.println("File is not found.");}

     
       return geneTypes;
    }

     ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public int[] counterTEasym (ArrayList<String> arrayName, String fileFirst, String fileSecond, int leftWindow, int rightWindow) throws Exception
    {
	// Construct geneTypes in 3 different groups.
	// 0 - Up
	// 1 - Down
	// 2 - Non-De
	// Since every TE will have a UP DOWN or NON-DE count, we create 3 times the TE size.
	// (3*count-3) start, (3*count-1) end of each TE.
	int counterForGene = 0;
	geneTypes = new int[( arrayName.size() * 3 )];
	// We need to store an array of TE names, check new TE if it is listed before before incrementing counter.
	String[] teNames = new String[arrayName.size()];
	// Initializing an array to count the genes's corresponding TE's and their numbers.

	int[] teCount = new int[arrayName.size()];
	
	final Process p = Runtime.getRuntime().exec("bedtools window -a " + fileFirst + " -b " + fileSecond + " -l " + leftWindow + " -r " + rightWindow);

    
           // We need another scanner in order to read the gene information file.
	File infoFile = new File(geneInfo);
	try{
        Scanner infoScan = new Scanner(infoFile);
	boolean isNameThere = false;

           Scanner geneScan = new Scanner(new InputStreamReader(p.getInputStream()));
              try{
              while((geneScan.hasNextLine()))
              {    
	            geneScan.next();
                    geneScan.next();
                    geneScan.next();
                    geneScan.next();
                    String teName = geneScan.next();
		    geneScan.next();
                    geneScan.next();
                    geneScan.next();
                    geneScan.next();
		    geneScan.next();
                    geneScan.next();
                    String geneName = geneScan.next();

		    // Increment counter for next TE.
		    boolean shouldIncrement = true;
		    for(int i = 0; i < teNames.length; i++)
		    {
			if(teName.equals(teNames[i]))
			{
		    		shouldIncrement = false;
			}
		    }
		   
		    if(shouldIncrement)
		    {
			teNames[counterForGene] = teName;
			counterForGene++;
		    }
		    

		    while(infoScan.hasNextLine())
		    {   
			String infoName = infoScan.next();
			String typeName = infoScan.next();
			if(geneName.equals(infoName))
		        {
			    if(typeName.equals("UP"))
			    {
				geneTypes[((counterForGene-1)*3)]++;
			    }
			    else if(typeName.equals("DOWN"))
   			    {
				geneTypes[((counterForGene-1)*3)+1]++;
			    }
			    isNameThere = true;
                        }
		        
			
			infoScan.nextLine();
		    }
		    if(isNameThere != true)
		    {
		 	geneTypes[((counterForGene-1)*3)+2]++;
				
		    }
		    
		   
		    // Restart boolean value.
		    isNameThere = false;
		    shouldIncrement = true;
		    // Restart infoScan for next TE.
		    infoScan = new Scanner(infoFile);
		    // Proceed to next line.
                    geneScan.nextLine();
		
		    
              }
             
              }catch(NoSuchElementException e)
              {System.out.println("Hello Im an exception.");}
	}catch(FileNotFoundException excep)
        {System.out.println("File is not found.");}

     
       return geneTypes;
    }
   
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
    public ArrayList<String> equalWindow(String firstFile, String secondFile, int width) throws Exception
    {
      
      final Process p = Runtime.getRuntime().exec("bedtools window -a " + firstFile + " -b " + secondFile + " -w " + width);

      new Thread(new Runnable() {
        public void run() {
           Scanner input = new Scanner(new InputStreamReader(p.getInputStream()));
           try {
              
              while((input.hasNextLine()))
              {     // In while we have to do some calculations so we can count our variables.
                    
                    
                    String chromosomeNum = input.next();
                    String start = input.next();
                    String end = input.next();
                    String strand = input.next();
                    String teName = input.next();
		    String classOf = input.next();
                    String family = input.next();
                    input.next();
                    input.next();
		    input.next();
                    input.next();
                    String geneName = input.next();
                 
                    if(countTemp == 1)
                    {
			   resulted = resulted + chromosomeNum + "\t" + start + "\t" + end + "\t" +
                      strand + "\t" + teName + "\t" + classOf + "\t" + family + "\n";
                           teList.add(teName);
                           countTemp++;
                    }
 
                    for(int i = 0; i < teList.size(); i++)
		    {
                        if(teName.equals(teList.get(i)))
                        {
                           teFlag = true;   
                        }
                    }

                    if(!teFlag)
                    {
                    	teList.add(teName);
			resulted = resulted + chromosomeNum + "\t" + start + "\t" + end + "\t" +
                      strand + "\t" + teName + "\t" + classOf + "\t" + family + "\n";
			document = createBed(resulted);
                    }
        
                    teFlag = false;
                    
           }
           } catch (NoSuchElementException e)
           { System.out.println(""); }
           
           
           // Call counterTE function to count which gene has how many TE's.

           try(  PrintWriter out = new PrintWriter( "UpDownInfo.txt" )  ){
          
           countedTe = counterTEequal(teList, firstFile, secondFile, width);
	   upDownPrint += "\t\t" + "UP" + "\t" + "DOWN" + "\t" + "NON-DE" + "\n";
           int totalUp = 0;
           int totalDown = 0;
           int totalNonDE = 0;
	   for(int i = 0; i < teList.size(); i++)
	   {
		upDownPrint +=  teList.get(i) + "\t\t" + countedTe[3*i] + "\t" + countedTe[3*i+1] + "\t" + countedTe[3*i+2] + "\n"; 
                totalUp += countedTe[3*i];
                totalDown += countedTe[3*i + 1];
                totalNonDE += countedTe[3*i + 2];
	   }
           
	   
           upDownPrint += "\t\t" + totalUp + "\t" + totalDown + "\t" + totalNonDE; 
	   out.println( upDownPrint.replace("null",""));  
	 
    	   }catch(FileNotFoundException exceptional)
           { System.out.println("File is not found."); }
           catch(Exception exception)
           { System.out.println("countedTE function exception."); }
          
           calculateP(upDownPrint);
         }
       }).start();
       p.waitFor(); 

       return teList;
    }

	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public ArrayList<String> asymWindow(String firstFile, String secondFile, int leftWidth, int rightWidth) throws Exception
    {
      final Process p = Runtime.getRuntime().exec("bedtools window -a " + firstFile + " -b " + secondFile + " -l " + leftWidth + " -r " + rightWidth);

      new Thread(new Runnable() {
        public void run() {
           Scanner input = new Scanner(new InputStreamReader(p.getInputStream()));
           try{
              while((input.hasNextLine()))
              {     // In while we have to do some calculations so we can count our variables.
                    
                    
                    String chromosomeNum = input.next();
                    String start = input.next();
                    String end = input.next();
                    String strand = input.next();
                    String teName = input.next();
		    String classOf = input.next();
                    String family = input.next();
                    input.next();
                    input.next();
		    input.next();
                    input.next();
                    String geneName = input.next();
	          
                    if(countTemp == 1)
                    {
                           teList.add(teName);
                           countTemp++;
                    }
 		   
                    for(int i = 0; i < teList.size(); i++)
		    {
                        
                        if(teName.equals(teList.get(i)))
                        {
                           teFlag = true;   
                           
                        }
                    }
                    

                    if(!teFlag)
                    {
                    	teList.add(teName);
			resulted = resulted + chromosomeNum + "\t" + start + "\t" + end + "\t" +
                      strand + "\t" + teName + "\t" + classOf + "\t" + family + "\n";
			
                        document = createBed(resulted);
                        
                    }
                  
                  
                    teFlag = false; 
           } 
           } catch (NoSuchElementException e)
           { System.out.println(""); }
         
           try{
           countedTe = counterTEasym(teList, firstFile, secondFile, leftWidth, rightWidth);
           } catch (Exception e) {
          }

         }
       }).start();
       p.waitFor();
      
       return teList;
    }   

     /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////// 

   public String createBed(String resultInput)
   {	
	
	try(  PrintWriter out = new PrintWriter( document )  ){
			 
		    out.println( resultInput.replace("null",""));  
    		
	}catch(FileNotFoundException exceptional)
        { System.out.println("File is not found."); }
	
        return document;

   }

     ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
   // In this method we find uniquely mapped reads for the user's given raw BAM files. Output in same name so callMultiCov can
   
   public void callSamTools(String[] bamFiles)
   {
	/**
	for(int i = 0; i < bamFiles.length; i++)
	{
		String execute = bamFiles[i] + " "; 
	}
	final Process p = Runtime.getRuntime().exec("samtools view -b -q 5 -o output.bam input.bam");
	*/

   }
     /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////// 
   
   public String callMultiCov(String[] bamFiles, String documentation)
   {
	
	try{
	// This code segment helps us to call many BAM files according to user's input.
	
	String execution = "bedtools multicov -bams";
	for(int i = 0; i < bamFiles.length; i++)
	{
		execution = execution + " " + bamFiles[i];
	}
	execution = execution + " -bed " + documentation; 
	
	countBams = new String[bamFiles.length];
	final Process multicov = Runtime.getRuntime().exec(execution);
	
             Scanner input = new Scanner(new InputStreamReader(multicov.getInputStream()));
         
              while((input.hasNextLine()))
              {     
                    // Find counts for given TE's and store them.
                    // System.out.println(input.nextLine());
		    input.next();
		    input.next();
		    input.next();
		    input.next();
                    String teName = input.next();
                    input.next();
		    input.next();
		    // You need to change this output depending on how many bam files are entered by user, which is defined as bamFiles.length !!
		    multiResult += teName + " has " + "\n"; // Enter information of TE, up down or non-de.
		    for(int i = 0; i < countBams.length; i++)
		    {
			countBams[i] = input.next();
			multiResult += countBams[i] + " counts for BAM" + (i+1) + "\n";
			
		    }

		    
              }   
        }
        catch(NoSuchElementException impossible)    
        {
		System.out.print("");
        }    
        catch(IOException impossible)
	{
		System.out.print("");
	}
        
	// For eliminating null in the start with substring.	
        int endIndex = multiResult.length();
	String finalResult = multiResult.substring(4, endIndex);
	
	System.out.println(""); 
	System.out.println("BAM FILE COUNTING RESULTS");
	System.out.println("-------------------------");
	System.out.println(finalResult); 
	return finalResult;
     }      
   

   ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

   public String[] getFromPhenoData(String phenoData)
   {
	
	int counter = 0;
	int elements = 0;
 	// Handling FileNotFoundException.
	File phenoFile = new File(phenoData);
	try{
        Scanner phenoScan = new Scanner(phenoFile);
	// This while counts how many bam Files we have from user, in phenodata.txt.
        while(phenoScan.hasNextLine())
	{	
	   counter++; 
	   phenoScan.nextLine(); 
	} 
        }catch(FileNotFoundException excep)
        {System.out.println("File is not found.");}
	
	String[] bamFiles = new String[counter];
	groupPheno = new String[counter];

	try{
        Scanner phenoFiller = new Scanner(phenoFile);
        while(phenoFiller.hasNextLine())
	{
	     bamFiles[elements] = phenoFiller.next();
	     groupPheno[elements] = phenoFiller.next();
	     elements++;
	} 
        }catch(NoSuchElementException excep)
        {}
	catch(FileNotFoundException e)
	{}

	return bamFiles;
   }
   ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
   public void calculateP(String upDownPrint)
   {
	// Here we will call some R scripts from terminal in order to calculate their p-values and later
	String teName = "";
	String up = "";
	String down = "";
	String nonde = "";
        // Draw some Barcharts and Heatmaps with the data to visualize our result in another method.
        // This method will modify updownprint.txt by adding p values for each row.
	Scanner updownprint = new Scanner(upDownPrint);
	// Need to keep a line counter and decrease 2 from it in order to obtain number of transposable elements.
	Scanner linecounter = new Scanner(upDownPrint);
	int lineCount = 0;
	while(linecounter.hasNextLine())
	{
		lineCount++;
		linecounter.nextLine();
	}
	lineCount = lineCount - 2;

	
        // Skips null, UP, DOWN and NON-DE.
	for(int skip = 0; skip < 4; skip++)
        {
		updownprint.next();
	}
	String RScript = "";
        for(int lines = 0; lines < lineCount; lines++)
        {	
                teName = updownprint.next();
  		up = updownprint.next();
		down = updownprint.next();
		nonde = updownprint.next();
		final Process callR = Runtime.getRuntime().exec(RScript);
		Scanner input = new Scanner(new InputStreamReader(callR.getInputStream()));
	}
	
        

        /**
        
	
             Scanner input = new Scanner(new InputStreamReader(multicov.getInputStream()));
         
              while((input.hasNextLine()))
              {     
                    // Find counts for given TE's and store them.
                    // System.out.println(input.nextLine());
		    input.next();
		    input.next();
		    input.next();
		    input.next();
                    String teName = input.next();
                    input.next();
		    input.next();
		    // You need to change this output depending on how many bam files are entered by user, which is defined as bamFiles.length !!
		    multiResult += teName + " has " + "\n"; // Enter information of TE, up down or non-de.
		    for(int i = 0; i < countBams.length; i++)
		    {
			countBams[i] = input.next();
			multiResult += countBams[i] + " counts for BAM" + (i+1) + "\n";
			
		    }

		    
              }   
        }
	*/
   }
   ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
   public void takeFromSql(




   ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
   // CHANGES WILL BE MADE: 
   // 1- First, we need to forget BAM files and give output of counts as a TAB-delimited text file. Later we will give
   // This file to R and start making calculations.
   // 2- MySql 84- data is necessary, still waiting to be analyzed.
   // 3- Create Github Repository for your code.
   // 4- Make your tool call by -t -u... blah blah.
   ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////	
}
