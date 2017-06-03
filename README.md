# Image Recognize Desktop ![build passing](https://travis-ci.org/GVCSystems/image_recognize_desktop.svg?branch=master)
This is small application for desktop to recognize text from Image. Webcam or USB camera will be needed to capture Images.

## Getting Started
These instructions will get you a copy of the project up and running on your local machine.

### Prerequisites
Download below tools if you don't have them


[Java Development Kit](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)

[Maven](https://maven.apache.org/download.cgi)

### Compiling
Download the repo using <code>git</code> or from the Website as a Zip. Go to the root directory of the project.

<pre>
mvn clean package
</pre>
Stay Calm, it will take several minutes if you are running it for first time.

You will get an executable JAR file in <code>deployments</code> folder in same directory.

### Running
If you are on *nix system then
<pre>
java -jar deployments/image.recognize-1.0.jar
</pre>
