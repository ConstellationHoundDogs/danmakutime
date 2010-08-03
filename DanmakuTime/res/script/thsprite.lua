
-------------------------------------------------------------------------------
-------------------------------------------------------------------------------
-------------------------------------------------------------------------------

THSprite = {
	hp=1,
	power=1,
	grazed=false,
    points=0
	}

function THSprite.new(self)
	self = extend(THSprite, self or {})
	return Sprite.new(self)
end

function THSprite:onCollision(other, myNode, otherNode)
	self.hp = self.hp - other.power
	if self.hp <= 0 then
		if self:destroy() then
			self:dropItems()
			Explosion.new(self)
		end
	end
end

function THSprite:dropItems()	
end

-------------------------------------------------------------------------------
-------------------------------------------------------------------------------
-------------------------------------------------------------------------------

THShot = {
    owner=nil
    }

function THShot.new(owner, self)
    self = extend(THShot, self or {})
    self = THSprite.new(self)
    
    self.owner = owner
    
    if owner ~= nil then
        self:setPos(owner:getX(), owner:getY())
    end
    self:setZ(500)
    
    return self
end    
    
function THShot:onCollision(other, myNode, otherNode)
    self:destroy()
end
    
-------------------------------------------------------------------------------
-------------------------------------------------------------------------------
-------------------------------------------------------------------------------
