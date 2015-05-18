import numpy as np
import matplotlib.pyplot as plt
import random
import os



Partitions=list()
#Partitions.append(1)
#Partitions.append(2)
#Partitions.append(3)
Partitions.append(5)
#Partitions.append(8)
#Partitions.append(13)
TrainDataset="data/train_49.txt"
TestDataset="data/test_49.txt"
command="java -cp target/simple-project-1.0.jar Main "
# Accuracy, #Objective, #Time, Consensus Error 
outputFile = "output/Out"
MaxIterations=250
AllObjectives = []
AllConsensus = []
Accuracy=[]
TotalTime=[]
TotalHyp=[]
NonZeroHyp=[]

def collectObjectiveValues(inputFile):
    tempOutFile= "output/temporary_file"
    commandCat = 'cat ' + inputFile + ' | grep "Function Objective" >' + tempOutFile
    print commandCat
    os.system(commandCat)
    lines=[]
    with open(tempOutFile) as f:
        lines = f.readlines()
    iterationNos= list();
    objectiveValues = list();
    for line in lines:
        if(line==''):
            continue
        splitLine= line.split(' ')
        #print splitLine
        iterationNos.append(int(splitLine[2]));
        objectiveValues.append(float(splitLine[3]));

    AllObjectives.append(objectiveValues)

def collectConsensusErrors(inputFile):
    tempOutFile= "output/temporary_file"
    commandCat = 'cat ' + inputFile + ' | grep "Consensus Error" > '+ tempOutFile 
    os.system(commandCat)
    lines=[]
    with open(tempOutFile) as f:
        lines = f.readlines()
    iterationNos= list();
    consensusError = list();
    for line in lines:
        if(line==''):
            continue
        splitLine= line.split(' ')
        #print splitLine
        iterationNos.append(int(splitLine[2]));
        consensusError.append(float(splitLine[3]));

    AllConsensus.append(consensusError)

def collectTime(inputFile):
    tempOutFile= "output/temporary_file"
    commandCat = 'cat ' + inputFile + ' | grep "Total Time" > '+ tempOutFile 
    os.system(commandCat)
    lines=[]
    with open(tempOutFile) as f:
        lines = f.readlines()

    for line in lines:
        splitLine= line.split(' ')
        #print splitLine
        TotalTime.append(int(splitLine[2]));

def collectTotalNonHypothesis(inputFile):
    tempOutFile= "output/temporary_file"
    commandCat = 'cat ' + inputFile + ' | grep "Non Zero Hyp Count" > '+ tempOutFile 
    os.system(commandCat)
    lines=[]
    with open(tempOutFile) as f:
        lines = f.readlines()

    for line in lines:
        splitLine= line.split(' ')
        #print splitLine
        NonZeroHyp.append(int(splitLine[4]));

def collectTotalHypothesis(inputFile):
    tempOutFile= "output/temporary_file"
    commandCat = 'cat ' + inputFile + ' | grep "Total hypothesis Count" > '+ tempOutFile 
    os.system(commandCat)
    lines=[]
    with open(tempOutFile) as f:
        lines = f.readlines()

    for line in lines:
        splitLine= line.split(' ')
        #print splitLine
        TotalHyp.append(int(splitLine[3]));

def collectAccuracy(inputFile):
    tempOutFile= "output/temporary_file"
    commandCat = 'cat ' + inputFile + ' | grep "Acc is" > '+ tempOutFile 
    os.system(commandCat)
    lines=[]
    with open(tempOutFile) as f:
        lines = f.readlines()

    for line in lines:
        splitLine= line.split(' ')
        #print splitLine
        Accuracy.append(float(splitLine[2]));
############################################3
for partitionNo in Partitions:
    outputFileTemp = outputFile + "_" + str(partitionNo) + "_" +str(MaxIterations)
    commandTemp =command + TrainDataset + " " + TestDataset + " " + str(partitionNo) + " " + str(MaxIterations)+ " ?> " + outputFileTemp
    
    print commandTemp
    os.system(commandTemp)
    collectObjectiveValues(outputFileTemp) 
    collectConsensusErrors(outputFileTemp) 
    collectAccuracy(outputFileTemp)
    collectTime(outputFileTemp)
    collectTotalNonHypothesis(outputFileTemp)
    collectTotalHypothesis(outputFileTemp)


### finally plot all value
noOfPlots = len(AllObjectives)
iterationNos=range(0,MaxIterations+1)
#print iterationNos
for i in range(0,noOfPlots):
    plt.plot(iterationNos,AllObjectives[i],label='Actors:'+str(Partitions[i]))

plt.grid()
plt.xlabel('Number of iterations')
plt.ylabel('Objective values')
plt.legend(loc='upper right')
plt.savefig('ObjectiveValues_'+ str(MaxIterations)+'.png')
#plt.show()
plt.clf()

iterationNos=range(0,MaxIterations)
for j in range(0,noOfPlots):
    plt.plot(iterationNos,AllConsensus[j],label='Actors:'+str(Partitions[j]))

plt.grid()
plt.xlabel('Number of iterations')
plt.ylabel('Consensus Error')
plt.legend(loc='upper right')
plt.savefig('ConsensusValues_' + str(MaxIterations)+'.png')
#plt.show()
plt.clf()

print Accuracy
print Partitions
plt.scatter(Partitions,Accuracy)
plt.xlabel('No of partitions')
plt.ylabel('Accuracy')
plt.grid()
plt.savefig('Accuracy_' + str(MaxIterations)+'.png')
plt.clf()

print TotalTime
print Partitions
plt.xlabel('No of partitions')
plt.ylabel('Total Time')
plt.scatter(Partitions,TotalTime)
plt.grid()
plt.savefig('Time_' + str(MaxIterations)+'.png')
plt.clf()

print NonZeroHyp
print Partitions
plt.xlabel('No of partitions')
plt.ylabel('Total Hyp  Weight >1E-5')
plt.scatter(Partitions,NonZeroHyp)
plt.grid()
plt.savefig('NonZeroHyp_' + str(MaxIterations)+'.png')
plt.clf()

print TotalHyp
print Partitions
plt.xlabel('No of partitions')
plt.ylabel('Total Hypothesis')
plt.scatter(Partitions,TotalHyp)
plt.grid()
plt.savefig('TotalHyp_' + str(MaxIterations)+'.png')
plt.clf()
