Steps to use:
1. Open a terminal and cd into the project folder
2. cd into the `src/` folder of the project
3. compile all the files using 
`javac error/*.java objects/*.java util/*.java validation/*.java *.java`
4. Start the receiver with `java Receiver 8080 new_image.png` in the same folder
5. Open another terminal and start the sender with `java Sender localhost 8080 image.png`

With optional settings on the Sender start up enter this command 
`java Sender -d 0.25 -s 464 -t 15 localhost 8080 image.png`

`java Sender -d 0.25 -s 465 localhost 8080 image.png`
