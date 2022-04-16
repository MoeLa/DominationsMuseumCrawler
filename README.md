
## Dominations Museum Crawler
Are you collecting more and more Mysterious Fragments? Are you too lazy to spend a couple of hours on crafting Artifacts? Have you lost (or never gained) an overview about the different skills and categories? Have you disregarded the Museum for too long? Do you play on an Android device?

<p align="center">
<b>Here is the solution!</b>
</p>

### Prerequisites
 - An Android device <ins>with Root</ins>
 - Download and install:
 [![](https://img.shields.io/badge/Dominations%20Museum%20Crawler-v1.0.2-blue)](https://github.com/MoeLa/DominationsMuseumCrawler/releases/download/1.0.2/app-debug.apk)

### How Does The Museum Crawler Work?
The crawler automates the hard and repetitive work of tapping 'craft 5 artifacts' > hurring the animation > decide to keep/sell the item > repeat four times > start all over again.
By automating I really mean simulating the taps you should have to do. There is no hacking the Dominations app, no background calls to any BHG server, no advantage to doing those steps manually.
After having set up the crawler, you start the crafting, put your mobile aside for a couple of minutes, and use your time in a more senseful way. The crawer taps a button, takes a screenshot, reads the content via OCR to decide its next step, performs the next tap and so on.

### First Steps
 1. Install the .apk. Note: You'll be asked to trust this app from an unknown source.
 2. Start the app. You'll be asked to grant root privileges.  
 Note: Without root permissions, this app is completely useless and you can directly uninstall it. See [Why Is Root Needed](#why-is-root-needed), if you're unsure about that topic. Further, please ensure, that there is no toast or any other visual notification, each time a root action is performed.
 3. Open SETTINGS by tapping the rack-wheel at the top right corner.
 4. Check/note the language of Museum Crawler.
 5. Go back to the main screen and tap the START button next to the SETTINGS icon.  
 If you haven't given permission to 'display over other apps', a snackbar at the bottom informs you about that and provides a button to open the android settings.
 6. Enable that permission for Museum Crawler and return by tapping BACK -> A red circle shall appear on the top left of the screen.  
 Note: You can drag that icon to a different position, if desired, but it must not hide anything important for crafting Artifacts..
 7. Open Dominations. Ensure, that Dominations runs on the same language as the Museum Crawler. If not, change either's language.
 8.  Switch into the Museum's Artifact Crafting screen. You should now see the button to start crafting five artifacts for 475 fragments and of course our red overlay icon.  
 Note: If the first craft is cheaper (since you've researched that skill in the Library), you have to tap that button manually before continuing to the next step.
 8. Tap the red overlay icon. It should turn green and start crafting. Every item is sold directly. After the five items, 475 fragments are spent again to craft another round of artifacts.
 9. Stop the process any time by pressing the green overlay icon. It should turn back to red.

### Second steps

 1. Long-tap the red overlay icon. The Museum Crawler opens and the icon disappears.
 2. If you're in Space Age (or higher) and have researched Archaeology level 5 in the Library, you should [enable 'Keep 3* artifacts'](#why-should-i-keep-3-artifacts) in the SETTINGS.
 3. Tap the PLUS button next to the START button to create a new rule.
 4. Provide a signifant name, select a category, select '1 of 5' amount of matches, and add some optional skills. Tipp: For testing purposes, you should add quite a lot skills.
 5. Tap the SAVE button. The rule appears in the list in the main screen.
 6. Start the service and then the crafting again.
 7. Watch the process. If an item is crafted, that matches your rule(s) or has level 3, it is kept. All other items are sold directly.
 8. Stop the process of crafting and create further rules. You can also edit and delete existing ones.

### How Do Rules Work?
Each crafted artifact is checked against all rules. If a rule describes the artifact, it is kept. If no rule applies, it is sold.
The name of a rule is completely irrelevant for the decision to keep/sell an artifact. You should choose a significant and meaningful name to distinguish your rules.
The meaning of 'Category' should be obvious.
'Amount Matches' determines, how many of the below selected skills an artifact must have to be kept. Example: A rule with optional skills Fighter Damage and Fighter Hitpoints plus '4 of 5' matches a weapon artifact with each twice those skills and a random fifth one.
The selection of skills applies to the selected category. Note:
 * If an artifact shall have one skill twice, add that skill twice to _mandatory skills_.
 * See [Known Bugs/Issues](#known-bugsissues).

### Why Is Root Needed?
As you might have already noticed, the Museum Crawler simulates real taps. For good (security) reasons, you cannot do that without full control of the device. So you either trust me that the crawler doesn't misuse that power or you should not use this app! Those of you that are more or less fluent in Java, can take a look at the source code.
Further, root is used to take the screenshots during crawling. There might by a way to do this without root privileges, but I was too lazy to check that out and since root is already available... you know ;-)

### Why Should I Keep 3* Artifacts?
I'm always short of Blueprints. To maximize the Blueprints outcome, you should upgrade each of the three skills to level 2 of any artifact before selling it. Thus, I implemented an option to keep those artifacts.

Notes:
* If you're short on oil, you can select to only keep artifacts, that can be upgraded with food and gold. Meaning: Three star war equiment is sold directly, while all other three star artifacts are kept.
* Upgrading and selling of those artifacts is not done by the crawler.

### Known Bugs/Issues
 - Do not rotate the phone while the crawling is running. The app is not robust enough to handle that.
 - If you take too long to grant (or revoke) root when promted, the app might hang up. Just wait for Android to offer you to kill it and take that offer. Then, start the Museum Crawler again.
 - The language of the game and the Museum Crawler must be the same. Otherwise, when starting the crafting, just the '475' fragments button is tapped and the crawler is caught in a loop.
 - In the first loop, some coordinates of buttons are gathered. Don't become impatient. Give the app a couple (like ten) seconds to perform the next tap.
 - There is no check for enough storage space or fragments. If you run out of either, you should stop the process. Otherwise you risk spending crowns!
 - One of the later DomiNation versions introduced more shelves in the library allowing to lower the crafting costs once a day. That change is **not** yet considered, meaning: If the button to start crafting shows less than 475 fragments, tap it manually and start the bot afterwards.
 - The screen is captured and parsed several times to determine the next step/tap. If the amount exceeds 4 and a decision could not be made, the bot stops and the icon turns orange. Just tap it once to continue, when you think it could.
 - A keep rule's _amount matches_ is not validated against its mandatory and optional skills. You won't be informed, if those attributes exclude each other.
 - The same applies to mandatory and optional skills: You can select a skill twice as mandatory and additionally as optional, resulting effectivly in that optional skill being considered void.

### How To Contribute
Just file a pull request ;-)
For those of you that are not that familiar with Git and Java development and just want to provide a translation to a new language, do as follows:
 1. Download strings.xml from app\src\main\res\values and translate the texts to your language
 2. Open a new issue and we'll discuss everything there

 If you're a developer and happen to know a faster way to take a screenshot than my code currently does, you could really speed things up.
