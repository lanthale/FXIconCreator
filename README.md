# FXIconcreator
Small app to create icon sets (multi resolution) for Linux, Windows, OSX from a single PNG image

Reason for creating such an app was that I had to create app icons out of png files but I was tired to always fire up different virtual machine only to use the native tools to create the icons. All web based icon converter are converting the icons in the wrong format (not as multi resolution).

All the credit goes to the following opensource projects:
- Javafx https://openjfx.io/
- iKonli https://github.com/kordamp/ikonli
- ICNS #gino0631 https://github.com/gino0631/icns
- ICO #imcdonagh https://github.com/imcdonagh/image4j

# Usage notes
- Windows: Execute the installer. If defender is poping up please click on "more info" and then on "run anyway"
- OSX: Goto in the download folder in Finder and right click on the installer file and click "Open". On OSX 11.x open the app and afterwards goto system settings - general tab in the Security & Privacy pane to instruct macOS to ignore its lack of notarization - click on "open anyway". An alternative way is to execute the following cmd on the terminal 'sudo xattr -r -d com.apple.quarantine ~/Applications/FXIconCreator.app'
- Linux:
Download the deb package
Run sudo dpkg -i fxiconcreator_1.0.0-1_amd64.deb
Click on the icon or run /opt/fxiconcreator/bin/FXIconCreator

#Build the app
You can build the app via cmd "mvn clean install" inside of the directory where the pom.xml file exists
