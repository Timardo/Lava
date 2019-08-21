# !!DISCLAIMER!!
### This is a test-implementation of Lava, do not refer to the text below the bucket image!

This implementation aims for high core-mod compatibility and the least amount of lines in minecraft patches. If you want to contribute, be sure to follow rules below:
```
-> original code refers to anything inside net.minecraft and net.minecraftforge packages
- do not format the original code, never, you can format the inserted code and code outside net.mine** package as you will though
- keep track of original code, do not delete it, just comment it out
- do not delete unused original code like local vars, imports...
- do not add imports in original code, use full class names if they're not already imported
- if you want to add CB/Paper patch that doesn't interfere with server's intended logic (remove useless logger warn, make condition shorter but still with same effect/logic..) don't
- mark EVERY single change with its origin of implementation through comments eg. '//Craftbukkit start - event', '//Paper - field' '//Lava-test - move code to event factory'...
- don't add paper's obf helper methods, they can be mapped with no side effects
- if you can move some code outside of it's execution class in original code like events or bukkit handlers, do it, in a clean way ofc
- avoid adding local vars in original code
- ALWAYS move methods/fields from patches (CraftBukkit's getters, Paper's fields..) at the end of file to avoid potential problems with index-based field reflection getters
- do not edit client-only original code, find a way to bypass it
- don't touch Side.java and SideOnly.java files because of patches, they are broken, just ignore them
- try to use forge enhanced methods for server manipulation in the API implementations (world unloading, teleport events)
- if there is a TODO, which is present in latest Paper implementation/API, remove it, support on 1.12.2 version has been dropped
- if there is a patch altering constructor/method arguments, keep the original one too
```
Current goal: go through all patches and make them as small as possible

Future goals: add Paper API, add Paper patches, move ALL events to the handler class, add wrappers for all bukkit types - somehow, replace patches system with ASM injections/SpongePowered mixins

More info: Damaging, teleporting and world loading logic is a mess, if you have problems with it, don't expect a fix anytime soon
           Any issues report on THIS fork's Issues tab, not on original Lava issue tracker nor its GitHub!

I will probably PR this to Lava anyway
# Lava
<p align="center">
  <img width="200" height="256" src="https://cdn.discordapp.com/attachments/363849042586763274/591474785356349450/Lava.png">
</p>

![](https://ci.codemc.org/buildStatus/icon?job=LavaPowered%2FLava&style=flat-square)
![](https://img.shields.io/github/last-commit/LavaPowered/Lava.svg?style=popout-square)
![](https://img.shields.io/github/stars/LavaPowered/Lava.svg?label=Stars&style=popout-square)
[![](https://img.shields.io/discord/558776046166474773.svg?label=Join%20us%20on%20Discord&style=popout-square)](https://discord.gg/QuEhEXY)
![](https://img.shields.io/github/license/LavaPowered/Lava.svg?style=popout-square)

## What's Lava?
Lava is a minecraft server implementation for Forge mods and Spigot mods for 1.12.2. Sponge has come out for this type of implementation, but most features are not available or are simply not able to be done with their API. This also will be constantly updated when Forge or Spigot is updated. We also will not have game-breaking issues much like this type of implementation's 1.7.10 counterpart.

We hope to eliminate all issues with craftbukkit forge servers. In the end, we envision a seamless, low lag Spigot and Forge experience.

Advantages over Sponge or other implementations:
+ Lag-lowering optimizations
+ Better world protection (Forge stuff doesn't bypass Bukkit plugins but rather works with it!)
+ Full use of **all** Spigot plugins and Forge mods (*Disclaimer the plugin/mod creator may have to make a version specifically for Lava for it to work*)
+ Hyperthreaded performance
+ Use of Mixin - Unlike other impl.

### What doesn't work / needs to be fixed?
*If you find a plugin or mod that doesn't work, please open an issue or let us know on the discord*
- Cross version Spigot plugins using NMS (*Probably wont get fixed for a while*)
- Some NMS properties may not be mapped correctly, thus unable to be accessed normally
- Quark and some other mods that heavily modify the vanilla code

## Downloads
You can download pre-compiled jars [here](https://ci.codemc.org/job/LavaPowered/job/Lava/)

**Lava is still in beta and you may encounter issues in using it with your server. Please report any issues to our issues tab at the top of this page**

> Note: **PLEASE** look at the release notes before downloading! :wink:

## API Usage - **Work in progress**
### ***WIP***

## Chat

Feel free to drop in on the [LavaPowered Discord](https://discord.gg/QuEhEXY).

## Donate/Support

You can pledge to support GMatrixGames and his team's work through a one-time [PayPal](http://paypal.me/GMatrixCodes) donation.

## Build Requirements
* Java 8u222 JDK or higher *(Tested using AdoptOpenJDK 8u222 with Eclipse OpenJ9 VM)*

## Build Instructions
* Clone Project
    * You can use an IDE or clone from a terminal
    `git clone https://github.com/LavaPowered/Lava`
* Setup
    `git submodule update --init --recursive`
* Build
    * Linux / Git Bash / MacOS
    `./gradlew launch4j`
    * Windows
    `.\gradlew.bat launch4j`

All builds are available in `build/distributions`

## Updating local repository

* Pull from source
    * `git pull origin`
* Reapply patches & build bianaries
    * Linux / Git Bash / MacOS
    `./gradlew clean launch4j`
    * Windows
    `.\gradlew.bat clean launch4j`