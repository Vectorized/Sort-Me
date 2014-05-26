Sort Me
=======

[Download Sort Me on Google Play]

Sorting is an important operation used extensively by computers in our everyday lives. However, we often take it for granted and hardly notice it in action.

SortMe! is a game to let us appreaciate the beauty of computer sorting... by challenging your friends to see who can sort the most tiles within the time limit!!!

This projected started as an assignment for the 50.003 Elements of Software Construction course in [Singapore University of Technology and Design] (SUTD).

We would like to thank our awesome teachers in [Information Systems Technology and Design] (ISTD) for their guidance and giving us the idea for this app. 

Importing of Game Code
----------------------

We recommend reading the code on an IDE with syntax highlighting for the best viewing experience.

For the game code, the following projects under the Game Code folder are recommended to be imported:
- SortMe                   (under Game Code)
- google-play-services_lib (under Game Code)
- BaseGameUtils            (under Game Code/libraries)

Dependencies:
SortMe <- google-play-services_lib <- BaseGameUtils


Importing of Testing Code
-------------------------

For the testing code, each test project comes paired with another project that contains copies of the game code tested.

It is recommended that you import all the projects:
- SortMeConcurrency      (under Testing Code)
- SortMeConcurrencyTest  (under Testing Code)
- SortMeGameScreen       (under Testing Code)
- SortMeGameScreenTest   (under Testing Code)
- SortMeHelpersLogic     (under Testing Code)
- SortMeHelpersLogicTest (under Testing Code)

Dependencies:
SortMeConcurrencyTest  <- SortMeConcurrency
SortMeGameScreenTest   <- SortMeGameScreen
SortMeHelpersLogicTest <- SortMeHelpersLogic

Trying out the Game
-------------------

It is highly recommended that you test our app using the published edition at http://tiny.cc/sortme

Compiling the game requires signing it with a certificate that matches the application signature registered in the Google Game Services portal. 

To protect the integrity of our game and other applications signed with the same key, we cannot give out the signing key.  

If there is a requirement to compile the project for verification purpose or anything, please send me a email on benjamin_kang@mymail.sutd.edu.sg, or drop me a contact message using the contact page http://tiny.cc/sortme.

Dependencies
------------

RenderScript Dependencies:

The following projects are dependent on an Android
support library called RenderScript, used for the blurring effect:
- SortMe           (under Game Code)
- SortMeGameScreen (under Testing Code)

If the project loses its link to the RenderScript library, please add the renderscript-v8.jar under RenderScript Library to the build path of the projects.

- Right click the project in the Package Explorer -> Build Path -> Configure Build Path
- Under the "Libraries" Tab, click "Add External JARs", and locate the renderscript-v8.jar
- The in the "Order and Export" Tab, check the renderscript-v8.jar.

Version
-------
1.0.0.5

License
-------

GNU General Public License

[Download Sort Me on Google Play]:https://play.google.com/store/apps/details?id=com.vengestudios.sortme
[Singapore University of Technology and Design]:http://sutd.edu.sg
[Information Systems Technology and Design]:http://istd.sutd.edu.sg/faculty/
