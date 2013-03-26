call del /Q "SampleExperiment.jar" "sample_output.csv"
call scalac -classpath ..\*;..\libs\* SampleExperiment.scala -d SampleExperiment.jar
call java -classpath *;..\*;..\libs\*  SampleExperiment

