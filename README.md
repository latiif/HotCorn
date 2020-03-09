**HotCorn** 

Kotlin wrapper tool to check for TV shows updates using the [Popcorntime api](https://popcornofficial.docs.apiary.io/#reference/show )

**Grab it**

Just download it from [here](https://github.com/latiif/HotCorn/releases/download/v0.7/HotCorn.jar), or via the terminal by running:

`wget https://github.com/latiif/HotCorn/releases/download/v0.7/HotCorn.jar`

**Running HotCorn**

This requires you to have `JRE` installed on your machine

Go to the folder where the `JAR` was downloaded and run:

`java -jar HotCorn.jar`

This will yield an error message because you have to provide arguments, more on this below.

**Synopsis**

`java -jar Hotcorn.jar <option flags> <unix epoch> [-epsilon <time in hours>]`

Reads from standard input a newline separated tv show name or an imdb id

*Options*
When using Hotcorn commandline tool, you first need to pass in the mandatory options flags, at least one of them must be there.
* `i` include all: This flag asks Hotcorn to print out a new line for every tvshow nomatter if it has updates or not, if a tvshow doesn't have a new update a blank line will be printed out.
* `l` get latest: This flag asks Hotcorn retrieve the latest episode of the shows regardless of the last check time.
* `t` 🆕 torrent: Prints a magnet torrent link to the latest episode. Tries to pick the highest resolution (first 1080p then 720p and finally 480p).
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
* `M` 🆕 Extracts multiple episodes of the show that were aired after the specified _Unix epoch_.

*Unix epoch*
This argument represents the timestamp at which the latest check for updates was performed.
Pass in 0 to get the latest episode.

*Epsilon*
**OPTIONAL - DEFAULT VALUE = 0** 
Due to the fact that popcorn shows are not uploaded once they are aired and that the tool looks at the first aired data to determine wheter an episode is new or not, an optional *epsilon* time _in hours_ is added to the *unix epoch* to allow for retrieval of episodes that aired *epsilon* hours ago.

*TV Shows*
For this argument you can either pass in the IMDB id for the show, or ask Hotcorn to look it up using keywords.
You can mix id's with keywords.

*NOTE* When using keywords, enclose them with double quotations.

**Examples**
#### Get latest episodes of Rick and Morty and Game Of Thrones

This examples identifies Rick and Morty by its name, and Game of Thrones by its id. It returns the episodes' Overview `O`, Show title `S` and episode number `e`.

```bash
java -jar Hotcorn.jar OSE 0 
> "rick and morty"
> tt0944947
```

This outputs _(as of early 2020)_:
```json
{
  "show_title": "Rick and Morty",
  "overview": "Lots of things in space broh. Snakes and sharp stuff. Watch this broh.",
  "episode": 5
}
{
  "show_title": "Game of Thrones",
  "overview": "In the aftermath of the devastating attack on King's Landing, Daenerys must face the survivors.",
  "episode": 6
}
```
#### Get All Latest Episodes of Vikings aired after January 1st 2020
In this example we get *ALL* episodes of Vikings aired afetr January 1st 2020 i.e. _1577842429_.

```bash
java -jar HotCorn.jar sTEM 1577842429
> 1577842429 
> Vikings
```

This outputs _(as of early 2020)_:
```json
{
  "title": "The Key",
  "episode": 5,
  "season": 6
}
{
  "title": "Death and the Serpent",
  "episode": 6,
  "season": 6
}
{
  "title": "The Ice Maiden",
  "episode": 7,
  "season": 6
}
{
  "title": "Valhalla Can Wait",
  "episode": 8,
  "season": 6
}
{
  "title": "Resurrection",
  "episode": 9,
  "season": 6
}
{
  "title": "The Best Laid Plans",
  "episode": 10,
  "season": 6
}
```

**Building from source**

In order to be able to download and use the code you have to have `git` and `maven` installed 

From within the folder you want to have the code installed run the following command

 `git clone https://github.com/latiif/HotCorn && cd HotCorn &&  mvn clean package`

This command will download the code and build it using `maven`. 

The resulting jar files will reside inside in `HotCorn/target` 

