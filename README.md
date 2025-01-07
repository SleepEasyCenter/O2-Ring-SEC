Documentation on SDK here: https://github.com/viatom-develop/LepuDemo
Overview
Development Environment used:
Android Studio

	Programming Language used:
Kotlin(base language of the project)
Java(used sparingly but can be used in conjunction to Kotlin)
	
	Project base:
LepuBle(source): LepuBle Demo
LepuBle Overview(Focusing only on OxyRing)
	Main Activity
needPermission(kicks start the whole bluetooth process)
initEventBus(switches to the corresponding activity script for the product detected


	Oxy Activity
oxyGetInfo
batteryState : 0 (no charge), 1 (charging), 2 (charging complete)
batteryValue : 0-100
oxiThr(Oxygen Threshold) : 80-95
motor : KidsO2、Oxylink（0-5：MIN，5-10：LOW，10-17：MID，17-22：HIGH，22-35：MAX）, O2Ring（0-20：MIN，20-40：LOW，40-60：MID，60-80：HIGH，80-100：MAX）
workMode : 0 (Sleep Mode: Ring is not tracking data), 1 (Monitor Mode: Ring is tracking data)
oxiSwitch(Oxygen buzzing) :
0 (off), 1 (on)-----just sound or vibration
0 (sound off, vibration off), 1 (sound off, vibration on), 2 (sound on, vibration off), 3 (sound on, vibration on)-----sound and vibration
hrSwitch (Heart rate buzzing):
0 (off), 1 (on)-----just sound or vibration
0 (sound off, vibration off), 1 (sound off, vibration on), 2 (sound on, vibration off), 3 (sound on, vibration on)-----sound and vibration
hrLowThr : 30-250
hrHighThr : 30-250
curState : 0 (preparing), 1 (is ready), 2 (measuring)
lightingMode : 0-2 (0 : Standard Mode, 1 : Always Off Mode, 2 : Always On Mode)
lightStr : light level, 0-2
buzzer : checkO2Plus (0-20 : MIN, 20-40 : LOW, 40-60 : MID, 60-80 : HIGH, 80-100 : MAX)
oxyReadFile
operationMode : 0 (Sleep Mode), 1 (Minitor Mode)
size : Total bytes of this data file package
asleepTime(session duration) : Reserved for total sleep time future
avgSpo2 : Average blood oxygen saturation
minSpo2 : Minimum blood oxygen saturation
dropsTimes3Percent(drops below the baseline) : drops below baseline - 3
dropsTimes4Percent : drops below baseline - 4 asleepTimePercent : T90 = (<90% duration time) / (total recording time) *100%
durationTime90Percent : Duration time when SpO2 lower than 90%
dropsTimes90Percent : Reserved for drop times when SpO2 lower than 90%
o2Score : Range: 0-100（For range 0-10, should be (O2 Score) / 10）
stepCounter : Total steps

Custom Scripts

	FileUtils
This compiles the oxydata into a csv(the session tracking not the average).
Initiate download(passes in oxyFile object to then extract the data from the object to use in the CSV)

	GraphManager
		Attempts to generate a graph based on the real time data
