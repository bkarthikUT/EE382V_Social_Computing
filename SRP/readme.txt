How to run the stable roommate incremental setting implementation:

1) Modify the run.sh with the correct input file names and execute run.sh

OR

2) Run using the following command - java srp <InputFile1> <InputFile2>...

Where, InputFile1 is the base input. InputFile2, InputFile3 are incremental updates with reference to the previous input file.

Example: java srp <file_path>/large_100_A.txt <file_path>/large_100_B.txt

Input file format:
<number of students>
<Increment Type (BASE/JOIN/LEAVE)> <Student1 (joins or leaves)> <Student2 (joins or leaves)>...
<Preference Matrix>

Example:
small_6_1A.txt -
6
BASE
4 6 2 5 3
6 3 5 1 4
4 5 1 6 2
2 6 5 1 3
4 2 3 6 1
5 1 4 2 3

small_6_1B.txt -
8
JOIN 7 8
4 6 2 5 3 7 8
6 3 5 1 7 8 4
4 5 1 6 2 8 7
2 6 5 8 1 7 3
4 2 7 3 6 8 1
5 1 4 8 7 2 3
8 4 6 1 2 5 3
6 7 3 2 1 4 5
