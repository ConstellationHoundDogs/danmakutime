
-------------------------------------------------------------------------------
-------------------------------------------------------------------------------
-------------------------------------------------------------------------------

MenuOption = {
	label=nil,
	selected=false,
	visible=true
	}

function MenuOption.new(field, x, y, self)
	self = extend(MenuOption, self or {})		
	self = TextDrawable.new(field, self)
		
	self:setPos(x or 0, y or 0)
	self:setZ(-100)
	self:setBlockAnchor(self.blockAnchor or 7)
	self:setFontName("DejaVuSans") --fontname is the file name without extension
	self:setFontStyle(FontStyle.BOLD)
	self:setFontSize(24)
	self:setOutlineColor(.1, .1, .1)
	self:setOutlineSize(4)
	self:setText(self.label or "---")
	
	return self
end

function MenuOption:update()
	while true do
		if self.selected then
			if input:consumeKey(Keys.ENTER) or input:consumeKey(Keys.Z) then
				self:select()
			end
		end
		yield()
	end
end

function MenuOption:animate()
	while true do
		local targetAlpha = 1.0
		if not self.selected then targetAlpha = 0.5 end
		if not self.visible then targetAlpha = 0.0 end
	
		self:setAlpha(targetAlpha)
		yield()
	end
end

--function MenuOption:select()
--	print("You forgot to override the select() function of a menu option, now selecting it does nothing!")
--end

-------------------------------------------------------------------------------
-------------------------------------------------------------------------------
-------------------------------------------------------------------------------

MenuGroup = {
	selected=1,
	items=nil
	}

function MenuGroup.new(self)
	self = extend(MenuGroup, self or {})
	self.items = self.items or {}
	return self
end

function MenuGroup:destroy()
	for i,v in ipairs(self.items) do
		v:destroy()
	end
	self.items = {}
	self.selected = 1
end

function MenuGroup:add(item)
	table.insert(self.items, item)
	if self.selected == #self.items then
		item.selected = true
	end 
end

function MenuGroup:setSelected(index)
	local old = self.items[self.selected]
	if old ~= nil then old.selected = false end
	
	self.selected = index
	local new = self.items[index]
	if new ~= nil then new.selected = true end
end

function MenuGroup:setVisible(visible)
	for i,v in ipairs(self.items) do
		v.visible = visible
	end
end

function MenuGroup:update()
	if input:consumeKey(Keys.UP) and self.selected > 1 then
		self:setSelected(self.selected - 1)
	end
	if input:consumeKey(Keys.DOWN) and self.selected < self:getSize() then
		self:setSelected(self.selected + 1)
	end
end

function MenuGroup:getSize()
	return #self.items
end

-------------------------------------------------------------------------------
-------------------------------------------------------------------------------
-------------------------------------------------------------------------------

background = nil

function setBackground(filename, fadeTime)
	local oldBackground = nil
	local fadeTime = fadeTime or 0
	if fadeTime > 0 then
		oldBackground = background
	end	

	background = Drawable.new(0)
	background:setPos(screenWidth/2, screenHeight/2)
	background:setZ(32000)	
	background:setTexture(texStore:get(filename))
	
	if oldBackground ~= nil then
		oldBackground:setZ(background:getZ() - 1)
		
		local time = 0
		while time < fadeTime do
			local a = time / fadeTime
			oldBackground:setAlpha(1.0 - a)
			time = time + 1
			yield()
		end
		
		oldBackground:destroy()
	end
end

function mainMenu()
	local started = false
	setBackground("title.png")

	--Create menu
	local menu = MenuGroup.new()
		
	local startItem = MenuOption.new(0, screenWidth - 200, 100,
		{label="Start Game"})
	startItem.select = function(self)
		started = true
	end
	menu:add(startItem)

	local exitItem = MenuOption.new(0, screenWidth - 200, 130,
		{label="Quit"})
	exitItem.select = function(self)
		quit()
	end
	menu:add(exitItem)
	
	--Event loop
	while not started do
		menu:update()
		yield()
	end
	
	--Destroy menu
	menu:destroy()
end

function pauseListener()
	while true do
		if input:consumeKey(Keys.ESCAPE) then
			doPause()
		end
		yield()
	end
end

function doPause()
	local ds = screenshot(0, 0, screenWidth, screenHeight, 4)
	while not ds:isAvailable() do
		yield()
	end
	
	local ss = Drawable.new(999)
	ss:setPos(screenWidth/2, screenHeight/2)
	ss:setColor(.66, .66, .66, 1.0)
	ss:setZ(32000)
	ss:setTexture(ds:asTexture())
							
	pause(function()
		pauseMenu()
		ss:destroy()
	end)
end

function confirmMenu()
	--Create menu
	local menu = MenuGroup.new()
	local result = nil
		
	local question = TextDrawable.new(999)		
	question:setPos(screenWidth/2, screenHeight/2 - 50)
	question:setBlockAnchor(5)
	question:setFont("DejaVuSans", FontStyle.BOLD, 24)
	question:setOutlineColor(.1, .1, .1)
	question:setOutlineSize(4)
	question:setText("Are you sure?")
		
	local yesItem = MenuOption.new(999, screenWidth/2, screenHeight/2 + 0,
		{ label="Yes", blockAnchor=5,
		select=function(self)
			result = true
		end
	})
	menu:add(yesItem)

	local noItem = MenuOption.new(999, screenWidth/2, screenHeight/2 + 25,
		{ label="No", blockAnchor=5,
		select=function(self)
			result = false
		end
	})
	menu:add(noItem)
	
	--Event loop
	while result == nil do
		if input:consumeKey(Keys.ESCAPE) or input:consumeKey(Keys.X) then
			result = false
		end
		menu:update()
		yield()
	end
	
	--Destroy menu
	menu:destroy()
	question:destroy()
	
	return result
end

function pauseMenu()
	input:clear()

	--Create menu
	local menu = MenuGroup.new()
	local exit = false
	local confirm = false
	
    local playerLives = 0
    for _,v in ipairs(players) do
        playerLives = playerLives + v.lives
    end
    
	if playerLives > 0 then
		local resumeItem = MenuOption.new(999, screenWidth/2, screenHeight/2 - 25,
			{ label="Resume", blockAnchor=5,
			select=function(self)
				exit = true
			end
		})
		menu:add(resumeItem)
	end

	local restartItem = MenuOption.new(999, screenWidth/2, screenHeight/2 + 0,
		{ label="Restart Game", blockAnchor=5,
		select=function(self)
			menu:setVisible(false)
			if confirmMenu() then
				exit = true
				Thread.new(restart)
			else
				menu:setVisible(true)
			end
		end
	})
	menu:add(restartItem)

	local titleItem = MenuOption.new(999, screenWidth/2, screenHeight/2 + 25,
		{ label="Quit Game", blockAnchor=5,
		select=function(self)
			menu:setVisible(false)
			if confirmMenu() then
				exit = true
				Thread.new(returnTitle)
			else
				menu:setVisible(true)
			end
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
