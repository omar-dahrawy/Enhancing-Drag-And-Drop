# Enhancing-Drag-And-Drop

This project is a prototype of a proposed system that enhances drag-and-drop operations on pointer-input based computers. 
Background and implementation details can be found in this [paper] (https://drive.google.com/open?id=1mnbdkZ_elqNBY4KdbrXUcjQY2XHtSv6S) .

## Description

This project uses machine learning to help organize files during drag-and-drop operations in order to make these operations easier and more efficient. It's based on TensorFlow's image classification library, but could be modified in the future to use object detection instead (e.g YOLO real-time object detection system). The model is trainend on a variety of classes, which can be found in the labels.txt file in the Data folder described below. The graph.pb file in the Data folder is the model it uses to classify images. You can train your own model using the tutorial on TensorFlow's website, and replace the labels.txt and graph.pb in the Data folder in order to use your new model.

## How to setup

To run this project, simply clone the repo and add the src files to a new Java project. Don't forget to edit the package name. Then add the two jar files in the External Libraries folder to your project. You will also need to download the Data folder and add it to your project: https://drive.google.com/open?id=1JfKgRozt1ZPRfaNhgnFrCSQLZPvSD7KQ

## How to run

In order to be able to run the project, first you have to create a folder with any desired name and destination, then add the folder's path to the Recognizer.java file at line 46 inside destinationPath String instead of the written path. This folder will be used as the destination to which you will paste the coppied/cut items, since there are no libraries for Java that can recognize open directories and access them. However, the original project idea was to detect the open folder (or foreground folder) and use it as paste destination. For full documentation, make sure to check out the paper on this project: https://drive.google.com/open?id=1mnbdkZ_elqNBY4KdbrXUcjQY2XHtSv6S

To run the program, simply run the Recognizer.java class. Once run, switch to your desktop and then you can start dragging any files to see the program working. The program only works on images currently (jpg, jpeg, or png formats) but that could be improved later. After dragging, you can press Ctrl + U (whether on Mac or Windows/Linux) in order to paste the copied/cut item in the folder that you created.
