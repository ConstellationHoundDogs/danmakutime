
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
	background=nil,
	pages=nil,
	curpage=1
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
	bg:setTexture(texStore:get("dialog-border.png"))
	self.background = bg
	
	return self
end

function Dialog:addPage(p)
	table.insert(self.pages, p)
end

function Dialog:update()
	input:consumeKey(Keys.Z)

	while self.curpage <= #self.pages do
		self:setText(self.pages[self.curpage])
	
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
	return true
end

-------------------------------------------------------------------------------
-------------------------------------------------------------------------------
-------------------------------------------------------------------------------
