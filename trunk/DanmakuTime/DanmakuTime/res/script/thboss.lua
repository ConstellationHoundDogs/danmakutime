
-------------------------------------------------------------------------------
-------------------------------------------------------------------------------
-------------------------------------------------------------------------------
-- 
-- External dependencies:
--
-- THSprite
-- ParamText
-- Timer
-- gameField
-- enemyColNode
-- 
-------------------------------------------------------------------------------
-------------------------------------------------------------------------------
-------------------------------------------------------------------------------

THBoss = {
	spellcards={},
	currentSpellcard=0,
	maxHP=1,
	lifebar=nil,
	timer=nil,
	timeText=nil
	}

function THBoss.new(self)
	self = extend(THBoss, self or {})	
	self = THSprite.new(self)
	
	self:setColNode(0, enemyColType, RectColNode.new(-16, -16, 32, 32))
	self:setPos(levelWidth/2, -self:getHeight())
	self:setZ(player:getZ() + 25)
	
	self.lifebar = LifeBar.new(self)
	
	self.timeText = ParamText.new(gameField, self, "time", "", levelWidth-8, 4, 9, 64-8*2)
	
	return self
end

function THBoss:update()
	self:nextSpellcard()
	
	while self.currentSpellcard <= #self.spellcards do
		local spellcard = self.spellcards[self.currentSpellcard]
		spellcard:update(self)
		yield()
	end
	
	self:destroy()
end

function THBoss:onDestroy()
	if self.currentSpellcard <= 0 then
		self.power = 1
		return
	end

	if self.time <= 0 then
		--timed out
	else
		--defeated
	end

	if self:nextSpellcard() then
		return false
	end
	
	self.lifebar:destroy()
	self.timeText:destroy()
	
	return true
end

function THBoss:nextSpellcard(spell)
	self.currentSpellcard = self.currentSpellcard + 1
	if self.currentSpellcard <= #self.spellcards then
		local spell = self.spellcards[self.currentSpellcard]
		
		self.maxHP = spell.hp
		self.hp = self.maxHP
		self.time = spell.time
		
		self.timer = Timer.new(self.time,
			function()
				self.time = 0
				self:destroy()
			end,
			function()
				self.time = self.time - 1
			end)
		
		return true
	end
	return false	
end

function THBoss:addSpellcard(spell)
	table.insert(self.spellcards, spell)
end

-------------------------------------------------------------------------------
-------------------------------------------------------------------------------
-------------------------------------------------------------------------------

LifeBar = {
	enemy=nil,
	maxPower
	}
	
function LifeBar.new(enemy, self)
	self = extend(LifeBar, self or {})	
	self = Drawable.new(self)

	self:setTexture(texStore:get("osd.png#lifebar"))
	self:setZ(-32000)
	
	self.enemy = enemy
	
	return self
end

function LifeBar:animate()
	local pad = 8
	local maxW = levelWidth - pad*2 - 32
	while true do
		local enemy = self.enemy
		self:setScaleX(maxW * enemy.hp / enemy.maxHP)
		self:setPos(pad + self:getWidth()/2, pad + self:getHeight()/2)
		yield()
	end
end

-------------------------------------------------------------------------------
-------------------------------------------------------------------------------
-------------------------------------------------------------------------------

Spellcard = {
	time=0,
	hp=0
	}

function Spellcard.new(self)
	self = extend(Spellcard, self or {})		
	return self
end

function Spellcard:update(boss)

end

-------------------------------------------------------------------------------
-------------------------------------------------------------------------------
-------------------------------------------------------------------------------