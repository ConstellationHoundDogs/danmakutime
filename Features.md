# Introduction #

Danmaku Time exposes a straightforward scripting interface to [Lua](http://en.wikipedia.org/wiki/Lua_%28programming_language%29). Other engines often use custom scripting languages with sparse documentation, but Lua tutorials and examples can easily be found all over the web. Lua is also a relatively easy language to learn.

The engine is packaged with a demo script taking care of common tasks (character selection, setting up collision detection, backgrounds, on-screen display) allowing scripters to start coding shot patterns immediately.

## Features ##

### Performance ###

A performance benchmark spawning 10000 circling shots runs at 60 fps.
(hardware used: Core i5 750, Radeon HD 4650)

sin/cos/asin/acos are implemented using lookup tables, making them less precise but very fast.

## Graphics ##

All rendering is hardware accelerated through OpenGL.

### Text ###

Advanced text rendering is available through the TextDrawable object. This includes automatic word wrapping and text styling (fontName, fontStyle, fontSize, color, anchor, outlineColor, outlineSize, underline). Text rendering is also hardware accelerated.

### Audio ###

Music can be synced to the gameplay. The default setting is soft sync (prefer gapless playback over perfect sync), but a hard sync implementation (maintains perfect sync with the gameplay) can also be used.

## Controls ##

Gamepads are supported and controls can be reconfigured by editing a text file (keyconfig.ini)

## Multiplayer ##

Support for up to 8 players simultaneously (although the packaged demo scripts only support up to 4).

In addition to local multiplayer (multiple people playing on the same machine), internet multiplayer is also available. Once all players are connected, there's no difference between the two types of multiplayer as far as scripts are concerned (supporting internet multiplayer is as easy as supporting local multiplayer).

## Customization ##

Nearly everything related to gameplay is customizable. The engine doesn't know what a player is, or a stage, a bomb, an item, a boss. Those things are all defined by Lua scripts and can be removed or altered by scripts if wanted.