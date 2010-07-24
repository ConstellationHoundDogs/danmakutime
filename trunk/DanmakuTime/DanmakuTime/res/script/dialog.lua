
-------------------------------------------------------------------------------
-------------------------------------------------------------------------------
-------------------------------------------------------------------------------
-- 
-- External dependencies:
--
-- player
-- THPlayer.
--
-------------------------------------------------------------------------------
-------------------------------------------------------------------------------
-------------------------------------------------------------------------------

Dialog = {
	pages=nil,
	curpage=1,
	background=nil,
	left=nil,
	right=nil
	}

function Dialog.new(self)
	self = extend(Dialog, self or {})
	self = TextDrawable.new(self)
	
	local pad = 10
	
	self.pages = self.pages or {}
	self.width = self.width or levelWidth - pad * 2
	self.height = self.height or 100
	self.x = self.x or pad
	self.y = self.y or screenHeight - pad - self.height
	
	self:setPos(self.x, self.y)
	self:setZ(-32000)
	self:setBlockAnchor(self.anchor or 7)
	self:setFont(self.fontName or "DejaVuSans",
		self.fontStyle or FontStyle.BOLD, self.fontSize or 14)
	self:setOutlineColor(.1, .1, .1)
	self:setOutlineSize(2)
	self:setWidth(self.width)
	
	local bg = Drawable.new()
	bg:setPos(self:getX() + self.width/2, self:getY() + self.height/2)	
	bg:setZ(self:getZ()+1)
	local bgtex = texStore:get("dialog-border.png")
	bg:setTexture(bgtex)
	self.background = bg
	
	local left = Drawable.new()
	left:setPos(128, levelHeight - 256)
	left:setZ(self:getZ()+2)
	self.left = left
	
	local right = Drawable.new()
	right:setScaleX(-1)
	right:setPos(levelWidth - 128, levelHeight - 256)
	right:setZ(self:getZ()+2)
	self.right = right
	
	return self
end

function Dialog:addPage(text, left, right)
	table.insert(self.pages, {text=text, left=left, right=right})
end

function Dialog:update()
	input:consumeKey(Keys.Z)

	while self.curpage <= #self.pages do
		local page = self.pages[self.curpage]
		self:setText(page.text)
		if type(page.left) == "string" then
			self.left:setTexture(texStore:get(page.left))
		else
			self.left:setTexture(page.left)
		end
		if type(page.right) == "string" then
			self.right:setTexture(texStore:get(page.right))
		else
			self.right:setTexture(page.right)
		end
	
		while not input:consumeKey(Keys.Z) do
			player.fireCooldown = 1
			yield()
		end
		
		self.curpage = self.curpage + 1
	end
	player.fireCooldown = 1
	
	self:destroy()
end

function Dialog:onDestroy()
	if self.background ~= nil then
		self.background:destroy()
	end
	if self.left ~= nil then
		self.left:destroy()
	end
	if self.right ~= nil then
		self.right:destroy()
	end
	return true
end

-------------------------------------------------------------------------------
-------------------------------------------------------------------------------
-------------------------------------------------------------------------------
