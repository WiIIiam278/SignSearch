# SignSearch
[![Discord](https://img.shields.io/discord/818135932103557162?color=7289da&logo=discord)](https://discord.gg/tVYhJfyDWG)
**SignSearch** is a simple plugin that lets you asynchronously search nearby signs for text.

## The Problem
Last year, the server I played on had a Christmas event in which we all left gifts under a tree with chests marked with the recipient. The problem? We had hundreds of chests with hundreds of signs, leading players to frustratingly trawl through hundreds of chests to find their gifts. Many players complained that they wished we had "that handy shop chest search feature from QuickShops but for sign messages". So, I came up with a solution...

## The Solution
This plugin provides one command: `/signsearch <text>`
This will search signs in a 3-chunk radius (and do so asynchronously so as to not lag the server) to see if the text matches. When it finds the nearest one, it will turn the player to face it and say how many blocks away it is in chat.
The plugin has no configuration or permissions, it's a simple drag+drop.
