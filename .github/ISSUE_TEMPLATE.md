#### General information

* **Version:** 3.0.0
* **Device:** Xiaomi Mi A1
* **Android Version:**  Android 10 (stock)
* **Server name:** monocles.de, conversations.im, jabber.de or self hosted
* **Server software:** ejabberd 19.09.1 or prosody 0.11.3 (if known)
* **Installed server modules:** Stream Managment, CSI, MAM
* **Monocles Messenger source:** PlayStore, PlayStore Beta Channel, F-Droid, Github, Codeberg, self build (latest HEAD)


#### Steps to reproduce

1. ?
2. ?


#### Expected result

What is the expected output? 


#### Actual result

What do you see instead?


#### Debug output

Please post the output of adb logcat. The log should begin with the start of monocles chat and include all the
steps it takes to reproduce the problem.

````
Linux: adb -d logcat -v time | grep -i monocles_chat > logcat.txt
Windows: adb -d logcat -v time | FINDSTR monocles_chat > logcat.txt
````
