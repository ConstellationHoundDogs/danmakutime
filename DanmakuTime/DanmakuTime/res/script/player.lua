

Player = {
	--stats
	speed=3,
	focusSpeed=1.5,
	fireDelay=3,
	deathBombTime=20,
	--state
	lives=2,
	focus=false,
	fireCooldown=0,
	deathTime=0,
	dx=0
	}

function Player.new(o)
	local o = extend(Player, o or {})
	return Sprite.new(o)
end

function Player:init()
	self:setPos(levelWidth/2, levelHeight - 32)
	self:setZ(1000)
	self:setTexture(textureStore:getTexture("player.png#idle0"));
end

function Player:update()
	while true do
		self:updateDeathTime()
		if self.deathTime <= 0 then
			self:updateFocus()	
			self:updatePos()
			self:updateFire()
		end
		
		yield()
	end
end

function Player:updateDeathTime()
	if self.deathTime > 0 then
		self:setAlpha(self.deathTime / self.deathBombTime)
		
		self.deathTime = self.deathTime - 1
		if self.deathTime <= 0 then
			if self.lives > 0 then
			    self.lives = self.lives - 1			    
			else
				destroy()
			end
		end
	else
		self:setAlpha(1)
	end	
end

function Player:updateFocus()
	self.focus = input:isKeyHeld(Keys.SHIFT)
end

function Player:updatePos()
	local x = self:getX()
	local y = self:getY()
		
	local spd = self.speed
	if self.focus then
		spd = self.focusSpeed
	end	
		
	if input:isKeyHeld(Keys.LEFT) then
		self.dx = -spd
		x = x - spd
	elseif input:isKeyHeld(Keys.RIGHT) then
		self.dx = spd
	    x = x + spd
	else
		self.dx = 0
	end
	
	if input:isKeyHeld(Keys.UP) then
		y = y - spd
	elseif input:isKeyHeld(Keys.DOWN) then
	    y = y + spd
	end
	
	x = math.max(16, math.min(levelWidth-16, x))
	y = math.max(24, math.min(levelHeight-24, y))
	
	self:setPos(x, y)
end

function Player:updateFire()	
	if self.fireCooldown > 0 then
		self.fireCooldown = self.fireCooldown - 1
	else
		if input:isKeyHeld(Keys.Z) then
			self.fireCooldown = self.fireDelay
			self:fire()
		end
	end
end

function Player:fire()
	local x = self:getX()
	local y = self:getY()
	local z = self:getZ() + 100

	for n=0,4 do
		local s = Sprite.new()
		s:setTexture(textureStore:getTexture("test.png#g0"));
		s:setPos(x, y)
		s:setZ(z)
		s:setAngle(-32 + 16 * n)
		s:setSpeed(10)
	end
end

function Player:animate()
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
	
		self:setTexture(textureStore:getTexture("player.png#" .. animPrefix[anim] .. frame))
		
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

function Player:onDestroy()
	if self.deathTime <= 0 then
		self.deathTime = self.deathBombTime
	end
	
	if self.lives <= 0 then
		print("Player is out of lives")
		return true
	end
	return false
end
