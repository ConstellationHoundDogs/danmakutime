
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
-- overlayField
-- enemyColType
-- playerZ
-- 
-------------------------------------------------------------------------------
-------------------------------------------------------------------------------
-------------------------------------------------------------------------------

THBoss = {
	spellcards=nil,
	currentSpellcard=0,
	maxHP=1,
    time=0,
	lifebar=nil,
	timer=nil,
	timeText=nil,
	indicator=nil,
    threads=nil,
    shotDestroyer=nil
	}

function THBoss.new(self)
	self = extend(THBoss, self or {})	
	self = THSprite.new(self)
	
	self.spellcards = self.spellcards or {}
	
	self:setColNode(0, enemyColType, RectColNode.new(-16, -16, 32, 32))
	self:setPos(levelWidth/2, -self:getHeight())
	
	self.lifebar = LifeBar.new(self)	
	self.timeText = ParamText.new(gameField, self, "time", "", levelWidth-8, 4, 9, 64-8*2)	
	self.indicator = BossIndicator.new(self)
    self.threads = {}
	
	return self
end

function THBoss:addThread(func, ...)
    local t = self.javaObject:addThread(func, ...)
    table.insert(self.threads, t)
    return t
end

function THBoss:update()
	if not self:nextSpellcard() then
        self:destroy()
    end
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
	self.indicator:destroy()
	
	return true
end

function THBoss:endSpellcard()
    for _,t in ipairs(self.threads) do
        t:destroy()
    end
    self.threads = {}
    
    if self.shotDestroyer ~= nil then
        self.shotDestroyer:destroy()
    end
    
    if self.timer ~= nil then
        self.timer:destroy()
        self.timer = nil
    end
    
    self.shotDestroyer = Sprite.new()
    self.shotDestroyer:setColNode(0, enemyDetectorColType, CircleColNode.new(999999))
    self.shotDestroyer:setColNode(1, enemyShotDetectorColType, CircleColNode.new(999999))
    self.shotDestroyer.onCollision = function(self, other, myNode, otherNode)
        dropMagnetItems(other:getX(), other:getY(), 1)
        other:destroy()
    end
end

function THBoss:nextSpellcard(spell)
    self:endSpellcard()

	self.currentSpellcard = self.currentSpellcard + 1
	if self.currentSpellcard <= #self.spellcards then
		local spell = self.spellcards[self.currentSpellcard]
        
		self.maxHP = spell.hp
		self.hp = self.maxHP
		self.time = spell.time
        self.invincible = true
		
        self:addThread(function()        
            yield()
            if self.shotDestroyer ~= nil then
                self.shotDestroyer:destroy()
                self.shotDestroyer = nil
            end
            self.invincible = false
            spell.update(self)
        end)
        
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

BossIndicator = {
	enemy=nil
	}
	
function BossIndicator.new(enemy, self)
	self = extend(BossIndicator, self or {})	
	self = Drawable.new(overlayField, self)
	
	self.enemy = enemy

	self:setTexture(texStore:get("osd.png#bossIndicator"))
	self:setPos(levelWidth/2, gameField:getY() + gameField:getHeight() + self:getHeight()/2)
	self:setZ(-32000)
	
	return self
end

function BossIndicator:update()
	local offsetX = gameField:getX()
	local minX = self:getWidth()/2
	local maxX = gameField:getWidth() - self:getWidth()/2
	while true do
		local newX = self.enemy:getX()
		newX = math.max(minX, math.min(maxX, newX))
		self:setPos(offsetX + newX, self:getY())
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
