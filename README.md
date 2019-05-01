**HotCorn** 

Kotlin wrapper tool to check for TV shows updates using the [Popcorntime api](https://popcornofficial.docs.apiary.io/#reference/show )

**Usage**

In order to be able to download and use the code you have to have `git` and `maven` installed 

**Downloading the code**

From within the folder you want to have the code installed run the following command

 `git clone https://github.com/llusx/HotCorn && cd HotCorn &&  mvn clean package`

This command will download the code and build it using `maven`. 

The resulting jar files will reside inside in `HotCorn/target` 

**Running HotCorn**

This requires you to have `JRE` installed on your machine

Within the target folder run:

`java -jar HotCorn-0.6-jar-with-dependencies.jar`

This will yield an error message because you have to provide arguments, more on this later

**Synopsis**

`java -jar Hotcorn.jar <option flags> <unix epoch> [-epsilon <time in hours>] "<tv show name or imdb code>"[<tv show name or imdb code> ...]`

*Options*
When using Hotcorn commandline tool, you first need to pass in the mandatory options flags, at least one of them must be there.
* `i` include all: This flag asks Hotcorn to print out a new line for every tvshow nomatter if it has updates or not, if a tvshow doesn't have a new update a blank line will be printed out.
* `l` get latest: This flag asks Hotcorn retrieve the latest episode of the shows regardless of the last check time.
* `t` torrent: Prints a magnet torrent link to the latest episode (usually the best one).
* `D` Downloads: Prints an array of all torrent magnet links.
* `P` First-aired Epoch: Prints the unixtime when the episode was first aired.
* `F` First-aired: Prints the human date when the episode was first aired.
* `O` Overview: Prints an overview of the episode.
* `T` Title: Prints the title of the episode.
* `E` Episode: Prints the number of the episode.
* `S` Show title: Prints the title of the show.
* `s` Season: Prints the season of the episode.
* `A` All: Prints all the information.
* `c` Print CSV: Prints the information in form of CSV file for easier manipulation
* `e` Print episode id from tvdb.

*Unix epoch*
This argument represents the timestamp at which the latest check for updates was performed.
Pass in 0 to get the latest episode.

*Epsilon*
**OPTIONAL - DEFAULT VALUE = 0** 
Due to the fact that popcorn shows are not uploaded once they are aired and that the tool looks at the first aired data to determine wheter an episode is new or not, an optional *epsilon* time _in hours_ is added to the *unix epoch* to allow for retrieval of episodes that aired *epsilon* hours ago.

*TV Shows*
For this argument you can either pass in the IMDB id for the show, or ask Hotcorn to look it up using keywords.
You can mix id's with keywords.

*NOTE* When using keywords, enclose them with double quotations and use _ instead of spaces.

**Examples**
`java -jar Hotcorn.jar OSE 0 "rick_and_morty" tt0944947`
This outputs:
`{"overview":"Rick goes on a confrontation with the President.","episode":10,"season":3}`
`{"overview":"House Lannister, Stark and Targaryen meet at the Dragonpit and negotiate the future of Westeros  ...","episode":7,"season":7}`



