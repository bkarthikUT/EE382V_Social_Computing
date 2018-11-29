import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

public class SRP {
	
	int numStudents; // Total number of students
	String incrementType; // could be BASE or LEAVE or JOIN
	ArrayList<String> incrementList; // A list of students leaving or joining
	Map<String, ArrayList<String>> studentPref; // Master copy of the preference list
	Map<String, ArrayList<String>> workingPref; // Working copy of the preference list
	Map<String,String> matching;
	Map<String,String> holding;
	ArrayList<String> unhappyPool;
	int debugLevel = 0;
	
	public SRP() {
        studentPref = new HashMap<>();
        workingPref = new HashMap<>();
        holding = new HashMap<>();
        matching = new HashMap<>();
		incrementList = new ArrayList<String>();
		unhappyPool = new ArrayList<String>();
    }
	
	public void addStudentPref(int i, int j) {
		String studStr1 = new String("s"+i);
		String studStr2 = new String("s"+j);
		// Create an array list for the preferences only if one does not already exist for this student
    	if(!studentPref.containsKey(studStr1)) {
    		ArrayList<String> sList = new ArrayList<>();
    		studentPref.put(studStr1, sList);
    	}
    	studentPref.get(studStr1).add(studStr2);
    	
    	if(!workingPref.containsKey(studStr1)) {
    		ArrayList<String> sList = new ArrayList<>();
    		workingPref.put(studStr1, sList);
    	}
    	workingPref.get(studStr1).add(studStr2);
    }
	
	public void debugPrint(String comment) {
		System.out.println(comment);
		System.out.println("Student Pref : " + studentPref);
		System.out.println("Working Pref : " + workingPref);
		System.out.println("Holding : " + holding);
	}
	
	public void verifyMatching() {
		System.out.println("Verifying matching!");
		for(String key : matching.keySet())
		{
			for (String student : studentPref.keySet())
			{
				if (!student.equals(matching.get(key)) && !student.equals(key)) {
					 if (studCompare(key, student, matching.get(key)) && studCompare(student, key, matching.get(student)))
					 {
						 System.out.println("Found a Blocking pair" + key + "," + student);	
						 System.out.println(key + " prefers " + student + " over " + matching.get(key));
						 System.out.println(student + " prefers " + key + " over " + matching.get(student));
						 System.exit(1);
					 }
				}
			}
		}
	}
	
	public void findSRMatching(){
		if(debugLevel >= 2)
			debugPrint("Before round1: ");
		irvingRound1();
		for(String wpSetKey : workingPref.keySet()) {
			if (workingPref.get(wpSetKey).size() == 0)
			{
				System.out.println("Could not find a roommate for " + wpSetKey);
				System.out.println("Stable matching not possible, exiting!");
				System.exit(1);
			}
		}
		if(debugLevel >= 2)
			debugPrint("After round1: ");
		irvingRound2();
		
		for(String wpSetKey : workingPref.keySet()) {
			if (workingPref.get(wpSetKey).size() == 0)
			{
				System.out.println("Could not find a roommate for " + wpSetKey);
				System.out.println("Stable matching not possible, exiting!");
				System.exit(1);
			}
		}
		if(debugLevel >= 2)
			debugPrint("After round2: ");
		irvingRound3();
		if(debugLevel >= 2)
			debugPrint("After round3: ");
		
		// getting the matching set
		//for (int i = 1; i <= numStudents; i++) {
		
		for(String wpSetKey : workingPref.keySet()) {
			if (workingPref.get(wpSetKey).size() == 0)
			{
				System.out.println("Could not find a roommate for " + wpSetKey);
				System.out.println("Stable matching not possible, exiting!");
				System.exit(1);
			}
			matching.put(wpSetKey, workingPref.get(wpSetKey).get(0));
		}

		if(debugLevel >= 0) {
			Map<String,String> printMatching = new HashMap<>();
			for (int i = 1; i <= numStudents; i++) {
				if(!printMatching.containsKey(matching.get("s"+i))) {
					printMatching.put("s"+i, matching.get("s"+i));
				}
			}
			System.out.println("\nMatching: " + printMatching);
			if(debugLevel >= 2) 
				verifyMatching();
		}
	}
	
	public boolean studCompare(String stud, String stud1, String stud2) {
		// return true if stud1 is preferable than stud2 for stud
		if(!studentPref.containsKey(stud1)) {
			return false;
		}
		else if(!studentPref.containsKey(stud2)) {
			return true;
		}
		else {
			return studentPref.get(stud).indexOf(stud1) < studentPref.get(stud).indexOf(stud2);
		}
	}
	
	public void symmetricRemoval(String stud1, String stud2) {
		workingPref.get(stud1).remove(stud2);
		workingPref.get(stud2).remove(stud1);
		if(debugLevel >= 3) {
			debugPrint("After symmetric removal:");
		}
	}
	
	public boolean keepProposingUntilAccept(String proposer) {
		// proposer needs to recursively propose until it gets accepted
		while(!workingPref.get(proposer).isEmpty()) {
			if(propose(proposer, workingPref.get(proposer).get(0))) {
				return true;
			}
		}
		
		// current holding's preference list became empty
		System.out.println("Could not find a roommate for " + proposer);
		System.out.println("Exiting as stable matching not possible");
		System.exit(1); // TBD - Change this to an exception 
		return false;
	}
	
	/* returns true when stud2 accepts stud1's proposal
	   returns false when stud2 rejects stud1's proposal */
	public boolean propose(String stud1, String stud2) {
		if(debugLevel >= 3) {
			System.out.println(stud1 + " is proposing to " + stud2);
		}
		// if stud2 is free accepts stud1 and returns true
		if(!holding.containsKey(stud2)) {
			holding.put(stud2, stud1);
			if(debugLevel >= 3) {
				System.out.println(stud2 + " accepts " + stud1);
				System.out.println("Holding : " + holding);
			}
			return true;
		} else{
			// stud2 is not free, check if current holding is better than stud1
			if(studCompare(stud2, holding.get(stud2), stud1)) {
				// stud2's current holding is better than stud1
				if(debugLevel >= 3) {
					System.out.println(stud2 + " rejects " + stud1 + " for " + holding.get(stud2) + " because" + stud2 + "'s current holding is better than" + stud1);
				}
				symmetricRemoval(stud1, stud2);
				return false;
			}
			else {
				// stud1 is better than stud2's current holding
				String currHolding = holding.get(stud2);
				// stud2 accepts stud1's proposal and rejects current holding
				if(debugLevel >= 3) {
					System.out.println(stud2 + " rejects " + holding.get(stud2) + " for " + stud1 + " because " + stud1 + " is better than " + stud2 + "'s current holding");
				}
				holding.put(stud2, stud1);
				// symmetrically remove stud2, current holding from their preference lists
				symmetricRemoval(currHolding, stud2);
				// current holding needs to recursively propose until it gets accepted
				return keepProposingUntilAccept(currHolding);
			}
		}
	}
	
	public void irvingRound1() {
		// each student proposes until it gets accepted
		for(String proposer : workingPref.keySet()) {
			keepProposingUntilAccept(proposer);
		}
		
		// Expectation at end of round1 is that everyone holds a proposal
		assert(holding.size() == numStudents);
	}
	
	public void irvingRound2() {
		/* for each student holding a proposal, symmetrically remove 
			everyone below his proposer in his preference list */
		for(String holder : holding.keySet()) {
			String myProposer = holding.get(holder);
			int index = workingPref.get(holder).indexOf(myProposer);
			if(debugLevel >= 3) {
				System.out.println("I am " + holder + " removing all below my proposer " + myProposer);
			}
			int purgeCount = workingPref.get(holder).size() - index - 1;
			for (int i = 1; i <= purgeCount; i++) {
				if(debugLevel >= 3) {
					System.out.println("I am " + holder + " removing " + workingPref.get(holder).get(workingPref.get(holder).size()-1));
				}
				symmetricRemoval(holder, workingPref.get(holder).get(workingPref.get(holder).size()-1));
			}
		}
	}

	public void irvingRound3() {
		// For each student in the preference list, find and remove cycles
		for(String student : workingPref.keySet()) {
			ArrayList<String> p = new ArrayList<>();
			ArrayList<String> q = new ArrayList<>();
			String currP = student;
			while(workingPref.get(currP).size() > 1) {
				String currQ = workingPref.get(currP).get(1);
				p.add(currP);
				q.add(currQ);
				String nextP = workingPref.get(currQ).get(workingPref.get(currQ).size() - 1);
				
				if(p.contains(nextP)) {
					// we found a cycle
					p.add(nextP);
					// symmetrically remove q[i] and p[i+1]
					for (int i = 0; i < q.size(); i++) {
						symmetricRemoval(q.get(i), p.get(i+1));
					}
					// for the same student there could be more cycles?
					p.clear();
					q.clear();
					nextP = student;
				}
				currP = nextP;
			}
		}
	}
	
	/*
	 * For every student that is being added to the unhappy pool,
	 * check if he is more preferred over their current match for every existing matching
	 * If so, break such pairs and invoke the same function again with these students who are broken up
	 */
	public void addToUnhappyPool(String student) {
		if(debugLevel >= 2)
			System.out.println("I am adding " + student + " to unhappy pool");
		unhappyPool.add(student);
		for(String studentMatchKey : matching.keySet()) {
			String studentMatchValue = matching.get(studentMatchKey);	
			if(debugLevel >= 3) {
				System.out.println("StudentMatchkey is "+ studentMatchKey);
				System.out.println("StudentMatchValue is "+ studentMatchValue);
				System.out.println("matching:" + matching);
			}
			
			if(studentPref.containsKey(studentMatchKey) && studentPref.containsKey(studentMatchValue) && !matching.get(studentMatchKey).equals("X")) {
				if(debugLevel >= 3) 
					System.out.println("Comparing " + student + " to " + studentMatchValue + " for " + studentMatchKey);
				if(studCompare(studentMatchKey, student, studentMatchValue) || studCompare(matching.get(studentMatchKey), student, studentMatchKey)) {
					if(debugLevel >= 2)
						System.out.println("The " + student + "is breaking the pair " + studentMatchKey + " , " + studentMatchValue);
					matching.put(studentMatchKey, "X");
					matching.put(studentMatchValue, "X");
					addToUnhappyPool(studentMatchKey);
					addToUnhappyPool(studentMatchValue);
				}
			}
		}
	}
	
	public void initWorkingPref(boolean incremental) {
		if(debugLevel >= 1) {
			System.out.println("Type: " + incrementType + " Students: " + incrementList);
			System.out.println("Student Pref: " + studentPref);
		}
			
		// If we are running using naive Irving for each incremental setting or base case,
		// copy studentPref over to workingPref
		if(!incremental || incrementType.equals("BASE")) {
			return;
		}
		// If new people joining, add each of them to the unhappyPool
		// including others in matching who he may be breaking up
		if(incrementType.equals("JOIN")) {
			for(String stud : incrementList) {
				addToUnhappyPool(stud);
			}	
		} else if(incrementType.equals("LEAVE")){
			/* for each student that leaves, if his partner is not leaving
			add his partner to unhappy pool*/
			for(String stud : incrementList) {
				String myMatch = matching.get(stud);
				matching.remove(stud);
				matching.remove(myMatch);
				if(!incrementList.contains(myMatch)) {
					addToUnhappyPool(myMatch);
				}
			}
		} 
		
		// Update the workingPref data structure by removing the happy couples
		ArrayList<String> myKeySet = new ArrayList<>(matching.keySet());
		for(String studMatchKey : myKeySet) {
			if(matching.get(studMatchKey).equals("X")) {
				matching.remove(studMatchKey);
				continue;
			}
			workingPref.remove(matching.get(studMatchKey));
			workingPref.remove(studMatchKey);
		}
		
		for( String wpSetKey : workingPref.keySet()) {
			workingPref.get(wpSetKey).removeAll(matching.keySet());
		}
		if(debugLevel >= 1)
			System.out.println("Happy Couples: " + matching);
	}
	
	public void readInput(String fileName) throws IOException {
		int n, k, diffStudents;
		String str;
		String st[] = new String[100];
		ArrayList<String> input = new ArrayList<String>();
		
		File file = new File(fileName); 
		BufferedReader br = new BufferedReader(new FileReader(file));
		
		while ((str = br.readLine()) != null) {
			st = str.split(" ");
			for(int s=0; s<st.length; s++)
				input.add(st[s]);
		}
		
		n = Integer.parseInt(input.get(0));
		incrementType = input.get(1);
		// IncrementType can be BASE, new students joining (JOIN) or some students leaving (LEAVE)
		if(incrementType.equals("BASE")) {
			diffStudents = 0;
		} else {
			// Find the number of students joining or number of students leaving
			diffStudents = Math.abs(numStudents - n);
			// clearing all data structures except matching for incremental runs
			holding.clear();
			workingPref.clear();
			studentPref.clear();
			incrementList.clear();
			unhappyPool.clear();
		}
		numStudents = n;
		
		for (int j=2; j<diffStudents+2; j++) {
			incrementList.add("s"+input.get(j));
		}
		
		k = diffStudents + 2;
		
		// Update the studentPref data structure with the updated preference list
		while(k<input.size()) {
			for(int i=1; i<=n; i++) {
				for (int j=1; j<n; j++) {
					addStudentPref(i, Integer.parseInt(input.get(k++)));
				}
			}
		}	
		br.close();
	}
	
	/* Invoke main using the following format:
	 * java srp <InputFile1> <InputFile2>...
	 * Example: java srp large_100_A.txt large_100_B.txt
	 */
	public static void main (String args[]) throws Exception
	{
		boolean incremental = true;
		SRP srp = new SRP();
		
		/* 
		 * If incremental is true, we use our incremental algorithm, 
		 * else we use Irving's algorithm from scratch for the same input
		 */
		
		// Get the input for base and incremental settings
		@SuppressWarnings("resource")
		Scanner userInput = new Scanner(System.in);
		long super_total_time=0;
		for(int i = 0; i < args.length; i++){
			/* if(i > 0) {
				System.out.println("Press any key to proceed further with next incremental setting " + args[i]);
				userInput.nextLine();
			}*/
			srp.readInput(args[i]);
			
			long startTime=System.nanoTime();
			// Construct the new working preference list 
			srp.initWorkingPref(incremental);
			// Find the Stable Roommate matching
			srp.findSRMatching();
			long endTime=System.nanoTime();
			
			if(i > 0) {
				long totalTime=endTime-startTime;
				super_total_time = super_total_time + totalTime;
				System.out.println("\nTotal time taken for this round of SRP is " + totalTime + " Cumulative time is " + super_total_time + "\n");
			}
		}
	}
}
