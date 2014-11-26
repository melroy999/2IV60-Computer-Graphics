all:
	mvn package

run: all
	 java -cp "target/classes:lib/RobotRaceLibrary.jar:lib/gluegen-rt.jar:lib/jogl-all.jar" RobotRace
