
FocusSprite = {
	parent=nil,
	rotSpeed=2,
	fadeSpeed=0.1,
	maxAlpha=0.5,
	dz=1
	}

function FocusSprite.new(parent, tex, dz, o)
	local o = extend(FocusSprite, o or {})
	local s = Sprite.new(o)
	s.parent = parent
	s.dz = dz
	s:setTexture(tex)
	return s
end

function FocusSprite:init()
	self:setAlpha(0)
end

function FocusSprite:update()
	while not self.parent:isDestroyed() do
		self:setPos(self.parent:getX(), self.parent:getY())
		self:setZ(self.parent:getZ() + self.dz)
		yield()
	end
	self:destroy()
end

function FocusSprite:animate()
	local targetAlpha = 0.0

	self:setDrawAngleAuto(false)
	while true do
		if self.parent.focus then
			targetAlpha = self.maxAlpha
		else
			targetAlpha = 0.0
		end
		
		local alpha = self:getAlpha()
		if math.abs(targetAlpha-alpha) > self.fadeSpeed then
			alpha = alpha + self.fadeSpeed * signum(targetAlpha-alpha)
		else
			alpha = targetAlpha
		end
		alpha = math.max(0.0, math.min(self.maxAlpha, alpha))		
		self:setAlpha(alpha)
		
		self:setDrawAngle(self:getDrawAngle() + self.rotSpeed)
		
		yield()
	end
end
