
function clone(obj)
	local result = {}
	for k,v in pairs(obj) do
		result[k] = v
	end
	return result
end

--Takes a list of tables and generates a new table containing SHALLOW copies
--of all attributes
function extend(...)
	local result = {}
	for tableIndex,table in ipairs(arg) do
		for k,v in pairs(table) do
			result[k] = v
		end
	end
	return result
end

function append(a, b)
	for i,v in ipairs(b) do
		table.insert(a, v)
	end
end

function signum(x)
	if x > 0 then
		return 1
	elseif x < 0 then
		return -1
	end
	return 0
end

function pauseHandler()
	showPauseMenu()

--[[
	local dx = screenWidth/2
	local dy = screenHeight/2

	local pausedText = TextDrawable.new(999)
	pausedText:setPos(dx, dy)
	pausedText:setBlockAnchor(5)
	pausedText:setFontName("DejaVuSans")
	pausedText:setFontStyle(FontStyle.BOLD)
	pausedText:setFontSize(14)	
	pausedText:setText("Paused")
	pausedText:setZ(-100)

	while true do
		if input:consumeKey(Keys.ESCAPE) then
			break
		end
		
		yield()
	end
	
	pausedText:destroy()
]]--	
end

function pauseListener()
	while true do
		if input:consumeKey(Keys.ESCAPE) then
			local ds = screenshot(0, 0, screenWidth, screenHeight, true)
			while not ds:isAvailable() do
				yield()
			end
			
			local ss = Drawable.new(999)
			ss:setPos(screenWidth/2, screenHeight/2)
			ss:setColor(.66, .66, .66, 1.0)
			ss:setZ(32000)
			ss:setTexture(ds:asTexture())
									
			pause(function()
				showPauseMenu()
				ss:destroy()
			end)
		end
		yield()
	end
end

function showPauseMenu(canResume)
	if canResume == nil then
		canResume = true
	end
	
	local exit = false

	--Create menu
	local menu = MenuGroup.new()
		
	local resumeItem = MenuOption.new(999, screenWidth/2, screenHeight/2 - 25,
		{ label="Resume", blockAnchor=5,
		select=function(self)
			exit = true
		end
	})
	menu:add(resumeItem)

	local restartItem = MenuOption.new(999, screenWidth/2, screenHeight/2 + 0,
		{ label="Restart Game", blockAnchor=5,
		select=function(self)
			print("Ask for confirmation (restart)")
			exit = true
			Thread.new(restart)
		end
	})
	menu:add(restartItem)

	local titleItem = MenuOption.new(999, screenWidth/2, screenHeight/2 + 25,
		{ label="Quit Game", blockAnchor=5,
		select=function(self)
			print("Ask for confirmation (title)")
			exit = true
			Thread.new(returnTitle)
		end
	})
	menu:add(titleItem)
	
	--Event loop
	while not exit and not input:consumeKey(Keys.ESCAPE) do
		menu:update()
		yield()
	end
	
	--Destroy menu
	menu:destroy()
end
