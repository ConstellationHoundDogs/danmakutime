
-------------------------------------------------------------------------------
-------------------------------------------------------------------------------
-------------------------------------------------------------------------------
-- 
-- External dependencies: none
-- 
-------------------------------------------------------------------------------
-------------------------------------------------------------------------------
-------------------------------------------------------------------------------

Explosion = {
	alpha=.75,
	frame=1,
	frames={0, 1, 2, 3, 2, 1},
	frameDurations={1, 1, 2, 4, 2, 1}
	}
	
function Explosion.new(parent, self)
	self = extend(Explosion, self or {})	
	self = Sprite.new(self)
	
	self:setPos(parent:getX(), parent:getY())
	self:setZ(parent:getZ() + 10)
	self:setAlpha(self.alpha)
	
	return self
end

function Explosion:animate()
	while self.frame <= #self.frames do
		self:setTexture(texStore:get("explosion.png#e" .. self.frames[self.frame]))
		yield(self.frameDurations[self.frame])
		
		self.frame = self.frame + 1
	end
	self:destroy()
end

-------------------------------------------------------------------------------
-------------------------------------------------------------------------------
-------------------------------------------------------------------------------
