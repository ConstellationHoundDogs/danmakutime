
-------------------------------------------------------------------------------
-------------------------------------------------------------------------------
-------------------------------------------------------------------------------

THPlayer = {
	--stats
	speed=3,
	focusSpeed=1.5,
	fireDelay=3,
	deathBombTime=20,
	deathInvincibleTime=150,
	maxLives=99,
    minBombsNewLife=2,
	maxBombs=99,
	maxShotPower=100,
	autoCollectY=nil,
	magnetAttrRadius=32,
    levelPadH=16,
    levelPadV=24,
	
	--state
    playerId=1,
    controls=nil,
	lives=3,
	bombs=2,
	shotPower=0,
	grazeCounter=0,	
	points=0,
	focus=false,
	fireCooldown=0,
	deathTime=0,
	invincible=0,
	dx=0,
	focusSprites=nil,
    texture=nil
	}
    
function THPlayer.new(playerId, self)
	self = extend(THPlayer, self or {})
	self = THSprite.new(self)
	
	self:setTexture(texStore:get(self.texture .. "#idle0"));
	self:setColNode(0, playerColType, CircleColNode.new(2.0))
	self:setColNode(1, playerGrazeColType, CircleColNode.new(10.0))
	self:setColNode(2, playerItemColType, RectColNode.new(-9, -18, 18, 34))
	self:setColNode(3, playerItemMagnetColType, CircleColNode.new(0))

	self:setPos(levelWidth/2, levelHeight - 32)
	self:setZ(1000 + 100 * playerId)	
	
	self.autoCollectY = self.autoCollectY or levelHeight/4
	
	self.focusSprites = self.focusSprites or {}
	self.focusSprites[1] = FocusSprite.new(self, texStore:get("focus.png#upper"), -10, {fadeSpeed=.1})
	self.focusSprites[2] = FocusSprite.new(self, texStore:get("focus.png#lower"), 10, {fadeSpeed=.05})
	
    if self.controls == nil then
        self.controls = {
            up=vkeys[playerId].UP,
            down=vkeys[playerId].DOWN,
            left=vkeys[playerId].LEFT,
            right=vkeys[playerId].RIGHT,
            fire=vkeys[playerId].BUTTON1,
            bomb=vkeys[playerId].BUTTON2,
            focus=vkeys[playerId].BUTTON3
        }            
    end
    
	return self
end

function THPlayer:onCollision(other, myColNode, otherColNode)
	if self.deathTime > 0 then
		return
	end

	local t0 = myColNode:getType()
	if t0 == playerItemMagnetColType then
		other.magnetTarget = self
		return
	elseif self.invincible > 0 then
		return
	elseif t0 == playerGrazeColType then
		if not other.grazed then
			other.grazed = true
			self:onGrazed(other)
		end
	else
		self:destroy()
	end
end

function THPlayer:onGrazed(other)
	self.grazeCounter = self.grazeCounter + 1
end

function THPlayer:update()
	while true do		
		self:updateDeathTime()		
		
		if self.deathTime <= 0 then		
			self:updateFocus()	
			self:updatePos()
			self:updateBomb()
			self:updateFire()
			self:updateItemCollect()
		end
		
		yield()
	end
end

function THPlayer:updateDeathTime()
	if self.invincible > 0 then
		self.invincible = self.invincible - 1
	end

	if self.deathTime > 0 then
		self:setAlpha(self.deathTime / self.deathBombTime)
		
		self.deathTime = self.deathTime - 1
		
		if self.deathTime > 0 then
			if self:updateBomb() then
				--Bomb used, cancel death...
				self.deathTime = 0
			end
		else
			if self.lives > 0 then
			    self.lives = self.lives - 1
			    self.invincible = self.deathInvincibleTime
			end
			
			if self.lives <= 0 then
				self:destroy()
			end
		end
	else
		self:setAlpha(0.75 + 0.25 * math.cos(32 * self.invincible))
	end	
end

function THPlayer:updateFocus()
	self.focus = input:isKeyHeld(self.controls.focus)
end

function THPlayer:updatePos()
	local x = self:getX()
	local y = self:getY()
		
	local spd = self.speed
	if self.focus then
		spd = self.focusSpeed
	end	
		
	if input:isKeyHeld(self.controls.left) then
		self.dx = -spd
		x = x - spd
	elseif input:isKeyHeld(self.controls.right) then
		self.dx = spd
	    x = x + spd
	else
		self.dx = 0
	end
	
	if input:isKeyHeld(self.controls.up) then
		y = y - spd
	elseif input:isKeyHeld(self.controls.down) then
	    y = y + spd
	end
	
	if input:isKeyHeld(Keys.R) then
		self:setAngle(self:getDrawAngle() + 2)
	end
	
    local padH = self.levelPadH
    local padV = self.levelPadV
	x = math.max(padH, math.min(levelWidth-padH, x))
	y = math.max(padV, math.min(levelHeight-padV, y))
	
	self:setPos(x, y)
end

function THPlayer:updateBomb()
	if self.bombs > 0 and self.invincible <= 0 then
        if input:consumeKey(self.controls.bomb) then
            self.bombs = self.bombs - 1		
            self:bomb(self.deathTime > 0)
            return true
        end
	end
	return false
end

function THPlayer:bomb(isDeathBomb)
	print("boom")
end

function THPlayer:updateFire()	
	if self.fireCooldown > 0 then
		self.fireCooldown = self.fireCooldown - 1
	else
		if input:isKeyHeld(self.controls.fire) then
			self.fireCooldown = self.fireDelay
			self:fire()
		end
	end
end

function THPlayer:fire()
	local x = self:getX()
	local y = self:getY()
	local z = self:getZ() + 25
	local angle = self:getAngle()

    local angleScale = 1
    if self.focus then
        angleScale = .25
    end
    
	for n=0,4 do
		local s = THPlayerShot.new(self, {power=1})
		s:setTexture(texStore:get("test.png#g0"));
		s:setColNode(0, playerShotColType, CircleColNode.new(7))
		s:setAngle(angle + angleScale * (16 * n - 32))
		s:setSpeed(10)
	end
end

function THPlayer:updateItemCollect()
	if self:getY() < self.autoCollectY then
		self:setColNode(3, playerItemMagnetColType, CircleColNode.new(999999))
	else
		self:setColNode(3, playerItemMagnetColType, CircleColNode.new(self.magnetAttrRadius))
	end
end

function THPlayer:animate()
	local animPrefix = {"idle", "left", "right"}
	local anim = 1
	local frame = 0
	local frameTime = 6
	local lastDX = 0
	
	while true do
		if signum(self.dx) ~= signum(lastDX) then
			frame = 0
			if self.dx < 0 then
				anim = 2
			elseif self.dx > 0 then
				anim = 3
			else
				anim = 1
			end
		end
	
		self:setTexture(texStore:get(self.texture .. "#" .. animPrefix[anim] .. frame))
		
		frame = frame + 1
		if frame >= 8 then
			if anim == 1 then
				frame = 0
			else
				frame = 4
			end
		end
		
		lastDX = self.dx
		
		yield(frameTime)
	end
end

function THPlayer:onDestroy()
	if self.deathTime <= 0 then
		self.deathTime = self.deathBombTime
	end
	
	if self.lives <= 0 then
        local totalLives = 0
        for _,p in ipairs(players) do
            totalLives = totalLives + math.max(0, p.lives)
        end
        if totalLives <= 0 then
            Timer.new(1, doPause)
        end
		return true
	end
	
	self.shotPower = math.max(0, self.shotPower - 20)
	self.bombs = math.max(self.bombs, self.minBombsNewLife)
    
	return false
end

-------------------------------------------------------------------------------
-------------------------------------------------------------------------------
-------------------------------------------------------------------------------

FocusSprite = {
	parent=nil,
	rotSpeed=2,
	fadeSpeed=0.1,
	maxAlpha=0.5,
	dz=1
	}

function FocusSprite.new(parent, tex, dz, self)
	self = extend(FocusSprite, self or {})
	self = Sprite.new(self)
	
	self.parent = parent
	self.dz = dz
	self:setTexture(tex)
	self:setAlpha(0)
	
	return self
end

function FocusSprite:update()
	while not self.parent:isDestroyed() do
		self:setPos(self.parent:getX(), self.parent:getY())
		self:setZ(self.parent:getZ() + self.dz)
		yield()
	end
end

function FocusSprite:animate()
	local targetAlpha = 0.0

	self:setDrawAngleAuto(false)
	while true do
		if not self.parent:isDestroyed() and self.parent.focus then
			targetAlpha = self.maxAlpha
		else
			targetAlpha = 0.0
		end
		
		local alpha = self:getAlpha()
		if math.abs(targetAlpha-alpha) > self.fadeSpeed then
			alpha = alpha + self.fadeSpeed * signum(targetAlpha-alpha)
		else
			alpha = targetAlpha
			if self.parent:isDestroyed() then
				self:destroy()
				return
			end
		end
		alpha = math.max(0.0, math.min(self.maxAlpha, alpha))		
		self:setAlpha(alpha)
		
		self:setDrawAngle(self:getDrawAngle() + self.rotSpeed)
		
		yield()
	end
end

-------------------------------------------------------------------------------
-------------------------------------------------------------------------------
-------------------------------------------------------------------------------

THPlayerShot = {
    }

function THPlayerShot.new(owner, self)
    self = extend(THPlayerShot, self or {})
    self = THShot.new(owner, self)
    
    self:setZ(1500)
    
    return self
end
    
function THPlayerShot:onCollision(other, myNode, otherNode)
    if self.owner ~= nil and self.owner.points ~= nil and other.pointValue ~= nil then
        self.owner.points = self.owner.points + other.pointValue
    end
    self:destroy()
end
    
-------------------------------------------------------------------------------
-------------------------------------------------------------------------------
-------------------------------------------------------------------------------
