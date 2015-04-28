# Introduction #

Danmaku Time (DT) uses [Lua](http://www.lua.org/manual/5.1/manual.html) as its scripting language. Familiarize yourself with Lua first before continuing.

# Internet Multiplayer #

To create a new networked game, start the program with:
```
DanmakuTime.exe -host <externalIP> <port>
```

Replacing `<externalIP>` with your [public-facing IP address](http://www.whatsmyip.org/) and `<tcpPort>` with the TCP/UDP port you want clients to connect to you on (port can be any [free](http://en.wikipedia.org/wiki/Well_known_ports) number between 1025 and 65535). If other players can't seem to connect with you, check out http://portforward.com/ for detailed instructions on how to make yourself reachable to others on the internet.

To join a networked game:
```
DanmakuTime.exe -join <address> <port> <myPort>
```

Where `<address>` and `<port>` match the `<externalIP>` and `<port>` parameters used by the one hosting the game. `<myPort>` changes the UDP port used for sending/receiving (can be any [free](http://en.wikipedia.org/wiki/Well_known_ports) number between 1025 and 65535).

# Resources #

Resources are stored in the `res` subfolder. When building a distribution of your game, all resources in `res` are automatically packaged into a `res.zip` file.

The `res` folder has the following structure:
```
- res + font --- *.ttf
      + img ---- *.png, *.jpg, *.tga
      + snd ---- *.ogg
      + script - *.lua
      - icon.ico
      - icon.png
      - keyconfig.ini
      - prefs.default.xml
```

Resources must be placed in the appropriate folders to be recognized by the engine, and resource paths are relative to the folder for that resource type (image paths are relative to `res/img`, sound paths to `res/snd`, etc.)

## Fonts ##

Fonts must be stored in the `font` folder. Where in-game you need to supply a font name, the font name is the filename of the font file without its file extension (DejaVuSans.ttf -> DejaVuSans).

Warning: The default font (DejaVuSans) doesn't have glyphs for East-Asian characters. If you need to use those, [change the default font](#Changing_preferences.md).

## Images ##

Images are stored in the `img` folder and its subfolders. You can define subrectangles to pack multiple sprites inside a single image file. To do so, add an entry to `img.xml`:

```
  <image filename="explosion.png" width="64" height="64">
    <subrect id="e0" rect=" 0, 0, 32, 32" />
    <subrect id="e1" rect="32, 0, 32, 32" />
    <subrect id="e2" rect=" 0,32, 32, 32" />
    <subrect id="e3" rect="32,32, 32, 32" />
  </image>
```

The XML is fairly straightforward. The filename, width and height attributes must match the relative path and size of an image. The subrect elements define named subrectangles inside the image. You can put additional `img.xml` files in subfolders, the filenames in image elements are then relative to the location of `img.xml`

To reference a subrectangle, use the image path followed by a '#' and the subrect id (`"explosion.png#e0"`).

## Sounds ##

Sound files must be of filetype Ogg Vorbis and reside in a subfolder of `snd`.

## Scripts ##

The Lua scripts are stored in `script`. When starting a game, all files in `script` and its subfolders are compiled and run in alphabetical order by path. Please don't write scripts that rely on this order, it can cause major headaches for yourself and others.

[Further information about scripting](#Scripting.md)

## Changing Preferences ##

The preferences are loaded from `prefs.default.xml` and accessible from the `Config` object at runtime. The default `prefs.default.xml` looks like this:
```
<root>
  <property name="game.gameName" type="string">Danmaku Time</property>
  <property name="game.defaultFont" type="string">DejaVuSans</property>

  <property name="graphics.fps" type="int">60</property>
  <property name="graphics.width" type="int">640</property>
  <property name="graphics.height" type="int">480</property>
  <property name="graphics.vsyncEnabled" type="boolean">true</property>

  <property name="audio.hardSync" type="boolean">false</property> 
</root>
```

  * **game.gameName**: Displayed in the title bar of the window
  * **game.defaultFont**: The default font's name
  * **graphics.fps**: The target FPS the game should run at
  * **graphics.width**, **graphics.height**: The desired width/height of the content area of the window. If they're small enough (800x600 or smaller) the window starts out at the desired size, otherwise the window starts at 800x600. When going to fullscreen (alt+enter to switch at runtime), the screen resolution changes to the available resolution closest to the desired values.
  * **graphics.vsyncEnabled**: Enable/disable vertical sync. Enabling prevents screen tearing at the cost of worse framerate drops during slowdown.
  * **audio.hardSync**: When `true`, syncs the music to the gameplay at the cost of stuttering music during slowdown. When `false`, prefers lighter resource usage and gapless playback over perfect synchronization.

To change the window icon, replace `icon.ico` and `icon.png` with your own images.

## Key Config ##

The `keyconfig.ini` file contains simple key/value pairs defining the default controls.

```
p1.UP=UP|JOY1_UP
p1.DOWN=DOWN|JOY1_DOWN
p1.LEFT=LEFT|JOY1_LEFT
p1.RIGHT=RIGHT|JOY1_RIGHT
p1.BUTTON1=Z|JOY1_BUTTON1
p1.BUTTON2=X|JOY1_BUTTON3
p1.BUTTON3=SHIFT|JOY1_BUTTON2
```

In this example, `p1.BUTTON1` is the first button of the first player. It's considered pressed if the `Z` key is pressed, or the first button of the first connected gamepad is pressed.

Gamepad support is not available in applet-mode.

# Distribution #

Most of the distribution-related settings are stored in `build-res`.

```
+ build-res + applet --- applet.html
            |          - applet.jnlp
            + launcher - *.bat, *.sh
            - build.properties
```

The `build.properties` files contains simple key=value pairs that modify the build parameters:

```
project-name = DanmakuTime
applet-width = 640
applet-height = 480
program-args = 
```

  * **project-name**: Name of the generated JAR and EXE files, please keep it ASCII to avoid cross-platform compatibility issues.
  * **applet-width**, **applet-height**: Default dimensions of the applet when building an applet distribution.
  * **program-args**: Additional arguments to pass to the application (`-debug` runs the game in debug mode).

The files in `launcher` are templates for run scripts that get filled in with properties from the build config.

The files in `applet` are templates for building in applet mode.

## Running the build scripts ##

The build script (`build.xml`) is written in Ant (a copy of Ant is included in the `tools` folder). The build script contains multiple targets. To call ant with a specific target, use:
```
tools/ant/bin/ant target_name
```
or on Windows:
```
tools\ant\bin\ant target_name
```
replacing `target_name` with the target you want.

Targets:
  * **clean**: Removes all generated files
  * **jar**: Compiles the game and generates launcher scripts
  * **dist**: Creates a clean copy of the game - ready for distribution - in the `dist` folder
  * **dist-applet**: Creates an applet distribution in `dist`
  * **make-installer**: Wraps the files in `dist` into a self-extracting Java program.

# Scripting #

The bulk of the engine is implemented in Java, but made available to Lua. Some Java objects call Lua functions, but you can't generally subclass a Java object with Lua code.

## Program Structure ##

Every game needs to somehow display things, in Danmaku Time these are called drawable.

_Note: The interface is defined as IDrawable, and Drawable is the implementation. This is a common pattern throughout the code. Lua doesn't care about the difference between the two._

When a drawable is created, it gets added to something called a field. There can be many fields, and they are drawn in order of field ID (you specify a field ID when you create a new field). A field's bounds define a [clipping rectangle](http://en.wikipedia.org/wiki/Clipping_%28computer_graphics%29) on the screen.

Sprites are a kind of drawable, and add some additional functionality. A sprite can have a number of colnode objects attached to it. These colnodes are used by the collision detection system to detect collisions between different colnodes and report those back to the sprites.

## Java objects creatable from Lua ##
  * [Drawable](#Drawable.md)
  * [Sprite](#Sprite.md)
  * [TextDrawable](#TextDrawable.md)
  * [ColMatrix](#ColMatrix.md)
  * [CircleColNode](#CircleColNode.md)
  * [LineSegColNode](#LineSegColNode.md)
  * [RectColNode](#RectColNode.md)
  * [Thread](#Thread.md)
  * [Field](#Field.md)

### Drawable ###

Everything you see on screen is a drawable. Almost everything you code will involve drawables (sprites to be more exact, which is a kind of drawable).

To create a new drawable object from Lua:
```
self = Drawable.new(fieldId, self)
```

For other kinds of Drawable, simply substitute `Drawable` for `Sprite` or `TextDrawable`.

`Drawable.new` takes a field id and a lua table as arguments. It returns returns a newly constructed drawable object attached to the specified field, and with all entries of the second argument added to it.

A full list of available methods is in the javadoc: `nl.weeaboo.dt.object.IDrawable`

In addition to the Java methods you can call from Lua, there are also Lua methods which are called from Java.

  * update()
  * animate()
  * onDestroy()
  * onCollision(other, myNode, otherNode) --(sprite only)

When a drawable is constructed, two new threads are started that call update/animate. Once the function ends it's not called again, but you can use `yield()` to pause execution until the next frame. If defined, an update function usually has the form:

```
function MySprite:update()
    while true do
        --Do something interesting
        yield()
    end
end
```

The `onDestroy` and `onCollision` functions can't use `yield()`, their execution must end in the frame they're called.

`onDestroy` gets called when an attempt is made to destroy the drawable (someone called `destroy` either directly on the Java object or through the Lua object). If `onDestroy` returns `false`, the destruction is cancelled.

`onCollision` gets (beside the implicit self argument) three arguments: `other`, `myNode`, `otherNode`. These arguments get filled in with information about a collision when one occurs; a reference to the other sprite and references to the colliding colnodes.

### Sprite ###

Sprite is a special kind of drawable that can do collision detection and has speed vector. Implement an `onCollision` function to receive any collision events.

The speed/angle attributes are there to improve performance for simple bullets. Often, you don't need an update function at all for simple bullets. Just set the `speed`, `speedInc`, `angle` and/or `angleInc` to the appropriate values.

A full list of available methods is in the javadoc: `nl.weeaboo.dt.object.ISprite`

### TextDrawable ###

A special kind of drawable that renders a paragraph of text.

A full list of available methods is in the javadoc: `nl.weeaboo.dt.object.ITextDrawable`

### ColMatrix ###

ColMatrix defines a collision matrix that limits the number of collision checks required. There are usually multiple 'kinds' of objects like players, items or enemies that don't care about every possible collision. For example:

|        | player | item | enemy |
|:-------|:-------|:-----|:------|
| player |        |      | x     |
| item   | x      |      |       |
| enemy  |        |      |       |

We only need 2 checks, `player -> enemy` and `item->player` (assuming enemies are not damaged by the player flying into them).

The above implemented in a script:
```
local colMatrix = ColMatrix.new()
playerColType = colMatrix:newColType()
itemColType = colMatrix:newColType()	
enemyColType = colMatrix:newColType()	
colMatrix:setColliding(playerColType, enemyColType)
colMatrix:setColliding(itemColType, playerColType)

gameField = Field.new(1, (screenWidth-384)/2, (screenHeight-448)/2, 384, 448, 32)
gameColField = gameField:getColField()	
gameColField:setColMatrix(colMatrix)
```

We've only activated collisions in one direction, so in this case when the player and an enemy collide only the player will be notified of the collision. To also have the enemy be notified, add a line `colMatrix:setColliding(enemyColType, playerColType)` or use `colMatrix:setColliding2(playerColType, enemyColType)` to set both at once.

### CircleColNode ###

Creates a circular colnode with the specified radius.

```
--defined as: CircleColNode.new(radius)

player:setColNode(0, playerColType, CircleColNode.new(2.0))
```

### LineSegColNode ###

Creates a line segment colnode with a given thickness. The coordinates are relative to the sprite you attach it to, and it rotates along with the sprite's draw angle.

```
--defined as: LineSegColNode.new(x0, y0, x1, y1, thickness)

player:setColNode(0, playerColType, LineSegColNode.new(0, -12, 0, 12, 4))
```

### RectColNode ###

Creates a rectangular colnode that doesn't rotate together with the sprite. The x and y coordinates are relative to the sprite's position.

```
--defined as: RectColNode.new(x, y, w, h)

player:setColNode(0, playerColType, LineSegColNode.new(0, -12, 0, 12, 4))
```

### Thread ###

Use `Thread.new(func, ...)` to create a new thread of execution using the supplied function. If additional arguments are given, they are passed as arguments to the function when it's called. The function may use `yield()` in its implementation.

### Field ###

Creates a new field with an specific `id`, `bounds` and `pad`.

Example use:
```
gameField = Field.new(1, (screenWidth-384)/2, (screenHeight-448)/2, 384, 448, 32)
```

The above creates a new field with `id=1` (the default field id for new sprites), `size=384,448`, centered on the screen, and with `pad=32`. The value of `pad` determines how fast sprites leaving the screen are automatically destroyed (the field pads the border with `pad` and removes all sprites outside this larger border automatically).

## Built-in globals ##

The following globals are made available to Lua by default:

  * [startGame(funcName)](#startGame.md)
  * [globalReset(funcName)](#globalReset.md)
  * [quit()](#quit.md)
  * [pause(func, ...)](#pause.md)
  * [screenshot(x, y, w, h, blurMagnitude)](#screenshot.md)
  * [saveReplay(filename)](#saveReplay.md)
  * [startReplay(filename)](#startReplay.md)
  * [hostNetGame(numPlayers, externalIP, localTCPUDPPort)](#hostNetGame.md)
  * [joinNetGame(targetIP, targetTCPPort, localUDPPort)](#joinNetGame.md)
  * [input](#input.md)
  * [vkeys](#vkeys.md)
  * [texStore](#texStore.md)
  * [soundEngine](#soundEngine.md)
  * [storage](#Persistent_Storage.md)
  * [BlendMode](#BlendMode.md)
  * [FontStyle](#FontStyle.md)
  * [Keys](#Keys.md)

### startGame ###

Resets _everything_, then restarts the game calling the global function with the supplied name (if no name is specified, "main" is used). Example:
```
startGame("restart0")
```

`startGame` is similar to `globalReset`, but `startGame` also starts recording a replay.

### globalReset ###

Resets _everything_, then restarts the game calling the global function with the supplied name (if no name is specified, "main" is used). Example:
```
globalReset("returnTitle0")
```

### quit ###

Exits the game and closes its window.

### pause ###

`pause` calls the supplied function with the supplied arguments after putting the game in a paused state. When paused, only drawables in a field with `id=999` are updated. The pause function may use `yield()`. To unpause, simply return from the function.

### screenshot ###

`screenshot` returns a `DelayedScreenshot` Java object which will take a screenshot of the specified subrect of the screen sometime in the future (may take several frames). An example using the screenshot function:

```
local ds = screenshot(0, 0, screenWidth, screenHeight, 4)
while not ds:isAvailable() do
    yield()
end
	
local ss = Drawable.new(999)
ss:setPos(screenWidth/2, screenHeight/2)
ss:setColor(.66, .66, .66, 1.0)
ss:setZ(32000)
ss:setTexture(ds:asTexture())
```

Here, the `blurMagnitude` is set to `4`. If the blur magnitude is supplied and it is greater than `1`, the resulting screenshot will be blurred. Higher magnitude results in a stronger blur.

Anyway, the screenshot command is called and the function waits until `isAvailable` returns `true`. Once it does, the screenshot is available and it can be turned into a texture through `asTexture`.

### saveReplay ###

`saveReplay` saves the replay currently recording to a file. This does not stop the recording; you may save multiple times during the same recording.

```
if saveReplay("r01") then
    print("Replay successfully saved")
end
```

### startReplay ###

`startReplay` starts playback of a previously saved replay. Playing back replays recorded on different versions of the program or with different scripts is undefined. Change the replay speed with `PAGE_UP` and `PAGE_DOWN`. Use `ESC` to end playback of the recorded replay.

```
if startReplay("r01") then
    print("Replay successfully loaded")
end
```

### hostNetGame ###

```
hostNetGame(numPlayers, externalIP, localTCPUDPPort)
```

see: [Internet multiplayer](#Internet_Multiplayer.md)

### joinNetGame ###

```
joinNetGame joinNetGame(targetIP, targetTCPPort, localUDPPort)
```

see: [Internet multiplayer](#Internet_Multiplayer.md)

### input ###

A lua-accessible implementation of `nl.weeaboo.dt.input.IInput`.

Example use:
```
if input:consumeKey(Keys.Z) then
    --shoot
end
```

### vkeys ###

A table containing the key codes for the player controls (the same keys as defined in the key config). Example:

```
if input:consumeKey(vkeys[playerId].BUTTON1) then
    print("player 1 button 1 pressed")
end
```

### texStore ###

Makes available a Java object implementing `nl.weeaboo.dt.renderer.ITextureStore`. Use the `get` method to retrieve textures by name. Example:

```
enemy:setTexture(texStore:get("test.png#g0"))
```

### soundEngine ###

Allows access to an `nl.weeaboo.dt.audio.ISoundEngine`.

Example use:
```
soundEngine:setBGM("bgm/bgm01.ogg")
soundEngine:playSound("test.ogg")
yield(300)
soundEngine:stopBGM()
```

### Persistent Storage ###

A hashtable containing primitives (number/boolean/string) that can be saved to disk. It can be used to store high-scores, cleared-states, etc.

Example use:
```
storage:set("property name", 1337)
print("value=" .. property:get("property name"))
storage:save() --Save changes to disk, this is -not- done automatically.
```

### BlendMode ###

A table containing the following values:

  * NORMAL
  * ADD

`NORMAL` is the default blend mode of OpenGL. `ADD` is the ever popular brightness-increasing blend where drawing multiple objects on top of each other increases the brightness each time.

### FontStyle ###

A table containing the following values:

  * PLAIN
  * BOLD
  * ITALIC
  * BOLDITALIC

### Keys ###

A table containing the key codes for all keys on a normal keyboard. The naming of the fields is the same as those in `java.awt.event.KeyEvent`, but without the `VK_` prefix.

# Built-in Functions #

## Restart ##

Pressing F5 restarts the game and reloads everything. This saves you the trouble of having to close->reopen the program everytime you change a script.

## Screenshot ##

Press F7 to take a screenshot and store it as a PNG file in the Danmaku Time folder.

## Screen Capture (Video) ##

Press F8 to toggle the screen capture feature. The screen capture records everything that gets displayed (no audio) and stores it as a video file in your Danmaku Time folder. Use of the screen capture requires that you have a working x264 executable in the program folder or some other folder that's in your default executable search path.

## View Textures ##

Press F12 to view all currently loaded textures (F12 toggles it on/off).